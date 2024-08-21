package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.domain.enums.MemberConsent;
import com.mediforme.mediforme.service.MemberService;
import com.mediforme.mediforme.service.RegisterService;
import com.mediforme.mediforme.util.SmsUtil;
import com.mediforme.mediforme.web.dto.MemberLoginResponseDTO;
import com.mediforme.mediforme.web.dto.RegisterRequestDTO;
import com.mediforme.mediforme.web.dto.VerificationDTO;
import jakarta.servlet.http.HttpSession;
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
    private final ConcurrentHashMap<String, RegisterRequestDTO.JoinDto> tempDataMap = new ConcurrentHashMap<>();

    @PostMapping("/consent")
    public ApiResponse<String> submitConsent(@RequestBody @Valid RegisterRequestDTO.JoinDto request) {
        MemberConsent consent = request.getConsent();
        String requestId = "UniqueId"; // 예: UUID.randomUUID().toString() 사용 가능

        RegisterRequestDTO.JoinDto data = new RegisterRequestDTO.JoinDto();
        data.setConsent(consent);
        tempDataMap.put(requestId, data);

        System.out.println(data.getConsent());
        System.out.println();
        System.out.println();

        return ApiResponse.onSuccess("Consent received.");
    }

    @PostMapping("/phone")
    public ApiResponse<String> submitPhone(@RequestBody @Valid RegisterRequestDTO.JoinDto request) {
        // 전화번호와 함께 임시 데이터 저장
        String phone = request.getPhone();
        String requestId = "UniqueId"; // 예: UUID.randomUUID().toString() 사용 가능

        RegisterRequestDTO.JoinDto data = new RegisterRequestDTO.JoinDto();
        data.setPhone(phone);
        tempDataMap.put(requestId, data);

        // Generate a verification code (e.g., 6-digit random number)
        String verificationCode = String.valueOf((int) (Math.random() * 899999) + 100000);

        // Return verification code or success message
        return ApiResponse.onSuccess("Verification code sent successfully.");
    }

    @PostMapping("/verifyPhone")
    public ApiResponse<String> verifyPhone(@RequestBody @Valid VerificationDTO request, HttpSession session) {
        String inputCode = request.getVerificationCode();
        String expectedCode = (String) session.getAttribute("verificationCode"); // For demonstration, this could be replaced with a validation service

        if (expectedCode != null && expectedCode.equals(inputCode)) {
            return ApiResponse.onSuccess("Phone number verified successfully.");
        } else {
            return ApiResponse.onFailure("VERIFICATION_FAILED", "Verification code is incorrect.", null);
        }
    }

    @PostMapping("/memberID")
    public ApiResponse<String> submitMemberID(@RequestBody @Valid RegisterRequestDTO.JoinDto request) {
        // Handle username submission logic
        String requestId = "UniqueId"; // This should match the ID used in submitPhone

        // Store memberID in the temporary data map
        RegisterRequestDTO.JoinDto data = tempDataMap.get(requestId);
        data.setMemberID(request.getMemberID());
        tempDataMap.put(requestId, data);

        return ApiResponse.onSuccess("MemberID received.");
    }

    @PostMapping("/password")
    public ApiResponse<String> submitPassword(@RequestBody @Valid RegisterRequestDTO.JoinDto request) {
        // Handle password submission logic
        String requestId = "UniqueId"; // This should match the ID used in submitPhone

        // Store password in the temporary data map
        RegisterRequestDTO.JoinDto data = tempDataMap.get(requestId);
        data.setPassword(request.getPassword());
        tempDataMap.put(requestId, data);

        return ApiResponse.onSuccess("Password received.");
    }

    @PostMapping("/name")
    public ApiResponse<MemberLoginResponseDTO> submitName(@RequestBody @Valid RegisterRequestDTO.JoinDto request) {
        String requestId = "UniqueId";

        // Retrieve and finalize the data
        RegisterRequestDTO.JoinDto data = tempDataMap.get(requestId);
        if (data != null) {
            data.setName(request.getName());

            RegisterRequestDTO.JoinDto newMember = new RegisterRequestDTO.JoinDto();
            newMember.setPhone(data.getPhone());
            newMember.setMemberID(data.getMemberID());
            newMember.setPassword(data.getPassword());
            newMember.setName(data.getName());
            newMember.setConsent(data.getConsent());

            registerService.registerUser(newMember);

            // Remove from temp data map
            tempDataMap.remove(requestId);

            // Automatically log in the new member and generate JWT token
            MemberLoginResponseDTO loginResponse = memberService.getNewMemberLoginResponse(newMember);

            return ApiResponse.onSuccess(loginResponse);
        } else {
            return ApiResponse.onFailure("DATA_NOT_FOUND", "Previous data not found.", null);
        }
    }
}
