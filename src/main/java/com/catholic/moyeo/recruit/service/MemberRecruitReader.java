package com.catholic.moyeo.recruit.service;

import com.catholic.moyeo.member.domain.Member;
import com.catholic.moyeo.member.repository.MemberRepository;
import com.catholic.moyeo.recruit.dto.ApplicationResponse;
import com.catholic.moyeo.recruit.dto.RecruitAuthorResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * MemberRepository 기반 RecruitMemberReader 구현체
 *
 * 정책:
 * - Recruit 모듈은 Member 전체 정보를 보관하지 않고 memberId(userId)만 참조한다.
 * - 응답 시점에 Member를 조회해 author / applicant 응답을 조합한다.
 * - local 환경이 아닐 때 사용되는 실제 구현체다.
 *
 * NOTE:
 * - 현재 recruit 단독 개발 단계에서는 Member 도메인이 완전히 연동되지 않았을 수 있다.
 * - 따라서 이 구현체는 "컴파일 가능"하고 "최소한의 값 반환"에 초점을 둔다.
 * - local 테스트에서는 MockRecruitMemberReader가 실제로 사용된다.
 * - Member 연동 완료 후 nickname / departmentName 을 실제 값으로 교체하면 된다.
 */
@Component
@Profile("!local")
@Transactional(readOnly = true)
public class MemberRecruitReader implements RecruitMemberReader {

    private final MemberRepository memberRepository;

    public MemberRecruitReader(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public RecruitAuthorResponse getAuthor(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        /*
         * 현재 Member 연동 전 단계이므로
         * nickname / departmentName 은 임시값으로 내려준다.
         *
         * TODO:
         * - member.getNickname()
         * - member.getDepartment() != null ? member.getDepartment().getName() : null
         * 로 교체
         */
        return new RecruitAuthorResponse(
                member.getId(),
                "TEMP_NICKNAME",
                null
        );
    }

    @Override
    public ApplicationResponse.Applicant getApplicant(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        /*
         * 현재 Member 연동 전 단계이므로
         * nickname 은 임시값,
         * contactEmail 은 Member.email 을 사용한다.
         *
         * TODO:
         * - nickname 을 member.getNickname() 으로 교체
         */
        return new ApplicationResponse.Applicant(
                member.getId(),
                "TEMP_NICKNAME",
                member.getEmail()
        );
    }
}