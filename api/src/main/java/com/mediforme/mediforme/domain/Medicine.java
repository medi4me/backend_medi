package com.mediforme.mediforme.domain;

import com.mediforme.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "t_medicine")
public class Medicine extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private Long medicineId;

    @Column(name = "medicine_name", length = 30, nullable = false)
    private String medicineName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "medicine_benefit", columnDefinition = "TEXT")
    private String medicineBenefit;

    @Column(name = "medicine_component", length = 50)
    private String medicineComponent;

    @Column(name = "drug_interaction", columnDefinition = "TEXT")
    private String drugInteraction;

    @Column(name = "medicine_amount")
    private Integer medicineAmount;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    public void updateDescription(String newDescription, Long modifierId) {
        this.description = newDescription;
        this.setModifierId(modifierId);
    }
    public void updateBenefit(String newBenefit, Long modifierId) {
        this.medicineBenefit = newBenefit;
        this.setModifierId(modifierId);
    }
    public void updateAmount(Integer newAmount, Long modifierId) {
        this.medicineAmount = newAmount;
        this.setModifierId(modifierId);
    }
    public void updateImage(String newImageUrl, Long modifierId) {
        this.imageUrl = newImageUrl;
        this.setModifierId(modifierId);
    }
}
