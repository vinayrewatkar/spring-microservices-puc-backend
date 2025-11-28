package com.puc.modelApiService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleDetailsDto {

    private String regNo;              // registrationNo
    private String ownerName;          // ownerName
    private String vehicleClassDesc;  // vehicleClass
    private String model;               // makerModel
    private String fitnessUpto;        // fitnessUpto
    private String insuranceUpto;      // insuranceUpto
    private String pucUpto;            // pucUpto
    private String state;               // registrationAuthority
}
