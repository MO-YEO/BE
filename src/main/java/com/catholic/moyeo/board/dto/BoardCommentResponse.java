package com.catholic.moyeo.board.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardCommentResponse {

    private Long commentId;
    private String content;

    private Long parentId;

    private Long userId;
    private String nickname;

    private boolean mine; //내가 쓴 댓글인지 여부

    private LocalDateTime createdAt;
}