package com.mediforme.mediforme.global.security.jwt;

import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.exception.ErrorCode;
import com.mediforme.mediforme.global.security.CustomUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-for-jwt-provider-at-least-32-bytes-long-please";
    private static final long ACCESS_EXP_MS = 60_000L;
    private static final long REFRESH_EXP_MS = 300_000L;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(userDetailsService);
        ReflectionTestUtils.setField(provider, "secretKey", SECRET);
        ReflectionTestUtils.setField(provider, "accessExpirationTime", ACCESS_EXP_MS);
        ReflectionTestUtils.setField(provider, "refreshExpirationTime", REFRESH_EXP_MS);
    }

    @Test
    @DisplayName("Access Token 발급 후 subject/uid claim 정상 추출")
    void createAccessToken_claimsAccessible() {
        String token = provider.createAccessToken(42L, "user01", 1001L);

        assertThat(provider.getUserLoginIdFromToken(token)).isEqualTo("user01");
        assertThat(provider.getUserIdClaim(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("Refresh Token 도 동일한 서명/subject 로 생성")
    void createRefreshToken_parseable() {
        String token = provider.createRefreshToken(42L, "user01", 1001L);

        assertThat(provider.getUserLoginIdFromToken(token)).isEqualTo("user01");
        assertThat(provider.getUserIdClaim(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("TTL 은 생성 직후 accessExpirationTime 근사값")
    void getRemainingTtlMs_nearExpirationTime() {
        String token = provider.createAccessToken(1L, "u", 1001L);

        long ttl = provider.getRemainingTtlMs(token);

        assertThat(ttl).isBetween(ACCESS_EXP_MS - 1_000L, ACCESS_EXP_MS);
    }

    @Test
    @DisplayName("validateTokenOrThrow: 정상 토큰은 true")
    void validateToken_valid_returnsTrue() {
        String token = provider.createAccessToken(1L, "u", 1001L);

        assertThat(provider.validateTokenOrThrow(token)).isTrue();
    }

    @Test
    @DisplayName("validateTokenOrThrow: 다른 키로 서명한 위조 토큰은 INVALID_JWT_TOKEN")
    void validateToken_tampered_throwsInvalid() {
        Key otherKey = new SecretKeySpec(
            "another-secret-that-is-also-at-least-32-bytes-long".getBytes(StandardCharsets.UTF_8),
            "HmacSHA256");
        String forged = Jwts.builder()
            .setSubject("u")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 60_000))
            .signWith(otherKey, SignatureAlgorithm.HS256)
            .compact();

        assertThatThrownBy(() -> provider.validateTokenOrThrow(forged))
            .isInstanceOf(CustomApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.INVALID_JWT_TOKEN);
    }

    @Test
    @DisplayName("validateTokenOrThrow: 만료된 토큰은 EXPIRED_JWT_TOKEN")
    void validateToken_expired_throwsExpired() {
        Key key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        String expired = Jwts.builder()
            .setSubject("u")
            .setIssuedAt(new Date(System.currentTimeMillis() - 120_000))
            .setExpiration(new Date(System.currentTimeMillis() - 60_000))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        assertThatThrownBy(() -> provider.validateTokenOrThrow(expired))
            .isInstanceOf(CustomApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.EXPIRED_JWT_TOKEN);
    }

    @Test
    @DisplayName("validateTokenOrThrow: 형식이 잘못된 토큰은 INVALID_JWT_TOKEN")
    void validateToken_malformed_throwsInvalid() {
        assertThatThrownBy(() -> provider.validateTokenOrThrow("not.a.jwt"))
            .isInstanceOf(CustomApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.INVALID_JWT_TOKEN);
    }

    @Test
    @DisplayName("resolveToken: Bearer prefix 가 있으면 토큰 부분만 추출")
    void resolveToken_bearerPrefix_returnsToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("Authorization")).willReturn("Bearer abc.def.ghi");

        assertThat(provider.resolveToken(request)).isEqualTo("abc.def.ghi");
    }

    @Test
    @DisplayName("resolveToken: Authorization 헤더 없으면 null")
    void resolveToken_noHeader_returnsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("Authorization")).willReturn(null);

        assertThat(provider.resolveToken(request)).isNull();
    }

    @Test
    @DisplayName("resolveToken: 잘못된 prefix 면 null")
    void resolveToken_wrongPrefix_returnsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("Authorization")).willReturn("Basic abc");

        assertThat(provider.resolveToken(request)).isNull();
    }

    @Test
    @DisplayName("getAuthentication: 정상(ACTIVE) 사용자 → UsernamePasswordAuthenticationToken")
    void getAuthentication_activeUser_returnsAuthenticationToken() {
        CustomUserDetails active = CustomUserDetails.builder()
            .userId(1L)
            .userLoginId("user01")
            .password("pw")
            .roleCd(1001L)
            .statusCd(2001L)
            .authorities(List.of())
            .build();
        given(userDetailsService.loadUserByUsername("user01")).willReturn(active);

        String token = provider.createAccessToken(1L, "user01", 1001L);
        Authentication auth = provider.getAuthentication(token);

        assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(auth.getPrincipal()).isSameAs(active);
    }

    @Test
    @DisplayName("getAuthentication: 탈퇴(9999) 사용자 → USER_RESIGNED")
    void getAuthentication_resignedUser_throwsUserResigned() {
        CustomUserDetails resigned = CustomUserDetails.builder()
            .userId(1L)
            .userLoginId("user01")
            .roleCd(1001L)
            .statusCd(9999L)
            .authorities(List.of())
            .build();
        given(userDetailsService.loadUserByUsername("user01")).willReturn(resigned);

        String token = provider.createAccessToken(1L, "user01", 1001L);

        assertThatThrownBy(() -> provider.getAuthentication(token))
            .isInstanceOf(CustomApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.USER_RESIGNED);
    }

    @Test
    @DisplayName("getAuthentication: isEnabled=true 인데 잠금 상태면 COMMON_UNAUTHORIZED")
    void getAuthentication_lockedButEnabled_throwsUnauthorized() {
        // 실제 status 조합으로는 도달 불가능해서 override 로 재현
        CustomUserDetails locked = new CustomUserDetails() {
            @Override public boolean isEnabled() { return true; }
            @Override public boolean isAccountNonLocked() { return false; }
        };
        given(userDetailsService.loadUserByUsername("user01")).willReturn(locked);

        String token = provider.createAccessToken(1L, "user01", 1001L);

        assertThatThrownBy(() -> provider.getAuthentication(token))
            .isInstanceOf(CustomApiException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.COMMON_UNAUTHORIZED);
    }
}
