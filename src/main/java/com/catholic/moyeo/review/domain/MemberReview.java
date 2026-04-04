package com.catholic.moyeo.review.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@Table(
        name = "member_review",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_review_unique", columnNames = {"recruit_post_id", "writer_user_id", "target_user_id"})
        }
)
public class MemberReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Column(name = "recruit_post_id", nullable = false)
    private Long recruitPostId;

    @Column(name = "writer_user_id", nullable = false)
    private Long writerUserId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "rating", nullable = false)
    private short rating;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected MemberReview() {}

    public MemberReview(Long recruitPostId, Long writerUserId, Long targetUserId, short rating, String content) {
        if (writerUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("자기 자신에게 리뷰를 작성할 수 없습니다.");
        }
        this.recruitPostId = recruitPostId;
        this.writerUserId = writerUserId;
        this.targetUserId = targetUserId;
        setRating(rating);
        this.content = content;
    }

    public void update(short rating, String content) {
        setRating(rating);
        if (content != null && !content.trim().isEmpty()) {
            this.content = content;
        }
    }

    private void setRating(short rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("별점은 1점에서 5점 사이여야 합니다.");
        }
        this.rating = rating;
    }
}
