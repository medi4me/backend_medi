package com.mediforme.mediforme.converter;

import com.mediforme.mediforme.domain.Status;
import com.mediforme.mediforme.dto.object.StatusDto;
import org.springframework.stereotype.Component;

@Component
public class StatusConverter {

    public Status toEntity(StatusDto dto) {
        if (dto == null) return null;
        return Status.builder()
                .status(dto.getStatus())
                .drink(dto.getDrink())
                .statusCondition(dto.getStatusCondition())
                .memo(dto.getMemo())
                .date(dto.getDate())
                .build();
    }

    public StatusDto toDto(Status entity) {
        if (entity == null) return null;
        StatusDto dto = new StatusDto();
        dto.setStatus(entity.getStatus());
        dto.setDrink(entity.getDrink());
        dto.setStatusCondition(entity.getStatusCondition());
        dto.setMemo(entity.getMemo());
        dto.setDate(entity.getDate());
        return dto;
    }

    public void updateEntityFromDto(StatusDto dto, Status target){
        if (dto==null|| target == null) return;
        target.setStatus(dto.getStatus());
        target.setDate(dto.getDate());
        target.setDrink(dto.getDrink());
        target.setMemo(dto.getMemo());
        target.setStatusCondition(dto.getStatusCondition());

    }
}
