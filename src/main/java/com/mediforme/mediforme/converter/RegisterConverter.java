package com.mediforme.mediforme.converter;

import com.mediforme.mediforme.config.jwt.JwtToken;
import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.domain.enums.MemberStatus;
import com.mediforme.mediforme.domain.enums.Role;
import com.mediforme.mediforme.dto.response.MemberLoginResponseDto;
import com.mediforme.mediforme.dto.request.RegisterRequestDto;
import com.mediforme.mediforme.dto.response.RegisterResponseDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RegisterConverter {

    public static RegisterResponseDto.JoinResultDTO toJoinResultDTO(Member member){
        return RegisterResponseDto.JoinResultDTO.builder()
                .memberId(member.getId())
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Member toMember(RegisterRequestDto.JoinDto request){
        return Member.builder()
                .name(request.getName())
                .memberID(request.getMemberID())
                .password(request.getPassword())
                .phone(request.getPhone())
                .consent(request.getConsent())
                .status(MemberStatus.ACTIVE)
                .InactiveDate(null)
                .role(Role.USER)
                .build();
    }

    public MemberLoginResponseDto toMemberLoginResponse(String memberId, JwtToken jwtToken) {
        return MemberLoginResponseDto.builder()
                .memberID(memberId)
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }
}
