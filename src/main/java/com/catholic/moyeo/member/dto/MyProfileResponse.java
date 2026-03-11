package com.catholic.moyeo.member.dto;


import com.catholic.moyeo.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyProfileResponse {

    private Long memberId;
    private String email;
    private boolean emailVerified;
    private String nickname;
    private String role;
    private String intro;
    private String githubUrl;
    private String profileImageUrl;
    private Long departmentId;
    private String departmentName;

    public static MyProfileResponse from(Member member) {
        return MyProfileResponse.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .emailVerified(member.isEmailVerified())
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