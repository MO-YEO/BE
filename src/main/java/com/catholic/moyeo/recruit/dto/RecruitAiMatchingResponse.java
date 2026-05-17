package com.catholic.moyeo.recruit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecruitAiMatchingResponse {

    private Long recruitPostId;
    private Long memberId;

    // 역할/포지션 매칭
    private String memberRole;
    private String recruitCategory;
    private boolean roleMatched;
    private int roleScore;

    // 관심 분야 매칭
    private List<String> memberActivityCategories;
    private String recruitActivityCategory;
    private boolean activityCategoryMatched;
    private int activityCategoryScore;

    // 기술스택 매칭
    private List<String> requiredSkills;
    private List<String> memberSkills;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private int skillScore;

    // 최종 매칭 점수
    private int matchingScore;

    // AI 추천 코멘트
    private String aiComment;
}