package com.catholic.moyeo.security;

import com.catholic.moyeo.member.domain.Member;
import com.catholic.moyeo.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 후 처리 핸들러
 *
 * 역할:
 * 1. Google OAuth 로그인 성공 시 호출
 * 2. 사용자 정보(OIDC) 검증
 * 3. 학교 이메일 도메인 검증
 * 4. 사용자 DB 조회 또는 신규 생성
 * 5. JWT Access Token 발급
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;

    // JWT 토큰 생성기
    private final JwtProvider jwtProvider;

    // OAuth 로그인 성공 후 이동할 프론트엔드 URL 설정
    private final OAuth2Props oauth2Props;

    /**
     * OAuth2 인증 성공 시 자동 호출되는 메서드
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();

        // Google OIDC 로그인 사용자가 아닐 경우
        if (!(principal instanceof OidcUser oidcUser)) {
            response.sendRedirect(oauth2Props.getRedirectUrl() + "?error=not_oidc");
            return;
        }


        String email = oidcUser.getEmail();
        String sub = oidcUser.getSubject();
        Boolean verified = oidcUser.getEmailVerified();  // 구글 기준 이메일 인증 여부 (메타 정보)

        // 필수 정보 누락 시 로그인 실패 처리
        if (email == null || sub == null) {
            response.sendRedirect(oauth2Props.getRedirectUrl() + "?error=invalid_userinfo");
            return;
        }

        // 학교 이메일 도메인 정책 검사 (@catholic.ac.kr)
        if (!EmailDomainPolicy.isAllowed(email)) {
            response.sendRedirect(oauth2Props.getRedirectUrl() + "?error=school_only");
            return;
        }

        /**
         * 사용자 조회 또는 자동 회원가입
         *
         * - provider + providerSub 기준으로 사용자 식별
         * - 이미 존재하면 그대로 사용
         * - 없으면 신규 Member 생성 후 저장
         */
        Member member = memberRepository
                .findByProviderAndProviderSub("google", sub)
                .orElseGet(() -> memberRepository.save(
                        Member.createGoogle(sub, email, verified != null && verified)
                ));

        // 로그인 성공 사용자에 대한 JWT Access Token 발급
        String token = jwtProvider.createAccessToken(member.getId(), member.getEmail());

        /**
         * JWT를 프론트엔드 리다이렉트 URL의 파라미터로 전달
         *
         * - 프론트엔드가 자체적으로 토큰을 수집하여 스토리지에 저장하고
         * - URL에서 즉시 파라미터를 삭제하도록 처리됨
         */
        String redirectUrl = oauth2Props.getRedirectUrl();
        if (redirectUrl.contains("?")) {
            redirectUrl += "&token=" + token;
        } else {
            redirectUrl += "?token=" + token;
        }

        // 프론트엔드로 리다이렉트 (파라미터에 토큰 첨부)
        response.sendRedirect(redirectUrl);
    }
}