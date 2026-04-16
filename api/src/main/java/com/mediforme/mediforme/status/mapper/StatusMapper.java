package com.mediforme.mediforme.status.mapper;

import com.mediforme.mediforme.status.domain.Status;
import com.mediforme.mediforme.status.dto.StatusAdminResponseDto;
import com.mediforme.mediforme.status.dto.StatusMeResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StatusMapper {

    @Mapping(target = "defaultStatusName", ignore = true)
    @Mapping(target = "drinkName", ignore = true)
    @Mapping(target = "conditionName", ignore = true)
    StatusMeResponseDto toMeResponse(Status entity);

    @Mapping(target = "defaultStatusName", ignore = true)
    @Mapping(target = "drinkName", ignore = true)
    @Mapping(target = "conditionName", ignore = true)
    StatusAdminResponseDto toAdminResponse(Status entity);
}
