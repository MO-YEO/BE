package com.catholic.moyeo.member.repository;

import com.catholic.moyeo.common.domain.ActivityCategory;
import com.catholic.moyeo.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderAndProviderSub(String provider, String providerSub);

    Optional<Member> findByEmail(String email);

    Page<Member> findByTeamProfileRegisteredTrueAndDeletedFalse(Pageable pageable);

    @Query("""
        select distinct m
        from Member m
        join MemberTechStack mts on mts.member = m
        join TechStack ts on mts.techStack = ts
        where ts.name = :tech
        and m.teamProfileRegistered = true
        and m.deleted = false
    """)
    Page<Member> findByTechStack(@Param("tech") String tech, Pageable pageable);

    @Query("""
        select distinct m
        from Member m
        join MemberActivityCategory mac on mac.member = m
        where mac.activityCategory = :activityCategory
        and m.teamProfileRegistered = true
        and m.deleted = false
    """)
    Page<Member> findByActivityCategory(@Param("activityCategory") ActivityCategory activityCategory,
                                        Pageable pageable);
}