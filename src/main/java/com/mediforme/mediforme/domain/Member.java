package com.mediforme.mediforme.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.mediforme.mediforme.domain.common.BaseEntity;
import com.mediforme.mediforme.domain.enums.MemberConsent;
import com.mediforme.mediforme.domain.enums.MemberStatus;
import com.mediforme.mediforme.domain.mapping.Calendar;
import com.mediforme.mediforme.domain.mapping.UserMedicine;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String memberID;

    @Column(nullable = false, length = 15)
    private String name;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false, length = 40)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15) DEFAULT 'ACTIVE'")
    private MemberStatus status;                // 고객 상태

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15)")
    private MemberConsent consent;              // 사용자 개인 정보 동의 여부

    private LocalDate InactiveDate;             // 비활성화 시간 저장


    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)           // 양방향 매핑
    private List<UserMedicine> UserMedicineList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)           // 양방향 매핑
    private List<Calendar> StatusList = new ArrayList<>();

}
