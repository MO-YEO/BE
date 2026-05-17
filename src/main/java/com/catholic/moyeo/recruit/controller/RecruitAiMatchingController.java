package com.catholic.moyeo.recruit.controller;

import com.catholic.moyeo.recruit.dto.RecruitAiMatchingResponse;
import com.catholic.moyeo.recruit.service.RecruitAiMatchingService;
import com.catholic.moyeo.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recruit-posts")
public class RecruitAiMatchingController {

    private final RecruitAiMatchingService recruitAiMatchingService;

    @GetMapping("/{recruitPostId}/matching/ai/me")
    public RecruitAiMatchingResponse getMyAiMatchingResult(
            @PathVariable Long recruitPostId
    ) {
        Long memberId = AuthUtil.currentMemberId();

        return recruitAiMatchingService.getAiMatchingResult(recruitPostId, memberId);
    }
}