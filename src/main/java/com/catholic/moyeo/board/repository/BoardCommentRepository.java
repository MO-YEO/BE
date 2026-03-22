package com.catholic.moyeo.board.repository;

import com.catholic.moyeo.board.domain.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

    List<BoardComment> findByBoardPostIdOrderByCreatedAtAsc(Long boardPostId);

    long countByBoardPostId(Long boardPostId);
}