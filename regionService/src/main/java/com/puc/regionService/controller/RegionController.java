package com.puc.regionService.controller;


import com.puc.regionService.dto.RegionRequestDto;
import com.puc.regionService.dto.RegionResponseDto;
import com.puc.regionService.dto.RegionStatsDto;
import com.puc.regionService.dto.UpdateRegionDto;
import com.puc.regionService.service.RegionService;
import com.puc.regionService.util.ResponseHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/region")
@Slf4j
public class RegionController {

    private final RegionService regionService;

    @PostMapping("/check-create")
    public ResponseEntity<Object> checkAndCreateRegion(@Valid @RequestBody RegionRequestDto regionRequest){
        try {
            RegionResponseDto regionResponse = regionService.checkAndCreateRegion(regionRequest);
            boolean isNew = regionResponse.getTotalCount() == 0;

            return ResponseHandler.generateResponse(
                    isNew ? "Region created successfully" : "Region fetched from database",
                    isNew ? HttpStatus.CREATED : HttpStatus.OK,
                    regionResponse
            );
        } catch (Exception e) {
            log.error("Error in checkAndCreateRegion", e);
            return ResponseHandler.generateResponse(
                    "Error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    null
            );
        }
    }

    @GetMapping("/stats/{regionName}")
    public ResponseEntity<Object> getRegionStats(@PathVariable String regionName) {
        try {
            RegionStatsDto stats = regionService.getRegionStats(regionName);
            return ResponseHandler.generateResponse(
                    "Region stats fetched successfully",
                    HttpStatus.OK,
                    stats
            );
        } catch (Exception e) {
            log.error("Error in getRegionStats", e);
            return ResponseHandler.generateResponse(
                    "Error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    null
            );
        }
    }

    @PutMapping("/update/{regionName}")
    public ResponseEntity<Object> updateRegionWithRCNumber(
            @PathVariable String regionName,
            @Valid @RequestBody UpdateRegionDto updateDto) {
        try {
            var updatedRegion = regionService.updateRegionWithRCNumber(regionName, updateDto);
            return ResponseHandler.generateResponse(
                    "Region updated successfully",
                    HttpStatus.OK,
                    updatedRegion
            );
        } catch (Exception e) {
            log.error("Error in updateRegionWithRCNumber", e);
            return ResponseHandler.generateResponse(
                    "Error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    null
            );
        }
    }
}
