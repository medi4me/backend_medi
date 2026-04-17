package com.mediforme.mediforme.global.security;

import com.mediforme.mediforme.global.security.jwt.JwtAccessDeniedHandler;
import com.mediforme.mediforme.global.security.jwt.JwtAuthenticationEntryPoint;
import com.mediforme.mediforme.global.security.jwt.JwtAuthenticationFilter;
import com.mediforme.mediforme.global.security.jwt.JwtTokenProvider;
import com.mediforme.mediforme.global.security.TokenBlacklistService;
import org.springframework.http.HttpMethod;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final TokenBlacklistService tokenBlacklistService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // CORS 설정
                .cors(cors -> cors.configure(http))
                // CSRF 비활성화 (JWT 기반이므로 세션 불필요)
                .csrf(csrf -> csrf.disable())
                // HTTP Basic / Form 로그인 비활성화
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                // 세션을 STATELESS로 설정 (JWT 인증)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 인증/인가 실패시 핸들러
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                // URL 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 URL 목록
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/favicon.ico",
                                "/error",
                                "/auth/**",
                                "/actuator/**"
                        ).permitAll()
                    
                        // 온보딩/조회성 API는 GET만 공개
                        .requestMatchers(HttpMethod.GET,
                            "/medicines",
                            "/medicines/info"
                        ).permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated())
                // JWT 인증 필터 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt Encoder 사용
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
