package com.mediforme.mediforme.medicine.support;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * OCR 파이프라인 메트릭
 */
@Component
@RequiredArgsConstructor
public class OcrMetrics {

    private final MeterRegistry registry;

    public void recordExtract(Duration duration, Outcome outcome, int textLength) {
        Timer.builder("ocr.extract.duration")
            .tag("outcome", outcome.tag())
            .register(registry)
            .record(duration);

        if (textLength >= 0) {
            DistributionSummary.builder("ocr.extract.text_length")
                .register(registry)
                .record(textLength);
        }
    }

    public enum Outcome {
        SUCCESS, EMPTY, FAILURE;
        public String tag() { return name().toLowerCase(); }
    }
}
