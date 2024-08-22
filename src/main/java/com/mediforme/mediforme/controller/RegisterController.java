package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.Repository.RegisterRepository;
import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.service.MemberService;
import com.mediforme.mediforme.service.RegisterService;
import com.mediforme.mediforme.util.SmsUtil;
import com.mediforme.mediforme.web.dto.MemberLoginResponseDTO;
import com.mediforme.mediforme.web.dto.RegisterRequestDTO;
import com.mediforme.mediforme.web.dto.VerificationDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegisterController {

    private final RegisterService registerService;
    private final SmsUtil smsUtil;
    private final MemberService memberService;
    private final RegisterRepository registerRepository;

    private final ConcurrentHashMap<String, VerificationDTO> verificationCodeMap = new ConcurrentHashMap<>();

    @Operation(summary = "휴대폰 번호 제출")
    @PostMapping("/phone")
    public ApiResponse<String> submitPhone(@RequestParam("phone") String phone) {
        // 전화번호와 함께 임시 데이터 저장
        String requestId = "UniqueId";

        // Generate a verification code (e.g., 6-digit random number)
        String verificationCode = String.valueOf((int) (Math.random() * 899999) + 100000);

        // Send the verification code using SmsCool API
        smsUtil.sendOne(phone, verificationCode);

        // Store memberID in the temporary data map
        VerificationDTO verifyData = verificationCodeMap.get(requestId);
        verifyData.setVerificationCode(verificationCode);
        verificationCodeMap.put(requestId, verifyData);

        // Return verification code or success message
        return ApiResponse.onSuccess("Verification code sent successfully.");
    }

    @Operation(summary = "휴대폰 번호 인증")
    @PostMapping("/verifyPhone")
    public ApiResponse<String> verifyPhone(@RequestBody @Valid VerificationDTO request) {
        String requestId = "UniqueId";
        String inputCode = request.getVerificationCode();

        // Retrieve and finalize the verificationCodeMap
        VerificationDTO verify = verificationCodeMap.get(requestId);
        String expectedCode = verify.getVerificationCode();

        if (expectedCode != null && expectedCode.equals(inputCode)) {
            return ApiResponse.onSuccess("Phone number verified successfully.");
        } else {
            return ApiResponse.onFailure("VERIFICATION_FAILED", "Verification code is incorrect.", null);
        }
    }

    @Operation(summary = "아이디 제출")
    @PostMapping("/memberID")
    public ApiResponse<String> submitMemberID(@RequestBody @Valid RegisterRequestDTO.JoinDto request) {
        // Handle username submission logic
        String requestId = "UniqueId"; // This should match the ID used in submitPhone

        // 이미 존재하는 아이디인지 확인
        if (registerRepository.findByMemberID(request.getMemberID()).isPresent()) {
            return ApiResponse.onFailure("DUPLICATE_MEMBER_ID", "MemberID already exists.", null);
        }

        return ApiResponse.onSuccess("MemberID received.");
    }

    @Operation(summary = "성명 제출")
    @PostMapping("/name")
    public ApiResponse<MemberLoginResponseDTO> submitName(@RequestBody @Valid RegisterRequestDTO.JoinDto request) {
        String requestId = "UniqueId";

        RegisterRequestDTO.JoinDto newMember = new RegisterRequestDTO.JoinDto();
        newMember.setPhone(request.getPhone());
        newMember.setMemberID(request.getMemberID());
        newMember.setPassword(request.getPassword());
        newMember.setName(request.getName());
        newMember.setConsent(request.getConsent());

        registerService.registerUser(newMember);

        // Automatically log in the new member and generate JWT token
        MemberLoginResponseDTO loginResponse = memberService.getNewMemberLoginResponse(newMember);

        return ApiResponse.onSuccess(loginResponse);
    }

    private boolean isMemberIDValid(String memberID) {
        // 아이디가 5~30자리인지 확인
        if (memberID.length() < 5 || memberID.length() > 30) {
            return false;
        }

        // 아이디가 숫자와 영문자로만 이루어져 있는지 확인
        if (!memberID.matches("[a-zA-Z0-9]+")) {
            return false;
        }

        return true;
    }

    private boolean isPasswordValid(String password) {
        // 비밀번호가 8자리 이상인지 확인
        if (password.length() < 8) {
            return false;
        }

        // 비밀번호에 숫자가 포함되어 있는지 확인
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        // 비밀번호에 영문자가 포함되어 있는지 확인
        if (!password.matches(".*[a-zA-Z].*")) {
            return false;
        }

        // 비밀번호에 특수문자가 포함되어 있는지 확인
        if (!password.matches(".*[!@#$%^&*()\\-_=+{};:,<.>].*")) {
            return false;
        }

        return true;
    }
}
