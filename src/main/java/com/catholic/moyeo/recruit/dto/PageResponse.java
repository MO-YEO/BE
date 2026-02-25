package com.catholic.moyeo.recruit.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * [MVP 응답 축약 DTO]
 * Spring Data Page를 그대로 반환하면 pageable/sort/offset 등 내부 메타데이터가 과다 노출됨.
 * 프론트 계약을 단순하게 유지하려고 필요한 값만 내려준다.
 */
public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(
                p.getContent(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.hasNext()
        );
    }
}