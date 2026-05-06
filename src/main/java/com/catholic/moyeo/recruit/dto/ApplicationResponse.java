package com.catholic.moyeo.recruit.dto;

import com.catholic.moyeo.recruit.domain.RecruitApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 지원자 목록 아이템 / 지원 승인·거절 응답 DTO
 *
 * API 명세:
 * {
 *   applicationId,
 *   applicant {
 *     memberId,
 *     nickname,
 *     contactEmail
 *   },
 *   status,
 *   createdAt
 * }
 */
public class ApplicationResponse {

    private Long applicationId;
    private Applicant applicant;
    private String name;
    private String role;
    private String introduction;
    private List<String> requiredSkills;
    private String contactEmail;
    private String phoneNumber;
    private String githubUrl;
    private RecruitApplicationStatus status;
    private LocalDateTime createdAt;

    // 리뷰 관련 정보 (모집자에게만 노출)
    private Double averageRating;
    private Integer reviewCount;
    private List<ReviewSummary> recentReviews;

    public static ApplicationResponse of(
            Long applicationId,
            Long memberId,
            String nickname,
            String contactEmail,
            String name,
            String role,
            String introduction,
            List<String> requiredSkills,
            String applicantContactEmail,
            String phoneNumber,
            String githubUrl,
            RecruitApplicationStatus status,
            LocalDateTime createdAt,
            Double averageRating,
            Integer reviewCount,
            List<ReviewSummary> recentReviews
    ) {
        ApplicationResponse r = new ApplicationResponse();
        r.applicationId = applicationId;
        r.applicant = new Applicant(memberId, nickname, contactEmail);
        r.name = name;
        r.role = role;
        r.introduction = introduction;
        r.requiredSkills = requiredSkills;
        r.contactEmail = applicantContactEmail;
        r.phoneNumber = phoneNumber;
        r.githubUrl = githubUrl;
        r.status = status;
        r.createdAt = createdAt;
        r.averageRating = averageRating;
        r.reviewCount = reviewCount;
        r.recentReviews = recentReviews;
        return r;
    }

    public Long getApplicationId() { return applicationId; }
    public Applicant getApplicant() { return applicant; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getIntroduction() { return introduction; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public String getContactEmail() { return contactEmail; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getGithubUrl() { return githubUrl; }
    public RecruitApplicationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Double getAverageRating() { return averageRating; }
    public Integer getReviewCount() { return reviewCount; }
    public List<ReviewSummary> getRecentReviews() { return recentReviews; }

    public static class Applicant {
        private Long memberId;
        private String nickname;
        private String contactEmail;

        public Applicant(Long memberId, String nickname, String contactEmail) {
            this.memberId = memberId;
            this.nickname = nickname;
            this.contactEmail = contactEmail;
        }

        public Long getMemberId() { return memberId; }
        public String getNickname() { return nickname; }
        public String getContactEmail() { return contactEmail; }
    }

    public static record ReviewSummary(
            Long reviewId,
            short rating,
            String content,
            LocalDateTime createdAt
    ) {}
}