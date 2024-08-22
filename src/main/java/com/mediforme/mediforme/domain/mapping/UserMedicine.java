package com.mediforme.mediforme.domain.mapping;

import com.mediforme.mediforme.domain.Medicine;
import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.domain.common.BaseEntity;
import com.mediforme.mediforme.domain.enums.UserMedicineMeal;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMedicine extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(nullable = false, length = 30)
//    private String day;             // 복용 날짜

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15)")
    private UserMedicineMeal meal;  // 식사 여부 : Meal, NoMeal

    @Column(columnDefinition = "VARCHAR(30)")
    private String time;            // 복용 시간

    @Column(columnDefinition = "VARCHAR(30)")
    private String dosage;          // 약 용량

    private boolean isCheck = false;

    private boolean isAlarm = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;
}
