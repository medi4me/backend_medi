package com.mediforme.mediforme.Controller;

import com.mediforme.mediforme.DTO.StatusDto;
import com.mediforme.mediforme.Service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "상태 API", description = "상태 관련 API")
@RestController
@RequestMapping("/api/status")
public class StatusController {

    @Autowired
    private StatusService statusService;

    @Operation(summary = "상태 추가", description = "상태 추가")
    @PostMapping
    public StatusDto createStatus(@Valid @RequestBody StatusDto statusDto) {
        return statusService.saveStatus(statusDto);
    }

    @Operation(summary = "모든 상태 조회", description = "상태 조회")
    @GetMapping
    public List<StatusDto> getAllStatuses() {
        return statusService.getAllStatuses();
    }

    @Operation(summary = "ID로 상태 조회", description = "ID로 특정 상태를 조회")
    @GetMapping("/{id}")
    public StatusDto getStatusById(@PathVariable Long id) {
        return statusService.getStatusById(id);
    }

    @Operation(summary = "상태 삭제", description = "상태 삭제")
    @DeleteMapping("/{id}")
    public void deleteStatus(@PathVariable Long id) {
        statusService.deleteStatus(id);
    }

    @GetMapping("/date/{date}")
    public StatusDto getStatusByDate(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        return statusService.getStatusByDate(localDate);
    }
}



