package com.puc.regionService.model;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "regionDetail")
public class RegionDetails {

    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Region name is required")
    private String regionName;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "City is required")
    private String city;

    private int validCount = 0;

    private int invalidCount = 0;

    private int totalCount = 0;

    private List<String> registeredNumbers = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
