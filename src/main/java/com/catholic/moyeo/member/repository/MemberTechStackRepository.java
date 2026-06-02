package com.catholic.moyeo.member.repository;

import com.catholic.moyeo.member.domain.Member;
import com.catholic.moyeo.member.domain.MemberTechStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberTechStackRepository extends JpaRepository<MemberTechStack, Long> {

    List<MemberTechStack> findByMember(Member member);

    @Modifying
    @Query("DELETE FROM MemberTechStack mts WHERE mts.member = :member")
    void deleteByMember(@Param("member") Member member);

    List<MemberTechStack> findByMemberIdIn(List<Long> memberIds); //여러 회원 기술스택 한 번에 조회
}