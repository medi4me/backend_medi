package com.mediforme.mediforme.global.code;

import com.mediforme.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "t_common_group_code")
public class CommonGroupCode extends BaseEntity {
    @Id
    @Column(name = "com_code_group_id", length = 50)
    private String comCodeGroupId;              // PK

    @Column(name = "com_code_group_name", length = 100, nullable = false)
    private String comCodeGroupName;

    // 공통 코드 그룹명 변경
    public void changeComCodeGroupName(String comCodeGroupName, Long modifierId) {
        this.comCodeGroupName = comCodeGroupName;
        this.setModifierId(modifierId);
    }
}
