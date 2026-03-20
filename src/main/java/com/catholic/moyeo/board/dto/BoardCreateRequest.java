package com.catholic.moyeo.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 게시글 작성 요청
 *
 * body:
 * - title
 * - content
 */
public class BoardCreateRequest {

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    private String content;

    public BoardCreateRequest() {}

    public String getTitle() { return title; }
    public String getContent() { return content; }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
}