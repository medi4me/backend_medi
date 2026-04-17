package com.mediforme.mediforme.search.application;

import com.mediforme.mediforme.medicine.dto.MedicineSearchItemDto;
import com.mediforme.mediforme.medicine.dto.MedicineSearchResponseDto;
import com.mediforme.mediforme.search.port.MedicineSearchPort;
import com.mediforme.mediforme.search.support.JaroWinklerSimilarity;
import com.mediforme.mediforme.search.support.SearchMetrics;
import com.mediforme.mediforme.search.support.TextNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 약 검색 유스케이스
 * - 여러 MedicineSearchPort를 병렬 호출 후 결과 병합
 * - 정규화된 이름 기준 중복 제거, 쿼리 유사도 내림차순 정렬
 * - 개별 어댑터 실패/타임아웃은 격리하며 메트릭으로 기록
 */
@Slf4j
@Service
public class MedicineSearchService {

    private static final long TIMEOUT_MS = 2000;

    private final List<MedicineSearchPort> ports;
    private final JaroWinklerSimilarity similarity;
    private final Executor executor;
    private final SearchMetrics metrics;

    public MedicineSearchService(List<MedicineSearchPort> ports,
                                 JaroWinklerSimilarity similarity,
                                 @Qualifier("searchExecutor") Executor executor,
                                 SearchMetrics metrics) {
        this.ports = ports;
        this.similarity = similarity;
        this.executor = executor;
        this.metrics = metrics;
    }

    public MedicineSearchResponseDto searchByName(String name) {
        String normalizedQuery = TextNormalizer.normalize(name);
        log.info("search query name='{}' normalized='{}'", name, normalizedQuery);

        List<CompletableFuture<AdapterResult>> futures = ports.stream()
            .map(port -> callAdapter(port, name))
            .toList();

        Map<String, Scored> best = new LinkedHashMap<>();
        Set<String> adaptersHit = new LinkedHashSet<>();

        for (CompletableFuture<AdapterResult> f : futures) {
            AdapterResult r = f.join();
            if (!r.items.isEmpty()) {
                adaptersHit.add(r.adapterName);
                metrics.recordSourceHit(r.adapterName);
            }
            for (MedicineSearchItemDto item : r.items) {
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

        metrics.recordQuery(!merged.isEmpty());
        log.info("search done count={} adapters_hit={}", merged.size(), adaptersHit);

        return MedicineSearchResponseDto.builder().medicines(merged).build();
    }

    private CompletableFuture<AdapterResult> callAdapter(MedicineSearchPort port, String name) {
        String adapterName = port.name();
        long start = System.nanoTime();

        return CompletableFuture
            .supplyAsync(() -> {
                List<MedicineSearchItemDto> raw = port.searchByName(name);
                List<MedicineSearchItemDto> items = raw != null ? raw : Collections.emptyList();
                Duration duration = Duration.ofNanos(System.nanoTime() - start);
                metrics.recordAdapterCall(adapterName, duration,
                    SearchMetrics.Outcome.SUCCESS, items.size());
                log.debug("adapter {} success count={} duration={}ms",
                    adapterName, items.size(), duration.toMillis());
                return new AdapterResult(adapterName, items);
            }, executor)
            .orTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .exceptionally(ex -> {
                Duration duration = Duration.ofNanos(System.nanoTime() - start);
                SearchMetrics.Outcome outcome = isTimeout(ex)
                    ? SearchMetrics.Outcome.TIMEOUT
                    : SearchMetrics.Outcome.FAILURE;
                metrics.recordAdapterCall(adapterName, duration, outcome, 0);
                log.warn("adapter {} {} duration={}ms: {}",
                    adapterName, outcome.tag(), duration.toMillis(), ex.getMessage());
                return new AdapterResult(adapterName, Collections.emptyList());
            });
    }

    private boolean isTimeout(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof TimeoutException) return true;
            t = t.getCause();
        }
        return false;
    }

    private record AdapterResult(String adapterName, List<MedicineSearchItemDto> items) {}
    private record Scored(MedicineSearchItemDto item, double score) {}
}
