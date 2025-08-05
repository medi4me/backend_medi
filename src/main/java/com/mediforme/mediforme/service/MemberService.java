package com.mediforme.mediforme.service;

import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.dto.request.MemberRequestDto;
import com.mediforme.mediforme.dto.request.RegisterRequestDto;
import com.mediforme.mediforme.dto.response.MemberLoginResponseDto;

public interface MemberService {
    MemberLoginResponseDto login(MemberRequestDto.LoginRequestDto request);
    MemberLoginResponseDto getMemberLoginResponse(final Member member);
    MemberLoginResponseDto getNewMemberLoginResponse(final RegisterRequestDto.JoinDto memberID);
    String findMemberNameByID(String memberID);
}