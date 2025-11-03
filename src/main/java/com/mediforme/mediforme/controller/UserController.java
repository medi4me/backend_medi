package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.dto.object.VerificationDto;
import com.mediforme.mediforme.dto.request.UserRegisterRequestDto;
import com.mediforme.mediforme.dto.response.UserLoginResponseDto;
import com.mediforme.mediforme.repository.UserRepository;
import com.mediforme.mediforme.service.AuthService;
import com.mediforme.mediforme.service.UserService;
import com.mediforme.mediforme.util.SmsUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/users")
@Tag(name = "User API", description = "회원가입 및 휴대폰 인증 관련 API")
public class UserController {
    private final SmsUtil smsUtil;
    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, VerificationDto> verificationCodeMap = new ConcurrentHashMap<>();
    private final AuthService authService;

    @Operation(summary = "휴대폰 인증코드 발송", description = "회원가입 시 입력한 휴대폰 번호로 인증 코드를 발송합니다.")
    @PostMapping("/phone")
    public ApiResponse<String> sendVerificationCode(@RequestParam("phone") String phone) {
        // 전화번호 중복 확인
        if (userRepository.findByPhone(phone).isPresent()) {
            return ApiResponse.onFailure("DUPLICATED_PHONE", "이미 등록된 전화번호입니다.", null);
        }

        String verificationCode = String.valueOf((int) (Math.random() * 899999) + 100000);
        smsUtil.sendOne(phone, verificationCode);

        VerificationDto dto = new VerificationDto();
        dto.setPhone(phone);
        dto.setVerificationCode(verificationCode);
        verificationCodeMap.put(phone, dto);

        return ApiResponse.onSuccess("인증 코드가 발송되었습니다.");
    }


    @Operation(summary = "휴대폰 인증 확인", description = "사용자가 입력한 인증 코드를 검증합니다.")
    @PostMapping("/verify-phone")
    public ApiResponse<String> verifyPhone(@RequestBody @Valid VerificationDto request) {
        VerificationDto stored = verificationCodeMap.get(request.getPhone());

        if (stored != null && stored.getVerificationCode().equals(request.getVerificationCode())) {
            return ApiResponse.onSuccess("휴대폰 인증이 완료되었습니다.");
        }
        return ApiResponse.onFailure("VERIFICATION_FAILED", "인증 코드가 올바르지 않습니다.", null);
    }


    @Operation(summary = "아이디 중복 확인", description = "회원가입 시 입력한 아이디의 중복 여부를 확인합니다.")
    @GetMapping("/check-id")
    public ApiResponse<String> checkUserLoginId(@RequestParam("userLoginId") String userLoginId) {
        boolean exists = userRepository.findByUserLoginId(userLoginId).isPresent();
        if (exists) {
            return ApiResponse.onFailure("DUPLICATED_USER_LOGIN_ID", "이미 존재하는 아이디입니다.", null);
        }
        return ApiResponse.onSuccess("사용 가능한 아이디입니다.");
    }


    @Operation(summary = "회원가입", description = "새로운 사용자를 등록하고, JWT 토큰을 발급받습니다.")
    @PostMapping("/register")
    public ApiResponse<UserLoginResponseDto> register(@RequestBody @Valid UserRegisterRequestDto.JoinRequest request) {
        UserLoginResponseDto response = authService.register(request);
        return ApiResponse.onSuccess(response);
    }
}