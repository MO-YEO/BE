package com.catholic.moyeo.recruit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 지원하기 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class RecruitApplyRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 30)
    private String name;

    @NotBlank(message = "역할/포지션은 필수입니다.")
    @Size(max = 100)
    private String role;

    @NotBlank(message = "자기소개는 필수입니다.")
    @Size(max = 1000)
    private String introduction;

    @NotBlank(message = "기술 스택은 필수입니다.")
    @Size(max = 500)
    private String requiredSkills; // 사용 가능한 툴 / 기술 스택

    @NotBlank(message = "연락처는 필수입니다.")
    @Size(max = 20)
    private String phoneNumber;

    @Email
    @Size(max = 255)
    private String contactEmail;

    @Size(max = 255)
    private String githubUrl;
}
