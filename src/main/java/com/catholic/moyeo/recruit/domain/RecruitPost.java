package com.catholic.moyeo.recruit.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * recruit_post 테이블 매핑
 *
 * 핵심 정책(팀 공유):
 * - required_skills: DB는 VARCHAR(255) CSV("A,B,C"), API는 List<String> (서버 join/split)
 * - applicant_count: 작성자 포함 현재 참여 인원. applicant_count 기본 1에서 시작.
 * - deadline 지난 경우: 서비스에서 행위 시점에 차단(400). (스케줄러로 CLOSED 처리하는 건 선택)
 *
 * 값 고정 정책(중요):
 * - type/category/status는 "허용 값 고정" 대상이지만, DB는 VARCHAR 유지(MVP).
 * - 따라서 엔티티는 String을 유지하고, 서비스에서 Enum 파싱/검증으로 400 처리한다.
 *
 * tag 정책(중요):
 * - tag는 표시만 한다. (필터/검색 X)
 * - ERD 상 tag는 VARCHAR(50) 단일 컬럼이므로 CSV 다중 태그는 MVP 범위를 벗어난다.
 *   (추후 다중 태그가 필요하면 ERD 변경 + 테이블 분리 등 재설계 필요)
 */
@Entity
@Table(name = "recruit_post")
public class RecruitPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruit_post_id")
    private Long id;

    @Column(name = "author_user_id", nullable = false)
    private Long authorUserId;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    /**
     * 표시용 단일 태그 (예: "AfterEffects")
     * - 검색/필터 파라미터에서는 사용하지 않음(MVP)
     */
    @Column(name = "tag", length = 50)
    private String tag;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 모집 기술(요구 기술) - CSV
     * 예: "Python,TensorFlow"
     */
    @Column(name = "required_skills", length = 255)
    private String requiredSkills;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecruitPostStatus status = RecruitPostStatus.OPEN;

    @Column(name = "total_headcount", nullable = false)
    private short totalHeadcount;

    @Column(name = "applicant_count", nullable = false)
    private short applicantCount;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected RecruitPost() {}

    public RecruitPost(Long authorUserId,
                       String type,
                       String category,
                       String tag,
                       String title,
                       String content,
                       String requiredSkills,
                       short totalHeadcount,
                       LocalDate deadline) {
        this.authorUserId = authorUserId;
        this.type = type;
        this.category = category;
        this.tag = tag;
        this.title = title;
        this.content = content;
        this.requiredSkills = requiredSkills;
        this.totalHeadcount = totalHeadcount;
        this.deadline = deadline;

        this.status = RecruitPostStatus.OPEN;
        this.applicantCount = 1; // 정책: 작성자 포함 참여 인원
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // MVP 방어 로직 (최소한의 무결성 보장)
        if (this.status == null) this.status = RecruitPostStatus.OPEN;
        if (this.totalHeadcount <= 0) this.totalHeadcount = 1;

        // 작성자 포함 정책: 최소 1
        if (this.applicantCount <= 0) this.applicantCount = 1;

        // applicant_count가 total_headcount를 초과하지 않도록 방어
        if (this.applicantCount > this.totalHeadcount) this.applicantCount = this.totalHeadcount;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getAuthorUserId() { return authorUserId; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getTag() { return tag; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getRequiredSkills() { return requiredSkills; }
    public RecruitPostStatus getStatus() { return status; }
    public short getTotalHeadcount() { return totalHeadcount; }
    public short getApplicantCount() { return applicantCount; }
    public LocalDate getDeadline() { return deadline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setType(String type) { this.type = type; }
    public void setCategory(String category) { this.category = category; }
    public void setTag(String tag) { this.tag = tag; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    public void setStatus(RecruitPostStatus status) { this.status = status; }
    public void setTotalHeadcount(short totalHeadcount) { this.totalHeadcount = totalHeadcount; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public void increaseApplicantCount() {
        if (this.applicantCount < this.totalHeadcount) this.applicantCount++;
    }

    public void decreaseApplicantCount() {
        // 정책상 작성자 포함이므로 최소 1
        if (this.applicantCount > 1) this.applicantCount--;
    }

    public boolean isClosed() { return this.status == RecruitPostStatus.CLOSED; }
}