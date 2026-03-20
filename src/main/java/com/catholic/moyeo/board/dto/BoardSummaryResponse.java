package com.catholic.moyeo.board.dto;

import java.time.LocalDateTime;

/**
 * 게시글 목록 응답
 *
 * 목록 / 내 게시글 목록에서 공통으로 사용한다.
 */
public class BoardSummaryResponse {

    private final Long postId;
    private final String title;
    private final BoardAuthorResponse author;
    private final LocalDateTime createdAt;

    public BoardSummaryResponse(
            Long postId,
            String title,
            BoardAuthorResponse author,
            LocalDateTime createdAt
    ) {
        this.postId = postId;
        this.title = title;
        this.author = author;
        this.createdAt = createdAt;
    }

    public static BoardSummaryResponse from(
            Long postId,
            String title,
            BoardAuthorResponse author,
            LocalDateTime createdAt
    ) {
        return new BoardSummaryResponse(postId, title, author, createdAt);
    }

    public Long getPostId() { return postId; }
    public String getTitle() { return title; }
    public BoardAuthorResponse getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}