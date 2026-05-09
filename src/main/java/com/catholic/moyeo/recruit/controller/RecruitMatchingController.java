package com.catholic.moyeo.recruit.controller;


import com.catholic.moyeo.recruit.dto.RecruitMatchingResponse;
import com.catholic.moyeo.recruit.service.RecruitMatchingService;
import com.catholic.moyeo.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recruit-posts")
public class RecruitMatchingController {

    private final RecruitMatchingService recruitMatchingService;

    @GetMapping("/{recruitPostId}/matching/me")
    public RecruitMatchingResponse getMyMatchingResult(
            @PathVariable Long recruitPostId
    ) {
        Long memberId = AuthUtil.currentMemberId();

        return recruitMatchingService.getMatchingResult(recruitPostId, memberId);
    }
}