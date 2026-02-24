package com.catholic.moyeo.recruit.dto;

import com.catholic.moyeo.recruit.domain.RecruitApplicationStatus;
import jakarta.validation.constraints.NotNull;

/**
 * 지원 승인/거절 요청
 *
 * 정책(중요):
 * - ACCEPTED 또는 REJECTED만 허용
 * - APPLIED로 되돌리는 API는 없음 (MVP)
 *
 * NOTE(팀 공유):
 * - 컨트롤러/DTO에서 APPLIED를 막지 말고, 서비스에서 최종 검증하여 400 처리하는 방식으로 통일한다.
 *   (이유: 메시지 통일 / 정책 변경 시 영향 최소화)
 */
public class ApplicationDecisionRequest {

    @NotNull
    private RecruitApplicationStatus status;

    public ApplicationDecisionRequest() {}

    public RecruitApplicationStatus getStatus() { return status; }
    public void setStatus(RecruitApplicationStatus status) { this.status = status; }
}