package com.mediforme.mediforme.search.support;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 검색 파이프라인 메트릭
 */
@Component
@RequiredArgsConstructor
public class SearchMetrics {

    private final MeterRegistry registry;

    public void recordAdapterCall(String adapter, Duration duration, Outcome outcome, int resultCount) {
        Timer.builder("search.adapter.duration")
            .tag("adapter", adapter)
            .tag("outcome", outcome.tag())
            .register(registry)
            .record(duration);

        DistributionSummary.builder("search.adapter.result_count")
            .tag("adapter", adapter)
            .register(registry)
            .record(resultCount);
    }

    public void recordQuery(boolean hasResults) {
        registry.counter("search.query.total",
            "has_results", String.valueOf(hasResults)).increment();
    }

    public void recordSourceHit(String adapter) {
        registry.counter("search.query.sources_hit", "adapter", adapter).increment();
    }

    public enum Outcome {
        SUCCESS, FAILURE, TIMEOUT;
        public String tag() { return name().toLowerCase(); }
    }
}
