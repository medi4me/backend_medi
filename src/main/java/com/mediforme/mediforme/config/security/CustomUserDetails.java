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

    @Override
    public boolean isAccountNonLocked() {
        return true;        // statusCd 값으로 제어
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // statusCd 로직으로 제어 (2001=활성, 2002=비활성)
        return true;
    }
}

