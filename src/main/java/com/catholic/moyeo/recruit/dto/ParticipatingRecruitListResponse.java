package com.catholic.moyeo.recruit.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record ParticipatingRecruitListResponse(
        List<ParticipatingRecruitResponse> recruits,
        PageInfoResponse pageInfo
) {
    public static ParticipatingRecruitListResponse from(Page<ParticipatingRecruitResponse> page) {
        return new ParticipatingRecruitListResponse(
                page.getContent(),
                PageInfoResponse.from(page)
        );
    }
}
