package com.mediforme.mediforme.service;

import com.mediforme.mediforme.Repository.RegisterRepository;
import com.mediforme.mediforme.converter.RegisterConverter;
import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.domain.enums.Role;
import com.mediforme.mediforme.web.dto.RegisterRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.mediforme.mediforme.domain.enums.Role.USER;

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
        registerRepository.findByName(joinDto.getName())
                .ifPresent(existingMember -> {
                    throw new IllegalArgumentException("Username already exists");
                });

        // 새로운 회원 생성
        Member newMember = RegisterConverter.toMember(joinDto);
        newMember.setRole(Role.USER);
        // 회원 정보 저장
        return registerRepository.save(newMember);
    }
}
