package com.puc.regionService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegionStatsDto {
    private String regionName;
    private String state;
    private String city;
    private Integer validCount;
    private Integer invalidCount;
    private Integer totalCount;
    private Integer unmatchedCount;
    private Integer registeredVehicles;
    private LocalDateTime lastUpdated;
}
