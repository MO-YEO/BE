package com.catholic.moyeo.recruit.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecruitMatchingResponse {

/*recruitPostId   → 어떤 모집글에 대한 매칭 결과인지
memberId        → 현재 로그인한 사용자 ID
requiredSkills  → 모집글이 요구하는 기술
memberSkills    → 내가 가진 기술
matchedSkills   → 서로 겹치는 기술
missingSkills   → 모집글에는 있는데 나에게 없는 기술
matchingScore   → 매칭 점수*/


    private Long recruitPostId;

    private Long memberId;

    private List<String> requiredSkills;

    private List<String> memberSkills;

    private List<String> matchedSkills;

    private List<String> missingSkills;

    private int matchingScore;
}