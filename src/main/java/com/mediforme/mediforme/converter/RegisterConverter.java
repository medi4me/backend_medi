package com.mediforme.mediforme.converter;

import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.domain.enums.MemberStatus;
import com.mediforme.mediforme.web.dto.RegisterRequestDTO;
import com.mediforme.mediforme.web.dto.RegisterResponseDTO;

import java.time.LocalDateTime;

public class RegisterConverter {

    public static RegisterResponseDTO.JoinResultDTO toJoinResultDTO(Member member){
        return RegisterResponseDTO.JoinResultDTO.builder()
                .memberId(member.getId())
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
                .build();
    }
}
