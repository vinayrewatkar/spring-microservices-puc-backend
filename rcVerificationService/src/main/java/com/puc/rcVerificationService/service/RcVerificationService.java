package com.puc.rcVerificationService.service;

import com.puc.rcVerificationService.dto.VehicleDetailsDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.puc.rcVerificationService.entity.VehicleDetailsEntity;
import com.puc.rcVerificationService.repository.VehicleDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RcVerificationService {

    @Value("${rc.api.url}")
    private String rcApiUrl;

    @Value("${rc.api.key}")
    private String rcApiKey;

    @Value("${rc.api.host}")
    private String rcApiHost;

    private final RestTemplate restTemplate;
    private final VehicleDetailsRepository vehicleDetailsRepository;  // ADD THIS

    public VehicleDetailsDto performPucValidation(String rcNumber) {
        try {
            // Check if already exists in DB
            if (vehicleDetailsRepository.existsByRegNo(rcNumber)) {
                log.info("Vehicle details for {} already exists in DB", rcNumber);
                VehicleDetailsEntity existing = vehicleDetailsRepository.findByRegNo(rcNumber).get();
                return entityToDto(existing);
            }

            // Fetch from API (existing code)
            Map<String, Object> payload = new HashMap<>();
            payload.put("reg_no", rcNumber);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-rapidapi-key", rcApiKey);
            headers.set("x-rapidapi-host", rcApiHost);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    rcApiUrl, HttpMethod.POST, requestEntity, JsonNode.class
            );

            log.info("RC API status: {}", response.getStatusCode());

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("RC API error: status=" + response.getStatusCode());
            }

            JsonNode body = response.getBody();
            JsonNode details = body.has("details") ? body.get("details") : body;

            // Map to DTO (existing)
            VehicleDetailsDto dto = VehicleDetailsDto.builder()
                    .regNo(getText(details, "registrationNo"))
                    .ownerName(getText(details, "ownerName"))
                    .vehicleClassDesc(getText(details, "vehicleClass"))
                    .model(getText(details, "makerModel"))
                    .fitnessUpto(getText(details, "fitnessUpto"))
                    .insuranceUpto(getText(details, "insuranceUpto"))
                    .pucUpto(getText(details, "pucUpto"))
                    .state(getText(details, "registrationAuthority"))
                    .build();

            // SAVE TO DB - NEW
            VehicleDetailsEntity entity = dtoToEntity(dto);
            vehicleDetailsRepository.save(entity);
            log.info("Saved vehicle details for {} to DB", rcNumber);

            return dto;

        } catch (Exception e) {
            log.error("Error calling RC API for {}: {}", rcNumber, e.getMessage(), e);
            throw new RuntimeException("RC API call failed: " + e.getMessage(), e);
        }
    }

    private String getText(JsonNode node, String fieldName) {
        if (node == null) return null;
        JsonNode value = node.get(fieldName);
        return (value != null && !value.isNull()) ? value.asText() : null;
    }

    // NEW: DTO <-> Entity converters
    private VehicleDetailsEntity dtoToEntity(VehicleDetailsDto dto) {
        return VehicleDetailsEntity.builder()
                .regNo(dto.getRegNo())
                .ownerName(dto.getOwnerName())
                .model(dto.getModel())
                .state(dto.getState())
                .vehicleClassDesc(dto.getVehicleClassDesc())
                .fitnessUpto(dto.getFitnessUpto())
                .insuranceUpto(dto.getInsuranceUpto())
                .pucUpto(dto.getPucUpto())
                .build();
    }

    private VehicleDetailsDto entityToDto(VehicleDetailsEntity entity) {
        return VehicleDetailsDto.builder()
                .regNo(entity.getRegNo())
                .ownerName(entity.getOwnerName())
                .model(entity.getModel())
                .state(entity.getState())
                .vehicleClassDesc(entity.getVehicleClassDesc())
                .fitnessUpto(entity.getFitnessUpto())
                .insuranceUpto(entity.getInsuranceUpto())
                .pucUpto(entity.getPucUpto())
                .build();
    }
}
