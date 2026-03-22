package com.catholic.moyeo.board.controller;

import com.catholic.moyeo.board.dto.BoardCommentResponse;
import com.catholic.moyeo.board.dto.CommentCreateRequest;
import com.catholic.moyeo.board.dto.CommentUpdateRequest;
import com.catholic.moyeo.board.service.BoardCommentService;
import com.catholic.moyeo.board.service.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boards/posts")
@RequiredArgsConstructor
public class BoardCommentController {

    private final BoardCommentService boardCommentService;
    private final CurrentUserProvider currentUserProvider;

    // 댓글 작성 (대댓글 포함)
    @PostMapping("/{postId}/comments")
    public ResponseEntity<Void> create(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest request
    ) {
        Long me = currentUserProvider.getCurrentUserId();

        boardCommentService.create(
                me,
                postId,
                request.getContent(),
                request.getParentId()
        );

        return ResponseEntity.ok().build();
    }

    // 댓글 조회
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<BoardCommentResponse>> getComments(
            @PathVariable Long postId
    ) {
        Long me = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(boardCommentService.getComments(postId, me));
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long commentId) {
        Long me = currentUserProvider.getCurrentUserId();
        boardCommentService.delete(me, commentId);
        return ResponseEntity.noContent().build();
    }

    //댓글 수정
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Void> update(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request
    ) {
        Long me = currentUserProvider.getCurrentUserId();
        boardCommentService.update(me, commentId, request.getContent());
        return ResponseEntity.ok().build();
    }
}
