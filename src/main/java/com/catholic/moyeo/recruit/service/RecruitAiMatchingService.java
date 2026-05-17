package com.catholic.moyeo.recruit.service;

import com.catholic.moyeo.recruit.dto.RecruitAiMatchingResponse;
import com.catholic.moyeo.recruit.dto.RecruitMatchingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;


//모집글 1개 + 내 정보
//→ 그 모집글과 나의 매칭 점수 계산


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitAiMatchingService {

    private final RecruitMatchingService recruitMatchingService;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    private final RestClient restClient = RestClient.create();

    public RecruitAiMatchingResponse getAiMatchingResult(Long recruitPostId, Long memberId) {

        // 1. 기존 백엔드 매칭 로직으로 점수 계산
        RecruitMatchingResponse matchingResult =
                recruitMatchingService.getMatchingResult(recruitPostId, memberId);

        // 2. Gemini에게 설명 문장만 요청
        String aiComment = createAiComment(matchingResult);

        // 3. 기존 매칭 결과 + AI 코멘트 합쳐서 반환
        return new RecruitAiMatchingResponse(
                matchingResult.getRecruitPostId(),
                matchingResult.getMemberId(),

                matchingResult.getMemberRole(),
                matchingResult.getRecruitCategory(),
                matchingResult.isRoleMatched(),
                matchingResult.getRoleScore(),

                matchingResult.getMemberActivityCategories(),
                matchingResult.getRecruitActivityCategory(),
                matchingResult.isActivityCategoryMatched(),
                matchingResult.getActivityCategoryScore(),

                matchingResult.getRequiredSkills(),
                matchingResult.getMemberSkills(),
                matchingResult.getMatchedSkills(),
                matchingResult.getMissingSkills(),
                matchingResult.getSkillScore(),

                matchingResult.getMatchingScore(),

                aiComment
        );
        }

    private String createAiComment(RecruitMatchingResponse matchingResult) {

        String prompt = """
        너는 대학생 팀 프로젝트 매칭을 도와주는 AI야.

        아래 매칭 결과를 바탕으로 사용자가 이 모집글에 얼마나 적합한지 짧게 설명해줘.

        조건:
        - 한국어로 작성
        - 3~5문장 정도로 작성
        - 너무 과장하지 말 것
        - 부족한 부분이 있으면 보완하면 좋다고 부드럽게 말할 것
        - 점수는 백엔드가 이미 계산한 값이므로 임의로 바꾸지 말 것

        [역할/포지션 매칭]
        사용자 역할: %s
        모집 포지션: %s
        역할 일치 여부: %s
        역할 점수: %d점

        [관심 분야 매칭]
        사용자 관심 분야: %s
        모집 활동 분야: %s
        관심 분야 일치 여부: %s
        관심 분야 점수: %d점

        [기술스택 매칭]
        모집글 요구 기술: %s
        사용자 보유 기술: %s
        일치한 기술: %s
        부족한 기술: %s
        기술 점수: %d점

        최종 매칭 점수: %d점
        """.formatted(
                matchingResult.getMemberRole(),
                matchingResult.getRecruitCategory(),
                matchingResult.isRoleMatched(),
                matchingResult.getRoleScore(),

                matchingResult.getMemberActivityCategories(),
                matchingResult.getRecruitActivityCategory(),
                matchingResult.isActivityCategoryMatched(),
                matchingResult.getActivityCategoryScore(),

                matchingResult.getRequiredSkills(),
                matchingResult.getMemberSkills(),
                matchingResult.getMatchedSkills(),
                matchingResult.getMissingSkills(),
                matchingResult.getSkillScore(),

                matchingResult.getMatchingScore()
        );
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        try {
            Map response = restClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return extractText(response);

        } catch (Exception e) {
            return "AI 코멘트를 생성하는 중 문제가 발생했습니다. 기본 매칭 점수와 기술스택 정보를 참고해주세요.";
        }
    }

    private String extractText(Map response) {
        try {
            List candidates = (List) response.get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List parts = (List) content.get("parts");
            Map firstPart = (Map) parts.get(0);

            return firstPart.get("text").toString();

        } catch (Exception e) {
            return "AI 코멘트를 불러오지 못했습니다.";
        }
    }
}