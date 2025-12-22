package com.puc.regionService.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionResponseDto {
    private String regionName;
    private String city;
    private String state;
    private Integer validCount;
    private Integer invalidCount;
    private Integer totalCount;
    private List<String> registeredNumbers;
}
