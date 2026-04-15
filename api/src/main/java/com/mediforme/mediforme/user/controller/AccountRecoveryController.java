package com.mediforme.mediforme.user.controller;

import com.mediforme.common.response.ApiResponse;
import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.exception.ErrorCode;
import com.mediforme.mediforme.user.domain.User;
import com.mediforme.mediforme.user.dto.VerificationDto;
import com.mediforme.mediforme.user.dto.ResetPasswordRequestDto;
import com.mediforme.mediforme.user.dto.FindLoginIdResponseDto;
import com.mediforme.mediforme.user.repository.UserRepository;
import com.mediforme.mediforme.user.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/find")
@Tag(name = "Account Recovery API", description = "아이디 및 비밀번호 찾기 관련 API")
public class AccountRecoveryController {

    private final UserRepository userRepository;
    private final VerificationService verificationService;
    private final PasswordEncoder passwordEncoder;


    /**
     * 휴대폰 인증 코드 발송
     */
    @Operation(summary = "휴대폰 인증코드 발송", description = "등록된 사용자에게 인증 코드를 발송합니다.")
    @PostMapping("/send-verification-code")
    public ApiResponse<String> sendVerificationCode(@RequestBody @Valid VerificationDto request) {

        // 등록된 사용자 여부 확인
        boolean exists = userRepository.existsByPhone(request.getPhone());
        if (!exists) {
            throw new CustomApiException(ErrorCode.USER_NOT_FOUND);
        }

        // Redis + SMS 발송
        verificationService.sendCode(request.getPhone());

        return ApiResponse.onSuccess("인증 코드가 발송되었습니다.");
    }


    /**
     * 인증 코드 검증 후 아이디 찾기
     */
    @Operation(summary = "아이디 찾기", description = "휴대폰 인증을 통해 아이디를 조회합니다.")
    @PostMapping("/verify-and-find-id")
    public ApiResponse<FindLoginIdResponseDto> verifyAndFindId(@RequestBody @Valid VerificationDto request) {

        // 인증 코드 검증
        boolean verified = verificationService.verifyCode(request.getPhone(), request.getVerificationCode());
        if (!verified) {
            throw new CustomApiException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 사용자 조회
        Optional<User> userOpt = userRepository.findByPhone(request.getPhone());
        if (userOpt.isEmpty()) {
            throw new CustomApiException(ErrorCode.USER_NOT_FOUND);
        }

        // 응답 DTO 생성
        User user = userOpt.get();
        FindLoginIdResponseDto response = new FindLoginIdResponseDto();
        response.setUserLoginId(user.getUserLoginId());
        response.setUserName(user.getUserName());

        // 인증 코드 삭제
        verificationService.removeCode(request.getPhone());

        return ApiResponse.onSuccess(response);
    }

    /**
     * 인증 코드 검증 후 비밀번호 찾기 (재설정 절차 안내)
     */
    @Operation(summary = "비밀번호 찾기", description = "휴대폰 인증을 통해 비밀번호 재설정 절차를 진행합니다.")
    @PostMapping("/verify-and-find-password")
    public ApiResponse<String> verifyAndFindPassword(@RequestBody @Valid VerificationDto request) {

        boolean verified = verificationService.verifyCode(request.getPhone(), request.getVerificationCode());
        if (!verified) {
            throw new CustomApiException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        Optional<User> userOpt = userRepository.findByPhone(request.getPhone());
        if (userOpt.isEmpty()) {
            throw new CustomApiException(ErrorCode.USER_NOT_FOUND);
        }

        // 비밀번호 재설정 토큰 발급 (Redis에 5분 TTL 저장)
        String resetToken = verificationService.generatePasswordResetToken(request.getPhone());
        return ApiResponse.onSuccess(resetToken);
    }


    /**
     * 비밀번호 재설정
     */
    @Operation(summary = "비밀번호 재설정", description = "발급받은 토큰으로 비밀번호를 재설정합니다.")
    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody @Valid ResetPasswordRequestDto request) {

        // 토큰 검증 (내부에서 유효하지 않으면 CustomApiException 발생)
        verificationService.validatePasswordResetToken(request.getPhone(), request.getToken());

        // 사용자 조회
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 암호화 후 갱신
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 응답 반환
        return ApiResponse.onSuccess("비밀번호가 성공적으로 변경되었습니다.");
    }

}
