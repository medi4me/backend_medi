package com.mediforme.mediforme;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

import com.mediforme.mediforme.domain.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
public class StatusEntityTest {
    @Test
    @DisplayName("엔티티 단위 테스트")
    void updateStatus_updatesAllFields() {
        Status s = Status.builder()
                .statusId(1L)
                .userId(10L)
                .defaultStatusCd(1001L)
                .drinkCd(0L)
                .conditionCd(0L)
                .statusMemo("old")
                .statusDate(LocalDate.of(2025, 11, 10))
                .build();

        s.updateStatus(1003L, 2001L, 3002L, "새 메모", LocalDate.of(2025, 11, 10), 99L);

        assertAll(
                () -> assertEquals(1003L, s.getDefaultStatusCd()),
                () -> assertEquals(2001l, s.getDrinkCd()),
                () -> assertEquals(3002L, s.getConditionCd()),
                () -> assertEquals("새 메모", s.getStatusMemo()),
                () -> assertEquals(LocalDate.of(2025, 11, 10), s.getStatusDate()),
                () -> assertEquals(99L, s.getModifierId())

        );
    }


    }
