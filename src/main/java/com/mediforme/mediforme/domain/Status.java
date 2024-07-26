package com.mediforme.mediforme.domain;

import com.mediforme.mediforme.domain.common.BaseEntity;
import com.mediforme.mediforme.domain.enums.StatusCondition;
import com.mediforme.mediforme.domain.enums.StatusDrink;
import com.mediforme.mediforme.domain.enums.StatusStatus;
import com.mediforme.mediforme.domain.mapping.Calendar;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
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
    private StatusCondition condition;          // Good, notBad, bad


    private String memo;        // 상태 메모

    @OneToMany(mappedBy = "status", cascade = CascadeType.ALL)           // 양방향 매핑
    private List<Calendar> StatusList = new ArrayList<>();
}
