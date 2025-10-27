package com.mediforme.mediforme.domain;

import com.mediforme.mediforme.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "t_medicine_intake_log")
public class MedicineIntakeLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "intake_id")
    private Long intakeId;

    @Column(name = "intake_date", nullable = false)
    private LocalDate intakeDate;

    @Column(name = "intake_time", nullable = false)
    private LocalTime intakeTime;

    @Column(name = "is_taken", nullable = false)
    private Boolean isTaken;

    // FK - user_medicine
    @Column(name = "user_medicine_id", nullable = false)
    private Long userMedicineId;

    // 복용 여부 변경
    public void markTaken(Boolean taken, Long modifierId) {
        this.isTaken = taken;
        this.setModifierId(modifierId);
    }
    // 날짜/시간 업데이트
    public void updateSchedule(LocalDate date, LocalTime time, Long modifierId) {
        this.intakeDate = date;
        this.intakeTime = time;
        this.setModifierId(modifierId);
    }
}
