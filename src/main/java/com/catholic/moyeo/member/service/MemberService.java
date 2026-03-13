package com.catholic.moyeo.member.service;

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
    private final DepartmentRepository departmentRepository;
    private final TechStackRepository techStackRepository;
    private final MemberTechStackRepository memberTechStackRepository;

    // 기술스택 조회 메서드
    private List<String> getTechStacks(Member member) {
        return memberTechStackRepository.findByMember(member)
                .stream()
                .map(ms -> ms.getTechStack().getName())
                .toList();
    }

    // 내 프로필 조회
    public MyProfileResponse getMyProfile(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        return MyProfileResponse.from(member);
    }

    // 특정 유저 프로필 조회
    public MemberDetailResponse getMemberDetail(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        return MemberDetailResponse.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .role(member.getRole())
                .intro(member.getIntro())
                .githubUrl(member.getGithubUrl())
                .profileImageUrl(member.getProfileImageUrl())
                .departmentId(member.getDepartment() != null ? member.getDepartment().getId() : null)
                .departmentName(member.getDepartment() != null ? member.getDepartment().getName() : null)
                .techStacks(getTechStacks(member))
                .build();
    }

    // 팀원 목록 조회
    public MemberListResponse getMembers(String techStack, int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Member> memberPage;

        if (techStack != null) {
            memberPage = memberRepository.findByTechStack(techStack.toLowerCase(), pageRequest);
        } else {
            memberPage = memberRepository.findAll(pageRequest);
        }

        List<MemberCardResponse> items = memberPage.getContent()
                .stream()
                .map(member -> MemberCardResponse.builder()
                        .memberId(member.getId())
                        .nickname(member.getNickname())
                        .role(member.getRole())
                        .intro(member.getIntro())
                        .githubUrl(member.getGithubUrl())
                        .profileImageUrl(member.getProfileImageUrl())
                        .departmentId(member.getDepartment() != null ? member.getDepartment().getId() : null)
                        .departmentName(member.getDepartment() != null ? member.getDepartment().getName() : null)
                        .techStacks(getTechStacks(member))
                        .build())
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

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        }

        member.updateProfile(
                request.getNickname(),
                request.getRole(),
                request.getIntro(),
                request.getGithubUrl(),
                request.getContactEmail(),
                department
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

        return MyProfileResponse.from(member);
    }
}