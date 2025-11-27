package com.puc.rcVerificationService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vehicle_details")
public class VehicleDetailsEntity {

    @Id
    private String id;

    private String regNo;               // was reg_no
    private String ownerName;           // optional: camelCase others too
    private String model;
    private String state;
    private String vehicleClassDesc;
    private String fitnessUpto;
    private String insuranceUpto;
    private String pucUpto;
}
