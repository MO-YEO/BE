package com.catholic.moyeo.member.service;

import com.catholic.moyeo.common.domain.ActivityCategory;
import com.catholic.moyeo.member.domain.*;
import com.catholic.moyeo.member.dto.*;
import com.catholic.moyeo.member.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final TechStackRepository techStackRepository;
    private final MemberTechStackRepository memberTechStackRepository;
    private final MemberActivityCategoryRepository memberActivityCategoryRepository;
    private final MemberBookmarkRepository memberBookmarkRepository;

    private List<String> getTechStacks(Member member) {
        return memberTechStackRepository.findByMember(member)
                .stream()
                .map(ms -> ms.getTechStack().getName())
                .toList();
    }

    private List<String> getActivityCategories(Member member) {
        return memberActivityCategoryRepository.findByMember(member)
                .stream()
                .map(mac -> mac.getActivityCategory().getLabel())
                .toList();
    }

    public MyProfileResponse getMyProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        return MyProfileResponse.from(member, getTechStacks(member), getActivityCategories(member));
    }

    public MemberDetailResponse getMemberDetail(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        if (member.isDeleted() || !member.isTeamProfileRegistered()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Team profile not found");
        }

        return MemberDetailResponse.from(member, getTechStacks(member), getActivityCategories(member));
    }

    public MemberListResponse getMembers(Long userId, String techStack, String activityCategory, int page, int size) {
        Member currentUser = memberRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Member> memberPage;

        if (techStack != null && !techStack.isBlank()) {
            memberPage = memberRepository.findByTechStack(techStack.trim().toLowerCase(), pageRequest);
        } else if (activityCategory != null && !activityCategory.isBlank()) {
            ActivityCategory category = ActivityCategory.from(activityCategory);
            memberPage = memberRepository.findByActivityCategory(category, pageRequest);
        } else {
            memberPage = memberRepository.findByTeamProfileRegisteredTrueAndDeletedFalse(pageRequest);
        }
        List<MemberCardResponse> items = memberPage.getContent()
                .stream()
                .map(member -> {
                    boolean bookmarked = memberBookmarkRepository.existsByUserAndTarget(currentUser, member);

                    return MemberCardResponse.from(
                            member,
                            getTechStacks(member),
                            getActivityCategories(member),
                            bookmarked
                    );
                })
                .toList();

        MemberListResponse.PageInfo pageInfo =
                MemberListResponse.PageInfo.builder()
                        .totalElements(memberPage.getTotalElements())
                        .totalPages(memberPage.getTotalPages())
                        .page(memberPage.getNumber())
                        .size(memberPage.getSize())
                        .build();

        return MemberListResponse.builder()
                .items(items)
                .pageInfo(pageInfo)
                .build();
    }

    @Transactional
    public MyProfileResponse updateMyProfile(Long memberId, UpdateMyProfileRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        member.updateProfile(
                request.getNickname(),
                request.getProfileImageUrl(),
                request.getRole(),
                request.getIntro(),
                request.getGithubUrl(),
                request.getContactEmail(),
                request.getPhoneNumber()
        );

        memberTechStackRepository.deleteByMember(member);
        memberTechStackRepository.flush();

        if (request.getTechStacks() != null) {
            Set<String> techStackNames = request.getTechStacks().stream()
                    .filter(name -> name != null && !name.isBlank())
                    .map(name -> name.trim().toLowerCase())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            for (String normalized : techStackNames) {
                TechStack techStack = techStackRepository
                        .findByName(normalized)
                        .orElseGet(() -> techStackRepository.save(new TechStack(normalized)));

                memberTechStackRepository.save(new MemberTechStack(member, techStack));
            }
        }

        memberActivityCategoryRepository.deleteByMember(member);
        memberActivityCategoryRepository.flush();

        if (request.getActivityCategories() != null) {
            Set<ActivityCategory> activityCategories = request.getActivityCategories().stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(ActivityCategory::from)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            for (ActivityCategory category : activityCategories) {
                memberActivityCategoryRepository.save(new MemberActivityCategory(member, category));
            }
        }

        return MyProfileResponse.from(member, getTechStacks(member), getActivityCategories(member));
    }

    @Transactional
    public void addBookmark(Long userId, Long targetId) {
        if (userId.equals(targetId)) {
            throw new IllegalArgumentException("자기 자신 북마크 불가");
        }

        Member user = memberRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Member target = memberRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!target.isTeamProfileRegistered()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Team profile not found");
        }

        if (memberBookmarkRepository.existsByUserAndTarget(user, target)) {
            return;
        }

        memberBookmarkRepository.save(new MemberBookmark(user, target));
    }

    @Transactional
    public void removeBookmark(Long userId, Long targetId) {
        Member user = memberRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Member target = memberRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        memberBookmarkRepository.deleteByUserAndTarget(user, target);
    }

    public MemberListResponse getMyBookmarks(Long userId) {
        Member user = memberRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<MemberBookmark> bookmarks = memberBookmarkRepository.findByUser(user);

        List<MemberCardResponse> items = bookmarks.stream()
                .map(MemberBookmark::getTarget)
                .filter(target -> !target.isDeleted())
                .filter(Member::isTeamProfileRegistered)
                .map(target -> MemberCardResponse.from(
                        target,
                        getTechStacks(target),
                        getActivityCategories(target),
                        true
                ))
                .toList();

        return MemberListResponse.builder()
                .items(items)
                .pageInfo(
                        MemberListResponse.PageInfo.builder()
                                .totalElements(items.size())
                                .totalPages(1)
                                .page(0)
                                .size(items.size())
                                .build()
                )
                .build();
    }

    @Transactional
    public void updateProfileImage(Long memberId, String url) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        member.updateProfile(null, url, null, null, null, null, null);
    }

    @Transactional
    public MyProfileResponse registerTeamProfile(Long memberId, UpdateMyProfileRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        member.updateProfile(
                request.getNickname(),
                request.getProfileImageUrl(),
                request.getRole(),
                request.getIntro(),
                request.getGithubUrl(),
                request.getContactEmail(),
                request.getPhoneNumber()
        );

        memberTechStackRepository.deleteByMember(member);
        memberTechStackRepository.flush();

        if (request.getTechStacks() != null) {
            Set<String> techStackNames = request.getTechStacks().stream()
                    .filter(name -> name != null && !name.isBlank())
                    .map(name -> name.trim().toLowerCase())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            for (String normalized : techStackNames) {
                TechStack techStack = techStackRepository
                        .findByName(normalized)
                        .orElseGet(() -> techStackRepository.save(new TechStack(normalized)));

                memberTechStackRepository.save(new MemberTechStack(member, techStack));
            }
        }

        memberActivityCategoryRepository.deleteByMember(member);
        memberActivityCategoryRepository.flush();

        if (request.getActivityCategories() != null) {
            Set<ActivityCategory> activityCategories = request.getActivityCategories().stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(ActivityCategory::from)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            for (ActivityCategory category : activityCategories) {
                memberActivityCategoryRepository.save(new MemberActivityCategory(member, category));
            }
        }

        member.registerTeamProfile();

        return MyProfileResponse.from(member, getTechStacks(member), getActivityCategories(member));
    }

    // 회원 탈퇴
    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        memberTechStackRepository.deleteByMember(member);
        memberTechStackRepository.flush();

        memberActivityCategoryRepository.deleteByMember(member);
        memberActivityCategoryRepository.flush();

        memberBookmarkRepository.deleteByUser(member);
        memberBookmarkRepository.deleteByTarget(member);

        member.withdraw();
    }
}