package com.catholic.moyeo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                //  CORS 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                //  API 서버 기본 설정
                .csrf(AbstractHttpConfigurer::disable)

                //  세션 안 씀 JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 지금은 전체 허용
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()
                )

                .build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        //  프론트 개발 서버 주소 허용
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"
        ));

        //  허용 HTTP 메서드
        config.setAllowedMethods(List.of(
                "GET","POST","PUT","PATCH","DELETE","OPTIONS"
        ));

        //  모든 헤더 허용
        config.setAllowedHeaders(List.of("*"));

        //  인증정보 포함 허용 JWT/쿠키
        config.setAllowCredentials(true);

        //  Authorization 헤더 노출
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
