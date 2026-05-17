package com.catholic.moyeo.recruit.controller;

import com.catholic.moyeo.recruit.dto.RecruitRecommendationResponse;
import com.catholic.moyeo.recruit.service.RecruitRecommendationService;
import com.catholic.moyeo.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recruits")
public class RecruitRecommendationController {

    private final RecruitRecommendationService recruitRecommendationService;

    @GetMapping("/recommendations/me")
    public List<RecruitRecommendationResponse> getMyRecommendations() {

        Long memberId = AuthUtil.currentMemberId();

        return recruitRecommendationService.getMyRecommendations(memberId);
    }
}