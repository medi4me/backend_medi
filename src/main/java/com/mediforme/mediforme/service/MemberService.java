package com.mediforme.mediforme.service;

import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.web.dto.MemberLoginResponseDTO;
import com.mediforme.mediforme.web.dto.MemberRequestDTO;
import com.mediforme.mediforme.web.dto.RegisterRequestDTO;

public interface MemberService {
    public MemberLoginResponseDTO login(MemberRequestDTO.LoginRequestDto request);
    public MemberLoginResponseDTO getMemberLoginResponse(final Member member);
    public MemberLoginResponseDTO getNewMemberLoginResponse(final RegisterRequestDTO.JoinDto memberID);
    String findMemberNameByID(String memberID);
}