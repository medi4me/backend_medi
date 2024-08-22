package com.mediforme.mediforme.service;

import com.mediforme.mediforme.config.ApiConfig;
import com.mediforme.mediforme.converter.MedicineConverter;
import com.mediforme.mediforme.domain.Medicine;
import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.domain.mapping.UserMedicine;
import com.mediforme.mediforme.dto.OnboardingDto;
import com.mediforme.mediforme.repository.MedicineRepository;
import com.mediforme.mediforme.repository.MemberRepository;
import com.mediforme.mediforme.repository.UserMedicineRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MedicineService {
    private final MedicineRepository medicineRepository;
    private final MemberRepository memberRepository;
    private final UserMedicineRepository userMedicineRepository;
    private final MedicineConverter medicineConverter;
    private final String SERVICE_URL;
    private final String SERVICE_KEY;

    @Autowired
    public MedicineService(MedicineRepository medicineRepository, MemberRepository memberRepository, UserMedicineRepository userMedicineRepository, MedicineConverter medicineConverter, ApiConfig apiConfig) {
        this.medicineRepository = medicineRepository;
        this.memberRepository = memberRepository;
        this.userMedicineRepository = userMedicineRepository;
        this.medicineConverter = medicineConverter;
        this.SERVICE_URL = apiConfig.getSERVICE_URL();
        this.SERVICE_KEY = apiConfig.getSERVICE_KEY();
    }

    // 약 이름으로 검색
    public OnboardingDto.OnboardingResponseDto getMedicineInfoByName(String itemName) throws IOException, ParseException {
        return getMedicineInfo(itemName);
    }


    private OnboardingDto.OnboardingResponseDto getMedicineInfo(String itemName) throws IOException, ParseException {
        StringBuilder result = new StringBuilder();

        StringBuilder urlStr = new StringBuilder(SERVICE_URL + "?");
        urlStr.append("serviceKey=").append(SERVICE_KEY);

        if (itemName != null) {
            urlStr.append("&itemName=").append(URLEncoder.encode(itemName, "UTF-8"));
        }

        urlStr.append("&pageNo=1");
        urlStr.append("&numOfRows=10");
        urlStr.append("&type=json");

        URL url = new URL(urlStr.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"))) {
            String returnLine;
            while ((returnLine = br.readLine()) != null) {
                result.append(returnLine).append("\n");
            }
        } finally {
            urlConnection.disconnect();
        }

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(result.toString());

        JSONObject jsonBody = (JSONObject) jsonObject.get("body");
        JSONArray jsonItems = (JSONArray) jsonBody.get("items");

        List<OnboardingDto.MedicineInfoDto> medicines = new ArrayList<>();

        // items 배열을 순회하며 각 아이템의 이름과 이미지를 가져옴
        for (Object itemObj : jsonItems) {
            JSONObject item = (JSONObject) itemObj;
            String itemNameValue = (String) item.get("itemName");
            String itemImageValue = (String) item.get("itemImage");

            medicines.add(OnboardingDto.MedicineInfoDto.builder()
                    .itemName(itemNameValue)
                    .itemImage(itemImageValue)
                    .build());
        }

        return OnboardingDto.OnboardingResponseDto.builder()
                .medicines(medicines)
                .build();
    }

    public OnboardingDto.OnboardingResponseDto saveMedicineInfo(OnboardingDto.OnboardingRequestDto requestDto, Long memberId) throws IOException, ParseException {
        StringBuilder result = new StringBuilder();

        StringBuilder urlStr = new StringBuilder(SERVICE_URL + "?");
        urlStr.append("serviceKey=").append(SERVICE_KEY);

        if (requestDto.getItemName() != null) {
            urlStr.append("&itemName=").append(URLEncoder.encode(requestDto.getItemName(), "UTF-8"));
        }

        urlStr.append("&pageNo=1");
        urlStr.append("&numOfRows=1");
        urlStr.append("&type=json");

        URL url = new URL(urlStr.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"))) {
            String returnLine;
            while ((returnLine = br.readLine()) != null) {
                result.append(returnLine).append("\n");
            }
        } finally {
            urlConnection.disconnect();
        }

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(result.toString());

        JSONObject jsonBody = (JSONObject) jsonObject.get("body");
        JSONArray jsonItems = (JSONArray) jsonBody.get("items");

        if (jsonItems.isEmpty()) {
            throw new RuntimeException("No medicine found for the given name.");
        }

        JSONObject item = (JSONObject) jsonItems.get(0);

        Medicine medicine = Medicine.builder()
                .name((String) item.get("itemName"))
                .description((String) item.get("useMethodQesitm"))
                .benefit((String) item.get("efcyQesitm"))
                .drugInteraction((String) item.get("intrcQesitm"))
                .itemImage((String) item.get("itemImage"))
                .build();

        medicine = medicineRepository.save(medicine);

        UserMedicine userMedicine = UserMedicine.builder()
                .meal(requestDto.getMeal())
                .time(requestDto.getTime())
                .dosage(requestDto.getDosage())
                .member(memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("Member not found"))) // 사용자 정보
                .medicine(medicine)
                .build();

        userMedicineRepository.save(userMedicine);

        OnboardingDto.MedicineInfoDto savedMedicineInfo = OnboardingDto.MedicineInfoDto.builder()
//                .userMedicineId(userMedicine.getId())
                .itemName(medicine.getName())
                .itemImage(medicine.getItemImage())
                .description(medicine.getDescription())
                .benefit(medicine.getBenefit())
                .drugInteraction(medicine.getDrugInteraction())
                .meal(requestDto.getMeal())
                .time(requestDto.getTime())
                .dosage(requestDto.getDosage())
                .build();

        return OnboardingDto.OnboardingResponseDto.builder()
                .medicines(Collections.singletonList(savedMedicineInfo))
                .build();
    }

    public OnboardingDto.OnboardingResponseDto getUserMedicines(Long memberId) {

        List<UserMedicine> userMedicines = userMedicineRepository.findByMemberId(memberId);

        List<OnboardingDto.MedicineInfoDto> userMedicineDtos = new ArrayList<>();

        for (UserMedicine userMedicine : userMedicines) {
            Medicine medicine = userMedicine.getMedicine();

            OnboardingDto.MedicineInfoDto dto = OnboardingDto.MedicineInfoDto.builder()
                    .userMedicineId(userMedicine.getId())
                    .itemName(medicine.getName())
                    .itemImage(medicine.getItemImage())
                    .description(medicine.getDescription())
                    .benefit(medicine.getBenefit())
                    .drugInteraction(medicine.getDrugInteraction())
                    .meal(userMedicine.getMeal())
                    .time(userMedicine.getTime())
                    .dosage(userMedicine.getDosage())
                    .isCheck(userMedicine.isCheck())
                    .isAlarm(userMedicine.isAlarm())
                    .build();

            userMedicineDtos.add(dto);
        }

        return OnboardingDto.OnboardingResponseDto.builder()
                .medicines(userMedicineDtos)
                .build();
    }

    public void deleteUserMedicine(Long memberId, Long userMedicineId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        UserMedicine userMedicine = userMedicineRepository.findById(userMedicineId)
                .orElseThrow(() -> new RuntimeException("User medicine not found"));

        if (!userMedicine.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("User does not have permission to delete this medicine.");
        }

        userMedicineRepository.delete(userMedicine);
    }
}
