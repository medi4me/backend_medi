package com.mediforme.mediforme.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ApiConfig {

    @Value("${spring.api.drug.service-url}")
    private String SERVICE_URL;

    @Value("${spring.api.drug.service-key}")
    private String SERVICE_KEY;
}
