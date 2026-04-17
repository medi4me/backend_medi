package com.mediforme.mediforme.search.support;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class SearchMetricsTest {

    private MeterRegistry registry;
    private SearchMetrics metrics;

    @BeforeEach
    void setup() {
        registry = new SimpleMeterRegistry();
        metrics = new SearchMetrics(registry);
    }

    @Test
    @DisplayName("어댑터 호출 기록 — Timer + ResultCount 각 태그로 저장")
    void recordAdapterCall() {
        metrics.recordAdapterCall("MFDS", Duration.ofMillis(120),
            SearchMetrics.Outcome.SUCCESS, 3);

        var timer = registry.find("search.adapter.duration")
            .tag("adapter", "MFDS")
            .tag("outcome", "success")
            .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);

        var summary = registry.find("search.adapter.result_count")
            .tag("adapter", "MFDS")
            .summary();
        assertThat(summary).isNotNull();
        assertThat(summary.count()).isEqualTo(1);
        assertThat(summary.totalAmount()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("타임아웃/실패도 outcome 태그로 구분")
    void outcomesAreTagged() {
        metrics.recordAdapterCall("FDA", Duration.ofSeconds(2),
            SearchMetrics.Outcome.TIMEOUT, 0);
        metrics.recordAdapterCall("RxNorm", Duration.ofMillis(50),
            SearchMetrics.Outcome.FAILURE, 0);

        assertThat(registry.find("search.adapter.duration")
            .tag("adapter", "FDA").tag("outcome", "timeout").timer().count())
            .isEqualTo(1);
        assertThat(registry.find("search.adapter.duration")
            .tag("adapter", "RxNorm").tag("outcome", "failure").timer().count())
            .isEqualTo(1);
    }

    @Test
    @DisplayName("쿼리 총계 — has_results 태그로 분기")
    void recordQuery() {
        metrics.recordQuery(true);
        metrics.recordQuery(false);
        metrics.recordQuery(true);

        assertThat(registry.find("search.query.total")
            .tag("has_results", "true").counter().count())
            .isEqualTo(2.0);
        assertThat(registry.find("search.query.total")
            .tag("has_results", "false").counter().count())
            .isEqualTo(1.0);
    }

    @Test
    @DisplayName("소스 히트 — 어댑터별 집계")
    void recordSourceHit() {
        metrics.recordSourceHit("MFDS");
        metrics.recordSourceHit("MFDS");
        metrics.recordSourceHit("FDA");

        assertThat(registry.find("search.query.sources_hit")
            .tag("adapter", "MFDS").counter().count()).isEqualTo(2.0);
        assertThat(registry.find("search.query.sources_hit")
            .tag("adapter", "FDA").counter().count()).isEqualTo(1.0);
    }
}
