package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.domain.User;
import com.mediforme.mediforme.repository.UserRepository;
import com.mediforme.mediforme.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // 회원 이름 조회
    @Override
    @Transactional(readOnly = true)
    public String findUserNameByLoginId(String userLoginId) {
        return userRepository.findByUserLoginId(userLoginId)
                .map(User::getUserName)
                .orElse("회원 이름을 찾을 수 없습니다.");
    }

    // TODO: 프로필 조회, 수정, 탈퇴
}