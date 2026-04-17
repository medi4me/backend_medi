package com.mediforme.mediforme.search.application;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import com.mediforme.mediforme.medicine.dto.MedicineSearchResponseDto;
import com.mediforme.mediforme.search.port.MedicineSearchPort;
import com.mediforme.mediforme.search.support.JaroWinklerSimilarity;
import com.mediforme.mediforme.search.support.TextNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 약 검색 유스케이스
 * - 여러 MedicineSearchPort를 병렬 호출 후 결과 병합
 * - 정규화된 이름 기준 중복 제거, 쿼리 유사도 내림차순 정렬
 * - 개별 어댑터 실패/타임아웃은 격리
 */
@Slf4j
@Service
public class MedicineSearchService {

    private static final long TIMEOUT_MS = 2000;

    private final List<MedicineSearchPort> ports;
    private final JaroWinklerSimilarity similarity;
    private final Executor executor;

    public MedicineSearchService(List<MedicineSearchPort> ports,
                                 JaroWinklerSimilarity similarity,
                                 @Qualifier("searchExecutor") Executor executor) {
        this.ports = ports;
        this.similarity = similarity;
        this.executor = executor;
    }

    public MedicineSearchResponseDto searchByName(String name) {
        String normalizedQuery = TextNormalizer.normalize(name);

        List<CompletableFuture<List<MedicineSearchItemDto>>> futures = ports.stream()
            .map(port -> CompletableFuture
                .supplyAsync(() -> safeSearch(port, name), executor)
                .orTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .exceptionally(ex -> {
                    log.warn("adapter {} timed out or failed: {}",
                        port.getClass().getSimpleName(), ex.getMessage());
                    return Collections.emptyList();
                }))
            .toList();

        Map<String, Scored> best = new LinkedHashMap<>();
        for (CompletableFuture<List<MedicineSearchItemDto>> f : futures) {
            for (MedicineSearchItemDto item : f.join()) {
                String key = TextNormalizer.normalize(item.getName());
                if (key.isEmpty()) continue;
                double score = similarity.score(normalizedQuery, key);
                best.merge(key, new Scored(item, score),
                    (a, b) -> a.score >= b.score ? a : b);
            }
        }

        List<MedicineSearchItemDto> merged = best.values().stream()
            .sorted(Comparator.comparingDouble((Scored s) -> s.score).reversed())
            .map(s -> s.item)
            .toList();

        return MedicineSearchResponseDto.builder().medicines(merged).build();
    }

    private List<MedicineSearchItemDto> safeSearch(MedicineSearchPort port, String name) {
        try {
            List<MedicineSearchItemDto> r = port.searchByName(name);
            return r != null ? r : Collections.emptyList();
        } catch (Exception e) {
            log.warn("adapter {} threw: {}", port.getClass().getSimpleName(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private record Scored(MedicineSearchItemDto item, double score) {}
}
