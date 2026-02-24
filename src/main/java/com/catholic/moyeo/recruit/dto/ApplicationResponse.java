package com.catholic.moyeo.recruit.dto;

import com.catholic.moyeo.recruit.domain.RecruitApplication;
import com.catholic.moyeo.recruit.domain.RecruitApplicationStatus;

import java.time.LocalDateTime;

/**
 * 지원자 목록 조회 응답 DTO (작성자 전용)
 *
 * API 기준:
 * - applicants[] { memberId, nickname, part, status }
 *
 * NOTE:
 * - recruit_application은 userId만 들고 있으므로 nickname/part는 user_profile에서 별도 조회 필요
 */
public class ApplicationResponse {

    private Long applicationId;
    private Long recruitPostId;

    /**
     * API의 memberId에 해당 (MVP에서는 user_profile.user_id = app_user.user_id 공유 PK)
     */
    private Long memberId;

    private String nickname; // user_profile.nickname (필수 정책)

    private RecruitApplicationStatus status;

    private LocalDateTime createdAt;

    public static ApplicationResponse from(RecruitApplication a, String nickname) {
        ApplicationResponse r = new ApplicationResponse();
        r.applicationId = a.getId();
        r.recruitPostId = a.getRecruitPostId();
        r.memberId = a.getUserId();
        r.nickname = nickname;
        r.status = a.getStatus();
        r.createdAt = a.getCreatedAt();
        return r;
    }

    public Long getApplicationId() { return applicationId; }
    public Long getRecruitPostId() { return recruitPostId; }
    public Long getMemberId() { return memberId; }
    public String getNickname() { return nickname; }
    public RecruitApplicationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}