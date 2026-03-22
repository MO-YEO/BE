package com.catholic.moyeo.board.controller;

import com.catholic.moyeo.board.dto.BoardCreateRequest;
import com.catholic.moyeo.board.dto.BoardDetailResponse;
import com.catholic.moyeo.board.dto.BoardListResponse;
import com.catholic.moyeo.board.dto.BoardUpdateRequest;
import com.catholic.moyeo.board.service.BoardService;
import com.catholic.moyeo.board.service.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글 API
 *
 * 명세 대응:
 * - GET    /api/boards/posts
 * - GET    /api/boards/posts/{id}
 * - POST   /api/boards/posts
 * - PUT    /api/boards/posts/{id}
 * - DELETE /api/boards/posts/{id}
 * - GET    /api/boards/posts/me
 */
@RestController
@RequestMapping("/api/boards/posts")
public class BoardController {

    private final BoardService boardService;
    private final CurrentUserProvider currentUserProvider;

    public BoardController(BoardService boardService, CurrentUserProvider currentUserProvider) {
        this.boardService = boardService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<BoardListResponse> listPosts(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long me = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(boardService.listPosts(keyword, pageable, me));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardDetailResponse> getPost(@PathVariable Long id) {
        Long me = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(boardService.getPost(id, me));
    }

    @PostMapping
    public ResponseEntity<BoardDetailResponse> createPost(
            @Valid @RequestBody BoardCreateRequest request
    ) {
        Long me = currentUserProvider.getCurrentUserId();
        BoardDetailResponse response = boardService.createPost(me, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardDetailResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody BoardUpdateRequest request
    ) {
        Long me = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(boardService.updatePost(me, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        Long me = currentUserProvider.getCurrentUserId();
        boardService.deletePost(me, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<BoardListResponse> listMyPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long me = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(boardService.listMyPosts(me, pageable));
    }
}