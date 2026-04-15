package com.mediforme.mediforme.controller;

import com.mediforme.common.response.ApiResponse;
import com.mediforme.mediforme.dto.response.MedicineCameraResponseDto;
import com.mediforme.mediforme.service.MedicineCameraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.json.simple.parser.ParseException;


@Tag(name = "Medicine Recognitions", description = "카메라/이미지 기반 약물 인식")
@RestController
@RequestMapping("/medicine-recognitions")
@RequiredArgsConstructor
public class MedicineRecognitionsController {

    private final MedicineCameraService medicineCameraService;

    @Operation(summary = "카메라로 약물 인식", description = "이미지를 업로드로 약물을 인식하고, 약 정보를 조회합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MedicineCameraResponseDto> recognizeMedicine(@RequestPart("file") MultipartFile file)
            throws IOException, ParseException {

        // 요청 유효성 검증
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");      // 400
        }

        return ApiResponse.onSuccess(medicineCameraService.processImage(file));
    }
}

