package com.mediforme.mediforme.global.security.jwt;

import com.mediforme.mediforme.global.security.CustomUserDetails;
import com.mediforme.mediforme.user.domain.User;
import com.mediforme.mediforme.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("정상 사용자 조회 → CustomUserDetails 로 래핑하고 필드 매핑 정확")
    void loadUserByUsername_existingUser_returnsCustomUserDetails() {
        User user = User.builder()
            .userId(42L)
            .userLoginId("user01")
            .userName("홍길동")
            .password("hashed-pw")
            .phone("010-1111-2222")
            .roleCd(1001L)
            .consentCd(1L)
            .statusCd(2001L)
            .build();
        given(userRepository.findByUserLoginId("user01")).willReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("user01");

        assertThat(details).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails cud = (CustomUserDetails) details;
        assertThat(cud.getUserId()).isEqualTo(42L);
        assertThat(cud.getUsername()).isEqualTo("user01");
        assertThat(cud.getPassword()).isEqualTo("hashed-pw");
        assertThat(cud.getStatusCd()).isEqualTo(2001L);
        assertThat(cud.isEnabled()).isTrue();
        assertThat(cud.getAuthorities())
            .extracting(a -> a.getAuthority())
            .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 → UsernameNotFoundException")
    void loadUserByUsername_nonExisting_throwsUsernameNotFound() {
        given(userRepository.findByUserLoginId("ghost")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("User not found");
    }
}
