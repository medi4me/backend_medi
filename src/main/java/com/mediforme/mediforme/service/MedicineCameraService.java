package com.mediforme.mediforme.service;

import com.mediforme.mediforme.dto.response.MedicineCameraResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.json.simple.parser.ParseException;


public interface MedicineCameraService {
    MedicineCameraResponseDto processImage(MultipartFile file) throws IOException, ParseException;
}
