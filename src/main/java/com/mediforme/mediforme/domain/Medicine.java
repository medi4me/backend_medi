package com.mediforme.mediforme.domain;

import com.mediforme.mediforme.domain.common.BaseEntity;
import com.mediforme.mediforme.domain.mapping.UserMedicine;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Medicine extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, columnDefinition = "Text")
    private String description;             // 약 설명 : info -> description 수정

    @Column(nullable = false, columnDefinition = "Text")
    private String benefit;                 // 약 효능

//    @Column(nullable = false, columnDefinition = "Text")
    private String drugInteraction;         // 약물 상호작용

    @Column(name = "image_url")
    private String itemImage;

//    @Column(nullable = false, length = 50)
//    private String component;               // 성분명


//    private Integer amount;                     // 함량

    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL)           // 양방향 매핑
    private List<UserMedicine> UserMedicineList = new ArrayList<>();
}
