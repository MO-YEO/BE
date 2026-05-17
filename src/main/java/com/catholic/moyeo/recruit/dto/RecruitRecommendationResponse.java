package com.catholic.moyeo.recruit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class RecruitRecommendationResponse {

    private Long recruitPostId;

    private String title;

    private String activityCategory;

    private String recruitCategory;

    private List<String> requiredSkills;

    private List<String> memberSkills;

    private List<String> matchedSkills;

    private List<String> missingSkills;

    private int roleScore;

    private int activityCategoryScore;

    private int skillScore;

    private int matchingScore;

    private int totalHeadcount;
    private int applicantCount;
    private LocalDate deadline;

    private String aiComment;
}