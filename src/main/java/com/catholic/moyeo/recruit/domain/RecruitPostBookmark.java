package com.catholic.moyeo.recruit.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 프로젝트 모집글 북마크 엔티티
 */
@Entity
@Table(
        name = "recruit_post_bookmark",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recruit_post_bookmark",
                        columnNames = {"recruit_post_id", "user_id"}
                )
        }
)
public class RecruitPostBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruit_post_bookmark_id")
    private Long id;

    @Column(name = "recruit_post_id", nullable = false)
    private Long recruitPostId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected RecruitPostBookmark() {}

    public RecruitPostBookmark(Long recruitPostId, Long userId) {
        this.recruitPostId = recruitPostId;
        this.userId = userId;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getRecruitPostId() { return recruitPostId; }
    public Long getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
