package com.mediforme.mediforme.medicine.external;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class DurApiConfig {     // 식약처 DUR 병용금기 (data.go.kr 15059486)

    @Value("${api.dur.service-url}")
    private String serviceUrl;

    @Value("${api.dur.service-key}")
    private String serviceKey;
}
