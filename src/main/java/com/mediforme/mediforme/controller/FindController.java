package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.Repository.RegisterRepository;
import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.util.SmsUtil;
import com.mediforme.mediforme.web.dto.FindResponseDTO;
import com.mediforme.mediforme.web.dto.VerificationDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/find")
public class FindController {

    private final RegisterRepository registerRepository;
    private final SmsUtil smsUtil;

    // 인증 코드를 임시 저장할 맵
    private final ConcurrentHashMap<String, VerificationDTO> verificationCodeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FindResponseDTO> findIDResultMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FindResponseDTO> findPasswordResultMap = new ConcurrentHashMap<>();

    @PostMapping("/send-verification-code")
    public ApiResponse<String> sendVerificationCode(@RequestBody @Valid VerificationDTO request) {

        // 폰 번호가 registerRepository에 존재하는지 확인
        Member phoneExists = registerRepository.findByPhone(request.getPhone());
        if (phoneExists == null) {
            return ApiResponse.onFailure("PHONE_NOT_FOUND", "Phone number does not exist.", null);
        }

        // 인증 코드 생성
        String verificationCode = String.valueOf((int) (Math.random() * 899999) + 100000);
        String requestId = "UniqueId";
        // 인증 코드 SMS 전송
        smsUtil.sendOne(request.getPhone(), verificationCode);

        // 인증 코드를 맵에 저장 (phone -> verificationCode)
        VerificationDTO data = new VerificationDTO();
        data.setPhone(request.getPhone());
        data.setVerificationCode(verificationCode);
        verificationCodeMap.put(requestId, data);

        return ApiResponse.onSuccess("Verification code sent successfully.");
    }

    @PostMapping("/verify-and-find-id")
    public ApiResponse<FindResponseDTO> verifyAndFindID(@RequestBody @Valid VerificationDTO request) {
        String requestId = "UniqueId";
        String ExpectedVerificationCode = verificationCodeMap.get(requestId).getVerificationCode();

        if (Optional.ofNullable(ExpectedVerificationCode)
                .filter(code -> code.equals(request.getVerificationCode()))
                .isPresent()) {

            Optional<Member> optionalMember = Optional.ofNullable(
                    registerRepository.findByPhone(verificationCodeMap.get(requestId).getPhone()));

            return optionalMember.map(member -> {
                FindResponseDTO responseDTO = new FindResponseDTO();
                responseDTO.setMemberID(member.getMemberID());

                // 결과를 맵에 저장하여 GetMapping에서 사용
                findIDResultMap.put(requestId, responseDTO);

                verificationCodeMap.remove(requestId);
                return ApiResponse.onSuccess(responseDTO);
            }).orElseGet(() ->
                    ApiResponse.onFailure("ID_NOT_FOUND", "Member ID not found for the provided phone number.", null));
        } else {
            return ApiResponse.onFailure("VERIFICATION_FAILED", "Verification code is incorrect.", null);
        }
    }

    @PostMapping("/verify-and-find-password")
    public ApiResponse<FindResponseDTO> verifyAndFindPassword(@RequestBody @Valid VerificationDTO request) {
        String requestId = "UniqueId";
        String ExpectedVerificationCode = verificationCodeMap.get(requestId).getVerificationCode();

        if (Optional.ofNullable(ExpectedVerificationCode)
                .filter(code -> code.equals(request.getVerificationCode()))
                .isPresent()) {

            Optional<Member> optionalMember = Optional.ofNullable(
                    registerRepository.findByPhone(verificationCodeMap.get(requestId).getPhone()));

            return optionalMember.map(member -> {
                FindResponseDTO responseDTO = new FindResponseDTO();
                responseDTO.setPassword(member.getPassword());

                // 결과를 맵에 저장하여 GetMapping에서 사용
                findPasswordResultMap.put(requestId, responseDTO);

                verificationCodeMap.remove(requestId);
                return ApiResponse.onSuccess(responseDTO);
            }).orElseGet(() ->
                    ApiResponse.onFailure("ID_NOT_FOUND", "Member ID not found for the provided phone number.", null));
        } else {
            return ApiResponse.onFailure("VERIFICATION_FAILED", "Verification code is incorrect.", null);
        }
    }

}
