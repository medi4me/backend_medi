package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.dto.object.MedicineRecognitionResultDto;
import com.mediforme.mediforme.dto.response.MedicineCameraResponseDto;
import com.mediforme.mediforme.service.MedicineService;
import com.mediforme.mediforme.service.MedicineCameraService;
import com.mediforme.mediforme.service.MedicineImageRecognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.json.simple.parser.ParseException;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineCameraServiceImpl implements MedicineCameraService {

    private final MedicineService medicineService;
    private final MedicineImageRecognitionService recognitionService;

    // 후보 조회 최대 개수 제한 (외부 API 호출 비용/지연 폭증 방지)
    private static final int MAX_QUERY_CANDIDATES = 5;

    /**
     * 후보별 조회 병렬 처리 스레드 풀
     */
    private static final ExecutorService LOOKUP_EXECUTOR =
        Executors.newFixedThreadPool(4);        // 후보 5개 정도면 4~8이 적당

    /**
     * 후보별 조회 타임아웃
     * - 공공 API 지연으로 전체 요청이 묶이지 않게 보호
     */
    private static final Duration LOOKUP_TIMEOUT = Duration.ofSeconds(3);

    @Override
    public MedicineCameraResponseDto processImage(MultipartFile file) throws IOException, ParseException {
        long startNs = System.nanoTime();

        // OCR 및 후보 리스트 추출 (약봉투/약통/박스 대응)
        MedicineRecognitionResultDto recognition = recognitionService.recognizeMedicineCandidates(file);

        if (recognition == null || recognition.getCandidates() == null || recognition.getCandidates().isEmpty()) {
            return MedicineCameraResponseDto.empty("인식 실패");
        }

        // 후보 정제 (null/blank 제거, 중복 제거, 개수 제한)
        List<String> candidates = recognition.getCandidates().stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .distinct()
            .limit(MAX_QUERY_CANDIDATES)
            .toList();

        if (candidates.isEmpty()) {
            return MedicineCameraResponseDto.empty("인식 실패");
        }

        // 후보별 조회를 병렬 실행 (사용자 체감 속도 개선)
        List<CompletableFuture<CandidateLookupResult>> futures = candidates.stream()
            .map(candidate ->
                CompletableFuture.supplyAsync(() -> lookupCandidate(candidate), LOOKUP_EXECUTOR)
                    // 후보 하나 실패해도 전체는 살림 (실패 격리)
                    .completeOnTimeout(CandidateLookupResult.timeout(candidate), LOOKUP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
                    .exceptionally(ex -> CandidateLookupResult.failed(candidate, ex))
            )
            .toList();

        // 결과 수집 (futures join은 여기서만)
        List<CandidateLookupResult> lookupResults = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        // API 응답용 results 변환 (성공한 것만 내려주되, 대표 선정은 lookupResults로)
        List<MedicineCameraResponseDto.RecognizedMedicineResultDto> results = lookupResults.stream()
            .filter(CandidateLookupResult::isSuccessWithData)
            .map(r -> MedicineCameraResponseDto.RecognizedMedicineResultDto.builder()
                .queryName(r.candidate())
                .medicineInfo(r.medicineInfo())
                .build())
            .toList();

        // 대표 후보 선정 정책
        // - <성공한 결과 중 가장 결과 개수가 많은 후보> 우선
        // - 동률이면 원래 후보 우선순위(인식된 순서)를 따르도록 진행
        CandidateLookupResult representative = chooseRepresentative(lookupResults, candidates);

        String primaryMedicineName = representative != null ? representative.candidate() : null;
        List<MedicineCameraResponseDto.MedicineInfoDto> primaryMedicineInfo =
            representative != null && representative.medicineInfo() != null
                ? representative.medicineInfo()
                : Collections.emptyList();

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        // 운영 관측성(로그)
        // OCR 텍스트 전체는 개인정보/민감정보가 섞일 수 있으니 길이만 기록
        log.info("medicine-recognition: candidates={}, successResults={}, elapsedMs={}, ocrTextLen={}",
            candidates,
            results.size(),
            elapsedMs,
            recognition.getFullText() == null ? 0 : recognition.getFullText().length()
        );

        // 후보별 실패는 warn으로 남겨서 운영에서 추적 가능하도록 함
        lookupResults.stream()
            .filter(r -> !r.isSuccessWithData())
            .forEach(r -> log.warn("medicine-lookup failed: candidate={}, reason={}", r.candidate(), r.reason()));

        // 8) 응답 생성 (기존 필드 하위호환 + 신규 필드 확장)
        return MedicineCameraResponseDto.builder()
            .recognizedText(recognition.getFullText())
            .recognizedMedicine(primaryMedicineName)
            .medicineInfo(primaryMedicineInfo)

            .recognizedMedicines(candidates)
            .results(results)
            .build();
    }

    /**
     * 후보 하나를 공공 API로 조회
     * - 후보 하나가 실패해도 전체는 살리기 위해 예외를 내부에서 흡수하고 결과 객체로 반환
     */
    private CandidateLookupResult lookupCandidate(String candidate) {
        try {
            List<MedicineCameraResponseDto.MedicineInfoDto> info = medicineService.getMedicineInfoSimple(candidate);

            if (info == null || info.isEmpty()) {
                return CandidateLookupResult.noData(candidate);
            }
            return CandidateLookupResult.success(candidate, info);

        } catch (IOException e) {
            // 외부 API 네트워크/타임아웃 등
            return CandidateLookupResult.failed(candidate, e);

        } catch (ParseException e) {
            // 외부 API 응답 파싱 문제
            return CandidateLookupResult.failed(candidate, e);

        } catch (Exception e) {
            // 예상치 못한 예외도 실패 격리
            return CandidateLookupResult.failed(candidate, e);
        }
    }

    /**
     * 대표 후보 선택:
     * 1. 조회 성공 + 결과가 있는 후보 중 "결과 개수"가 가장 많은 후보
     * 2. 동률이면 OCR 후보 순서(candidates 리스트 순서) 우선
     */
    private CandidateLookupResult chooseRepresentative(List<CandidateLookupResult> lookupResults, List<String> candidatesOrder) {
        Map<String, Integer> order = new HashMap<>();
        for (int i = 0; i < candidatesOrder.size(); i++) order.put(candidatesOrder.get(i), i);

        return lookupResults.stream()
            .filter(CandidateLookupResult::isSuccessWithData)
            .sorted((a, b) -> {
                int sizeCompare = Integer.compare(b.dataSize(), a.dataSize());
                if (sizeCompare != 0) return sizeCompare;
                // 후보 순서 우선
                return Integer.compare(order.getOrDefault(a.candidate(), 999), order.getOrDefault(b.candidate(), 999));
            })
            .findFirst()
            .orElse(null);
    }

    /**
     * 후보별 조회 결과를 표현하는 내부 레코드 (운영/디버깅)
     * - 성공/무데이터/타임아웃/실패를 명확히 표현
     */
    private record CandidateLookupResult(
        String candidate,
        Status status,
        List<MedicineCameraResponseDto.MedicineInfoDto> medicineInfo,
        String reason
    ) {
        enum Status { SUCCESS, NO_DATA, TIMEOUT, FAILED }

        static CandidateLookupResult success(String candidate, List<MedicineCameraResponseDto.MedicineInfoDto> info) {
            return new CandidateLookupResult(candidate, Status.SUCCESS, info, "OK");
        }

        static CandidateLookupResult noData(String candidate) {
            return new CandidateLookupResult(candidate, Status.NO_DATA, Collections.emptyList(), "NO_DATA");
        }

        static CandidateLookupResult timeout(String candidate) {
            return new CandidateLookupResult(candidate, Status.TIMEOUT, Collections.emptyList(), "TIMEOUT");
        }

        static CandidateLookupResult failed(String candidate, Throwable ex) {
            String msg = ex == null ? "FAILED" : (ex.getClass().getSimpleName() + ": " + ex.getMessage());
            return new CandidateLookupResult(candidate, Status.FAILED, Collections.emptyList(), msg);
        }

        boolean isSuccessWithData() {
            return status == Status.SUCCESS && medicineInfo != null && !medicineInfo.isEmpty();
        }

        int dataSize() {
            return medicineInfo == null ? 0 : medicineInfo.size();
        }
    }
}
