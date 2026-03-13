package com.catholic.moyeo.recruit.dto;

import com.catholic.moyeo.recruit.domain.RecruitPost;
import com.catholic.moyeo.recruit.domain.RecruitPostStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 모집글 상세 응답 DTO
 *
 * API 기준:
 * {
 *   "recruit": {
 *     "recruitId": ...,
 *     "type": ...,
 *     "category": ...,
 *     "tag": ...,
 *     "title": ...,
 *     "content": ...,
 *     "skills": [...],
 *     "status": ...,
 *     "totalHeadcount": ...,
 *     "deadline": ...,
 *     "author": {
 *       "memberId": ...,
 *       "nickname": ...,
 *       "departmentName": ...
 *     },
 *     "createdAt": ...,
 *     "updatedAt": ...
 *   },
 *   "appliedByMe": ...,
 *   "applicantCount": ...
 * }
 *
 * NOTE:
 * - author 정보는 recruit_post 컬럼이 아니라 별도 Member 조회 결과를 서비스에서 주입한다.
 * - profileImageUrl은 현재 범위에서 제외한다.
 */
public class RecruitDetailResponse {

    private Recruit recruit;
    private boolean appliedByMe;
    private long applicantCount;

    public static RecruitDetailResponse from(
            RecruitPost post,
            RecruitAuthorResponse author,
            List<String> skills,
            boolean appliedByMe
    ) {
        RecruitDetailResponse response = new RecruitDetailResponse();

        Recruit recruit = new Recruit();
        recruit.recruitId = post.getId();
        recruit.type = post.getType();
        recruit.category = post.getCategory();
        recruit.tag = post.getTag();
        recruit.title = post.getTitle();
        recruit.content = post.getContent();
        recruit.skills = skills;
        recruit.status = post.getStatus();
        recruit.totalHeadcount = (int) post.getTotalHeadcount();
        recruit.deadline = post.getDeadline();
        recruit.author = author;
        recruit.createdAt = post.getCreatedAt();
        recruit.updatedAt = post.getUpdatedAt();

        response.recruit = recruit;
        response.appliedByMe = appliedByMe;
        response.applicantCount = post.getApplicantCount();

        return response;
    }

    public Recruit getRecruit() { return recruit; }
    public boolean isAppliedByMe() { return appliedByMe; }
    public long getApplicantCount() { return applicantCount; }

    /**
     * 상세 응답의 recruit 본문
     */
    public static class Recruit {
        private Long recruitId;
        private String type;
        private String category;
        private String tag;
        private String title;
        private String content;
        private List<String> skills;
        private RecruitPostStatus status;
        private Integer totalHeadcount;
        private LocalDate deadline;
        private RecruitAuthorResponse author;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Long getRecruitId() { return recruitId; }
        public String getType() { return type; }
        public String getCategory() { return category; }
        public String getTag() { return tag; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public List<String> getSkills() { return skills; }
        public RecruitPostStatus getStatus() { return status; }
        public Integer getTotalHeadcount() { return totalHeadcount; }
        public LocalDate getDeadline() { return deadline; }
        public RecruitAuthorResponse getAuthor() { return author; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }
}