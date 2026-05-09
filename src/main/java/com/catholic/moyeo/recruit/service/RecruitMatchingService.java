package com.catholic.moyeo.recruit.service;

import com.catholic.moyeo.member.domain.Member;
import com.catholic.moyeo.member.domain.MemberTechStack;
import com.catholic.moyeo.member.repository.MemberRepository;
import com.catholic.moyeo.member.repository.MemberTechStackRepository;
import com.catholic.moyeo.recruit.domain.RecruitPost;
import com.catholic.moyeo.recruit.dto.RecruitMatchingResponse;
import com.catholic.moyeo.recruit.repository.RecruitPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitMatchingService {

    private final RecruitPostRepository recruitPostRepository;
    private final MemberRepository memberRepository;
    private final MemberTechStackRepository memberTechStackRepository;

    public RecruitMatchingResponse getMatchingResult(Long recruitPostId, Long memberId) {

        // 1. 모집글 조회
        RecruitPost recruitPost = recruitPostRepository.findById(recruitPostId)
                .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));

        // 2. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 3. 모집글 requiredSkills 파싱
        List<String> requiredSkills = parseRequiredSkills(recruitPost.getRequiredSkills());

        // 4. 회원 기술스택 조회
        List<String> memberSkills = memberTechStackRepository.findByMember(member)
                .stream()
                .map(MemberTechStack::getTechStack)
                .map(techStack -> techStack.getName())
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();

        // 5. 일치하는 기술 계산
        List<String> matchedSkills = requiredSkills.stream()
                .filter(memberSkills::contains)
                .toList();

        // 6. 부족한 기술 계산
        List<String> missingSkills = requiredSkills.stream()
                .filter(skill -> !memberSkills.contains(skill))
                .toList();

        // 7. 매칭 점수 계산
        int matchingScore = calculateScore(matchedSkills.size(), requiredSkills.size());

        // 8. 응답 반환
        return new RecruitMatchingResponse(
                recruitPostId,
                memberId,
                requiredSkills,
                memberSkills,
                matchedSkills,
                missingSkills,
                matchingScore
        );
    }

    private List<String> parseRequiredSkills(String requiredSkills) {

        if (requiredSkills == null || requiredSkills.isBlank()) {
            return List.of();
        }

        return Arrays.stream(requiredSkills.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(skill -> !skill.isBlank())
                .toList();
    }

    private int calculateScore(int matchedCount, int requiredCount) {

        if (requiredCount == 0) {
            return 0;
        }

        return (int) Math.round((matchedCount * 100.0) / requiredCount);
    }
}