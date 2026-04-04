package com.catholic.moyeo.review.dto;

import com.catholic.moyeo.review.domain.MemberReview;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponse {
    
    private Long reviewId;
    private short rating;
    private String content;
    private LocalDateTime createdAt;
    
    protected ReviewResponse() {}
    
    private ReviewResponse(Long reviewId, short rating, String content, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
    }
    
    public static ReviewResponse from(MemberReview review) {
        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
