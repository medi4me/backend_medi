package com.mediforme.mediforme.config.api;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RxNormApiConfig {      // RxNorm

    @Value("${api.rxnorm.base-url}")
    private String baseUrl;
}