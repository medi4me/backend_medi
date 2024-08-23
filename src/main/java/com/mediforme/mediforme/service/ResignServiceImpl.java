package com.mediforme.mediforme.service;

import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.config.jwt.JwtTokenProvider;
import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResignServiceImpl implements ResignService{
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider; // JwtTokenProvider

    @Transactional
    public void resignUser(Long Id, String token) {
        // JWT 토큰 검증 및 무효화 (선택적)
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 사용자 정보 삭제
            Member member = memberRepository.findById(Id)
                    .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
            memberRepository.delete(member);
        } else {
            throw new CustomApiException(ErrorCode.INVALID_JWT_TOKEN);
        }
    }
}
