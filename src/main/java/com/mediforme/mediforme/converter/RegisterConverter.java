package com.mediforme.mediforme.converter;

import com.mediforme.mediforme.config.jwt.JwtToken;
import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.domain.enums.MemberStatus;
import com.mediforme.mediforme.domain.enums.Role;
import com.mediforme.mediforme.web.dto.MemberLoginResponseDTO;
import com.mediforme.mediforme.web.dto.RegisterRequestDTO;
import com.mediforme.mediforme.web.dto.RegisterResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RegisterConverter {

    public static RegisterResponseDTO.JoinResultDTO toJoinResultDTO(Member member){
        return RegisterResponseDTO.JoinResultDTO.builder()
                .memberId(member.getId())
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Member toMember(RegisterRequestDTO.JoinDto request){
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

    public MemberLoginResponseDTO toMemberLoginResponse(String memberId, JwtToken jwtToken) {
        return MemberLoginResponseDTO.builder()
                .memberID(memberId)
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }
}
