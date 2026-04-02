package com.catholic.moyeo.review.controller;

import com.catholic.moyeo.review.dto.CreateReviewRequest;
import com.catholic.moyeo.review.dto.ReviewResponse;
import com.catholic.moyeo.review.dto.UpdateReviewRequest;
import com.catholic.moyeo.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Long> createReview(
            Authentication authentication,
            @Valid @RequestBody CreateReviewRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        Long reviewId = reviewService.createReview(userId, request);
        return ResponseEntity.ok(reviewId);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Void> updateReview(
            Authentication authentication,
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        reviewService.updateReview(userId, reviewId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            Authentication authentication,
            @PathVariable Long reviewId) {
        Long userId = (Long) authentication.getPrincipal();
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.ok().build();
    }

    // 특정 모집글(postId)에 지원한 지원자(applicantUserId)의 리뷰 내역 조회
    @GetMapping("/recruits/{postId}/applicants/{applicantUserId}")
    public ResponseEntity<List<ReviewResponse>> getApplicantReviews(
            Authentication authentication,
            @PathVariable Long postId,
            @PathVariable Long applicantUserId) {
        Long userId = (Long) authentication.getPrincipal();
        List<ReviewResponse> responses = reviewService.getApplicantReviews(userId, postId, applicantUserId);
        return ResponseEntity.ok(responses);
    }
}
