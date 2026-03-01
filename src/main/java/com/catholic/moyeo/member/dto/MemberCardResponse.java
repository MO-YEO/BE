package com.catholic.moyeo.member.dto;

import com.catholic.moyeo.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberCardResponse {

    private Long memberId;
    private String nickname;
    private String role;
    private String intro;
    private String githubUrl;
    private String profileImageUrl;
    private Long departmentId;
    private String departmentName;

    public static MemberCardResponse from(Member member) {
        return MemberCardResponse.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .role(member.getRole())
                .intro(member.getIntro())
                .githubUrl(member.getGithubUrl())
                .profileImageUrl(member.getProfileImageUrl())
                .departmentId(
                        member.getDepartment() != null ? member.getDepartment().getId() : null
                )
                .departmentName(
                        member.getDepartment() != null ? member.getDepartment().getName() : null
                )
                .build();
    }
}