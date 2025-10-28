package com.mediforme.mediforme.service.impl;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.mediforme.mediforme.service.MedicineImageRecognitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
@Slf4j
@Service
public class MedicineImageRecognitionServiceImpl  implements MedicineImageRecognitionService {
    // 비전 모델을 통한 약물명 인식
    @Override
    public String recognizeMedicineName(MultipartFile imageFile) {
        try {
            ByteString imgBytes = ByteString.copyFrom(imageFile.getBytes());
            Image img = Image.newBuilder().setContent(imgBytes).build();

            List<Feature> features = List.of(
                    Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build(),
                    Feature.newBuilder().setType(Feature.Type.LOGO_DETECTION).build(),
                    Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION).build()
            );

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addAllFeatures(features)
                    .setImage(img)
                    .build();

            try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                AnnotateImageResponse response =
                        client.batchAnnotateImages(List.of(request)).getResponsesList().get(0);

                if (response.hasError()) {
                    log.error("GCP Vision API 오류: {}", response.getError().getMessage());
                    return null;
                }

                // 인식된 텍스트 추출
                return extractText(response);
            }

        } catch (IOException e) {
            log.error("Vision API 이미지 처리 오류", e);
            return null;
        }
    }

    private String extractText(AnnotateImageResponse response) {
        if (!response.getTextAnnotationsList().isEmpty()) {
            String recognizedText = response.getTextAnnotationsList().get(0).getDescription();
            log.info("인식 텍스트 전체: {}", recognizedText);

            for (String line : recognizedText.split("\n")) {
                String trimmed = line.trim();
                if (trimmed.length() > 1) {
                    log.info("후보 약물명: {}", trimmed);
                    return trimmed; // 첫 번째 후보 반환
                }
            }
        }
        return null;
    }
}
