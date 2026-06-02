package com.catholic.moyeo.member.repository;
import com.catholic.moyeo.member.domain.Member;
import com.catholic.moyeo.member.domain.MemberBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberBookmarkRepository extends JpaRepository<MemberBookmark, Long> {

    boolean existsByUserAndTarget(Member user, Member target);

    void deleteByUserAndTarget(Member user, Member target);

    List<MemberBookmark> findByUser(Member user);

    // 회원 탈퇴 시 해당 유저가 한 북마크 전체 삭제
    void deleteByUser(Member user);

    // 회원 탈퇴 시 해당 유저가 대상인 북마크 전체 삭제
    void deleteByTarget(Member target);
}