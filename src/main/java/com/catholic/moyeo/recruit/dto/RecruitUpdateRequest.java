package com.catholic.moyeo.recruit.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * 모집글 수정 요청
 * - null 필드는 미수정
 *
 * 수정 가능 필드(ERD 기반):
 * - type, category, tag(표시용), title, content, skills(required_skills), totalHeadcount, deadline
 *
 * 정책:
 * - totalHeadcount를 applicantCount보다 작게 줄이려 하면 400 (서비스에서 검증)
 * - type/category 값 고정은 서비스에서 검증 후 400
 */
public class RecruitUpdateRequest {

    @Size(max = 50)
    private String type;

    @Size(max = 50)
    private String category;

    @Size(max = 50)
    private String tag;

    @Size(max = 120)
    private String title;

    private String content;

    private List<String> skills;

    private Integer totalHeadcount;

    private LocalDate deadline;

    public RecruitUpdateRequest() {}

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