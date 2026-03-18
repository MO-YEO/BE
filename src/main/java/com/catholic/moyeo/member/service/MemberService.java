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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final TechStackRepository techStackRepository;
    private final MemberTechStackRepository memberTechStackRepository;
    private final MemberActivityCategoryRepository memberActivityCategoryRepository;


    // 기술스택 조회 메서드
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

    // 내 프로필 조회
    public MyProfileResponse getMyProfile(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        return MyProfileResponse.from(
                member,
                getTechStacks(member),
                getActivityCategories(member)
        );
    }

    // 특정 유저 프로필 조회
    public MemberDetailResponse getMemberDetail(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        return MemberDetailResponse.from(
                member,
                getTechStacks(member),
                getActivityCategories(member)
        );
    }

    // 팀원 목록 조회
    public MemberListResponse getMembers(String techStack, String activityCategory, int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Member> memberPage;

        if (techStack != null && !techStack.isBlank()) {
            memberPage = memberRepository.findByTechStack(techStack.trim().toLowerCase(), pageRequest);
        } else if (activityCategory != null && !activityCategory.isBlank()) {
            ActivityCategory category = ActivityCategory.from(activityCategory);
            memberPage = memberRepository.findByActivityCategory(category, pageRequest);
        } else {
            memberPage = memberRepository.findAll(pageRequest);
        }

        List<MemberCardResponse> items = memberPage.getContent()
                .stream()
                .map(member -> MemberCardResponse.from(
                        member,
                        getTechStacks(member),
                        getActivityCategories(member)
                ))
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

    // 내 프로필 수정
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

        // 기존 기술 삭제
        memberTechStackRepository.deleteByMember(member);

        // 새 기술 저장
        if (request.getTechStacks() != null) {

            for (String name : request.getTechStacks()) {

                if (name == null || name.isBlank()) continue;

                String normalized = name.trim().toLowerCase();

                TechStack techStack = techStackRepository
                        .findByName(normalized)
                        .orElseGet(() -> techStackRepository.save(new TechStack(normalized)));

                memberTechStackRepository.save(
                        new MemberTechStack(member, techStack)
                );
            }
        }
        memberActivityCategoryRepository.deleteByMember(member);

        if (request.getActivityCategories() != null) {
            for (String value : request.getActivityCategories()) {
                if (value == null || value.isBlank()) continue;

                ActivityCategory category = ActivityCategory.from(value);

                memberActivityCategoryRepository.save(
                        new MemberActivityCategory(member, category)
                );
            }
        }

        return MyProfileResponse.from(
                member,
                getTechStacks(member),
                getActivityCategories(member)
        );
    }
}