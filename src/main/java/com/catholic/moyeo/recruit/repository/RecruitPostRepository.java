package com.catholic.moyeo.recruit.repository;

import com.catholic.moyeo.recruit.domain.RecruitPost;
import com.catholic.moyeo.recruit.domain.RecruitPostStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecruitPostRepository extends JpaRepository<RecruitPost, Long>, JpaSpecificationExecutor<RecruitPost> {

    Page<RecruitPost> findByAuthorUserId(Long authorUserId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from RecruitPost p where p.id = :id")
    Optional<RecruitPost> findByIdForUpdate(@Param("id") Long id);

    default Page<RecruitPost> search(
            String type,
            String category,
            RecruitPostStatus status,
            String keyword,
            Pageable pageable
    ) {
        return findAll((root, query, cb) -> {
            var predicates = cb.conjunction();

            if (type != null && !type.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("type"), type));
            }
            if (category != null && !category.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("category"), category));
            }
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword + "%";
                predicates = cb.and(predicates,
                        cb.or(
                                cb.like(root.get("title"), like),
                                cb.like(root.get("content"), like)
                        )
                );
            }

            return predicates;
        }, pageable);
    }
}