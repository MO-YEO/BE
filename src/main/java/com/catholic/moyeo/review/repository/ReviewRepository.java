package com.catholic.moyeo.review.repository;

import com.catholic.moyeo.review.domain.MemberReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<MemberReview, Long> {

    boolean existsByRecruitPostIdAndWriterUserIdAndTargetUserId(Long recruitPostId, Long writerUserId, Long targetUserId);
    
    // 특정 회원이 받은 모든 리뷰 조회 (모집자 확인용, 최신순)
    List<MemberReview> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);

    List<MemberReview> findByTargetUserId(Long targetUserId);
}
