package com.mediforme.mediforme.medicine.support;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class OcrMetricsTest {

    private MeterRegistry registry;
    private OcrMetrics metrics;

    @BeforeEach
    void setup() {
        registry = new SimpleMeterRegistry();
        metrics = new OcrMetrics(registry);
    }

    @Test
    @DisplayName("OCR 추출 기록 — Timer 와 텍스트 길이 분포")
    void recordExtract() {
        metrics.recordExtract(Duration.ofMillis(800),
            OcrMetrics.Outcome.SUCCESS, 150);

        var timer = registry.find("ocr.extract.duration")
            .tag("outcome", "success").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);

        var summary = registry.find("ocr.extract.text_length").summary();
        assertThat(summary).isNotNull();
        assertThat(summary.count()).isEqualTo(1);
        assertThat(summary.totalAmount()).isEqualTo(150.0);
    }

    @Test
    @DisplayName("EMPTY / FAILURE 도 outcome 태그로 구분")
    void outcomesAreTagged() {
        metrics.recordExtract(Duration.ofMillis(100), OcrMetrics.Outcome.EMPTY, 0);
        metrics.recordExtract(Duration.ofMillis(50), OcrMetrics.Outcome.FAILURE, -1);

        assertThat(registry.find("ocr.extract.duration")
            .tag("outcome", "empty").timer().count()).isEqualTo(1);
        assertThat(registry.find("ocr.extract.duration")
            .tag("outcome", "failure").timer().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("textLength < 0 인 경우 분포 기록 스킵")
    void negativeLengthSkipped() {
        metrics.recordExtract(Duration.ofMillis(50), OcrMetrics.Outcome.FAILURE, -1);

        var summary = registry.find("ocr.extract.text_length").summary();
        if (summary != null) {
            assertThat(summary.count()).isEqualTo(0);
        }
    }
}
