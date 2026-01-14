package com.mediforme.mediforme.controller.admin;

import com.mediforme.mediforme.apiPayload.ApiResponse;
import com.mediforme.mediforme.dto.response.StatusAdminResponseDto;
import com.mediforme.mediforme.service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AdminStatusController
 * - 관리자/매니저 운영 목적의 Status 조회/삭제
 */
@Tag(name = "상태(Admin) API", description = "관리자/매니저용 상태 조회/삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/statuses")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public class AdminStatusController {

    private final StatusService statusService;

    @Operation(summary = "상태 id로 상태 조회 (관리자)", description = "관리자/매니저 권한으로 상태 단건을 조회합니다.")
    @GetMapping("/{statusId}")
    public ApiResponse<StatusAdminResponseDto> getStatusById(@PathVariable Long statusId) {
        return ApiResponse.onSuccess(statusService.getStatusById(statusId));
    }

    @Operation(summary = "상태 삭제 (관리자)", description = "관리자/매니저 권한으로 상태를 삭제합니다.")
    @DeleteMapping("/{statusId}")
    public ApiResponse<String> deleteStatusByAdmin(@PathVariable Long statusId) {
        statusService.deleteStatusAdmin(statusId);
        return ApiResponse.onSuccess("상태가 삭제되었습니다.");
    }
}
