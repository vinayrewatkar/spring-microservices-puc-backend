package com.puc.regionService.service;

import com.puc.regionService.dto.RegionRequestDto;
import com.puc.regionService.dto.RegionResponseDto;
import com.puc.regionService.dto.RegionStatsDto;
import com.puc.regionService.dto.UpdateRegionDto;
import com.puc.regionService.model.RegionDetails;

public interface RegionService {
    RegionResponseDto checkAndCreateRegion(RegionRequestDto requestDto);
    RegionStatsDto getRegionStats(String regionName);
    RegionDetails updateRegionWithRCNumber(String regionName, UpdateRegionDto regionDto);
}
