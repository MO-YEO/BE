package com.catholic.moyeo.member.repository;

import com.catholic.moyeo.member.domain.Member;
import com.catholic.moyeo.member.domain.MemberActivityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberActivityCategoryRepository extends JpaRepository<MemberActivityCategory, Long> {
    List<MemberActivityCategory> findByMember(Member member);

    @Modifying
    @Query("DELETE FROM MemberActivityCategory mac WHERE mac.member = :member")
    void deleteByMember(@Param("member") Member member);
}
