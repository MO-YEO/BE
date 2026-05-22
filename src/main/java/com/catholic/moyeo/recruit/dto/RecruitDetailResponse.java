package com.catholic.moyeo.recruit.dto;

/**
 * 모집글 단건 조회 응답 DTO
 *
 * API 명세:
 * {
 *   "recruit": { ...RecruitResponse 필드... }
 * }
 *
 * 정책:
 * - GET /api/recruits 의 recruits[] 안 아이템과 동일한 shape
 * - 단건이므로 pageInfo 없이 recruit 단일 필드로 래핑
 */
public record RecruitDetailResponse(
        RecruitResponse recruit
) {
    public static RecruitDetailResponse from(RecruitResponse recruit) {
        return new RecruitDetailResponse(recruit);
    }
}
