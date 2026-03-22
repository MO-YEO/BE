package com.catholic.moyeo.board.service;

import com.catholic.moyeo.board.domain.BoardPostLike;
import com.catholic.moyeo.board.repository.BoardPostLikeRepository;
import com.catholic.moyeo.board.repository.BoardPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardPostLikeService {

    private final BoardPostRepository boardPostRepository;
    private final BoardPostLikeRepository boardPostLikeRepository;

    @Transactional
    public void toggleLike(Long userId, Long postId) {

        // 게시글 존재 체크
        boardPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // 이미 좋아요 했는지
        Optional<BoardPostLike> like =
                boardPostLikeRepository.findByBoardPostIdAndUserId(postId, userId);

        if (like.isPresent()) {
            // 좋아요 취소
            boardPostLikeRepository.delete(like.get());
        } else {
            // 좋아요 추가
            boardPostLikeRepository.save(new BoardPostLike(postId, userId));
        }
    }
}