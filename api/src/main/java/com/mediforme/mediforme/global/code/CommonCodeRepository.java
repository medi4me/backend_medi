package com.mediforme.mediforme.global.code;

import com.mediforme.mediforme.global.code.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonCodeRepository extends JpaRepository<CommonCode, Long> {
    List<CommonCode> findByComCodeGroupCd(String groupCd);      // 그룹코드별로 공통코드 조회
}
