package com.catholic.moyeo.board.service;

import com.catholic.moyeo.board.domain.BoardPost;
import com.catholic.moyeo.board.domain.BoardPostImage;
import com.catholic.moyeo.board.dto.BoardAuthorResponse;
import com.catholic.moyeo.board.dto.BoardCreateRequest;
import com.catholic.moyeo.board.dto.BoardDetailResponse;
import com.catholic.moyeo.board.dto.BoardListResponse;
import com.catholic.moyeo.board.dto.BoardSummaryResponse;
import com.catholic.moyeo.board.dto.BoardUpdateRequest;
import com.catholic.moyeo.board.dto.PageInfoResponse;
import com.catholic.moyeo.board.repository.BoardCommentRepository;
import com.catholic.moyeo.board.repository.BoardPostImageRepository;
import com.catholic.moyeo.board.repository.BoardPostLikeRepository;
import com.catholic.moyeo.board.repository.BoardPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 게시글 서비스
 *
 * 정책:
 * - 작성자는 현재 로그인 사용자
 * - 수정/삭제는 작성자만 가능
 * - 목록/내 목록은 같은 summary DTO를 사용한다.
 *
 * 참고:
 * - 현재 게시판은 category 없이 title + content만 사용한다.
 */
@Service
public class BoardService {

    private final BoardPostRepository boardPostRepository;
    private final BoardAuthorReader boardAuthorReader;
    private final BoardPostLikeRepository boardPostLikeRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final BoardPostImageRepository boardPostImageRepository;

    public BoardService(
            BoardPostRepository boardPostRepository,
            BoardAuthorReader boardAuthorReader,
            BoardPostLikeRepository boardPostLikeRepository,
            BoardCommentRepository boardCommentRepository,
            BoardPostImageRepository boardPostImageRepository
    ) {
        this.boardPostRepository = boardPostRepository;
        this.boardAuthorReader = boardAuthorReader;
        this.boardPostLikeRepository = boardPostLikeRepository;
        this.boardCommentRepository = boardCommentRepository;
        this.boardPostImageRepository = boardPostImageRepository;
    }

    /**
     * 게시글 목록 조회
     *
     * - keyword는 제목/본문 검색에 사용
     * - page/size는 Pageable로 처리
     */
    @Transactional(readOnly = true)
    public BoardListResponse listPosts(String keyword, Pageable pageable, Long me) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<BoardPost> page = boardPostRepository.search(normalizedKeyword, pageable);

