package com.puc.rcVerificationService.controller;

import com.puc.rcVerificationService.service.ImageProcessingService;
import com.puc.rcVerificationService.dto.VehicleDetailsDto;
import com.puc.rcVerificationService.service.RcVerificationService;
import com.puc.rcVerificationService.utils.FileData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
public class rcVerificationController {

    private final ImageProcessingService imageProcessingService;

    private final RcVerificationService rcVerificationService;


    /**
     * Endpoint to upload images for vehicle verification.
     * Accepts multiple image files.
     *
     * @param files Multipart image files
     * @return Vehicle details DTO
     */
    @PostMapping(value = "/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VehicleDetailsDto> verifyVehicle(
            @RequestParam("file") List<MultipartFile> files) {

        try {
            // Convert MultipartFile to FileData expected by service
            List<FileData> fileDataList = files.stream().map(file -> {
                try {
                    return new FileData(file.getOriginalFilename(), file.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read file " + file.getOriginalFilename(), e);
                }
            }).collect(Collectors.toList());

            // Call the service
            VehicleDetailsDto vehicleDetails = imageProcessingService.verifyVehicle(fileDataList);

            return ResponseEntity.ok(vehicleDetails);

        } catch (Exception e) {
            log.error("Error in vehicle verification: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/rc-verify")
    public ResponseEntity<VehicleDetailsDto> verifyRcNumber(
            @RequestParam("rcNumber") String rcNumber) {

        try {
            VehicleDetailsDto vehicleDetails = rcVerificationService.performPucValidation(rcNumber);
            return ResponseEntity.ok(vehicleDetails);
        } catch (Exception e) {
            log.error("Error verifying RC number {}: {}", rcNumber, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

}
