package com.mediforme.mediforme.global.advice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RestControllerAdviceRegistrationTest {

    private static final String SCAN_BASE_PACKAGE = "com.mediforme.mediforme";

    @Test
    void shouldHaveExactlyOneRestControllerAdvice() {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestControllerAdvice.class));

        Set<BeanDefinition> advices = scanner.findCandidateComponents(SCAN_BASE_PACKAGE);

        assertEquals(
            1,
            advices.size(),
            () -> "오직 하나의 @RestControllerAdvice 만 존재해야 함. 발견: "
                + advices.stream()
                    .map(BeanDefinition::getBeanClassName)
                    .collect(Collectors.joining(", "))
        );
    }
}
