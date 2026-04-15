package com.mediforme.mediforme.domain;

import com.mediforme.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "t_status")
public class Status extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Long statusId;

    // 공통코드 참조
    @Column(name = "default_status_cd", nullable = false)       // 기본 상태 (좋음, 보통, 나쁨)
    private Long defaultStatusCd;

    @Column(name = "drink_cd", nullable = false)
    private Long drinkCd;

    @Column(name = "condition_cd", nullable = false)            // 피곤, 상쾌 등
    private Long conditionCd;

    @Column(name = "status_memo", columnDefinition = "TEXT")
    private String statusMemo;

    @Column(name = "status_date", nullable = false)
    private LocalDate statusDate;

    // FK - User
    @Column(name = "user_id", nullable = false)
    private Long userId;

    public void updateStatus(Long defaultStatusCd, Long drinkCd, Long conditionCd, String memo, LocalDate statusDate, Long modifierId) {
        this.defaultStatusCd = defaultStatusCd;
        this.drinkCd = drinkCd;
        this.conditionCd = conditionCd;
        this.statusMemo = memo;
        this.statusDate = statusDate;
        this.setModifierId(modifierId);
    }
}
