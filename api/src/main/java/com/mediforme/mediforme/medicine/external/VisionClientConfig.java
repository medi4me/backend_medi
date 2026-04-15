package com.mediforme.mediforme.medicine.external;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
public class VisionClientConfig {

    /**
     * GCP Vision API Client
     *
     * - ImageAnnotatorClient는 thread-safe
     * - 매 요청마다 생성하지 않고 Singleton Bean으로 재사용
     * - 애플리케이션 종료 시 Spring이 close() 호출
     */
    @Bean(destroyMethod = "close")
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        log.info("Initializing GCP Vision ImageAnnotatorClient");
        return ImageAnnotatorClient.create();
    }
}
