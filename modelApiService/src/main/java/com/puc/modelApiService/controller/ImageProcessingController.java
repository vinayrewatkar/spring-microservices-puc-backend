package com.puc.modelApiService.controller;

import com.puc.modelApiService.dto.VehicleDetailsDto;
import com.puc.modelApiService.service.ImageProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ImageProcessingController {

    private final ImageProcessingService imageProcessingService;

    @PostMapping(value = "/crop-and-verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VehicleDetailsDto> cropAndVerify(@RequestParam("image") MultipartFile image) {
        try {
            VehicleDetailsDto vehicleDetails = imageProcessingService.cropAndVerify(image);
            return ResponseEntity.ok(vehicleDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


