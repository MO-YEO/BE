package com.catholic.moyeo.recruit.repository;

import com.catholic.moyeo.recruit.domain.RecruitApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * recruit_application 접근 레이어
 *
 * 정책(중요):
 * - 중복 지원 방지는 UNIQUE(recruit_post_id, user_id) + exists/find 조회로 2중 방어한다.
 * - 취소는 row 삭제이므로, "내가 지원한 목록"에는 실제 row가 존재하는 것만 조회된다.
 *
 * NOTE(팀 공유):
 * - applicant_count의 기준은 recruit_application row 개수가 아니라
 *   recruit_post.applicant_count(작성자 포함 현재 참여 인원)이다.
 * - 따라서 countByRecruitPostId 같은 메서드는 정책 오사용 가능성이 있어 두지 않는다.
 */
public interface RecruitApplicationRepository extends JpaRepository<RecruitApplication, Long> {

    /**
     * 특정 모집글에 대해 현재 사용자의 지원 row 조회
     * - apply 멱등 / cancel 멱등 / appliedByMe 계산에 사용
     */
    Optional<RecruitApplication> findByRecruitPostIdAndUserId(Long recruitPostId, Long userId);

    /**
     * 특정 모집글에 대해 현재 사용자의 지원 row 존재 여부
     * - 목록/상세의 appliedByMe 계산
     * - apply 멱등 체크
     */
    boolean existsByRecruitPostIdAndUserId(Long recruitPostId, Long userId);

    /**
     * 특정 모집글의 지원(application) 목록 조회
     * - 작성자 전용 지원자 목록 API에서 사용
     */
    Page<RecruitApplication> findByRecruitPostId(Long recruitPostId, Pageable pageable);

    Page<RecruitApplication> findByUserId(Long userId, Pageable pageable);

    /**
     * 특정 모집글의 총 지원자 수 조회
     */
    long countByRecruitPostId(Long recruitPostId);

    /**
     * 특정 모집글의 특정 상태인 지원 목록 조회 (예: ACCEPTED 팀원 목록)
     */
    List<RecruitApplication> findByRecruitPostIdAndStatus(Long recruitPostId, com.catholic.moyeo.recruit.domain.RecruitApplicationStatus status);

    /**
     * 특정 사용자가 지원한 모집글 ID 목록 조회 (배치 처리용)
     */
    @Query("SELECT a.recruitPostId FROM RecruitApplication a WHERE a.userId = :userId AND a.recruitPostId IN :recruitPostIds")
    List<Long> findAppliedPostIdsByUserIdAndRecruitPostIds(@Param("userId") Long userId, @Param("recruitPostIds") List<Long> recruitPostIds);

    /**
     * 여러 모집글의 지원자 수를 한 번에 조회 (배치 처리용)
     */
    @Query("SELECT a.recruitPostId, COUNT(a) FROM RecruitApplication a WHERE a.recruitPostId IN :recruitPostIds GROUP BY a.recruitPostId")
    List<Object[]> countGroupByRecruitPostIds(@Param("recruitPostIds") List<Long> recruitPostIds);

    /**
     * 회원 탈퇴 시 해당 유저의 지원 내역 전체 삭제
     */
    void deleteByUserId(Long userId);

    /**
     * 모집글 삭제 시 해당 모집글의 지원 내역 전체 삭제
     */
    void deleteByRecruitPostId(Long recruitPostId);
}