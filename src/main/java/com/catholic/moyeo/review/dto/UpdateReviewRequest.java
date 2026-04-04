package com.catholic.moyeo.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateReviewRequest {

    @Min(value = 1, message = "별점은 최소 1점입니다.")
    @Max(value = 5, message = "별점은 최대 5점입니다.")
    private short rating;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    private String content;
    
    protected UpdateReviewRequest() {}
}
