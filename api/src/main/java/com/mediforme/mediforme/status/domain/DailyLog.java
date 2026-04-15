package com.mediforme.mediforme.status.domain;

import com.mediforme.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(
        name = "t_daily_log",
        uniqueConstraints = {
                // 한 사용자(user_id)는 하루(log_date)에 단 하나의 일일 로그만 가질 수 있음
                @UniqueConstraint(name = "uq_dailylog_user_date", columnNames = {"user_id", "log_date"})
        }
)
public class DailyLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_log_id")
    private Long dailyLogId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    // FK - User
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // FK - Status
    @Column(name = "status_id", nullable = false)                       // 당일 하루의 상태만 기록
    private Long statusId;

    public void updateStatus(Long newStatusId, Long modifierId) {       // 하루 상태만 갱신 가능
        this.statusId = newStatusId;
        this.setModifierId(modifierId);
    }
}
