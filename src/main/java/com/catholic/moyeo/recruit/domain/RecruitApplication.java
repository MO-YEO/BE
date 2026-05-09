package com.catholic.moyeo.recruit.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * recruit_application 테이블 매핑
 *
 * 정책(중요):
 * - 취소는 row 삭제로 처리 (status=CANCELED 같은 상태 전환 안 씀)
 * - 중복지원은 DB UNIQUE(recruit_post_id, user_id) + 서비스 체크로 2중 방어
 * - ACCEPTED 취소(삭제) 시 applicant_count는 "서비스에서" -1 처리해야 함(자동 아님)
 * - CLOSED 상태의 모집글에서는 취소(삭제) 불가(서비스에서 IllegalStateException)
 *
 * NOTE(팀 공유):
 * - 관계(@ManyToOne)로 매핑하지 않고 FK 값(Long)만 보관한다.
 * - 따라서 post 조회/검증은 서비스에서 recruitPostId로 별도 조회해야 한다.
 */
@Entity
@Table(
        name = "recruit_application",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recruit_application_post_user",
                        columnNames = {"recruit_post_id", "user_id"}
                )
        }
)
public class RecruitApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long id;

    @Column(name = "recruit_post_id", nullable = false)
    private Long recruitPostId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", length = 30)
    private String name;

    @Column(name = "role", length = 100)
    private String role;

    @Column(name = "introduction", length = 1000)
    private String introduction;

    @Column(name = "required_skills", length = 500)
    private String requiredSkills;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecruitApplicationStatus status = RecruitApplicationStatus.APPLIED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected RecruitApplication() {}

    public RecruitApplication(Long recruitPostId, Long userId, String name, String role, String introduction, 
                              String requiredSkills, String contactEmail, String phoneNumber, String githubUrl) {
        this.recruitPostId = recruitPostId;
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.introduction = introduction;
        this.requiredSkills = requiredSkills;
        this.contactEmail = contactEmail;
        this.phoneNumber = phoneNumber;
        this.githubUrl = githubUrl;
        this.status = RecruitApplicationStatus.APPLIED;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = RecruitApplicationStatus.APPLIED;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getRecruitPostId() { return recruitPostId; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getIntroduction() { return introduction; }
    public String getRequiredSkills() { return requiredSkills; }
    public String getContactEmail() { return contactEmail; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getGithubUrl() { return githubUrl; }
    public RecruitApplicationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setStatus(RecruitApplicationStatus status) { this.status = status; }
}