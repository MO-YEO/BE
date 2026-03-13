package com.catholic.moyeo.recruit.dto;

/**
 * 모집글 작성자 요약 정보
 *
 * API 기준:
 * - memberId
 * - nickname
 * - departmentName
 *
 * NOTE:
 * - profileImageUrl은 현재 범위에서 제외한다.
 * - department가 없는 경우 departmentName은 null 가능
 */
public class RecruitAuthorResponse {

    private Long memberId;
    private String nickname;
    private String departmentName;

    public RecruitAuthorResponse(Long memberId, String nickname, String departmentName) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.departmentName = departmentName;
    }

    public Long getMemberId() { return memberId; }
    public String getNickname() { return nickname; }
    public String getDepartmentName() { return departmentName; }
}