        List<BoardSummaryResponse> posts = toSummaryResponses(page.getContent(), me);
        return BoardListResponse.of(posts, PageInfoResponse.from(page));
    }

    /**
     * 게시글 상세 조회
     *
     * - mine은 현재 사용자가 작성자인지 여부
     */
    @Transactional(readOnly = true)
    public BoardDetailResponse getPost(Long postId, Long me) {
        BoardPost post = getPostEntity(postId);
        BoardAuthorResponse author = boardAuthorReader.getAuthor(post.getAuthorUserId());

        long likeCount = boardPostLikeRepository.countByBoardPostId(post.getId());
        long commentCount = boardCommentRepository.countByBoardPostId(post.getId());

        boolean likedByMe = (me != null) &&
                boardPostLikeRepository.existsByBoardPostIdAndUserId(post.getId(), me);

        //  이미지 조회 추가
        List<String> images = getImages(post.getId());

        return BoardDetailResponse.from(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                author,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getAuthorUserId().equals(me),
                likeCount,
                commentCount,
                likedByMe,
                images   // 추가
        );
    }

    /**
     * 게시글 작성
     */
    @Transactional
    public BoardDetailResponse createPost(Long me, BoardCreateRequest request) {

        BoardPost saved = boardPostRepository.save(
                new BoardPost(
                        me,
                        request.getTitle().trim(),
                        request.getContent().trim()
                )
        );

        //  이미지 저장
        if (request.getImages() != null) {
            for (String url : request.getImages()) {
                boardPostImageRepository.save(
                        new BoardPostImage(saved.getId(), url)
                );
            }
        }

        BoardAuthorResponse author = boardAuthorReader.getAuthor(saved.getAuthorUserId());

        long likeCount = boardPostLikeRepository.countByBoardPostId(saved.getId());
        long commentCount = boardCommentRepository.countByBoardPostId(saved.getId());

        List<String> images = getImages(saved.getId());

        return BoardDetailResponse.from(
                saved.getId(),
                saved.getTitle(),
                saved.getContent(),
                author,
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                true,
                likeCount,
                commentCount,
                false,
                images   //  추가
        );
    }

    /**
     * 게시글 수정
     *
     * - 작성자만 가능
     * - null이 아닌 필드만 반영
     */
    @Transactional
    public BoardDetailResponse updatePost(Long me, Long postId, BoardUpdateRequest request) {
        BoardPost post = getPostEntity(postId);
        ensureAuthor(post, me);

        post.update(
                trimToNull(request.getTitle()),
                trimToNull(request.getContent())
        );

        //  기존 이미지 삭제 후 다시 저장
        if (request.getImages() != null) {
            boardPostImageRepository.deleteByPostId(postId);

            for (String url : request.getImages()) {
                boardPostImageRepository.save(
                        new BoardPostImage(postId, url)
                );
            }
        }

        BoardAuthorResponse author = boardAuthorReader.getAuthor(post.getAuthorUserId());

        long likeCount = boardPostLikeRepository.countByBoardPostId(post.getId());
        long commentCount = boardCommentRepository.countByBoardPostId(post.getId());

        boolean likedByMe = (me != null) &&
                boardPostLikeRepository.existsByBoardPostIdAndUserId(post.getId(), me);

        List<String> images = getImages(post.getId());

        return BoardDetailResponse.from(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                author,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                true,
                likeCount,
                commentCount,
                likedByMe,
                images   //  추가
        );
    }

    /**
     * 게시글 삭제
     *
     * - 작성자만 가능
     */
    @Transactional
    public void deletePost(Long me, Long postId) {
        BoardPost post = getPostEntity(postId);
        ensureAuthor(post, me);

        //  이미지도 같이 삭제
        boardPostImageRepository.deleteByPostId(postId);

        boardPostRepository.delete(post);
    }

    /**
     * 내가 작성한 게시글 목록 조회
     */
    @Transactional(readOnly = true)
    public BoardListResponse listMyPosts(Long me, Pageable pageable) {
        Page<BoardPost> page = boardPostRepository.findByAuthorUserId(me, pageable);
        List<BoardSummaryResponse> posts = toSummaryResponses(page.getContent(), me);
        return BoardListResponse.of(posts, PageInfoResponse.from(page));
    }

    private BoardPost getPostEntity(Long postId) {
        return boardPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. postId=" + postId));
    }

    private void ensureAuthor(BoardPost post, Long me) {
        if (!post.getAuthorUserId().equals(me)) {
            throw new IllegalStateException("작성자만 접근할 수 있습니다.");
        }
    }

    /**
     * 목록에서 작성자 정보를 게시글마다 개별 조회하지 않도록
     * userId를 모아 한 번에 조회한다.
     */
    private List<BoardSummaryResponse> toSummaryResponses(List<BoardPost> posts, Long me) {

        Set<Long> userIds = new LinkedHashSet<>();
        for (BoardPost post : posts) {
            userIds.add(post.getAuthorUserId());
        }

        Map<Long, BoardAuthorResponse> authors = boardAuthorReader.getAuthors(userIds);

        return posts.stream()
                .map(post -> {

                    long likeCount = boardPostLikeRepository
                            .countByBoardPostId(post.getId());

                    long commentCount = boardCommentRepository.countByBoardPostId(post.getId());

                    boolean likedByMe = (me != null) &&
                            boardPostLikeRepository.existsByBoardPostIdAndUserId(post.getId(), me);

                    return BoardSummaryResponse.from(
                            post.getId(),
                            post.getTitle(),
                            authors.getOrDefault(
                                    post.getAuthorUserId(),
                                    new BoardAuthorResponse(post.getAuthorUserId(), null)
                            ),
                            post.getCreatedAt(),
                            likeCount,
                            commentCount,
                            likedByMe
                    );
                })
                .toList();
    }

    //  이미지 조회
    private List<String> getImages(Long postId) {
        List<BoardPostImage> images = boardPostImageRepository.findByPostId(postId);

        List<String> result = new ArrayList<>();
        for (BoardPostImage img : images) {
            result.add(img.getImageUrl());
        }
        return result;
    }


    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }


}