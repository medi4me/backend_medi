package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.dto.object.VerificationDto;
import com.mediforme.mediforme.dto.response.FindLoginIdResponseDto;
import com.mediforme.mediforme.repository.UserRepository;
import com.mediforme.mediforme.util.SmsUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/find")
public class AccountRecoveryController {

    private final UserRepository userRepository;
    private final SmsUtil smsUtil;

    // 인증 코드 임시 저장 (TODO: 추후 Redis로 교체 예정)
    private final ConcurrentHashMap<String, String> verificationMap = new ConcurrentHashMap<>();


    // 인증 코드 발송
    @PostMapping("/send-verification-code")
    public ApiResponse<String> sendVerificationCode(@RequestBody @Valid VerificationDto request) {

        // 등록된 사용자 여부 확인
        boolean exists = userRepository.existsByPhone(request.getPhone());
        if (!exists) {
            return ApiResponse.onFailure("PHONE_NOT_FOUND", "등록되지 않은 전화번호입니다.", null);
        }

        // 6자리 인증 코드 생성
        String verificationCode = String.valueOf((int) (Math.random() * 899999) + 100000);
        // 코드 발송 (SMS)
        smsUtil.sendOne(request.getPhone(), verificationCode);
        // 메모리에 저장
        // TODO: 레디스 저장 예정
        verificationMap.put(request.getPhone(), verificationCode);

        return ApiResponse.onSuccess("인증 코드가 전송되었습니다.");
    }


    // 인증 코드 검증 + 아이디 찾기
    @PostMapping("/verify-and-find-id")
    public ApiResponse<FindLoginIdResponseDto> verifyAndFindId(@RequestBody @Valid VerificationDto request) {
        // 코드 검증
        if (!verifyCode(request.getPhone(), request.getVerificationCode())) {
            return ApiResponse.onFailure("VERIFICATION_FAILED", "인증 코드가 올바르지 않습니다.", null);
        }
        // 사용자 조회
        Optional<User> userOpt = userRepository.findByPhone(request.getPhone());
        if (userOpt.isEmpty()) {
            return ApiResponse.onFailure("USER_NOT_FOUND", "해당 번호의 사용자를 찾을 수 없습니다.", null);
        }

        FindLoginIdResponseDto response = new FindLoginIdResponseDto();
        response.setUserLoginId(userOpt.get().getUserLoginId());
        response.setUserName(userOpt.get().getUserName());

        // 인증 코드 제거
        verificationMap.remove(request.getPhone());
        return ApiResponse.onSuccess(response);
    }

    // 인증 코드 검증 + 비밀번호 찾기 (비밀번호 직접 노출 X)
    @PostMapping("/verify-and-find-password")
    public ApiResponse<String> verifyAndFindPassword(@RequestBody @Valid VerificationDto request) {
        // 코드 검증
        if (!verifyCode(request.getPhone(), request.getVerificationCode())) {
            return ApiResponse.onFailure("VERIFICATION_FAILED", "인증 코드가 올바르지 않습니다.", null);
        }

        Optional<User> userOpt = userRepository.findByPhone(request.getPhone());
        if (userOpt.isEmpty()) {
            return ApiResponse.onFailure("USER_NOT_FOUND", "해당 번호의 사용자를 찾을 수 없습니다.", null);
        }

        // TODO: 비밀번호 재설정용 토큰 발급 / 이메일 전송 로직 추가
        verificationMap.remove(request.getPhone());
        return ApiResponse.onSuccess("비밀번호 재설정 절차를 진행해주세요.");
    }

    // 인증 코드 검증
    private boolean verifyCode(String phone, String code) {
        return Optional.ofNullable(verificationMap.get(phone))
                .filter(savedCode -> savedCode.equals(code))
                .isPresent();
    }
}
