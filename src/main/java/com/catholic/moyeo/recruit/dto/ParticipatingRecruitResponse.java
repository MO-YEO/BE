package com.catholic.moyeo.recruit.dto;

import com.catholic.moyeo.recruit.domain.RecruitPost;
import com.catholic.moyeo.recruit.domain.RecruitPostStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ParticipatingRecruitResponse {
    private Long recruitId;
    private String type;
    private String title;
    private RecruitPostStatus status;
    private List<String> skills;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    
    // 참여 중인 팀원들의 ID 목록 (리뷰 기능용)
    private List<Long> participantIds;

    public static ParticipatingRecruitResponse from(RecruitPost p, List<String> skills, List<Long> participantIds) {
        ParticipatingRecruitResponse r = new ParticipatingRecruitResponse();
        r.recruitId = p.getId();
        r.type = p.getType();
        r.title = p.getTitle();
        r.status = p.getStatus();
        r.skills = skills;
        r.deadline = p.getDeadline();
        r.createdAt = p.getCreatedAt();
        r.participantIds = participantIds;
        return r;
    }

    public Long getRecruitId() { return recruitId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public RecruitPostStatus getStatus() { return status; }
    public List<String> getSkills() { return skills; }
    public LocalDate getDeadline() { return deadline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<Long> getParticipantIds() { return participantIds; }
}
