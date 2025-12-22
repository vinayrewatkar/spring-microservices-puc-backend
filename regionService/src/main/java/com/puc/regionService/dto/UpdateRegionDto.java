package com.puc.regionService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRegionDto {
    @NotBlank(message = "RC number is required")
    private String rcNumber;

    private Boolean isValid;
}
