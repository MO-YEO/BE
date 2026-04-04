package com.catholic.moyeo.review.service;

import com.catholic.moyeo.recruit.domain.RecruitApplication;
import com.catholic.moyeo.recruit.domain.RecruitApplicationStatus;
import com.catholic.moyeo.recruit.domain.RecruitPost;
import com.catholic.moyeo.recruit.domain.RecruitPostStatus;
import com.catholic.moyeo.recruit.repository.RecruitApplicationRepository;
import com.catholic.moyeo.recruit.repository.RecruitPostRepository;
import com.catholic.moyeo.review.domain.MemberReview;
import com.catholic.moyeo.review.dto.CreateReviewRequest;
import com.catholic.moyeo.review.dto.ReviewResponse;
import com.catholic.moyeo.review.dto.UpdateReviewRequest;
import com.catholic.moyeo.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RecruitPostRepository recruitPostRepository;
    private final RecruitApplicationRepository recruitApplicationRepository;

    @Transactional
    public Long createReview(Long writerUserId, CreateReviewRequest request) {
        if (writerUserId.equals(request.getTargetUserId())) {
            throw new IllegalArgumentException("자기 자신에게 리뷰를 작성할 수 없습니다.");
        }

        RecruitPost post = recruitPostRepository.findById(request.getRecruitPostId())
                .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));

        if (post.getStatus() != RecruitPostStatus.CLOSED) {
            throw new IllegalStateException("모집 마감(CLOSED) 상태인 프로젝트에만 리뷰를 작성할 수 있습니다.");
        }

        // 작성자와 대상자 모두 프로젝트 참여자인지 검증 (작성자거나 승인된 지원자)
        validateParticipant(post, writerUserId);
        validateParticipant(post, request.getTargetUserId());

        // 중복 작성 여부 체크
        if (reviewRepository.existsByRecruitPostIdAndWriterUserIdAndTargetUserId(
                post.getId(), writerUserId, request.getTargetUserId())) {
            throw new IllegalStateException("해당 팀원에게 이미 리뷰를 작성했습니다.");
        }

        MemberReview review = new MemberReview(
                post.getId(),
                writerUserId,
                request.getTargetUserId(),
                request.getRating(),
                request.getContent()
        );

        return reviewRepository.save(review).getId();
    }

    @Transactional
    public void updateReview(Long writerUserId, Long reviewId, UpdateReviewRequest request) {
        MemberReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getWriterUserId().equals(writerUserId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        review.update(request.getRating(), request.getContent());
    }

    @Transactional
    public void deleteReview(Long writerUserId, Long reviewId) {
        MemberReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getWriterUserId().equals(writerUserId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getApplicantReviews(Long requesterUserId, Long recruitPostId, Long applicantUserId) {
        // Validation: 요청자가 조회하는 모집글의 작성자인지 확인
        RecruitPost viewingPost = recruitPostRepository.findById(recruitPostId)
                .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));
        
        if (!viewingPost.getAuthorUserId().equals(requesterUserId)) {
            throw new IllegalArgumentException("해당 프로젝트의 모집자만 지원자의 리뷰를 열람할 수 있습니다.");
        }

        // Validation: 조회 대상자(지원자)가 실제로 이 모집글에 지원했었는지 확인
        boolean hasApplied = recruitApplicationRepository.existsByRecruitPostIdAndUserId(recruitPostId, applicantUserId);
        if (!hasApplied) {
            throw new IllegalArgumentException("해당 팀원은 이 프로젝트에 지원한 기록이 없습니다.");
        }

        // 대상자가 받은 모든 리뷰 조회 (어플리케이션 내의 다른 모든 프로젝트 참여로 받은 리뷰 포함)
        List<MemberReview> reviews = reviewRepository.findByTargetUserId(applicantUserId);

        return reviews.stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    private void validateParticipant(RecruitPost post, Long userId) {
        // 프로젝트 모집자인 경우 통과
        if (post.getAuthorUserId().equals(userId)) {
            return;
        }
        
        // 지원자인 경우 승인된(ACCEPTED) 상태인지 확인
        RecruitApplication application = recruitApplicationRepository.findByRecruitPostIdAndUserId(post.getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 프로젝트에 참여하지 않았습니다. (지원 내역 없음)"));
                
        if (application.getStatus() != RecruitApplicationStatus.ACCEPTED) {
            throw new IllegalArgumentException("해당 지원자는 프로젝트 참여 승인을 받지 못했습니다. 참여자만 리뷰 대상이 될 수 있습니다.");
        }
    }
}
