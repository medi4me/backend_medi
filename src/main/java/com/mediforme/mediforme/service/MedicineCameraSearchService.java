package com.mediforme.mediforme.service;

import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Image;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



@Service
public class MedicineCameraSearchService {

    private final MedicineService medicineService;

    @Autowired
    public MedicineCameraSearchService(MedicineService medicineService) {
        this.medicineService = medicineService;
    }


    // 이미지 처리 및 약물 이름 인식
    public String processImageAndRecognizeMedicine(MultipartFile file) throws IOException{
        // GCP Vision API를 사용하여 이미지에서 알약 모양 인식
        String recognizedMedicineName = recognizePillFromImage(file);

        if ("Pill not recognized".equals(recognizedMedicineName)) {
            return recognizedMedicineName;
        }

        // 인식된 알약 이름을 기반으로 약물 정보를 조회하여 반환
        return medicineService.getMedicineInfoByName(recognizedMedicineName);

    }

    // 약물 이미지를 통한 약물 인식
    private String recognizePillFromImage(MultipartFile file) throws IOException {
        ByteString imgBytes = ByteString.copyFrom(file.getBytes());
        Image img = Image.newBuilder().setContent(imgBytes).build();

        // Vision API에서 Object Localization을 위한 기능 설정
        Feature feat = Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();
        List<AnnotateImageRequest> requests = new ArrayList<>();
        requests.add(request);

        // Vision API 호출
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            AnnotateImageResponse response = client.batchAnnotateImages(requests).getResponsesList().get(0);
            if (response.hasError()) {
                throw new IOException("Error: " + response.getError().getMessage());
            }

            // 인식된 알약 모양을 분석하고 관련 정보를 반환
            for (LocalizedObjectAnnotation annotation : response.getLocalizedObjectAnnotationsList()) {
                if ("Pill".equalsIgnoreCase(annotation.getName())) {
                    return annotation.getName();  // 알약의 이름을 반환
                }
            }

            return "Pill not recognized";
        }
    }
}
