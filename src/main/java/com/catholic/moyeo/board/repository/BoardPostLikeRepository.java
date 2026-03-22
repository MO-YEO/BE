package com.catholic.moyeo.board.repository;


import com.catholic.moyeo.board.domain.BoardPostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardPostLikeRepository extends JpaRepository<BoardPostLike, Long> {

    Optional<BoardPostLike> findByBoardPostIdAndUserId(Long boardPostId, Long userId);

    void deleteByBoardPostIdAndUserId(Long boardPostId, Long userId);

    long countByBoardPostId(Long boardPostId);

    boolean existsByBoardPostIdAndUserId(Long boardPostId, Long userId);
}