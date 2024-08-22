package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.Repository.RegisterRepository;
import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.domain.Member;
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

        // 폰 번호가 registerRepository에 존재하는지 확인
        Member phoneExists = registerRepository.findByPhone(phone);
        if (phoneExists == null) {
            return ApiResponse.onFailure("PHONE_NOT_FOUND", "Phone number does not exist.", null);
        }

        // Generate a verification code (e.g., 6-digit random number)
        String verificationCode = String.valueOf((int) (Math.random() * 899999) + 100000);

        // Send the verification code using SmsCool API
        smsUtil.sendOne(phone, verificationCode);

        // 인증 코드를 맵에 저장 (phone -> verificationCode)
        VerificationDTO data = new VerificationDTO();
        data.setPhone(phone);
        data.setVerificationCode(verificationCode);
        verificationCodeMap.put(requestId, data);

        // Return verification code or success message
        return ApiResponse.onSuccess("Verification code sent successfully.");
    }

    @Operation(summary = "휴대폰 번호 인증")
    @PostMapping("/verifyPhone")
    public ApiResponse<String> verifyPhone(@RequestBody @Valid VerificationDTO request) {
        String requestId = "UniqueId";

        String inputCode = request.getVerificationCode();
        String ExpectedVerificationCode = verificationCodeMap.get(requestId).getVerificationCode();

        System.out.println(ExpectedVerificationCode);
        System.out.println(ExpectedVerificationCode);
        System.out.println(ExpectedVerificationCode);

        if (ExpectedVerificationCode != null && ExpectedVerificationCode.equals(inputCode)) {
            return ApiResponse.onSuccess("Phone number verified successfully.");
        } else {
            return ApiResponse.onFailure("VERIFICATION_FAILED", "Verification code is incorrect.", null);
        }
    }

    @Operation(summary = "아이디 제출")
    @PostMapping("/memberID")
    public ApiResponse<String> submitMemberID(@RequestBody @Valid RegisterRequestDTO.JoinDto request) {
        // 이미 존재하는 아이디인지 확인
        if (registerRepository.findByMemberID(request.getMemberID()).isPresent()) {
            return ApiResponse.onFailure("DUPLICATE_MEMBER_ID", "MemberID already exists.", null);
        }

        return ApiResponse.onSuccess("MemberID received.");
    }

    @Operation(summary = "성명 제출")
    @PostMapping("/name")
    public ApiResponse<MemberLoginResponseDTO> submitName(@RequestBody @Valid RegisterRequestDTO.JoinDto request) {
        RegisterRequestDTO.JoinDto newMember = new RegisterRequestDTO.JoinDto();
        newMember.setPhone(request.getPhone());
        newMember.setMemberID(request.getMemberID());
        newMember.setPassword(request.getPassword());
        newMember.setName(request.getName());
        newMember.setConsent(request.getConsent());

        // Automatically log in the new member and generate JWT token
        MemberLoginResponseDTO loginResponse = memberService.getNewMemberLoginResponse(newMember);

        return ApiResponse.onSuccess(loginResponse);
    }
}