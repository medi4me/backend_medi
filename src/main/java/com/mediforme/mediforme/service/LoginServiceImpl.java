package com.mediforme.mediforme.service;

import com.mediforme.mediforme.Repository.RegisterRepository;
import com.mediforme.mediforme.domain.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginServiceImpl implements LoginService {
    private final RegisterRepository registerRepository;

    @Autowired
    public LoginServiceImpl(RegisterRepository registerRepository) {
        this.registerRepository = registerRepository;
    }

    @Transactional
    public boolean authenticate(String memberID, String password) {
        Member member = registerRepository.findByMemberID(memberID);

        if (member != null) {
            return member.getPassword().equals(password);
        }
        return false;
    }
}
