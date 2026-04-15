package com.mediforme.mediforme.service.impl;

import com.mediforme.common.exception.CustomApiException;
import com.mediforme.common.exception.ErrorCode;
import com.mediforme.mediforme.domain.Medicine;
import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.domain.UserMedicine;
import com.mediforme.mediforme.dto.object.OnboardingDto;
import com.mediforme.mediforme.dto.request.OnboardingRequestDto;
import com.mediforme.mediforme.dto.response.OnboardingResponseDto;
import com.mediforme.mediforme.repository.MedicineRepository;
import com.mediforme.mediforme.repository.UserMedicineRepository;
import com.mediforme.mediforme.repository.UserRepository;
import com.mediforme.mediforme.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {
    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final UserMedicineRepository userMedicineRepository;

    // 약, 사용자 복용 정보 저장
    @Override
    public OnboardingResponseDto saveMedicineInfo(Long userId, OnboardingRequestDto request) throws IOException, ParseException {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

        Medicine medicine = medicineRepository.findByMedicineName(request.getItemName())
                .orElseGet(() -> medicineRepository.save(Medicine.builder()
                        .medicineName(request.getItemName())
                        .description("등록된 설명 없음")
                        .build()));

        UserMedicine userMedicine = userMedicineRepository.save(UserMedicine.builder()
                .userId(user.getUserId())
                .medicineId(medicine.getMedicineId())
                .dosage(request.getDosage())
                .mealCd(request.getMealCd())
                .timeCd(request.getTimeCd())
                .daysOfWeekCd(request.getDaysOfWeekCd())
                .isAlarm(false)
                .build());

        OnboardingDto dto = OnboardingDto.builder()
                .userMedicineId(userMedicine.getUserMedicineId())
                .itemName(medicine.getMedicineName())
                .description(medicine.getDescription())
                .benefit(medicine.getMedicineBenefit())
                .drugInteraction(medicine.getDrugInteraction())
                .dosage(userMedicine.getDosage())
                .isAlarm(userMedicine.getIsAlarm())
                .build();

        return OnboardingResponseDto.builder().medicines(Collections.singletonList(dto)).build();
    }

    // 사용자 복용 약 전체 조회
    @Override
    @Transactional(readOnly = true)
    public OnboardingResponseDto getUserMedicines(Long userId) {
        List<UserMedicine> list = userMedicineRepository.findByUserId(userId);
        List<OnboardingDto> result = new ArrayList<>();

        for (UserMedicine um : list) {
            Medicine m = medicineRepository.findById(um.getMedicineId())
                    .orElseThrow(() -> new CustomApiException(ErrorCode.MEDICINE_NOT_FOUND));
            result.add(OnboardingDto.builder()
                    .userMedicineId(um.getUserMedicineId())
                    .itemName(m.getMedicineName())
                    .dosage(um.getDosage())
                    .isAlarm(um.getIsAlarm())
                    .build());
        }

        return OnboardingResponseDto.builder().medicines(result).build();
    }

    // 특정 복용약 삭제
    @Override
    public void deleteUserMedicine(Long userMedicineId, Long userId) {
        UserMedicine userMedicine = userMedicineRepository.findById(userMedicineId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_MEDICINE_NOT_FOUND));
        if (!Objects.equals(userMedicine.getUserId(), userId)) {
            throw new CustomApiException(ErrorCode.FORBIDDEN_ACTION);
        }
        userMedicineRepository.delete(userMedicine);
    }
}
