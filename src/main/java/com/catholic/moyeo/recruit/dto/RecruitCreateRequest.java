package com.catholic.moyeo.recruit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * 모집글 생성 요청
 *
 * API 명세 body:
 * - type
 * - category
 * - tag
 * - title
 * - content
 * - skills
 * - totalHeadcount
 * - deadline
 *
 * 정책:
 * - author는 서버에서 인증 사용자로 결정한다.
 * - status는 서버에서 OPEN으로 강제한다.
 * - applicantCount는 서버에서 초기값을 설정한다.
 *
 * 값 고정 필드:
 * - type, category는 DTO에서 String으로 받고
 *   서비스에서 허용값 검증 후 400으로 처리한다.
 *
 * skills 저장 정책:
 * - API는 List<String>으로 받고
 * - 서버에서 CSV 문자열로 변환하여 저장한다.
 *
 * tag 정책:
 * - tag는 표시용 단일 문자열이다.
 * - 검색/필터 파라미터로는 사용하지 않는다.
 */
public class RecruitCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String type;

    @NotBlank
    @Size(max = 50)
    private String category;

    @Size(max = 50)
    private String tag;

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    private String content;

    /**
     * required_skills
     * - API: List<String>
     * - DB: CSV 문자열
     */
    @Valid
    private List<@NotBlank @Size(max = 50) String> skills;

    @NotNull
    @Min(1)
    private Integer totalHeadcount;

    private LocalDate deadline;

    public RecruitCreateRequest() {}

    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getTag() { return tag; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public List<String> getSkills() { return skills; }
    public Integer getTotalHeadcount() { return totalHeadcount; }
    public LocalDate getDeadline() { return deadline; }

    public void setType(String type) { this.type = type; }
    public void setCategory(String category) { this.category = category; }
    public void setTag(String tag) { this.tag = tag; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public void setTotalHeadcount(Integer totalHeadcount) { this.totalHeadcount = totalHeadcount; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
}