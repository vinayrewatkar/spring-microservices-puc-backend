package com.puc.modelApiService.controller;

import com.puc.modelApiService.dto.VehicleDetailsDto;
import com.puc.modelApiService.service.ImageProcessingService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor

public class ImageProcessingController {

    private final ImageProcessingService imageProcessingService;

    @PostMapping(value = "/crop-and-verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CircuitBreaker(name = "rcModelBreaker", fallbackMethod = "rcModelFallBack")
    public ResponseEntity<VehicleDetailsDto> cropAndVerify(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("image") MultipartFile image) throws IOException {

        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;

        return ResponseEntity.ok(imageProcessingService.cropAndVerify(image, token));
    }


    public ResponseEntity<VehicleDetailsDto> rcModelFallBack(@RequestParam("image") MultipartFile image,
                                                             Throwable ex) {
        // You can log the error and return a safe default response
        VehicleDetailsDto fallbackDto = VehicleDetailsDto.builder()
                .regNo(null)
                .ownerName(null)
                .vehicleClassDesc(null)
                .model(null)
                .fitnessUpto(null)
                .insuranceUpto(null)
                .pucUpto(null)
                .state(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(fallbackDto);
    }

}


