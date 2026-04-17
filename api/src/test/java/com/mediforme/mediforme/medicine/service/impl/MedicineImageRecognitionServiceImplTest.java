package com.mediforme.mediforme.medicine.service.impl;

import com.mediforme.mediforme.medicine.dto.MedicineRecognitionResultDto;
import com.mediforme.mediforme.medicine.external.port.OcrPort;
import com.mediforme.mediforme.medicine.support.OcrMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MedicineImageRecognitionServiceImplTest {

    @Mock private OcrPort ocrPort;

    private MeterRegistry registry;
    private MedicineImageRecognitionServiceImpl service;

    @BeforeEach
    void setup() {
        registry = new SimpleMeterRegistry();
        OcrMetrics metrics = new OcrMetrics(registry);
        service = new MedicineImageRecognitionServiceImpl(ocrPort, metrics);
    }

    private MultipartFile sampleImage() {
        return new MockMultipartFile("file", "img.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    @Test
    @DisplayName("빈 이미지는 메트릭 기록 없이 빈 결과")
    void emptyImage_noMetric() {
        MultipartFile empty = new MockMultipartFile("file", new byte[0]);

        MedicineRecognitionResultDto result = service.recognizeMedicineCandidates(empty);

        assertThat(result.getCandidates()).isEmpty();
        assertThat(registry.find("ocr.extract.duration").timer()).isNull();
    }

    @Test
    @DisplayName("OCR 성공 시 SUCCESS outcome 메트릭과 텍스트 길이 기록")
    void success_recordsSuccess() {
        given(ocrPort.extractText(any())).willReturn("타이레놀\n500mg");

        MedicineRecognitionResultDto result = service.recognizeMedicineCandidates(sampleImage());

        assertThat(result.getFullText()).isNotBlank();
        assertThat(registry.find("ocr.extract.duration")
            .tag("outcome", "success").timer().count()).isEqualTo(1);
        assertThat(registry.find("ocr.extract.text_length").summary().totalAmount())
            .isEqualTo("타이레놀\n500mg".length());
    }

    @Test
    @DisplayName("OCR이 null 반환하면 EMPTY outcome 메트릭")
    void nullOcr_recordsEmpty() {
        given(ocrPort.extractText(any())).willReturn(null);

        MedicineRecognitionResultDto result = service.recognizeMedicineCandidates(sampleImage());

        assertThat(result.getCandidates()).isEmpty();
        assertThat(registry.find("ocr.extract.duration")
            .tag("outcome", "empty").timer().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("OCR 빈 문자열도 EMPTY outcome")
    void blankOcr_recordsEmpty() {
        given(ocrPort.extractText(any())).willReturn("   ");

        service.recognizeMedicineCandidates(sampleImage());

        assertThat(registry.find("ocr.extract.duration")
            .tag("outcome", "empty").timer().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("OcrPort 예외 시 FAILURE outcome 메트릭")
    void ocrThrows_recordsFailure() {
        given(ocrPort.extractText(any())).willThrow(new RuntimeException("vision down"));

        MedicineRecognitionResultDto result = service.recognizeMedicineCandidates(sampleImage());

        assertThat(result.getCandidates()).isEmpty();
        assertThat(registry.find("ocr.extract.duration")
            .tag("outcome", "failure").timer().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("recognizeMedicineName은 1순위 후보만 반환")
    void recognizeMedicineName_returnsTopCandidate() {
        given(ocrPort.extractText(any())).willReturn("타이레놀정\n아세트아미노펜");

        String name = service.recognizeMedicineName(sampleImage());

        assertThat(name).isNotNull();
    }
}
