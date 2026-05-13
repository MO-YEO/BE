package com.catholic.moyeo.recruit.dto;

import com.catholic.moyeo.recruit.domain.RecruitPost;
import com.catholic.moyeo.recruit.domain.RecruitPostStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 모집글 응답 DTO (표준)
 *
 * API 기준:
 * {
 *   recruitId,
 *   type,
 *   category,
 *   tag,
 *   department,
 *   title,
 *   content,
 *   status,
 *   skills,
 *   appliedByMe,
 *   applicantCount,
 *   totalHeadcount,
 *   deadline,
 *   createdAt,
 *   updatedAt,
 *   author {
 *     memberId,
 *     nickname,
 *     departmentName
 *   }
 * }
 *
 * 카테고리 2단계 정책:
 * - type     = 1차 필터(ActivityCategory)
 * - category = 2차 필터(RecruitCategory)
 *
 * 레거시/호환 정책:
 * - 기존 응답 스키마와 프론트 호환을 위해 type/category 필드는 유지한다.
 * - 추가로 activityCategory/recruitCategory alias getter를 제공한다.
 */
public class RecruitResponse {

    private Long recruitId;

    /**
     * 1차 카테고리
     * - ActivityCategory Enum name 저장값
     */
    private String type;

    /**
     * 2차 카테고리
     * - RecruitCategory Enum name 저장값
     */
    private String category;

    private String tag;
    private String department;
    private String title;
    private String content;
    private RecruitPostStatus status;
    private List<String> skills;
    private boolean appliedByMe;
    private boolean bookmarkedByMe;
    private long applicantCount;
    private long approvedCount;
    private Integer totalHeadcount;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private RecruitAuthorResponse author;

    public static RecruitResponse from(
            RecruitPost post,
            List<String> skills,
            boolean appliedByMe,
            boolean bookmarkedByMe,
            long applicantCount,
            RecruitAuthorResponse author
    ) {
        RecruitResponse response = new RecruitResponse();
        response.recruitId = post.getId();
        response.type = post.getType();
        response.category = post.getCategory();
        response.tag = post.getTag();
        response.department = post.getDepartment();
        response.title = post.getTitle();
        response.content = post.getContent();
        response.status = post.getStatus();
        response.skills = skills;
        response.appliedByMe = appliedByMe;
        response.bookmarkedByMe = bookmarkedByMe;
        response.applicantCount = applicantCount;
        response.approvedCount = post.getApplicantCount(); // 현재 참여 확정 인원 (작성자 포함)
        response.totalHeadcount = (int) post.getTotalHeadcount();
        response.deadline = post.getDeadline();
        response.createdAt = post.getCreatedAt();
        response.updatedAt = post.getUpdatedAt();
        response.author = author;
        return response;
    }

    public Long getRecruitId() { return recruitId; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getTag() { return tag; }
    public String getDepartment() { return department; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public RecruitPostStatus getStatus() { return status; }
    public List<String> getSkills() { return skills; }
    public boolean isAppliedByMe() { return appliedByMe; }
    public boolean isBookmarkedByMe() { return bookmarkedByMe; }
    public long getApplicantCount() { return applicantCount; }
    public long getApprovedCount() { return approvedCount; }
    public Integer getTotalHeadcount() { return totalHeadcount; }
    public LocalDate getDeadline() { return deadline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public RecruitAuthorResponse getAuthor() { return author; }

    /**
     * alias getter
     * - activityCategory == type
     */
    public String getActivityCategory() { return type; }

    /**
     * alias getter
     * - recruitCategory == category
     */
    public String getRecruitCategory() { return category; }
}
