package com.catholic.moyeo.board.dto;

import lombok.Getter;

@Getter
public class CommentCreateRequest {
    private String content;
    private Long parentId;
}