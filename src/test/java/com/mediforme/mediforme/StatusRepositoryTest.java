package com.mediforme.mediforme;

import com.mediforme.mediforme.domain.Status;
import com.mediforme.mediforme.repository.StatusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application-test.yaml")
@EntityScan(basePackageClasses = Status.class)
@EnableJpaRepositories(basePackageClasses = StatusRepository.class)
class StatusRepositoryTest {

    @Autowired
    private StatusRepository statusRepository;

    @Test
    @DisplayName("Status 저장 후 정상적으로 조회되는지 확인")
    void saveAndFindStatus() {

        Status s = Status.builder()
                .userId(10L)
                .defaultStatusCd(1001L)
                .drinkCd(2001L)
                .conditionCd(3001L)
                .statusMemo("테스트")
                .statusDate(LocalDate.of(2025, 11, 10))
                .build();

        Status saved = statusRepository.save(s);
        Optional<Status> found = statusRepository.findById(saved.getStatusId());

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Status 삭제 시 정상적으로 제거되는지 확인")
    void deleteStatus() {

        Status s = Status.builder()
                .userId(22L)
                .defaultStatusCd(1002L)
                .drinkCd(2002L)
                .conditionCd(3002L)
                .statusMemo("삭제 테스트")
                .statusDate(LocalDate.of(2025, 11, 11))
                .build();

        Status saved = statusRepository.save(s);
        statusRepository.delete(saved);

        Optional<Status> found = statusRepository.findById(saved.getStatusId());
        assertThat(found).isEmpty();
    }
}
