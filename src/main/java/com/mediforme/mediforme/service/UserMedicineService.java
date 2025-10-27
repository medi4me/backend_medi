package com.mediforme.mediforme.service;

public interface UserMedicineService {
    void checkMedicine(Long userMedicineId);
    void uncheckMedicine(Long userMedicineId);
    void toggleAlarm(Long userMedicineId, boolean isOn);
}
