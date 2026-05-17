package com.catholic.moyeo.recruit.service;

import com.catholic.moyeo.recruit.domain.RecruitPost;
import com.catholic.moyeo.recruit.domain.RecruitPostStatus;
import com.catholic.moyeo.recruit.dto.RecruitMatchingResponse;
import com.catholic.moyeo.recruit.dto.RecruitRecommendationResponse;
import com.catholic.moyeo.recruit.repository.RecruitPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitRecommendationService {

    private final RecruitPostRepository recruitPostRepository;
    private final RecruitMatchingService recruitMatchingService;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    private final RestClient restClient = RestClient.create();

    public List<RecruitRecommendationResponse> getMyRecommendations(Long memberId) {

        List<RecruitPost> openPosts =
                recruitPostRepository.findByStatus(RecruitPostStatus.OPEN);

        return openPosts.stream()
                .map(recruitPost -> {
                    RecruitMatchingResponse matching =
                            recruitMatchingService.getMatchingResult(recruitPost.getId(), memberId);

                    return new RecruitRecommendationResponse(
                            recruitPost.getId(),
                            recruitPost.getTitle(),
                            recruitPost.getActivityCategory(),
                            recruitPost.getRecruitCategory(),

                            matching.getRequiredSkills(),
                            matching.getMemberSkills(),
                            matching.getMatchedSkills(),
                            matching.getMissingSkills(),

                            matching.getRoleScore(),
                            matching.getActivityCategoryScore(),
                            matching.getSkillScore(),
                            matching.getMatchingScore(),

                            recruitPost.getTotalHeadcount(),
                            recruitPost.getApplicantCount(),
                            recruitPost.getDeadline(),

                            null
                    );
                })
                .sorted(
                        Comparator.comparingInt(RecruitRecommendationResponse::getMatchingScore).reversed()
                                .thenComparing(
                                        RecruitRecommendationResponse::getDeadline,
                                        Comparator.nullsLast(Comparator.naturalOrder())
                                )
                )
                .limit(10)
                .map(response -> new RecruitRecommendationResponse(
                        response.getRecruitPostId(),
                        response.getTitle(),
                        response.getActivityCategory(),
                        response.getRecruitCategory(),

                        response.getRequiredSkills(),
                        response.getMemberSkills(),
                        response.getMatchedSkills(),
                        response.getMissingSkills(),

                        response.getRoleScore(),
                        response.getActivityCategoryScore(),
                        response.getSkillScore(),
                        response.getMatchingScore(),

                        response.getTotalHeadcount(),
                        response.getApplicantCount(),
                        response.getDeadline(),

                        createAiComment(response)
                ))
                .toList();
    }

    private String createAiComment(RecruitRecommendationResponse response) {

        String prompt = """
                너는 대학생 팀 프로젝트 매칭 서비스를 위한 추천 설명을 작성하는 AI야.

                아래 정보를 보고 사용자가 이 모집글에 왜 추천되는지 짧고 자연스럽게 설명해줘.

                조건:
                - 한국어로 작성
                - 2~3문장
                - 너무 과장하지 말 것
                - 점수 계산을 다시 하지 말 것
                - 부족한 기술이 있으면 부드럽게 언급할 것
                - 사용자가 지원할 때 어필하면 좋은 점을 포함할 것

                [모집글 정보]
                제목: %s
                활동 분야: %s
                모집 역할: %s
                모집 인원: %d명
                현재 신청 인원: %d명
                마감일: %s

                [매칭 결과]
                전체 매칭 점수: %d점
                역할 점수: %d점
                활동 분야 점수: %d점
                기술스택 점수: %d점

                필요한 기술스택: %s
                내 기술스택: %s
                일치하는 기술스택: %s
                부족한 기술스택: %s
                """.formatted(
                response.getTitle(),
                response.getActivityCategory(),
                response.getRecruitCategory(),
                response.getTotalHeadcount(),
                response.getApplicantCount(),
                response.getDeadline(),

                response.getMatchingScore(),
                response.getRoleScore(),
                response.getActivityCategoryScore(),
                response.getSkillScore(),

                response.getRequiredSkills(),
                response.getMemberSkills(),
                response.getMatchedSkills(),
                response.getMissingSkills()
        );

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            Map responseBody = restClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent")
                    .header("x-goog-api-key", geminiApiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return extractText(responseBody);

        } catch (Exception e) {
            return "AI 추천 설명을 생성하지 못했습니다. 매칭 점수를 기준으로 추천된 모집글입니다.";
        }
    }

    private String extractText(Map responseBody) {

        if (responseBody == null) {
            return "AI 추천 설명을 생성하지 못했습니다.";
        }

        List candidates = (List) responseBody.get("candidates");

        if (candidates == null || candidates.isEmpty()) {
            return "AI 추천 설명을 생성하지 못했습니다.";
        }

        Map firstCandidate = (Map) candidates.get(0);
        Map content = (Map) firstCandidate.get("content");

        if (content == null) {
            return "AI 추천 설명을 생성하지 못했습니다.";
        }

        List parts = (List) content.get("parts");

        if (parts == null || parts.isEmpty()) {
            return "AI 추천 설명을 생성하지 못했습니다.";
        }

        Map firstPart = (Map) parts.get(0);
        Object text = firstPart.get("text");

        if (text == null) {
            return "AI 추천 설명을 생성하지 못했습니다.";
        }

        return text.toString();
    }
}