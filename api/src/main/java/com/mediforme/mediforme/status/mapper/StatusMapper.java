package com.mediforme.mediforme.status.mapper;

import com.mediforme.mediforme.status.domain.Status;
import com.mediforme.mediforme.status.dto.StatusAdminRequestDto;
import com.mediforme.mediforme.status.dto.StatusAdminResponseDto;
import com.mediforme.mediforme.status.dto.StatusMeResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatusMapper {
    StatusMeResponseDto toMeResponse(Status entity);
    StatusAdminResponseDto toAdminResponse(Status entity);
}
