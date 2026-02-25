// src/main/java/com/catholic/moyeo/recruit/controller/RecruitController.java
package com.catholic.moyeo.recruit.controller;

import com.catholic.moyeo.recruit.dto.*;
import com.catholic.moyeo.recruit.service.RecruitService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Recruit API (MVP)
 *
 * [핵심 정책(팀 공유)]
 * 1) 검색은 프론트에서 "검색 버튼 클릭 시"만 호출한다. 그래도 백엔드는 Query 필터를 지원한다.
 *
 * 2) type/category/status는 값 고정(Enum 강제) 대상이다.
 *    - 컨트롤러는 String으로 받고, 서비스에서 Enum 파싱하여 잘못된 값은 400 처리한다.
 *    - 이유: Spring 바인딩 단계에서 터지는 400보다, 정책 메시지/제어를 서비스에서 통일하는 게 MVP에서 관리가 쉽다.
 *
 * 3) tag는 "표시만" 한다. (필터/검색 Query에서 제외)
 *    - ERD에 tag 컬럼은 존재하지만, MVP 검색/필터 스펙에서는 제외하기로 결정됨.
 *
 * 4) required_skills는 DB에 CSV 문자열로 저장한다.
 *    - API 입력/응답은 List<String> 형태로 노출하되, 검색 파라미터는 CSV 문자열 하나(skills)를 받는다.
 *    - 예: skills=Python,FastAPI
 *    - (주의) List<String> 파라미터는 skills=Python&skills=FastAPI 형태가 되어 프론트/명세가 혼동되기 쉬워 CSV 1개로 고정한다.
 *
 * 5) apply/cancel은 멱등(idempotent)
 *    - 이미 지원한 상태에서 apply 재호출 → 200 OK (에러 아님)
 *    - 지원 안 한 상태에서 cancel 재호출 → 200 OK (에러 아님)
 *
 * 6) 취소는 row 삭제(soft delete 안 함)
 *    - ACCEPTED도 삭제 가능
 *
 * 7) OPEN + deadline 유효할 때만 행위 가능
 *    - CLOSED 또는 deadline 지난 경우 apply/cancel/decide 모두 불가
 *    - 정책상 "행위 불가"는 400으로 통일
 *
 * [ERD vs API 혼동 시 처리]
 * - 이번 MVP 구현에서는 "API 명세"를 우선 기준으로 삼는다.
 * - ERD/기존 코드와 혼동되는 지점은 서비스/DTO 쪽에서 주석으로 명시하고, 결정 사항을 공유한다.
 */
@RestController
@RequestMapping("/api/recruits")
public class RecruitController {

    private final RecruitService recruitService;

    public RecruitController(RecruitService recruitService) {
        this.recruitService = recruitService;
    }

