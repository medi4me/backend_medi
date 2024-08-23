package com.mediforme.mediforme.service;

import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.config.jwt.JwtToken;
import com.mediforme.mediforme.config.jwt.JwtTokenProvider;
import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService{

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public JwtToken getToken(Member member) {
        JwtToken jwtToken = jwtTokenProvider.generateToken(member.getMemberID().toString());
        member.saveRefreshToken(jwtToken.getRefreshToken());
        return jwtToken;
    }

    @Override
    public Long getLoginMemberId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Override
    public Member getLoginMember() {
        return memberRepository.findById(getLoginMemberId())
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    }
}
