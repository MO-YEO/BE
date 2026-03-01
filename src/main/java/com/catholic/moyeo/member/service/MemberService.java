package com.catholic.moyeo.member.service;

import com.catholic.moyeo.member.domain.Department;
import com.catholic.moyeo.member.domain.Member;
import com.catholic.moyeo.member.dto.*;
import com.catholic.moyeo.member.repository.DepartmentRepository;
import com.catholic.moyeo.member.repository.MemberRepository;
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

        return MemberDetailResponse.from(member);
    }

    // 팀원 목록 조회
    public MemberListResponse getMembers(int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Member> memberPage = memberRepository.findAll(pageRequest);

        List<MemberCardResponse> items = memberPage.getContent()
                .stream()
                .map(MemberCardResponse::from)
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
                department
        );

        return MyProfileResponse.from(member);
    }
}