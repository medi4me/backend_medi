package com.mediforme.mediforme.config;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ApiConfig {

    @Value("${api.drug.service-url}")
    private String serviceUrl;

    @Value("${api.drug.service-key}")
    private String serviceKey;
}
