package com.catholic.moyeo.recruit.dto;

import com.catholic.moyeo.recruit.domain.RecruitPost;
import com.catholic.moyeo.recruit.domain.RecruitPostStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 모집글 목록 아이템 응답 DTO
 *
 * API 기준:
 * recruits[] {
 *   recruitId,
 *   type,
 *   category,
 *   tag,
 *   title,
 *   status,
 *   skills,
 *   appliedByMe,
 *   applicantCount,
 *   totalHeadcount,
 *   deadline,
 *   createdAt,
 *   author {
 *     memberId,
 *     nickname,
 *     departmentName
 *   }
 * }
 *
 * NOTE:
 * - applicantCount는 "작성자 포함 현재 참여 인원"이다.
 * - author 정보는 Member 조회 결과를 서비스에서 조합해서 내려준다.
 */
public class RecruitSummaryResponse {

    private Long recruitId;
    private String type;
    private String category;
    private String tag;
    private String title;
    private RecruitPostStatus status;
    private List<String> skills;
    private boolean appliedByMe;
    private long applicantCount;
    private Integer totalHeadcount;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private RecruitAuthorResponse author;

    public static RecruitSummaryResponse from(
            RecruitPost post,
            List<String> skills,
            boolean appliedByMe,
            RecruitAuthorResponse author
    ) {
        RecruitSummaryResponse response = new RecruitSummaryResponse();
        response.recruitId = post.getId();
        response.type = post.getType();
        response.category = post.getCategory();
        response.tag = post.getTag();
        response.title = post.getTitle();
        response.status = post.getStatus();
        response.skills = skills;
        response.appliedByMe = appliedByMe;
        response.applicantCount = post.getApplicantCount();
        response.totalHeadcount = (int) post.getTotalHeadcount();
        response.deadline = post.getDeadline();
        response.createdAt = post.getCreatedAt();
        response.author = author;
        return response;
    }

    public Long getRecruitId() { return recruitId; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getTag() { return tag; }
    public String getTitle() { return title; }
    public RecruitPostStatus getStatus() { return status; }
    public List<String> getSkills() { return skills; }
    public boolean isAppliedByMe() { return appliedByMe; }
    public long getApplicantCount() { return applicantCount; }
    public Integer getTotalHeadcount() { return totalHeadcount; }
    public LocalDate getDeadline() { return deadline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public RecruitAuthorResponse getAuthor() { return author; }
}