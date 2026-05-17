package com.catholic.moyeo.recruit.service;

import com.catholic.moyeo.member.domain.Member;
import com.catholic.moyeo.member.domain.MemberActivityCategory;
import com.catholic.moyeo.member.domain.MemberTechStack;
import com.catholic.moyeo.member.repository.MemberActivityCategoryRepository;
import com.catholic.moyeo.member.repository.MemberRepository;
import com.catholic.moyeo.member.repository.MemberTechStackRepository;
import com.catholic.moyeo.recruit.domain.RecruitPost;
import com.catholic.moyeo.recruit.dto.RecruitMatchingResponse;
import com.catholic.moyeo.recruit.repository.RecruitPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitMatchingService {

    private final RecruitPostRepository recruitPostRepository;
    private final MemberRepository memberRepository;
    private final MemberTechStackRepository memberTechStackRepository;
    private final MemberActivityCategoryRepository memberActivityCategoryRepository;

    public RecruitMatchingResponse getMatchingResult(Long recruitPostId, Long memberId) {

        // 1. 모집글 조회
        RecruitPost recruitPost = recruitPostRepository.findById(recruitPostId)
                .orElseThrow(() -> new IllegalArgumentException("모집글을 찾을 수 없습니다."));

        // 2. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 3. 모집글의 필요한 기술/툴 파싱
        List<String> requiredSkills = parseRequiredSkills(recruitPost.getRequiredSkills());

        // 4. 회원 기술스택 조회
        List<String> memberSkills = memberTechStackRepository.findByMember(member)
                .stream()
                .map(MemberTechStack::getTechStack)
                .map(techStack -> techStack.getName())
                .map(this::normalize)
                .filter(skill -> !skill.isBlank())
                .toList();

        // 5. 일치하는 기술 계산
        List<String> matchedSkills = requiredSkills.stream()
                .filter(memberSkills::contains)
                .toList();

        // 6. 부족한 기술 계산
        List<String> missingSkills = requiredSkills.stream()
                .filter(skill -> !memberSkills.contains(skill))
                .toList();

        // 7. 회원 관심 활동 카테고리 조회
        // 예: project, contest, study, academic
        List<String> memberActivityCategories = memberActivityCategoryRepository.findByMember(member)
                .stream()
                .map(MemberActivityCategory::getActivityCategory)
                .map(activityCategory -> activityCategory.name())
                .map(this::normalize)
                .toList();

        // 8. 회원 역할 / 모집 세부 카테고리
        // 예:
        // memberRole = BACKEND
        // recruitCategory = DEVELOP
        String memberRole = member.getRole();
        String recruitCategory = recruitPost.getRecruitCategory();

        // 9. 모집 활동 카테고리
        // 예: PROJECT, CONTEST, STUDY, ACADEMIC
        String recruitActivityCategory = recruitPost.getActivityCategory();

        boolean activityCategoryMatched = memberActivityCategories.contains(
                normalize(recruitActivityCategory)
        );

        // 10. 필요한 기술/툴이 있는 모집글인지 확인
        boolean hasRequiredSkills = !requiredSkills.isEmpty();

        int roleScore;
        int activityCategoryScore;
        int skillScore;

        if (hasRequiredSkills) {
            /*
             * 필요한 기술/툴이 있는 모집글
             *
             * 총점 100점
             * - 역할/세부 카테고리: 40점
             * - 활동 카테고리: 30점
             * - 기술/툴: 30점
             */
            roleScore = calculateRoleScore(memberRole, recruitCategory, 40);
            activityCategoryScore = activityCategoryMatched ? 30 : 0;
            skillScore = calculateSkillScore(matchedSkills.size(), requiredSkills.size(), 30);

        } else {
            /*
             * 필요한 기술/툴이 없는 모집글
             *
             * 총점 100점
             * - 역할/세부 카테고리: 60점
             * - 활동 카테고리: 40점
             * - 기술/툴: 0점
             *
             * 기획/마케팅/수업/스터디 모집글은 기술스택이 비어 있을 수 있으므로
             * 기술스택이 없다고 추천에서 제외하지 않는다.
             */
            roleScore = calculateRoleScore(memberRole, recruitCategory, 60);
            activityCategoryScore = activityCategoryMatched ? 40 : 0;
            skillScore = 0;
        }

        // roleScore가 0보다 크면 역할이 어느 정도 매칭된 것으로 본다.
        // ETC는 절반 점수를 주기 때문에 roleMatched도 true가 된다.
        boolean roleMatched = roleScore > 0;

        // 11. 최종 점수 계산
        int matchingScore = roleScore + activityCategoryScore + skillScore;

        // 12. 응답 반환
        return new RecruitMatchingResponse(
                recruitPostId,
                memberId,

                memberRole,
                recruitCategory,
                roleMatched,
                roleScore,

                memberActivityCategories,
                recruitActivityCategory,
                activityCategoryMatched,
                activityCategoryScore,

                requiredSkills,
                memberSkills,
                matchedSkills,
                missingSkills,
                skillScore,

                matchingScore
        );
    }

    private List<String> parseRequiredSkills(String requiredSkills) {

        if (requiredSkills == null || requiredSkills.isBlank()) {
            return List.of();
        }

        return Arrays.stream(requiredSkills.split(","))
                .map(this::normalize)
                .filter(skill -> !skill.isBlank())
                .toList();
    }

    private int calculateSkillScore(int matchedCount, int requiredCount, int maxScore) {

        if (requiredCount == 0) {
            return 0;
        }

        return (int) Math.round((matchedCount * 1.0 / requiredCount) * maxScore);
    }

    private int calculateRoleScore(String memberRole, String recruitCategory, int maxScore) {

        if (memberRole == null || recruitCategory == null) {
            return 0;
        }

        String role = normalize(memberRole);
        String category = normalize(recruitCategory);

        // 1. 완전히 같은 경우
        // 예: BACKEND == BACKEND, DESIGN == DESIGN
        if (role.equals(category)) {
            return maxScore;
        }

        // 2. 모집 세부 카테고리가 DEVELOP이면 세부 개발 직무도 매칭으로 인정
        // 예: BACKEND, FRONTEND, FULLSTACK 등은 DEVELOP에 포함된다고 봄
        if (category.equals("develop")) {
            if (role.equals("backend")
                    || role.equals("frontend")
                    || role.equals("fullstack")
                    || role.equals("android")
                    || role.equals("ios")
                    || role.equals("developer")
                    || role.equals("develop")) {
                return maxScore;
            }
        }

        // 3. 모집 세부 카테고리가 DESIGN이면 디자인 관련 직무 매칭
        if (category.equals("design")) {
            if (role.equals("designer")
                    || role.equals("uiux")
                    || role.equals("ui")
                    || role.equals("ux")
                    || role.equals("design")) {
                return maxScore;
            }
        }

        // 4. 모집 세부 카테고리가 PLAN이면 기획 관련 직무 매칭
        if (category.equals("plan")) {
            if (role.equals("planner")
                    || role.equals("pm")
                    || role.equals("service_planner")
                    || role.equals("plan")) {
                return maxScore;
            }
        }

        // 5. 모집 세부 카테고리가 MARKETING이면 마케팅 관련 직무 매칭
        if (category.equals("marketing")) {
            if (role.equals("marketer")
                    || role.equals("marketing")) {
                return maxScore;
            }
        }

        // 6. 기타 카테고리는 완전 불일치로 보지 않고 절반 점수
        // 예: 모집글이 ETC면 다양한 역할을 받을 가능성이 있으므로 0점 처리하지 않음
        if (category.equals("etc") || category.equals("기타")) {
            return maxScore / 2;
        }

        return 0;
    }

    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase();
    }
}