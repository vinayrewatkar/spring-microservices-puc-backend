package com.puc.regionService.service.impl;

import com.puc.regionService.exception.RegionNotFoundException;
import com.puc.regionService.repository.RegionRepository;
import com.puc.regionService.dto.RegionRequestDto;
import com.puc.regionService.dto.RegionResponseDto;
import com.puc.regionService.dto.RegionStatsDto;
import com.puc.regionService.dto.UpdateRegionDto;
import com.puc.regionService.model.RegionDetails;
import com.puc.regionService.service.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;


    @Override
    @Transactional
    public RegionResponseDto checkAndCreateRegion(RegionRequestDto requestDto) {
        log.info("Checking/Creating region: {}", requestDto.getRegionName());
        return regionRepository.findByRegionName(requestDto.getRegionName())
                .map(this::mapToResponseDto)
                .orElseGet(() -> createNewRegion(requestDto));
    }


    private RegionResponseDto createNewRegion(RegionRequestDto requestDto){
        RegionDetails newRegion = new RegionDetails();
        newRegion.setRegionName(requestDto.getRegionName());
        newRegion.setCity(requestDto.getCity());
        newRegion.setState(requestDto.getState());
        newRegion.setValidCount(0);
        newRegion.setInvalidCount(0);
        newRegion.setTotalCount(0);
        newRegion.setRegisteredNumbers(new ArrayList<>());

        RegionDetails savedRegion = regionRepository.save(newRegion);
        log.info("Created new region: {}", savedRegion.getRegionName());
        return mapToResponseDto(savedRegion);
    }


    @Override
    public RegionStatsDto getRegionStats(String regionName) {
        log.info("Fetching stats for region: {}", regionName);
        RegionDetails region = regionRepository.findByRegionName(regionName)
                .orElseThrow(() -> new RegionNotFoundException("Region " + regionName + " not found"));
        int totalRegistered = region.getRegisteredNumbers() != null ?
                region.getRegisteredNumbers().size() : 0;
        int unmatched = Math.max(0, totalRegistered -
                (region.getValidCount() + region.getInvalidCount()));

        return RegionStatsDto.builder()
                .regionName(region.getRegionName())
                .state(region.getState())
                .city(region.getCity())
                .validCount(region.getValidCount())
                .invalidCount(region.getInvalidCount())
                .totalCount(region.getTotalCount())
                .unmatchedCount(unmatched)
                .registeredVehicles(totalRegistered)
                .lastUpdated(region.getUpdatedAt())
                .build();
    }

    @Override
    public RegionDetails updateRegionWithRCNumber(String regionName, UpdateRegionDto regionDto) {
        log.info("Updating region {} with RC number: {}", regionName, regionDto.getRcNumber());

        RegionDetails region = regionRepository.findByRegionName(regionName)
                .orElseThrow(() -> new RegionNotFoundException("Region " + regionName + " not found"));

        boolean isExisting = region.getRegisteredNumbers().contains(regionDto.getRcNumber());
        if (!isExisting) {
            // Add RC number to registered list
            region.getRegisteredNumbers().add(regionDto.getRcNumber());

            // Update counts
            region.setTotalCount(region.getTotalCount() + 1);

            if (Boolean.TRUE.equals(regionDto.getIsValid())) {
                region.setValidCount(region.getValidCount() + 1);
            } else {
                region.setInvalidCount(region.getInvalidCount() + 1);
            }

        }
        return regionRepository.save(region);
    }

    private RegionResponseDto mapToResponseDto(RegionDetails region) {
        return RegionResponseDto.builder()
                .regionName(region.getRegionName())
                .city(region.getCity())
                .state(region.getState())
                .validCount(region.getValidCount())
                .invalidCount(region.getInvalidCount())
                .totalCount(region.getTotalCount())
                .registeredNumbers(region.getRegisteredNumbers())
                .build();
    }
}
