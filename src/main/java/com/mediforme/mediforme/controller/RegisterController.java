package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.Repository.RegisterRepository;
import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.domain.enums.MemberConsent;
import com.mediforme.mediforme.service.MemberService;
import com.mediforme.mediforme.service.RegisterService;
import com.mediforme.mediforme.util.SmsUtil;
import com.mediforme.mediforme.web.dto.MemberLoginResponseDTO;
import com.mediforme.mediforme.web.dto.RegisterRequestDTO;
import com.mediforme.mediforme.web.dto.VerificationDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegisterController {

    private final RegisterService registerService;
    private final SmsUtil smsUtil;
    private final MemberService memberService;
    private final RegisterRepository registerRepository;

    private final ConcurrentHashMap<String, RegisterRequestDTO.JoinDto> tempDataMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, VerificationDTO> verificationCodeMap = new ConcurrentHashMap<>();

    @Operation(summary = "개인정보 사용 동의 여부")
    @PostMapping("/consent")
    public ApiResponse<String> submitConsent(@RequestBody @Valid RegisterRequestDTO.JoinDto request) {
        MemberConsent consent = request.getConsent();
        String requestId = "UniqueId"; // 예: UUID.randomUUID().toString() 사용 가능

        RegisterRequestDTO.JoinDto data = new RegisterRequestDTO.JoinDto();
        data.setConsent(consent);
        tempDataMap.put(requestId, data);

        return ApiResponse.onSuccess("Consent received.");
    }

    @Operation(summary = "휴대폰 번호 제출")
    @PostMapping("/phone")
    public ApiResponse<String> submitPhone(@RequestBody @Valid RegisterRequestDTO.JoinDto request) {
        // 전화번호와 함께 임시 데이터 저장
        String phone = request.getPhone();
        String requestId = "UniqueId";

        RegisterRequestDTO.JoinDto data = new RegisterRequestDTO.JoinDto();
        data.setPhone(phone);
        tempDataMap.put(requestId, data);

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

        // 유효성 검사
        if (!isMemberIDValid(request.getMemberID())) {
            return ApiResponse.onFailure("INVALID_MEMBER_ID", "MemberID must be 5-30 characters long and contain only letters and numbers.", null);
        }

        // 이미 존재하는 아이디인지 확인
        if (registerRepository.findByMemberID(request.getMemberID()).isPresent()) {
            return ApiResponse.onFailure("DUPLICATE_MEMBER_ID", "MemberID already exists.", null);
        }

        // Store memberID in the temporary data map
        RegisterRequestDTO.JoinDto data = tempDataMap.get(requestId);
        data.setMemberID(request.getMemberID());
        tempDataMap.put(requestId, data);

        return ApiResponse.onSuccess("MemberID received.");
    }

    @Operation(summary = "아이디 유효성 검사")
    @GetMapping("/validate/memberID")
    public ApiResponse<String> validateMemberID(@RequestParam("memberID") String memberID) {
        // 유효성 검사: 5~30자리 숫자와 영문자로 이루어져 있는지 체크
        String memberIDPattern = "^[a-zA-Z0-9]{5,30}$";
        if (!Pattern.matches(memberIDPattern, memberID)) {
            return ApiResponse.onFailure("INVALID_MEMBER_ID", "Member ID must be 5-30 characters long and contain only letters and numbers.", null);
        }

        // 중복 검사: 이미 존재하는 아이디인지 확인
        boolean isMemberIDExists = registerRepository.findByMemberID(memberID).isPresent();
        if (isMemberIDExists) {
            return ApiResponse.onFailure("DUPLICATE_MEMBER_ID", "Member ID already exists.", null);
        }

        return ApiResponse.onSuccess("Member ID is valid and available.");
    }

    @Operation(summary = "비밀번호 제출")
    @PostMapping("/password")
    public ApiResponse<String> submitPassword(@RequestBody @Valid RegisterRequestDTO.JoinDto request) {
        // Handle password submission logic
        String requestId = "UniqueId"; // This should match the ID used in submitPhone

        // 유효성 검사
        if (!isPasswordValid(request.getPassword())){
            return ApiResponse.onFailure("INVALID_PASSWORD", "Password must be at least 8 characters long, contain a number, a letter, and a special character.", null);
        }

        // Store password in the temporary data map
        RegisterRequestDTO.JoinDto data = tempDataMap.get(requestId);
        data.setPassword(request.getPassword());
        tempDataMap.put(requestId, data);

        return ApiResponse.onSuccess("Password received.");
    }

    @Operation(summary = "비밀번호 유효성 검사")
    @GetMapping("/validate/password-length")
    public ApiResponse<String> validatePasswordLength(@RequestParam("password") String password) {
        if (password.length() >= 8) {
            return ApiResponse.onSuccess("Password length is valid.");
        } else {
            return ApiResponse.onFailure("INVALID_PASSWORD_LENGTH", "Password must be at least 8 characters long.", null);
        }
    }

    @Operation(summary = "비밀번호 유효성 검사")
    @GetMapping("/validate/password-number")
    public ApiResponse<String> validatePasswordNumber(@RequestParam("password") String password) {
        if (password.matches(".*\\d.*")) {  // \d는 숫자를 의미합니다.
            return ApiResponse.onSuccess("Password contains at least one number.");
        } else {
            return ApiResponse.onFailure("INVALID_PASSWORD_NUMBER", "Password must contain at least one number.", null);
        }
    }

    @Operation(summary = "비밀번호 유효성 검사")
    @GetMapping("/validate/password-letter")
    public ApiResponse<String> validatePasswordLetter(@RequestParam("password") String password) {
        if (password.matches(".*[a-zA-Z].*")) {  // [a-zA-Z]는 영문자를 의미합니다.
            return ApiResponse.onSuccess("Password contains at least one letter.");
        } else {
            return ApiResponse.onFailure("INVALID_PASSWORD_LETTER", "Password must contain at least one letter.", null);
        }
    }

    @Operation(summary = "비밀번호 유효성 검사")
    @GetMapping("/validate/password-special-char")
    public ApiResponse<String> validatePasswordSpecialChar(@RequestParam("password") String password) {
        if (password.matches(".*[!@#$%^&*()\\-_=+{};:,<.>].*")) {  // 특수문자를 의미합니다.
            return ApiResponse.onSuccess("Password contains at least one special character.");
        } else {
            return ApiResponse.onFailure("INVALID_PASSWORD_SPECIAL_CHAR", "Password must contain at least one special character.", null);
        }
    }

    @Operation(summary = "성명 제출")
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
