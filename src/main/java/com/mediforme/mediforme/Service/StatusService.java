package com.mediforme.mediforme.Service;

import com.mediforme.mediforme.DTO.StatusDto;
import com.mediforme.mediforme.domain.Status;
import com.mediforme.mediforme.Repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatusService {

    @Autowired
    private StatusRepository statusRepository;

    public StatusDto saveStatus(StatusDto statusDto) {
        Status status = toEntity(statusDto);
        Status savedStatus = statusRepository.save(status);
        return toDto(savedStatus);
    }

    public List<StatusDto> getAllStatuses() {
        return statusRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public StatusDto getStatusById(Long id) {
        return statusRepository.findById(id).map(this::toDto).orElse(null);
    }

    public void deleteStatus(Long id) {
        statusRepository.deleteById(id);
    }

    public StatusDto getStatusByDate(LocalDate date) {
        return statusRepository.findByDate(date).map(this::toDto).orElse(null);
    }

    private Status toEntity(StatusDto statusDto) {
        return Status.builder()
                .status(statusDto.getStatus())
                .drink(statusDto.getDrink())
                .statusCondition(statusDto.getStatusCondition())
                .memo(statusDto.getMemo())
                .date(statusDto.getDate()) // date 필드를 포함시킴
                .build();
    }

    private StatusDto toDto(Status status) {
        StatusDto statusDto = new StatusDto();
        statusDto.setStatus(status.getStatus());
        statusDto.setDrink(status.getDrink());
        statusDto.setStatusCondition(status.getStatusCondition());
        statusDto.setMemo(status.getMemo());
        statusDto.setDate(status.getDate()); // date 필드를 포함시킴
        return statusDto;
    }
}
