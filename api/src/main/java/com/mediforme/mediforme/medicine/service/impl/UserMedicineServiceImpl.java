package com.mediforme.mediforme.medicine.service.impl;

import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.exception.ErrorCode;
import com.mediforme.mediforme.medicine.domain.UserMedicine;
import com.mediforme.mediforme.medicine.repository.UserMedicineRepository;
import com.mediforme.mediforme.medicine.service.UserMedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserMedicineServiceImpl implements UserMedicineService {

    private final UserMedicineRepository userMedicineRepository;

    // 약물 복용 여부 체크 - O
    @Override
    public void checkMedicine(Long userMedicineId) {
        UserMedicine um = find(userMedicineId);
        um.updateDaysOfWeek(um.getDaysOfWeekCd(), um.getModifierId());
    }

    // 약물 복용 여부 체크 - X
    @Override
    public void uncheckMedicine(Long userMedicineId) {
        UserMedicine um = find(userMedicineId);
        um.changeAlarm(false, um.getModifierId());
    }

    // 알람
    @Override
    public void toggleAlarm(Long userMedicineId, boolean isOn) {
        UserMedicine um = find(userMedicineId);
        um.changeAlarm(isOn, um.getModifierId());
    }

    private UserMedicine find(Long id) {
        return userMedicineRepository.findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_MEDICINE_NOT_FOUND));
    }
}