package com.mediforme.mediforme.service;

import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.dto.request.MemberRequestDTO;
import com.mediforme.mediforme.dto.request.RegisterRequestDTO;
import com.mediforme.mediforme.dto.response.MemberLoginResponseDTO;

public interface MemberService {
    MemberLoginResponseDTO login(MemberRequestDTO.LoginRequestDto request);
    MemberLoginResponseDTO getMemberLoginResponse(final Member member);
    MemberLoginResponseDTO getNewMemberLoginResponse(final RegisterRequestDTO.JoinDto memberID);
    String findMemberNameByID(String memberID);
}