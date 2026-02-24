package com.catholic.moyeo.recruit.dto;

import com.catholic.moyeo.recruit.domain.RecruitPost;
import com.catholic.moyeo.recruit.domain.RecruitPostStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 모집글 목록(Summary) 응답 (MVP)
 *
 * API 기준:
 * - recruits[] { recruitId, title, type, status, skills[], appliedByMe, applicantCount }
 *
 * applicantCount 정의(확정):
 * - recruit_post.applicant_count (작성자 포함 현재 참여 인원)
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

    public static RecruitSummaryResponse from(
            RecruitPost p,
            List<String> skills,
            boolean appliedByMe
    ) {
        RecruitSummaryResponse r = new RecruitSummaryResponse();
        r.recruitId = p.getId();
        r.type = p.getType();
        r.category = p.getCategory();
        r.tag = p.getTag();
        r.title = p.getTitle();
        r.status = p.getStatus();
        r.skills = skills;
        r.appliedByMe = appliedByMe;
        r.applicantCount = p.getApplicantCount();
        r.totalHeadcount = (int) p.getTotalHeadcount();
        r.deadline = p.getDeadline();
        r.createdAt = p.getCreatedAt();
        return r;
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
}