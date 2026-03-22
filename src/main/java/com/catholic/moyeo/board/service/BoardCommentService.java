package com.catholic.moyeo.board.service;

import com.catholic.moyeo.board.domain.BoardComment;
import com.catholic.moyeo.board.dto.BoardAuthorResponse;
import com.catholic.moyeo.board.dto.BoardCommentResponse;
import com.catholic.moyeo.board.repository.BoardCommentRepository;
import com.catholic.moyeo.board.repository.BoardPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BoardCommentService {

    private final BoardCommentRepository boardCommentRepository;
    private final BoardPostRepository boardPostRepository;
    private final BoardAuthorReader boardAuthorReader;

    @Transactional
    public void create(Long userId, Long postId, String content, Long parentId) {

        // 게시글 존재 체크
        boardPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // 대댓글이면 부모 댓글 체크
        if (parentId != null) {
            BoardComment parent = boardCommentRepository.findById(parentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            //  다른 게시글 댓글에 달면 안됨
            if (!parent.getBoardPostId().equals(postId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }

        // 내용 검증 (추가)
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        boardCommentRepository.save(
                new BoardComment(postId, userId, content.trim(), parentId)
        );
    }

    @Transactional(readOnly = true)
    public List<BoardCommentResponse> getComments(Long postId, Long me) {

        List<BoardComment> comments =
                boardCommentRepository.findByBoardPostIdOrderByCreatedAtAsc(postId);

        // 작성자 한번에 조회
        Set<Long> userIds = new HashSet<>();
        for (BoardComment c : comments) {
            userIds.add(c.getUserId());
        }

        Map<Long, BoardAuthorResponse> authors =
                boardAuthorReader.getAuthors(userIds);

        return comments.stream()
                .map(c -> BoardCommentResponse.builder()
                        .commentId(c.getId())
                        .content(c.getContent())
                        .parentId(c.getParentId())
                        .userId(c.getUserId())
                        .nickname(
                                authors.getOrDefault(
                                        c.getUserId(),
                                        new BoardAuthorResponse(c.getUserId(), null)
                                ).getNickname()
                        )
                        .mine(c.getUserId().equals(me))
                        .createdAt(c.getCreatedAt())
                        .build()
                )
                .toList();
    }

    @Transactional
    public void delete(Long userId, Long commentId) {

        BoardComment comment = boardCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (userId == null || !comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 댓글만 삭제할 수 있습니다.");
        }

        boardCommentRepository.delete(comment);
    }

    @Transactional
    public void update(Long userId, Long commentId, String content) {

        BoardComment comment = boardCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));

        if (userId == null || !comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 댓글만 수정할 수 있습니다.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글 내용은 비어 있을 수 없습니다.");
        }

        comment.updateContent(content.trim());
    }
}