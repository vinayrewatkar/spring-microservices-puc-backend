package com.puc.modelApiService.external;

import com.puc.modelApiService.dto.VehicleDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "RCVERIFICATIONSERVICE") // Use Eureka service name if service discovery is enabled
public interface RcVerificationFeignClient {

    @PostMapping(value = "/api/vehicle/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    VehicleDetailsDto verifyVehicle(@RequestPart("file") List<MultipartFile> images);
}

