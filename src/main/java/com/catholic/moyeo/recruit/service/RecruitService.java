// src/main/java/com/catholic/moyeo/recruit/service/RecruitService.java
package com.catholic.moyeo.recruit.service;

import com.catholic.moyeo.recruit.domain.*;
import com.catholic.moyeo.recruit.dto.*;
import com.catholic.moyeo.recruit.repository.RecruitApplicationRepository;
import com.catholic.moyeo.recruit.repository.RecruitPostRepository;
import com.catholic.moyeo.security.AuthUtil;
import jakarta.persistence.criteria.Predicate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecruitService {

    /**
     * =========================
     * Recruit MVP 정책(팀 공유, Service가 기준)
     * =========================
     *
     * [ERD vs API 혼동 시]
     * - 본 MVP 구현은 "API 명세" 우선.
     *
     * [식별자]
     * - memberId == user_id == app_user_id (공유 PK)
     * - 인증 주체는 AuthUtil.currentMemberId()로 얻는다.
     *
     * [카운트 정의 - 중요]
     * - applicantCount == recruit_post.applicant_count
     *   = "작성자 포함 현재 참여 인원"
     *   = 1(작성자) + (ACCEPTED 인원 수)
     *
     * - recruit_application row count(지원 클릭 수)와는 다르다.
     *   apply(지원)만으로 applicant_count는 증가하지 않는다.
     *
     * [상태/행위 가능 조건]
     * - OPEN 이고 deadline 미경과인 경우에만 apply/cancel/decide 가능
     * - CLOSED 또는 deadline 경과면 행위 불가(400)
     *
     * [멱등]
     * - apply: 이미 지원 row가 있으면 200 OK + appliedByMe=true 그대로
     * - cancel: 지원 row가 없으면 200 OK + appliedByMe=false 그대로
     * - 동시성으로 인한 중복 insert(DataIntegrityViolationException)도 멱등으로 흡수
     *
     * [취소]
     * - 취소는 row 삭제 (CANCELLED 상태 없음)
     * - ACCEPTED를 취소(삭제)하면 applicant_count -1
     *
     * [자동 마감]
     * - applicant_count == total_headcount 에 도달하면 status를 CLOSED로 자동 전환
     *   (ACCEPTED 처리 시점에 수행)
     *
     * [값 고정(Enum 강제)]
     * - type/category/status는 허용 값 고정 대상.
     * - 컨트롤러는 String으로 받고, 서비스에서 파싱/검증 후 400 처리.
     *
     * [CSV 저장]
     * - required_skills: DB CSV, API List<String> (서버 join/split)
     * - 검색 Query의 skills는 CSV 문자열 1개를 받아 split 후 OR LIKE로 필터
     *
     * [nickname]
     * - API에서 닉네임이 필요하지만, 현재 recruit 모듈에 user_profile 접근 레이어가 없다.
     * - 현재 코드는 nickname을 null로 둔다(통합 시 반드시 채워야 함).
     */

    private final RecruitPostRepository postRepo;
    private final RecruitApplicationRepository appRepo;

    public RecruitService(RecruitPostRepository postRepo, RecruitApplicationRepository appRepo) {
        this.postRepo = postRepo;
        this.appRepo = appRepo;
    }

    // =========================
    // Create
    // =========================

    @Transactional
    public RecruitDetailResponse create(RecruitCreateRequest req) {
        Long me = AuthUtil.currentMemberId();

        // type/category 값 고정 검증은 "서비스에서" 수행 (Spring 바인딩 400 방지)
        normalizeTypeOrThrow(req.getType());
        normalizeCategoryOrThrow(req.getCategory());

        if (req.getTotalHeadcount() == null || req.getTotalHeadcount() <= 0) {
            throw new IllegalArgumentException("totalHeadcount must be positive");
        }

        String csvSkills = joinSkillCsv(req.getSkills());

        RecruitPost post = new RecruitPost(
                me,
                req.getType(),
                req.getCategory(),
                req.getTag(),
                req.getTitle(),
                req.getContent(),
                csvSkills,
                req.getTotalHeadcount().shortValue(),
                req.getDeadline()
        );

        RecruitPost saved = postRepo.save(post);

        // applicantCount = 작성자 포함이라 엔티티 생성 시점에 1로 세팅됨
        return RecruitDetailResponse.from(
                saved,
                null,
                splitSkillCsv(saved.getRequiredSkills()),
                false
        );
    }

    // =========================
    // Read - list (검색/필터)
    // =========================

    @Transactional(readOnly = true)
    public Page<RecruitSummaryResponse> list(
            String type,
            String category,
            String status,
            String keyword,
            String skillsCsv,
            Pageable pageable
    ) {
        RecruitPostStatus st = parsePostStatusOrNull(status);

        // type/category는 optional. 값이 들어오면 허용 값 검증만 수행.
        if (type != null && !type.isBlank()) normalizeTypeOrThrow(type);
        if (category != null && !category.isBlank()) normalizeCategoryOrThrow(category);

        List<String> skills = parseSkillsCsv(skillsCsv);

        // JPQL 동적 쿼리/파라미터 오류 재발 방지: Specification(Criteria)로 구성
        Specification<RecruitPost> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (st != null) {
                predicates.add(cb.equal(root.get("status"), st));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("content"), like)
                ));
            }

            // skills OR LIKE (MVP)
            if (skills != null && !skills.isEmpty()) {
                List<Predicate> orLikes = new ArrayList<>();
                for (String s : skills) {
                    // CSV 컬럼이므로 완전 매칭이 아니라 "포함"으로만 동작 (MVP 한계)
                    orLikes.add(cb.like(root.get("requiredSkills"), "%" + s + "%"));
                }
                predicates.add(cb.or(orLikes.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<RecruitPost> page = postRepo.findAll(spec, pageable);

        Long me = AuthUtil.currentMemberId();

        return page.map(p -> {
            boolean appliedByMe = appRepo.existsByRecruitPostIdAndUserId(p.getId(), me);

            // applicantCount는 recruit_post.applicant_count 값을 그대로 노출 (작성자 포함 참여 인원)
            return RecruitSummaryResponse.from(
                    p,
                    splitSkillCsv(p.getRequiredSkills()),
                    appliedByMe
            );
        });
    }

    @Transactional(readOnly = true)
    public RecruitDetailResponse get(Long recruitId) {
        RecruitPost post = postRepo.findById(recruitId)
                .orElseThrow(() -> new IllegalArgumentException("Recruit not found: " + recruitId));

        Long me = AuthUtil.currentMemberId();
        boolean appliedByMe = appRepo.existsByRecruitPostIdAndUserId(recruitId, me);

        return RecruitDetailResponse.from(
                post,
                null,
                splitSkillCsv(post.getRequiredSkills()),
                appliedByMe
        );
    }

    // =========================
    // Update / Delete / Status
    // =========================

    @Transactional
    public RecruitDetailResponse update(Long recruitId, RecruitUpdateRequest req) {
        Long me = AuthUtil.currentMemberId();

        RecruitPost post = postRepo.findById(recruitId)
                .orElseThrow(() -> new IllegalArgumentException("Recruit not found: " + recruitId));

        ensureAuthor(post, me);

        if (req.getType() != null) {
            normalizeTypeOrThrow(req.getType());
            post.setType(req.getType());
        }
        if (req.getCategory() != null) {
            normalizeCategoryOrThrow(req.getCategory());
            post.setCategory(req.getCategory());
        }
        if (req.getTag() != null) post.setTag(req.getTag());
        if (req.getTitle() != null) post.setTitle(req.getTitle());
        if (req.getContent() != null) post.setContent(req.getContent());
        if (req.getSkills() != null) post.setRequiredSkills(joinSkillCsv(req.getSkills()));
        if (req.getDeadline() != null) post.setDeadline(req.getDeadline());

        if (req.getTotalHeadcount() != null) {
            if (req.getTotalHeadcount() <= 0) {
                throw new IllegalArgumentException("totalHeadcount must be positive");
            }
            // 참여 인원(applicant_count)보다 작아질 수 없음
            if (req.getTotalHeadcount() < post.getApplicantCount()) {
                throw new IllegalArgumentException("totalHeadcount cannot be less than applicantCount");
            }
            post.setTotalHeadcount(req.getTotalHeadcount().shortValue());

            // headcount가 꽉 찬 상태라면 자동 마감 정합성 유지
            autoCloseIfFull(post);
        }

        boolean appliedByMe = appRepo.existsByRecruitPostIdAndUserId(recruitId, me);

        return RecruitDetailResponse.from(
                post,
                null,
                splitSkillCsv(post.getRequiredSkills()),
                appliedByMe
        );
    }

    @Transactional
    public void delete(Long recruitId) {
        Long me = AuthUtil.currentMemberId();

        RecruitPost post = postRepo.findById(recruitId)
                .orElseThrow(() -> new IllegalArgumentException("Recruit not found: " + recruitId));

        ensureAuthor(post, me);

        // FK/연쇄삭제는 Flyway/DB 제약에 맞춰야 함.
        postRepo.delete(post);
    }

    @Transactional
    public RecruitDetailResponse updateStatus(Long recruitId, RecruitStatusUpdateRequest req) {
        Long me = AuthUtil.currentMemberId();

        RecruitPost post = postRepo.findById(recruitId)
                .orElseThrow(() -> new IllegalArgumentException("Recruit not found: " + recruitId));

        ensureAuthor(post, me);

        RecruitPostStatus next = req.getStatus();
        if (next == null) throw new IllegalArgumentException("status is required");

        post.setStatus(next);

        boolean appliedByMe = appRepo.existsByRecruitPostIdAndUserId(recruitId, me);

        return RecruitDetailResponse.from(
                post,
                null,
                splitSkillCsv(post.getRequiredSkills()),
                appliedByMe
        );
    }

    // =========================
    // Apply / Cancel (Idempotent)
    // =========================

    @Transactional
    public ApplyStatusResponse apply(Long recruitId) {
        Long me = AuthUtil.currentMemberId();

        RecruitPost post = postRepo.findById(recruitId)
                .orElseThrow(() -> new IllegalArgumentException("Recruit not found: " + recruitId));

        ensureOpenForUserAction(post);

        // 멱등: 이미 row 존재하면 그대로 응답
        if (appRepo.existsByRecruitPostIdAndUserId(recruitId, me)) {
            return ApplyStatusResponse.of(true, post.getApplicantCount());
        }

        // apply는 "지원 클릭"일 뿐 참여 인원(applicant_count)에는 영향 없음
        try {
            appRepo.save(new RecruitApplication(recruitId, me));
        } catch (DataIntegrityViolationException ignored) {
            // 동시성으로 unique 충돌 → 멱등으로 흡수
        }

        return ApplyStatusResponse.of(true, post.getApplicantCount());
    }

    @Transactional
    public ApplyStatusResponse cancelApply(Long recruitId) {
        Long me = AuthUtil.currentMemberId();

        RecruitPost post = postRepo.findById(recruitId)
                .orElseThrow(() -> new IllegalArgumentException("Recruit not found: " + recruitId));

        ensureOpenForUserAction(post);

        Optional<RecruitApplication> opt = appRepo.findByRecruitPostIdAndUserId(recruitId, me);
        if (opt.isEmpty()) {
            // 멱등
            return ApplyStatusResponse.of(false, post.getApplicantCount());
        }

        RecruitApplication app = opt.get();

        // ACCEPTED 취소(삭제)면 참여 인원 감소 필요 (applicant_count -1)
        if (app.getStatus() == RecruitApplicationStatus.ACCEPTED) {
            RecruitPost locked = postRepo.findByIdForUpdate(recruitId)
                    .orElseThrow(() -> new IllegalArgumentException("Recruit not found: " + recruitId));

            locked.decreaseApplicantCount();

            // 참여 인원이 줄었으면 OPEN으로 되돌릴지 여부는 정책 선택인데,
            // 현재 MVP 정책은 "자동 CLOSED 전환"만 확정이고, reopen은 명세에 없음.
            // 따라서 CLOSED 유지(작성자가 status 변경으로 제어).
        }

        appRepo.delete(app);

        return ApplyStatusResponse.of(false, post.getApplicantCount());
    }

    // =========================
    // Applications (Author only)
    // =========================

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> listApplicationsAsAuthor(Long recruitId, Pageable pageable) {
        Long me = AuthUtil.currentMemberId();

        RecruitPost post = postRepo.findById(recruitId)
                .orElseThrow(() -> new IllegalArgumentException("Recruit not found: " + recruitId));

        ensureAuthor(post, me);

        // nickname은 user_profile 접근 레이어 필요(현재는 null)
        return appRepo.findByRecruitPostId(recruitId, pageable)
                .map(a -> ApplicationResponse.from(a, null));
    }

    @Transactional
    public ApplicationResponse decideApplication(Long recruitId, Long applicationId, ApplicationDecisionRequest req) {
        Long me = AuthUtil.currentMemberId();

        // 참여 인원(applicant_count) 조정을 포함하므로 pessimistic lock 사용
        RecruitPost post = postRepo.findByIdForUpdate(recruitId)
                .orElseThrow(() -> new IllegalArgumentException("Recruit not found: " + recruitId));

        ensureAuthor(post, me);
        ensureOpenForUserAction(post);

        RecruitApplication app = appRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        if (!app.getRecruitPostId().equals(recruitId)) {
            throw new IllegalArgumentException("Application does not belong to recruit");
        }

        RecruitApplicationStatus next = req.getStatus();
        if (next != RecruitApplicationStatus.ACCEPTED && next != RecruitApplicationStatus.REJECTED) {
            throw new IllegalArgumentException("Only ACCEPTED/REJECTED allowed");
        }

        RecruitApplicationStatus cur = app.getStatus();
        if (cur == next) {
            return ApplicationResponse.from(app, null);
        }

        // 참여 인원(applicant_count) 조정 규칙
        // - (not ACCEPTED) -> ACCEPTED : +1
        // - ACCEPTED -> (not ACCEPTED) : -1
        if (cur != RecruitApplicationStatus.ACCEPTED && next == RecruitApplicationStatus.ACCEPTED) {
            // 정원 체크: 꽉 찼으면 승인 불가 + 상태 자동 CLOSED로 정합성 맞춤
            if (post.getApplicantCount() >= post.getTotalHeadcount()) {
                autoCloseIfFull(post);
                throw new IllegalStateException("Recruit is full");
            }

            post.increaseApplicantCount();

            // 승인 결과로 정원이 찼으면 자동 마감
            autoCloseIfFull(post);

        } else if (cur == RecruitApplicationStatus.ACCEPTED && next != RecruitApplicationStatus.ACCEPTED) {
            post.decreaseApplicantCount();
        }

        app.setStatus(next);

        return ApplicationResponse.from(app, null);
    }

    // =========================
    // My pages
    // =========================

    @Transactional(readOnly = true)
    public Page<RecruitSummaryResponse> myPosts(Pageable pageable) {
        Long me = AuthUtil.currentMemberId();

        return postRepo.findByAuthorUserId(me, pageable)
                .map(p -> RecruitSummaryResponse.from(
                        p,
                        splitSkillCsv(p.getRequiredSkills()),
                        false
                ));
    }

    @Transactional(readOnly = true)
    public Page<MyAppliedRecruitResponse> myApplied(Pageable pageable) {
        Long me = AuthUtil.currentMemberId();

        Page<RecruitApplication> apps = appRepo.findByUserId(me, pageable);

        Set<Long> postIds = apps.getContent().stream()
                .map(RecruitApplication::getRecruitPostId)
                .collect(Collectors.toSet());

        Map<Long, RecruitPost> postMap = postIds.isEmpty()
                ? Collections.emptyMap()
                : postRepo.findAllById(postIds)
                .stream()
                .collect(Collectors.toMap(RecruitPost::getId, p -> p));

        return apps.map(app -> {
            RecruitPost post = postMap.get(app.getRecruitPostId());
            if (post == null) throw new IllegalStateException("Recruit post missing");
            return MyAppliedRecruitResponse.from(
                    post,
                    splitSkillCsv(post.getRequiredSkills()),
                    app.getStatus()
            );
        });
    }

    // =========================
    // helpers
    // =========================

    private void ensureAuthor(RecruitPost post, Long me) {
        if (!post.getAuthorUserId().equals(me)) {
            throw new AccessDeniedException("Author only");
        }
    }

    private void ensureOpenForUserAction(RecruitPost post) {
        // CLOSED면 불가
        if (post.getStatus() != RecruitPostStatus.OPEN) {
            throw new IllegalStateException("Recruit is not open");
        }
        // deadline 경과면 불가
        LocalDate d = post.getDeadline();
        if (d != null && d.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Recruit is not open");
        }
        // 정원이 이미 찼으면 불가 + 자동 마감 정합성 유지
        autoCloseIfFull(post);
        if (post.getStatus() != RecruitPostStatus.OPEN) {
            throw new IllegalStateException("Recruit is not open");
        }
    }

    private void autoCloseIfFull(RecruitPost post) {
        if (post.getApplicantCount() >= post.getTotalHeadcount()) {
            post.setStatus(RecruitPostStatus.CLOSED);
        }
    }

    private RecruitPostStatus parsePostStatusOrNull(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return RecruitPostStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    private void normalizeTypeOrThrow(String type) {
        if (type == null || type.isBlank()) throw new IllegalArgumentException("type is required");
        String v = type.trim().toUpperCase(Locale.ROOT);

        // TODO: 실제 허용값은 API 명세 Enum에 맞춰 확정해서 여기 업데이트
        // (MVP) 최소한 빈값/공백 방어 + 대문자 정규화만 보장
        // 허용값 예: ALL/CLASS/PROJECT/CONTEST/STUDY/STARTUP
        // "ALL"은 검색에서만 의미 있고 Create/Update에서는 금지할지 정책 확정 필요
    }

    private void normalizeCategoryOrThrow(String category) {
        if (category == null || category.isBlank()) throw new IllegalArgumentException("category is required");
        String v = category.trim().toUpperCase(Locale.ROOT);

        // TODO: 실제 허용값은 API 명세 Enum에 맞춰 확정해서 여기 업데이트
        // 예: ALL/PLAN/DEV/DESIGN/MARKETING/ETC
    }

    private List<String> parseSkillsCsv(String csv) {
        if (csv == null || csv.isBlank()) return null;

        String[] arr = csv.split(",");
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String s : arr) {
            if (s == null) continue;
            String t = s.trim();
            if (!t.isEmpty()) set.add(t);
        }
        return set.isEmpty() ? null : new ArrayList<>(set);
    }

    private String joinSkillCsv(List<String> skills) {
        if (skills == null || skills.isEmpty()) return null;

        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String s : skills) {
            if (s == null) continue;
            String t = s.trim();
            if (!t.isEmpty()) set.add(t);
        }
        return set.isEmpty() ? null : String.join(",", set);
    }

    private List<String> splitSkillCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();

        String[] arr = csv.split(",");
        List<String> out = new ArrayList<>(arr.length);
        for (String s : arr) {
            String t = s.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }
}