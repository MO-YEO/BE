package com.catholic.moyeo.recruit.dto;

import com.catholic.moyeo.recruit.domain.RecruitPostStatus;
import jakarta.validation.constraints.NotNull;

/**
 * 모집 상태 변경 요청 (작성자 전용)
 * - OPEN <-> CLOSED
 */
public class RecruitStatusUpdateRequest {

    @NotNull
    private RecruitPostStatus status;

    public RecruitStatusUpdateRequest() {}

    public RecruitPostStatus getStatus() { return status; }
    public void setStatus(RecruitPostStatus status) { this.status = status; }
}