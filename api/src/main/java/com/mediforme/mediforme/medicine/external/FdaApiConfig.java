package com.mediforme.mediforme.medicine.external;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class FdaApiConfig {     // 해외 약물, 최신 약물 조회 용도

    @Value("${api.fda.base-url}")
    private String baseUrl;
}
