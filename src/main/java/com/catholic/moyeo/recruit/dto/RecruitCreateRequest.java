package com.catholic.moyeo.recruit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * 모집글 생성 요청 (MVP)
 *
 * 정책(중요):
 * - author_user_id는 서버에서 인증 주체(memberId)로 채운다. (클라에서 받지 않음)
 * - status는 항상 OPEN으로 시작한다. (클라에서 받지 않음)
 * - applicant_count는 작성자 포함 1로 시작한다. (클라에서 받지 않음)
 *
 * 값 고정:
 * - type/category는 허용 값 고정 대상. DTO에서는 String으로 받고 서비스에서 검증 후 400 처리한다.
 *
 * CSV 정책:
 * - required_skills는 API는 List<String> skills로 받고, 서버에서 CSV로 join하여 DB에 저장한다.
 *
 * tag 정책:
 * - tag는 표시만. (검색/필터 X)
 * - ERD상 tag는 VARCHAR(50) 단일 값이므로, MVP에서는 단일 String로만 받는다.
 *
 * NOTE(팀 공유 / API-ERD 불일치 가능):
 * - 너가 올린 API 스샷에 contactType/contactValue/roles/studyDetail 등이 있었지만,
 *   현재 ERD( recruit_post )에는 해당 컬럼이 없다.
 *   => MVP에서는 ERD에 존재하는 필드만 Create에 포함한다.
 */
public class RecruitCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String type;

    @NotBlank
    @Size(max = 50)
    private String category;

    @Size(max = 50)
    private String tag; // 표시용 단일 태그 (optional)

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    private String content;

    /**
     * required_skills (CSV 저장)
     */
    private List<String> skills;

    @NotNull
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