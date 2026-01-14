package com.mediforme.mediforme.mapper;

import com.mediforme.mediforme.domain.Status;
import com.mediforme.mediforme.dto.request.StatusAdminRequestDto;
import com.mediforme.mediforme.dto.response.StatusAdminResponseDto;
import com.mediforme.mediforme.dto.response.StatusMeResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatusMapper {
    StatusMeResponseDto toMeResponse(Status entity);
    StatusAdminResponseDto toAdminResponse(Status entity);
}
