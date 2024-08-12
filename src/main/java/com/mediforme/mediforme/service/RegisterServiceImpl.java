package com.mediforme.mediforme.service;

import com.mediforme.mediforme.Repository.RegisterRepository;
import com.mediforme.mediforme.converter.RegisterConverter;
import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.web.dto.RegisterRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterServiceImpl implements RegisterService {
    private final RegisterRepository registerRepository;

    @Autowired
    public RegisterServiceImpl(RegisterRepository registerRepository) {
        this.registerRepository = registerRepository;
    }

    @Transactional
    public Member registerUser(RegisterRequestDTO.JoinDto joinDto) {
        // 사용자 이름 중복 확인
        if (registerRepository.findByName(joinDto.getName()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        // 새로운 회원 생성
        Member newMember = RegisterConverter.toMember(joinDto);

        // 회원 정보 저장
        return registerRepository.save(newMember);
    }
}
