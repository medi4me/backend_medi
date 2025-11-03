package com.mediforme.mediforme.config.security.jwt;

import com.mediforme.lib.redis.entity.BlacklistToken;
import com.mediforme.lib.redis.entity.UserToken;
import com.mediforme.lib.redis.repository.BlacklistRedisRepository;
import com.mediforme.lib.redis.repository.UserTokenRedisRepository;
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
    private final UserTokenRedisRepository userTokenRedisRepository;
    private final BlacklistRedisRepository blacklistRedisRepository;

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

        String accessToken = createAccessToken(userLoginId);

        String refreshToken =  Jwts.builder()
                .setClaims(Jwts.claims().setSubject(userLoginId))
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        // 리프레시 토큰 Redis에 저장
        userTokenRedisRepository.save(UserToken.builder()
                .accessToken(accessToken)
                .userId(userId)
                .userLoginId(userLoginId)
                .refreshToken(refreshToken)
                .build());

        return refreshToken;
    }

    /**
     * Redis에 저장된 Refresh Token 검증
     */
    public boolean isRefreshTokenValid(String userLoginId, String refreshToken) {
        return userTokenRedisRepository.findByUserLoginId(userLoginId)
                .map(UserToken::getRefreshToken)
                .filter(token -> token.equals(refreshToken))
                .isPresent();
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
            // 블랙리스트 검증
            if (blacklistRedisRepository.findById(token).isPresent()){
                throw new CustomApiException(ErrorCode.INVALID_JWT_TOKEN);
            }
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
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * 로그아웃 시 블랙리스트 등록
     */
    public void addToBlacklist(String token){
        blacklistRedisRepository.save(BlacklistToken.builder()
                .accessToken(token)
                .reason("logout")
                .build());
    }
}
