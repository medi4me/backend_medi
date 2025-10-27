package com.mediforme.mediforme.domain;

import com.mediforme.mediforme.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "t_user_medicine")
public class UserMedicine extends BaseEntity {
    @Id
    @Column(name = "user_medicine_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userMedicineId;

    @Column(name = "dosage", length = 30)       // 약 용량
    private String dosage;

    // 공통코드 참조
    @Column(name = "meal_cd")                   // 식사 여부
    private Long mealCd;

    @Column(name = "time_cd")                   // 아침, 점심, 저녁
    private Long timeCd;

    @Column(name = "days_of_week_cd")           // 요일 (mon, wed, fri)
    private Long daysOfWeekCd;

    @Column(name = "is_alarm")
    private Boolean isAlarm;

    // FK - User, Medicine
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    public void updateDosage(String newDosage, Long modifierId) {
        this.dosage = newDosage;
        this.setModifierId(modifierId);
    }
    public void changeAlarm(Boolean alarmYn, Long modifierId) {
        this.isAlarm = alarmYn;
        this.setModifierId(modifierId);
    }
    public void updateMeal(Long newMealCd, Long modifierId) {
        this.mealCd = newMealCd;
        this.setModifierId(modifierId);
    }
    public void updateTime(Long newTimeCd, Long modifierId) {
        this.timeCd = newTimeCd;
        this.setModifierId(modifierId);
    }
    public void updateDaysOfWeek(Long newDaysOfWeekCd, Long modifierId) {
        this.daysOfWeekCd = newDaysOfWeekCd;
        this.setModifierId(modifierId);
    }
}
