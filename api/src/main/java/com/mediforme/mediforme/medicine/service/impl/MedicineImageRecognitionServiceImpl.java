package com.mediforme.mediforme.medicine.service.impl;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.mediforme.mediforme.medicine.dto.MedicineRecognitionResultDto;
import com.mediforme.mediforme.medicine.service.MedicineImageRecognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineImageRecognitionServiceImpl  implements MedicineImageRecognitionService {

    // client는 Bean으로 주입 받아 재사용
    private final ImageAnnotatorClient imageAnnotatorClient;

    // 후보 최대 개수 (외부 API 호출 폭증 방지)
    private static final int MAX_CANDIDATES = 5;


    // 약봉투/약통/처방전에서 흔히 나오는 불필요 단어(후보 제거)
    private static final Set<String> STOPWORDS = Set.of(
        "처방", "조제", "복용", "용법", "용량", "주의", "환자", "의사", "약국",
        "성분", "효능", "보관", "유통기한", "일", "회", "아침", "점심", "저녁",
        "정", "캡슐", "시럽", "연고", "외용", "밀리그람"
    );

    // 용량/단위 제거(500mg, 160밀리그램 등)
    private static final Pattern DOSAGE_PATTERN =
        Pattern.compile("(?i)\\b\\d+(\\.\\d+)?\\s*(mg|g|ml|mcg|ug|밀리그램|그램|밀리리터)\\b");

    // 괄호 제거(성분명 등이 들어가는 경우가 많음)
    private static final Pattern PAREN_PATTERN =
        Pattern.compile("\\([^)]*\\)");


    /**
     * 가장 유력한 후보 1개만 반환
     * @param imageFile
     * @return
     */
    @Override
    public String recognizeMedicineName(MultipartFile imageFile) {
        // 후보 중 1순위만 반환
        MedicineRecognitionResultDto result = recognizeMedicineCandidates(imageFile);
        if (result == null || result.getCandidates() == null || result.getCandidates().isEmpty()) {
            return null;
        }
        return result.getCandidates().get(0);
    }

    /**
     * 후보 여러 개인 경우, OCR 전체 텍스트 반환
     * @param imageFile
     * @return
     */
    @Override
    public MedicineRecognitionResultDto recognizeMedicineCandidates(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return MedicineRecognitionResultDto.builder()
                .fullText(null)
                .candidates(Collections.emptyList())
                .build();
        }

        try {
            ByteString imgBytes = ByteString.copyFrom(imageFile.getBytes());
            Image img = Image.newBuilder().setContent(imgBytes).build();

            // TEXT_DETECTION을 우선 적용
            List<Feature> features = List.of(
                Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build()
            );

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addAllFeatures(features)
                .setImage(img)
                .build();

            BatchAnnotateImagesResponse batch = imageAnnotatorClient.batchAnnotateImages(List.of(request));
            AnnotateImageResponse response = batch.getResponsesList().get(0);

            if (response.hasError()) {
                log.warn("GCP Vision error: {}", response.getError().getMessage());
                return MedicineRecognitionResultDto.builder()
                    .fullText(null)
                    .candidates(Collections.emptyList())
                    .build();
            }

            String fullText = extractFullText(response);
            List<String> candidates = extractCandidates(fullText);

            return MedicineRecognitionResultDto.builder()
                .fullText(fullText)
                .candidates(candidates)
                .build();

        } catch (IOException e) {
            log.warn("Vision image read failed", e);
            return MedicineRecognitionResultDto.builder()
                .fullText(null)
                .candidates(Collections.emptyList())
                .build();
        } catch (Exception e) {
            log.warn("Vision recognition failed", e);
            return MedicineRecognitionResultDto.builder()
                .fullText(null)
                .candidates(Collections.emptyList())
                .build();
        }
    }

    private String extractFullText(AnnotateImageResponse response) {
        if (response.getTextAnnotationsList() == null || response.getTextAnnotationsList().isEmpty()) {
            return null;
        }
        // 보통 첫 번째 element가 전체 텍스트를 담는 케이스가 많음
        return response.getTextAnnotationsList().get(0).getDescription();
    }

    /**
     * OCR 텍스트에서 약 이름 후보를 여러 개 추출
     * - 라인 기준 후보 + 토큰 기준 후보를 결합
     * - 불필요 문구/용량 제거 후 score로 정렬
     */
    private List<String> extractCandidates(String fullText) {
        if (fullText == null || fullText.isBlank()) return Collections.emptyList();

        String normalized = Normalizer.normalize(fullText, Normalizer.Form.NFKC);

        List<String> raw = new ArrayList<>();

        // 1) 라인 단위(약 이름이 한 줄로 적히는 경우 많음)
        for (String line : normalized.split("\\R")) {
            String cleaned = clean(line);
            if (isCandidate(cleaned)) raw.add(cleaned);
        }

        // 2) 토큰 단위(분절된 경우)
        for (String token : normalized.split("[\\s,;:/\\\\|]+")) {
            String cleaned = clean(token);
            if (isCandidate(cleaned)) raw.add(cleaned);
        }

        // 중복 제거 및 스코어링 정렬
        Map<String, Integer> scored = new HashMap<>();
        for (String t : raw) {
            scored.put(t, Math.max(scored.getOrDefault(t, 0), score(t)));
        }

        return scored.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .map(Map.Entry::getKey)
            .distinct()
            .limit(MAX_CANDIDATES)
            .collect(Collectors.toList());
    }

    private String clean(String token) {
        if (token == null) return null;

        String t = token.trim();
        if (t.isBlank()) return t;

        // 괄호 제거(성분명/부가정보가 붙는 경우)
        t = PAREN_PATTERN.matcher(t).replaceAll("").trim();

        // 용량 제거
        t = DOSAGE_PATTERN.matcher(t).replaceAll("").trim();

        // 불필요 기호 제거
        t = t.replaceAll("[\\[\\]{}<>\"'`~!@#$%^&*_+=?]", "").trim();

        // 너무 긴 문장은 후보로 부적합(폭발 방지)
        if (t.length() > 40) t = t.substring(0, 40);

        return t;
    }

    private boolean isCandidate(String token) {
        if (token == null) return false;

        String t = token.trim();
        if (t.isBlank()) return false;
        if (t.length() < 2) return false;

        // 숫자만 제거
        if (t.matches("\\d+")) return false;

        // 한글/영문이 있어야 함
        if (!t.matches(".*[가-힣A-Za-z].*")) return false;

        // 불필요 단어 제거(완전 일치만)
        if (STOPWORDS.contains(t)) return false;

        return true;
    }

    /**
     * 간단 스코어링:
     * - 약품명 suffix(정/캡슐/서방정 등) 포함 시 가산
     * - 한글 포함 가산
     */
    private int score(String token) {
        if (token == null) return 0;

        int s = 0;
        if (token.matches(".*[가-힣].*")) s += 2;

        if (token.endsWith("정")) s += 3;
        if (token.contains("서방")) s += 2;
        if (token.contains("캡슐")) s += 2;
        if (token.contains("현탁")) s += 2;
        if (token.contains("시럽")) s += 2;

        if (token.length() > 25) s -= 1; // 너무 길면 약 이름이 아닐 확률 증가

        return s;
    }
}