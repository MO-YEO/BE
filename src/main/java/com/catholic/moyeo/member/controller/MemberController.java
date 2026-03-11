package com.catholic.moyeo.member.controller;

import com.catholic.moyeo.member.dto.MyProfileResponse;
import com.catholic.moyeo.member.dto.UpdateMyProfileRequest;
import com.catholic.moyeo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    // 내 프로필 조회
    @GetMapping("/me")
    public MyProfileResponse getMyProfile(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal(); // JWT에서 넣어준 principal
        return memberService.getMyProfile(memberId);
    }

    // 내 프로필 수정
    @PatchMapping("/me")
    public MyProfileResponse updateMyProfile(
            Authentication authentication,
            @RequestBody UpdateMyProfileRequest request
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        return memberService.updateMyProfile(memberId, request);
    }
}