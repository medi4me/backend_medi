package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.dto.response.MedicineCameraResponseDto;
import com.mediforme.mediforme.service.MedicineCameraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.json.simple.parser.ParseException;


@Tag(name = "Medicine Camera", description = "Vision API를 활용한 카메라 약물 인식")
@RestController
@RequestMapping("/v2/medicine-camera")
@RequiredArgsConstructor
public class MedicineCameraController {

    private final MedicineCameraService medicineCameraService;

    @Operation(
            summary = "카메라로 약물 인식",
            description = "이미지를 업로드하면 Vision API를 통해 약물명을 인식하고, 공공데이터 API로 약 정보를 조회합니다."
    )
    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicineCameraResponseDto> recognizeMedicine(@RequestPart("file") MultipartFile file)
            throws IOException, ParseException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        MedicineCameraResponseDto response = medicineCameraService.processImage(file);

        if (response.getMedicineInfo() == null || response.getMedicineInfo().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
}

