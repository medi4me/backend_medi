package com.mediforme.mediforme.mapper;

import com.mediforme.mediforme.domain.Status;
import com.mediforme.mediforme.dto.request.StatusRequestDto;
import com.mediforme.mediforme.dto.response.StatusResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatusMapper {
    Status toEntity(StatusRequestDto dto);
    StatusResponseDto toResponse(Status entity);
}
