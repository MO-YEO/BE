package com.catholic.moyeo.recruit.repository;

import com.catholic.moyeo.recruit.domain.RecruitApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecruitApplicationRepository extends JpaRepository<RecruitApplication, Long> {

    Optional<RecruitApplication> findByRecruitPostIdAndUserId(Long recruitPostId, Long userId);

    boolean existsByRecruitPostIdAndUserId(Long recruitPostId, Long userId);

    long countByRecruitPostId(Long recruitPostId);

    Page<RecruitApplication> findByRecruitPostId(Long recruitPostId, Pageable pageable);

    Page<RecruitApplication> findByUserId(Long userId, Pageable pageable);
}