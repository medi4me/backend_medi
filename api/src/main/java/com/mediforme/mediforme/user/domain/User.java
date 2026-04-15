package com.mediforme.mediforme.user.domain;

import com.mediforme.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "t_user")
public class User extends BaseEntity {
    // 내부 시스템 식별용 PK (AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_login_id", length = 30, unique = true, nullable = false)
    private String userLoginId; // 로그인용 Id (사용자 입력값)

    @Column(name = "user_name", length = 30, nullable = false)
    private String userName;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "phone", length = 15, nullable = false)
    private String phone;

    // 공통 코드 참조 (조인 없이 id만 저장)
    @Column(name = "role_cd", nullable = false)
    private Long roleCd;   // 역할 코드 (1001=USER, 1002=ADMIN)

    @Column(name = "consent_cd", nullable = false)
    private Long consentCd;

    @Column(name = "status_cd", nullable = false)
    private Long statusCd;



    public void updateStatus(Long statusCd, Long modifier_id){
        this.statusCd = statusCd;
        this.setModifierId(modifier_id);
    }
    public void updateConsent(Long consentCd, Long modifier_id){
        this.consentCd = consentCd;
        this.setModifierId(modifier_id);
    }
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public boolean isResigned() {
        return this.statusCd != null && this.statusCd.equals(9999L);
    }

}
