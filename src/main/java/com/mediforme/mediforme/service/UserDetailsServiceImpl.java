package com.mediforme.mediforme.service;

import com.mediforme.mediforme.Repository.RegisterRepository;
import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final RegisterRepository registerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String memberID) {
        return registerRepository
                .findByMemberID(memberID)
                .map(member -> User.builder()
                        .username(String.valueOf(member.getMemberID()))
                        .password(passwordEncoder.encode(member.getPassword())) // 데이터베이스에 저장된 인코딩된 비밀번호 사용
                        .roles(String.valueOf(member.getRole())) // 'ROLE_' 접두사가 포함된 권한 값 사용
                        .build())
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    }
}
