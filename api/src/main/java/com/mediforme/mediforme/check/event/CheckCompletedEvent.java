package com.mediforme.mediforme.check.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 약물 상호작용 검사가 완료되었을 때 발행되는 Spring ApplicationEvent
 */
@Getter
@RequiredArgsConstructor
public class CheckCompletedEvent {

    private final Long userId;
    private final String newMedication;
    private final List<String> warnings;
    private final Instant occurredAt;

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
}
