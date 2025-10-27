package com.mediforme.mediforme.repository;

import com.mediforme.mediforme.domain.code.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonCodeRepository extends JpaRepository<CommonCode, Long> {
    List<CommonCode> findByComCodeGroupCd(String groupCd);      // 그룹코드별로 공통코드 조회
}
