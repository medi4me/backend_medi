package com.mediforme.mediforme.service;

import com.mediforme.mediforme.Repository.RegisterRepository;
import com.mediforme.mediforme.apiPayload.exception.CustomApiException;
import com.mediforme.mediforme.apiPayload.exception.ErrorCode;
import com.mediforme.mediforme.config.jwt.JwtToken;
import com.mediforme.mediforme.config.jwt.JwtTokenProvider;
import com.mediforme.mediforme.converter.RegisterConverter;
import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.web.dto.MemberLoginResponseDTO;
import com.mediforme.mediforme.web.dto.MemberRequestDTO;
import com.mediforme.mediforme.web.dto.RegisterRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final RegisterRepository registerRepository;
    private final RegisterConverter registerConverter;
    private final AuthService authService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    //사용자의 로그인 인증을 처리하고, JWT 토큰을 발급하여 반환하는 메서드
    public MemberLoginResponseDTO login(MemberRequestDTO.LoginRequestDto request){
        String memberID = request.getMemberID();
        String password = request.getPassword();

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(memberID, password);

        Authentication authentication =
                authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        String authenticatedUserId = authentication.getName(); //  UserId

        Member member = registerRepository.findById(Long.valueOf(authenticatedUserId))
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND)); // id
        JwtToken jwtToken = jwtTokenProvider.generateToken(member.getId().toString());

        return registerConverter.toMemberLoginResponse(member.getId(), jwtToken);
    }

    // 기존 사용자에 대해 JWT 토큰을 생성하여 반환하는 메서드. 특히 리프레시 토큰을 사용해 새로운 액세스 토큰을 발급할 때 유용
    public MemberLoginResponseDTO getMemberLoginResponse(final Member member) {
        // TODO RefreshToken으로 AccessToken만 재발급 받도록 구현
        JwtToken jwtToken = authService.getToken(member);
        return registerConverter.toMemberLoginResponse(member.getId(), jwtToken);
    }

    // 새로운 사용자에 대해 JWT 토큰을 생성하여 반환하는 메서드. 회원가입 후 첫 로그인 시 사용.
    public MemberLoginResponseDTO getNewMemberLoginResponse(final RegisterRequestDTO.JoinDto memberID) {
        Member member = registerRepository.save(registerConverter.toMember(memberID));
        JwtToken jwtToken = authService.getToken(member);
        return registerConverter.toMemberLoginResponse(member.getId(), jwtToken);
    }

    /**
     * 주어진 memberID로 Member 객체를 찾고, 해당 Member의 이름을 반환합니다.
     *
     * @param memberID 회원 ID
     * @return 회원 이름. ID가 유효하지 않거나 회원이 존재하지 않을 경우 "회원 이름을 찾을 수 없습니다."
     */
    public String findMemberNameByID(String memberID) {
        Optional<Member> optionalMember = registerRepository.findByMemberID(memberID);

        // Optional<Member>로부터 Member 객체를 추출하고 이름을 반환합니다.
        return optionalMember
                .map(Member::getName) // Member가 존재하면 이름을 가져옵니다.
                .orElse("회원 이름을 찾을 수 없습니다."); // Member가 없으면 기본 메시지를 반환합니다.
    }
}