package com.catholic.moyeo.recruit.repository;

import com.catholic.moyeo.recruit.domain.RecruitPostBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RecruitPostBookmarkRepository extends JpaRepository<RecruitPostBookmark, Long> {
    Optional<RecruitPostBookmark> findByRecruitPostIdAndUserId(Long recruitPostId, Long userId);
    boolean existsByRecruitPostIdAndUserId(Long recruitPostId, Long userId);
    void deleteByRecruitPostIdAndUserId(Long recruitPostId, Long userId);

    // 회원 탈퇴 시 해당 유저가 찜한 모집글 북마크 전체 삭제
    void deleteByUserId(Long userId);

    // 모집글 삭제 시 해당 모집글의 북마크 전체 삭제
    void deleteByRecruitPostId(Long recruitPostId);

    @org.springframework.data.jpa.repository.Query("SELECT b.recruitPostId FROM RecruitPostBookmark b WHERE b.userId = :userId AND b.recruitPostId IN :recruitPostIds")
    java.util.List<Long> findBookmarkedPostIdsByUserIdAndRecruitPostIds(@org.springframework.data.repository.query.Param("userId") Long userId, @org.springframework.data.repository.query.Param("recruitPostIds") java.util.List<Long> recruitPostIds);
}