    /**
     * 모집글 목록 조회 + 필터/검색
     *
     * Query (API 기준):
     * - type: (optional) 1차 필터 (예: CLASS/PROJECT/CONTEST/STUDY/STARTUP)
     * - category: (optional) 2차 필터 (예: PLAN/DEV/DESIGN/MARKETING/ETC)
     * - status: (optional) OPEN/CLOSED
     * - keyword: (optional) 제목/본문 keyword LIKE
     * - skills: (optional) CSV 문자열. split 후 OR LIKE (MVP)
     * - page, size: Pageable
     *
     * NOTE:
     * - tag는 표시만 하므로 Query에서 받지 않는다.
     * - skills 검색은 CSV 구조상 정확/고성능 검색이 한계가 있으므로 MVP에서는 OR LIKE로 동작 우선.
     * - 동적 조건은 Repository에서 @Query JPQL로 만들지 말고, Specification(Criteria)로 구성한다 (JPQL 문법 오류 재발 방지).
     */
    @GetMapping
    public ResponseEntity<PageResponse<RecruitSummaryResponse>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String skills,
            Pageable pageable
    ) {
        Page<RecruitSummaryResponse> page = recruitService.list(type, category, status, keyword, skills, pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    /**
     * 모집글 단건 조회
     *
     * API 기준:
     * - recruit {..., author{memberId,nickname}, appliedByMe, applicantCount ...}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecruitDetailResponse> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(recruitService.get(id));
    }

    /**
     * 모집글 생성 (로그인 필요)
     *
     * - author_user_id는 현재 로그인 사용자로 강제(클라에서 받지 않음)
     * - applicant_count는 정책상 작성자 포함이라 1로 시작
     * - status는 OPEN으로 시작
     */
    @PostMapping
    public ResponseEntity<RecruitDetailResponse> create(@RequestBody @Valid RecruitCreateRequest req) {
        return ResponseEntity.ok(recruitService.create(req));
    }

    /**
     * 모집글 수정 (작성자만)
     *
     * - 수정 가능 필드는 API/DTO 기준으로 제한한다.
     * - totalHeadcount 변경으로 applicantCount가 초과되는 경우 → 400 (정합성 방어)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<RecruitDetailResponse> update(
            @PathVariable("id") Long id,
            @RequestBody @Valid RecruitUpdateRequest req
    ) {
        return ResponseEntity.ok(recruitService.update(id, req));
    }

    /**
     * 모집글 삭제 (작성자만)
     *
     * NOTE:
     * - recruit_application FK/연쇄 삭제 정책은 Flyway/DB 정책에 맞춰야 한다.
     * - MVP에서는 단순 삭제를 기본으로 하되, FK 제약이 존재한다면 ON DELETE CASCADE 또는 선삭제 로직이 필요하다.
     *   → 이 부분은 service/repository 구현에서 "현재 DB 제약"에 맞춰 확정해야 함.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        recruitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 모집글 상태 변경 (작성자만)
     * - OPEN <-> CLOSED
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<RecruitDetailResponse> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody @Valid RecruitStatusUpdateRequest req
    ) {
        return ResponseEntity.ok(recruitService.updateStatus(id, req));
    }

    /**
     * 모집글 지원 (멱등)
     * - OPEN + deadline 유효할 때만 가능
     * - 이미 지원한 경우에도 200 OK로 동일 상태 반환
     */
    @PostMapping("/{id}/apply")
    public ResponseEntity<ApplyStatusResponse> apply(@PathVariable("id") Long id) {
        return ResponseEntity.ok(recruitService.apply(id));
    }

    /**
     * 지원 취소 (멱등, row 삭제)
     * - OPEN + deadline 유효할 때만 가능
     * - ACCEPTED도 삭제 가능하며, 이 경우 applicant_count는 서비스에서 -1 처리
     */
    @DeleteMapping("/{id}/apply")
    public ResponseEntity<ApplyStatusResponse> cancelApply(@PathVariable("id") Long id) {
        return ResponseEntity.ok(recruitService.cancelApply(id));
    }

    /**
     * 지원자 목록 조회 (작성자만)
     *
     * API 기준:
     * - applicants[] { memberId, nickname, part(=departmentName로 매핑), status }
     */
    @GetMapping("/{id}/applications")
    public ResponseEntity<PageResponse<ApplicationResponse>> listApplications(
            @PathVariable("id") Long id,
            Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(recruitService.listApplicationsAsAuthor(id, pageable)));
    }

    /**
     * 지원 승인/거절 (작성자만)
     * - ACCEPTED / REJECTED만 허용
     * - CANCELLED 상태는 따로 만들지 않고 row 삭제로만 처리한다.
     */
    @PatchMapping("/{recruitId}/applications/{applicationId}")
    public ResponseEntity<ApplicationResponse> decide(
            @PathVariable Long recruitId,
            @PathVariable Long applicationId,
            @RequestBody @Valid ApplicationDecisionRequest req
    ) {
        return ResponseEntity.ok(recruitService.decideApplication(recruitId, applicationId, req));
    }

    /**
     * 내가 작성한 모집글 목록
     *
     * NOTE:
     * - 목록 DTO는 일반 목록과 동일한 Summary를 사용한다.
     * - appliedByMe는 작성자 목록에서는 의미가 애매할 수 있으나, API 스펙에 포함돼 있으면 일관되게 내려준다.
     */
    @GetMapping("/me")
    public ResponseEntity<PageResponse<RecruitSummaryResponse>> myPosts(Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(recruitService.myPosts(pageable)));
    }

    /**
     * 내가 지원한 모집글 목록
     */
    @GetMapping("/applied")
    public ResponseEntity<PageResponse<MyAppliedRecruitResponse>> myApplied(Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(recruitService.myApplied(pageable)));
    }
}