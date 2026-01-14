package com.mediforme.mediforme.controller.admin;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.dto.response.UserAdminResponseDto;
import com.mediforme.mediforme.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@Tag(name = "Admin User API", description = "관리자/매니저 유저 관리 API")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public class AdminUserController {

    private final UserService userService;

    /**
     * 유저 상세 조회 (관리자/매니저)
     */
    @Operation(summary = "유저 상세 조회(관리자)")
    @GetMapping("/{userId}")
    public ApiResponse<UserAdminResponseDto> getUser(@PathVariable Long userId) {
        return ApiResponse.onSuccess(userService.getUserForAdmin(userId));
    }
}
