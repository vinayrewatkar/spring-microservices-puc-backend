package com.puc.rcVerificationService.service;

import com.puc.rcVerificationService.dto.VehicleDetailsDto;
import com.fasterxml.jackson.databind.JsonNode;
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

    public VehicleDetailsDto performPucValidation(String rcNumber) {
        try {
            // Payload for new API: { "reg_no": "MH49BY8222" }
            Map<String, Object> payload = new HashMap<>();
            payload.put("reg_no", rcNumber);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-rapidapi-key", rcApiKey);
            headers.set("x-rapidapi-host", rcApiHost);

            HttpEntity<Map<String, Object>> requestEntity =
                    new HttpEntity<>(payload, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    rcApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    JsonNode.class
            );

            log.info("RC API status: {}", response.getStatusCode());
            log.debug("RC API body  : {}", response.getBody());

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("RC API error: status=" + response.getStatusCode());
            }

            JsonNode body = response.getBody();

            // If API wraps data, e.g. { statusCode, statusMessage, details: { ... } }
            JsonNode details = body.has("details") ? body.get("details") : body;

            String registrationAuthority = getText(details, "registrationAuthority");
            String registrationNo        = getText(details, "registrationNo");
            String ownerName             = getText(details, "ownerName");
            String vehicleClass          = getText(details, "vehicleClass");
            String makerModel            = getText(details, "makerModel");
            String fitnessUpto           = getText(details, "fitnessUpto");
            String insuranceUpto         = getText(details, "insuranceUpto");
            String pucUpto               = getText(details, "pucUpto");

            return VehicleDetailsDto.builder()
                    .regNo(registrationNo)
                    .ownerName(ownerName)
                    .vehicleClassDesc(vehicleClass)
                    .model(makerModel)
                    .fitnessUpto(fitnessUpto)
                    .insuranceUpto(insuranceUpto)
                    .pucUpto(pucUpto)
                    .state(registrationAuthority)
                    .build();

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
}
