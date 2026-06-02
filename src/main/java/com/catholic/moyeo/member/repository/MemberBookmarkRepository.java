package com.catholic.moyeo.member.repository;
import com.catholic.moyeo.member.domain.Member;
import com.catholic.moyeo.member.domain.MemberBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberBookmarkRepository extends JpaRepository<MemberBookmark, Long> {

    boolean existsByUserAndTarget(Member user, Member target);

    void deleteByUserAndTarget(Member user, Member target);

    List<MemberBookmark> findByUser(Member user);

    @Modifying
    @Query("DELETE FROM MemberBookmark mb WHERE mb.user = :user")
    void deleteByUser(@Param("user") Member user);

    @Modifying
    @Query("DELETE FROM MemberBookmark mb WHERE mb.target = :target")
    void deleteByTarget(@Param("target") Member target);
}