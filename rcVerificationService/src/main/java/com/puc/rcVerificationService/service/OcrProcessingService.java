package com.puc.rcVerificationService.service;

import com.puc.rcVerificationService.utils.FileData;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrProcessingService {

    @Value("${ocr.api.url}")
    private String ocrApiUrl;

    @Value("${ocr.api.key}")
    private String ocrApiKey;

    @Value("${ocr.api.host}")
    private String ocrApiHost;

    private final RestTemplate restTemplate;

    /**
     * Debug helper: call OCR API using only url field (like docs curl).
     */
    public void debugWithUrlOnly() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("url", "https://storage.googleapis.com/api4ai-static/samples/ocr-1.png");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", ocrApiKey);
        headers.set("X-RapidAPI-Host", ocrApiHost);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                ocrApiUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        log.info("DEBUG URL ONLY - status: {}", resp.getStatusCode());
        log.info("DEBUG URL ONLY - body  : {}", resp.getBody());
    }

    /**
     * Main method: send image bytes as multipart "image" and extract texts.
     */
    public List<String> processOcr(List<FileData> extractedFiles) {
        if (ocrApiUrl == null || ocrApiKey == null || ocrApiHost == null) {
            throw new RuntimeException("OCR API config missing");
        }

        List<String> imageProcessedData = new ArrayList<>();

        for (FileData file : extractedFiles) {
            String filename = file.getFilename();
            byte[] buffer = file.getBuffer();

            if (filename == null || buffer == null) {
                log.error("Invalid file format: Missing filename or buffer");
                continue;
            }

            log.info("Processing OCR for file: {} (size {} bytes)", filename, buffer.length);

            try {
                // 1) Build multipart body.
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

                Resource fileResource = new ByteArrayResource(buffer) {
                    @Override
                    public String getFilename() {
                        // must be non-null; Postman sends file with its name
                        return filename;
                    }
                };

                // field name MUST be "image" to match OCR API spec.
                body.add("image", fileResource);

                // 2) Headers: only RapidAPI ones.
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-RapidAPI-Key", ocrApiKey);
                headers.set("X-RapidAPI-Host", ocrApiHost);
                // DO NOT set Content-Type; RestTemplate will set multipart/form-data with boundary.

                HttpEntity<MultiValueMap<String, Object>> requestEntity =
                        new HttpEntity<>(body, headers);

                // 3) Execute request.
                ResponseEntity<JsonNode> response = restTemplate.exchange(
                        ocrApiUrl,
                        HttpMethod.POST,
                        requestEntity,
                        JsonNode.class
                );

                log.info("OCR API status: {}", response.getStatusCode());
                log.debug("OCR API body  : {}", response.getBody());

                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    log.error("OCR failed. Status: {}, body: {}",
                            response.getStatusCode(), response.getBody());
                    continue;
                }

                // 4) Parse JSON like in your JS.
                JsonNode root = response.getBody().get("results");
                if (root != null && root.isArray()) {
                    for (JsonNode resultNode : root) {
                        JsonNode entitiesNode = resultNode.get("entities");
                        if (entitiesNode != null && entitiesNode.isArray()) {
                            for (JsonNode entity : entitiesNode) {
                                JsonNode objectsNode = entity.get("objects");
                                if (objectsNode != null && objectsNode.isArray()) {
                                    for (JsonNode objNode : objectsNode) {
                                        JsonNode subEntities = objNode.get("entities");
                                        if (subEntities != null && subEntities.isArray()) {
                                            for (JsonNode subEntity : subEntities) {
                                                JsonNode kindNode = subEntity.get("kind");
                                                JsonNode textNode = subEntity.get("text");
                                                if (kindNode != null
                                                        && "text".equals(kindNode.asText())
                                                        && textNode != null
                                                        && !textNode.isNull()) {
                                                    String extractedText = textNode.asText();
                                                    log.info("Extracted text: {}", extractedText);
                                                    imageProcessedData.add(extractedText);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    log.warn("No 'results' array found in OCR response for file {}", filename);
                }

            } catch (Exception ex) {
                log.error("Error in OCR processing for file {}: {}", filename, ex.getMessage(), ex);
            }
        }

        if (imageProcessedData.isEmpty()) {
            log.warn("No text was successfully extracted from any of the images.");
        } else {
            log.info("Successfully extracted {} text elements", imageProcessedData.size());
        }

        return imageProcessedData;
    }
}
