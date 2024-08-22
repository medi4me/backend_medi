package com.mediforme.mediforme.domain;

import com.mediforme.mediforme.domain.common.BaseEntity;
import com.mediforme.mediforme.domain.enums.StatusCondition;
import com.mediforme.mediforme.domain.enums.StatusDrink;
import com.mediforme.mediforme.domain.enums.StatusStatus;
import com.mediforme.mediforme.domain.mapping.Calendar;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "status_table")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Status extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusStatus status;                // Good, notBad, bad

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusDrink drink;                  // drink, noDrink

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusCondition statusCondition;          // Good, notBad, bad


    private String memo;        // 상태 메모

    @OneToMany(mappedBy = "status", cascade = CascadeType.ALL)           // 양방향 매핑
    private List<Calendar> StatusList = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate date; //캘린더

    @Builder
    public Status(StatusStatus status, StatusDrink drink, StatusCondition statusCondition, String memo, LocalDate date) {
        this.status = status;
        this.drink = drink;
        this.statusCondition = statusCondition;
        this.memo = memo;
        this.date = date;
    }

    public void setStatus(StatusStatus status) {
        this.status = status;
    }

    public void setDrink(StatusDrink drink) {
        this.drink = drink;
    }

    public void setStatusCondition(StatusCondition statusCondition) {
        this.statusCondition = statusCondition;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
