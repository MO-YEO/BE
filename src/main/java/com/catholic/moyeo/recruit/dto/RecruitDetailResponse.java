package com.catholic.moyeo.recruit.dto;

import com.catholic.moyeo.recruit.domain.RecruitPost;
import com.catholic.moyeo.recruit.domain.RecruitPostStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 모집글 상세(Detail) 응답 (MVP)
 *
 * API 기준:
 * - recruit { recruitId, type, title, content, skills[], status, author{memberId,nickname}, createdAt } + appliedByMe + applicantCount
 *
 * NOTE(팀 공유 / API-ERD 불일치):
 * - roles/contact/studyDetail 등은 ERD에 없으므로 MVP에서는 제외한다.
 * - author 닉네임은 user_profile에서 조인/조회해서 내려준다.
 */
public class RecruitDetailResponse {

    private Long recruitId;

    private Long authorUserId;
    private String authorNickname;

    private String type;
    private String category;
    private String tag;

    private String title;
    private String content;

    private List<String> skills;

    private RecruitPostStatus status;

    private Integer totalHeadcount;

    private boolean appliedByMe;
    private long applicantCount;

    private LocalDate deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RecruitDetailResponse from(
            RecruitPost p,
            String authorNickname,
            List<String> skills,
            boolean appliedByMe
    ) {
        RecruitDetailResponse r = new RecruitDetailResponse();
        r.recruitId = p.getId();
        r.authorUserId = p.getAuthorUserId();
        r.authorNickname = authorNickname;
        r.type = p.getType();
        r.category = p.getCategory();
        r.tag = p.getTag();
        r.title = p.getTitle();
        r.content = p.getContent();
        r.skills = skills;
        r.status = p.getStatus();
        r.totalHeadcount = (int) p.getTotalHeadcount();
        r.appliedByMe = appliedByMe;
        r.applicantCount = p.getApplicantCount();
        r.deadline = p.getDeadline();
        r.createdAt = p.getCreatedAt();
        r.updatedAt = p.getUpdatedAt();
        return r;
    }

    public Long getRecruitId() { return recruitId; }
    public Long getAuthorUserId() { return authorUserId; }
    public String getAuthorNickname() { return authorNickname; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getTag() { return tag; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public List<String> getSkills() { return skills; }
    public RecruitPostStatus getStatus() { return status; }
    public Integer getTotalHeadcount() { return totalHeadcount; }
    public boolean isAppliedByMe() { return appliedByMe; }
    public long getApplicantCount() { return applicantCount; }
    public LocalDate getDeadline() { return deadline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}