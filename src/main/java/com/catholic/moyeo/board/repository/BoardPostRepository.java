package com.catholic.moyeo.board.repository;

import com.catholic.moyeo.board.domain.BoardPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 게시글 조회/검색 Repository
 *
 * 역할:
 * - 전체 게시글 목록 조회
 * - keyword 기반 제목/본문 검색
 * - 특정 사용자의 게시글 목록 조회
 *
 * 주의:
 * - keyword가 null이면 전체 조회로 동작해야 한다.
 * - Pageable의 sort 필드(createdAt 등)가 엔티티 필드와 반드시 일치해야 한다.
 */
public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    /**
     * 게시글 검색 (전체 조회 + keyword 검색)
     *
     * 동작:
     * - keyword == null → 전체 게시글 조회
     * - keyword != null → 제목/본문 LIKE 검색
     *
     * 정렬:
     * - 기본적으로 최신순(createdAt desc)
     * - Pageable에서 sort를 따로 넘겨도 JPQL 기준 정렬 안전하게 유지
     */
    @Query("""
        select p
        from BoardPost p
        where (:keyword is null
               or lower(p.title) like lower(concat('%', :keyword, '%'))
               or lower(p.content) like lower(concat('%', :keyword, '%')))
        order by p.createdAt desc
        """)
    Page<BoardPost> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 내 게시글 조회
     *
     * authorUserId 기준으로 조회
     */
    Page<BoardPost> findByAuthorUserId(Long authorUserId, Pageable pageable);
}