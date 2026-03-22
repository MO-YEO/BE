package com.catholic.moyeo.board.controller;

import com.catholic.moyeo.board.service.BoardPostLikeService;
import com.catholic.moyeo.board.service.CurrentUserProvider;
import com.catholic.moyeo.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards/posts")
@RequiredArgsConstructor
public class BoardPostLikeController {

    private final BoardPostLikeService boardPostLikeService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable Long id) {
        Long me = currentUserProvider.getCurrentUserId();
        boardPostLikeService.toggleLike(me, id);
        return ResponseEntity.ok().build();
    }
}