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
@RequestMapping("/user")
@Tag(name = "User API", description = "사용자 상태 정보 관련 API")
public class UserController {
    private final UserService userService;

    @Operation(summary = "사용자 이름 조회")
    @GetMapping("/{userLoginId}/name")
    public ApiResponse<String> getUserName(@PathVariable String userLoginId) {
        return ApiResponse.onSuccess(
                userService.findUserNameByLoginId(userLoginId)
        );
    }
}