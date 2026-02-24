package com.catholic.moyeo.recruit.dto;

import com.catholic.moyeo.recruit.domain.RecruitApplicationStatus;
import com.catholic.moyeo.recruit.domain.RecruitPost;
import com.catholic.moyeo.recruit.domain.RecruitPostStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 내가 지원한 모집글 목록 아이템
 *
 * API 기준:
 * - skills[] 키로 내려주는 형태가 일반적이므로 필드명을 skills로 통일한다.
 *
 * NOTE(팀 공유):
 * - applicationStatus는 recruit_application.status를 그대로 내려준다.
 * - 취소는 row 삭제라서, "내가 지원한 목록"에는 row가 존재하는 것만 조회된다.
 */
public class MyAppliedRecruitResponse {

    private Long recruitId;
    private String type;
    private String title;
    private RecruitPostStatus status;

    private List<String> skills;

    private RecruitApplicationStatus applicationStatus;

    private LocalDate deadline;
    private LocalDateTime createdAt;

    public static MyAppliedRecruitResponse from(RecruitPost p, List<String> skills, RecruitApplicationStatus applicationStatus) {
        MyAppliedRecruitResponse r = new MyAppliedRecruitResponse();
        r.recruitId = p.getId();
        r.type = p.getType();
        r.title = p.getTitle();
        r.status = p.getStatus();
        r.skills = skills;
        r.applicationStatus = applicationStatus;
        r.deadline = p.getDeadline();
        r.createdAt = p.getCreatedAt();
        return r;
    }

    public Long getRecruitId() { return recruitId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public RecruitPostStatus getStatus() { return status; }
    public List<String> getSkills() { return skills; }
    public RecruitApplicationStatus getApplicationStatus() { return applicationStatus; }
    public LocalDate getDeadline() { return deadline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}