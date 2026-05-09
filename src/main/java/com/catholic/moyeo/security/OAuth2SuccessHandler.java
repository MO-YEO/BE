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

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final OAuth2Props oauth2Props;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();

        // OIDC 사용자 검증
        if (!(principal instanceof OidcUser oidcUser)) {
            response.sendRedirect(oauth2Props.getRedirectUrl() + "?error=not_oidc");
            return;
        }

        String email = oidcUser.getEmail();
        String sub = oidcUser.getSubject();
        Boolean verified = oidcUser.getEmailVerified();

        // 필수값 체크
        if (email == null || sub == null) {
            response.sendRedirect(oauth2Props.getRedirectUrl() + "?error=invalid_userinfo");
            return;
        }

        // 학교 이메일 검증
        if (!EmailDomainPolicy.isAllowed(email)) {
            response.sendRedirect(oauth2Props.getRedirectUrl() + "?error=school_only");
            return;
        }

        // 회원 조회 or 생성
        Member member = memberRepository
                .findByProviderAndProviderSub("google", sub)
                .orElseGet(() -> memberRepository.save(
                        Member.createGoogle(sub, email, verified != null && verified)
                ));

        // JWT 생성
        String token = jwtProvider.createAccessToken(member.getId(), member.getEmail());
        System.out.println("TOKEN = " + token);

// 토큰을 URL에 붙여서 프론트로 리다이렉트
        String redirectUrl = oauth2Props.getRedirectUrl() + "?token=" + token;
        System.out.println("REDIRECT = " + redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}