package com.catholic.moyeo.member.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateMyProfileRequest {

    @Size(max = 30, message = "nickname must be <= 30")
    private String nickname;

    @Size(max = 20, message = "role must be <= 20")
    private String role;

    @Size(max = 200, message = "intro must be <= 200")
    private String intro;

    @Size(max = 255, message = "githubUrl must be <= 255")
    private String githubUrl;

    private Long departmentId;
}