package com.mediforme.mediforme.config.security;

import com.mediforme.mediforme.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Schema(description = "회원 Principal 사용 용도")
public class CustomUserDetails implements UserDetails {
    private static final Long STATUS_ACTIVE = 2001L;
    private static final Long STATUS_INACTIVE = 2002L;
    private static final Long STATUS_RESIGNED = 9999L;

    private Long userId;
    private String userLoginId;
    private String userName;
    private String password;
    private String phone;
    private Long roleCd;
    private Long consentCd;
    private Long statusCd;

    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.userId = user.getUserId();
        this.userLoginId = user.getUserLoginId();
        this.password = user.getPassword();
        this.userName = user.getUserName();
        this.phone = user.getPhone();
        this.roleCd = user.getRoleCd();
        this.statusCd = user.getStatusCd();

        // 공통코드 기반 ROLE 매핑
        String role = mapRole(user.getRoleCd());
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private String mapRole(Long roleCd) {
        return switch (roleCd.intValue()) {
            case 1002 -> "ADMIN";
            case 1003 -> "MANAGER";
            default -> "USER";
        };
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userLoginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }


    /**
     * 계정 잠금/정지 개념이 있으면 여기서 제어
     * - STATUS_INACTIVE(비활성): 잠금 처리
     *  - 탈퇴(9999)도 잠금으로 처리
     */
    @Override
    public boolean isAccountNonLocked() {
        if (statusCd == null) return false;
        return !STATUS_INACTIVE.equals(statusCd) && !STATUS_RESIGNED.equals(statusCd);        // statusCd 값으로 제어
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 탈퇴(9999)면 로그인/ 인증 불가
        if (statusCd == null) return false;
        // 활성만 true
        return STATUS_ACTIVE.equals(statusCd);
    }
}

