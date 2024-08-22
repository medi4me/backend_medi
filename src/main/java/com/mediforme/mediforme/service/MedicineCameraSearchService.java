package com.mediforme.mediforme.service;

import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Image;
import com.google.protobuf.ByteString;
import com.mediforme.mediforme.dto.OnboardingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




@Service
public class MedicineCameraSearchService {
    private static final Logger logger = LoggerFactory.getLogger(MedicineCameraSearchService.class);

    private final MedicineService medicineService;

    @Autowired
    public MedicineCameraSearchService(MedicineService medicineService) {
        this.medicineService = medicineService;
    }


    // 이미지 처리 및 약물 이름 인식
    public OnboardingDto.OnboardingResponseDto processImageAndRecognizeMedicine(MultipartFile file) throws IOException, ParseException {
        try {
            // GCP Vision API를 사용하여 이미지에서 알약 모양 -> 약통 인식
            String recognizedMedicineName = recognizePillFromImage(file);

            if ("Medicine not recognized".equals(recognizedMedicineName)) {
                return OnboardingDto.OnboardingResponseDto.builder()
                        .medicines(new ArrayList<>())
                        .build();
            }

            try {
                return medicineService.getMedicineInfoByName(recognizedMedicineName);
            } catch (org.json.simple.parser.ParseException e) {
                logger.error("약물 정보 파싱 실패", e);
                return OnboardingDto.OnboardingResponseDto.builder()
                        .medicines(new ArrayList<>())
                        .build();
            }

        } catch (IOException e) {
            logger.error("이미지 처리 및 약물 인식 실패", e);
            return OnboardingDto.OnboardingResponseDto.builder()
                    .medicines(new ArrayList<>())
                    .build();
        }
    }

    // 약물 이미지를 통한 약물->약(통) 인식
    private String recognizePillFromImage(MultipartFile file) throws IOException {
        ByteString imgBytes = ByteString.copyFrom(file.getBytes());
        Image img = Image.newBuilder().setContent(imgBytes).build();

        /*
        // 알약(경구약제)-알약 자체 인식 로직
        // Vision API에서 Object Localization을 위한 기능 설정
        Feature feat = Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();
        List<AnnotateImageRequest> requests = new ArrayList<>();
        requests.add(request);*/

        // 약 객체 인식
        // Vision API에서 Text/Logo/Object Detection을 위한 기능 설정
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
            AnnotateImageResponse response = client.batchAnnotateImages(List.of(request)).getResponsesList().get(0);

            if (response.hasError()) {
                throw new IOException("Error: " + response.getError().getMessage());
            }

            // 텍스트에서 약물 이름을 추출
            return extractMedicineName(response);


            /*
            // 인식된 알약 모양을 분석하고 관련 정보를 반환 (알약 자체 인식)
            for (LocalizedObjectAnnotation annotation : response.getLocalizedObjectAnnotationsList()) {
                System.out.println("Detected object: " + annotation.getName());         // 인식된 모든 객체의 이름을 로그로 출력
                if ("Pill".equalsIgnoreCase(annotation.getName()) || "Tablet".equalsIgnoreCase(annotation.getName()) || "Medicine".equalsIgnoreCase(annotation.getName())) {
                    return annotation.getName();  // 알약의 이름 반환
                }
            }*/
            //return "Medicine  not recognized";          // 약물 인식 실패
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }



    // 객체 인식을 통해 약을 인식( - 알약 경구약제 데이터 인식)
    private String detectMedicineObject(AnnotateImageResponse response) {
        for (LocalizedObjectAnnotation annotation : response.getLocalizedObjectAnnotationsList()) {
            System.out.println("Detected object: " + annotation.getName());
            if ("Pill".equalsIgnoreCase(annotation.getName()) ||
                    "Tablet".equalsIgnoreCase(annotation.getName()) ||
                    "Medicine".equalsIgnoreCase(annotation.getName())) {
                return annotation.getName(); // 객체에서 약물 인식됨
            }
        }
        return null; // 약물을 인식하지 못함
    }




    // 인식된 텍스트에서 약물 이름을 추출하는 메서드
    private String extractMedicineName(AnnotateImageResponse response) throws IOException, ParseException{
        if (!response.getTextAnnotationsList().isEmpty()) {
            // 인식된 텍스트의 첫 번째 항목이 가장 중요한 텍스트일 가능성이 높음
            String recognizedText = response.getTextAnnotationsList().get(0).getDescription();

            // 로그로 인식된 전체 텍스트 출력
            logger.info("Recognized Text: {}", recognizedText);

            // 각 줄을 순회하며 데이터베이스에서 일치하는 약물 이름인지 확인
            String[] textLines = recognizedText.split("\n");
            for (String line : textLines) {
                String trimmedLine = line != null ? line.trim() : "";
                if (!trimmedLine.isEmpty()) {
                    logger.info("텍스트에 해당하는 알약 존재 확인: {}", trimmedLine);
                    try {
                        OnboardingDto.OnboardingResponseDto medicineInfo = medicineService.getMedicineInfoByName(trimmedLine);
                        if (medicineInfo != null && !medicineInfo.getMedicines().isEmpty()) {
                            return trimmedLine;
                        }
                    } catch (IOException | org.json.simple.parser.ParseException e) {
                        logger.error("약물 정보 가져오는 중 에러 발생: ", e);
                    }
                }
            }
        }
        return "약물 정보를 찾을 수 없음";
    }
}
