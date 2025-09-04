package com.mediforme.mediforme.domain.code;

import com.mediforme.mediforme.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "t_common_code")
public class CommonCode extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "com_code_id")
    private Long comCodeId;

    @Column(name = "com_code_group_cd", length = 50, nullable = false)
    private String comCodeGroupCd;

    @Column(name = "com_code_cd", length = 50, nullable = false)
    private String comCodeCd;

    @Column(name = "com_code_name", length = 50, nullable = false)
    private String comCodeName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "usage_yn", nullable = false)
    private Boolean usageYn;

    @Column(name = "attribute1", length = 100)
    private String attribute1;

    @Column(name = "attribute2", length = 100)
    private String attribute2;

    @Column(name = "attribute3", length = 100)
    private String attribute3;

    // 코드명 변경
    public void updateName(String comCodeName, Long modifier_id) {
        this.comCodeName = comCodeName;
        this.setModifierId(modifier_id);
    }
    // 코드 비활성화
    public void disable(Long modifier_id) {
        this.usageYn = false;
        this.setModifierId(modifier_id);
    }
    // 코드 활성화
    public void enable(Long modifier_id) {
        this.usageYn = true;
        this.setModifierId(modifier_id);
    }

}
