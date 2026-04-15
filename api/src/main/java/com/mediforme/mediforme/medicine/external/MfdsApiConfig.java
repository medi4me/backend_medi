package com.mediforme.mediforme.medicine.external;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class MfdsApiConfig {    // 식약처 (국내 약물 조회 용도)

    @Value("${api.mfds.service-url}")
    private String serviceUrl;

    @Value("${api.mfds.service-key}")
    private String serviceKey;
}
