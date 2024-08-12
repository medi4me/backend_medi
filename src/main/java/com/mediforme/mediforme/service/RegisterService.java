package com.mediforme.mediforme.service;

import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.web.dto.RegisterRequestDTO;

public interface RegisterService {
    Member registerUser(RegisterRequestDTO.JoinDto joinDto);
}
