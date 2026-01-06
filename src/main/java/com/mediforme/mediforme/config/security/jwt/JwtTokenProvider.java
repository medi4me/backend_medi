package com.mediforme.mediforme.config.security.jwt;

import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @Value("${spring.jwt.token.access-expiration-time}")
    private Long accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private Long refreshExpirationTime;

    private Key getSigningKey() {
        // secretKey를 바이트 배열로 변환 (signWith 메서드 기존의 방식을 deprecated함)
        byte[] secretKeyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        // 바이트 배열로 SecretKeySpec 객체 생성
        return new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length, "HmacSHA256");
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(String userLoginId){
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessExpirationTime);

        return Jwts.builder()
                .setClaims(Jwts.claims().setSubject(userLoginId))
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(Long userId, String userLoginId){
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + refreshExpirationTime);

        return Jwts.builder()
                .setClaims(Jwts.claims().setSubject(userLoginId))
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT에서 사용자 식별자 추출
     */
    public String getUserLoginIdFromToken(String token) {
        return parseToken(token);
    }


    /**
     * 토큰에서 Authentication 객체 복원
     */
    public Authentication getAuthentication(String token) {
        String userLoginId = this.parseToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userLoginId);
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }


    /**
     * 토큰 유효성 검사
     */
    public boolean validateToken(String token) {
        try{
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e){
            throw new CustomApiException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (JwtException e){
            throw new CustomApiException(ErrorCode.INVALID_JWT_TOKEN);
        }
    }

    /**
     * Bearer 토큰 추출
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


    /**
     * 토큰에서 userLoginId 추출
     */
    public String parseToken(String token) {
        return getClaims(token).getSubject();
    }


    /**
     * 토큰 만료 시각(exp) 추출
     * - TTL 계산을 위한 exp
     * - 만료된 토큰이어도 exp는 필요할 수 있으므로 ExpiredJwtException에서 claims를 복구
     */
    public Date getExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * 토큰의 남은 TTL(ms) 계산
     * - exp - now
     * - 이미 만료된 경우 0 이하가 반환될 수 있음.
     */
    public long getRemainingTtlMs(String token) {
        Date exp = getExpiration(token);
        return exp.getTime() - System.currentTimeMillis();
    }

    /**
     * JWT Claims 파싱 공통 메서드
     * - 만료된 토큰이라도 claims(subject/exp)를 얻어야 하는 요구가 있을 수 있음.
     * - 만료된 경우 ExpiredJwtException에서 claims를 꺼내 반환함.
     */
    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
