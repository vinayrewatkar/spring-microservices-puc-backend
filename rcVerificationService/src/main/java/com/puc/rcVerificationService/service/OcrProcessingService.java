package com.puc.rcVerificationService.service;

import com.puc.rcVerificationService.utils.FileData;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public void debugWithLocalImage() {
        try {
            Path path = Path.of("E:/BACKEND_puc/temp/MyVehicleImg.jpg");
            byte[] buffer = Files.readAllBytes(path);

            HttpHeaders partHeaders = new HttpHeaders();
            partHeaders.setContentType(MediaType.IMAGE_JPEG);

            HttpEntity<byte[]> part = new HttpEntity<>(buffer, partHeaders);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", part); // key MUST be "image"

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-RapidAPI-Key", ocrApiKey);
            headers.set("X-RapidAPI-Host", ocrApiHost);
            // DO NOT set Content-Type

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    ocrApiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("DEBUG BYTE[] IMAGE status: {}", resp.getStatusCode());
            log.info("DEBUG BYTE[] IMAGE body  : {}", resp.getBody());

        } catch (Exception e) {
            log.error("DEBUG BYTE[] IMAGE failed: {}", e.getMessage(), e);
        }
    }



    public List<String> processOcr(List<FileData> extractedFiles) {
        if (ocrApiUrl == null || ocrApiKey == null || ocrApiHost == null) {
            throw new RuntimeException("OCR API config missing");
        }

        List<String> imageProcessedData = new ArrayList<>();

        for (FileData fileData : extractedFiles) {
            String originalFilename = fileData.getFilename();
            byte[] buffer = fileData.getBuffer();

            if (originalFilename == null || buffer == null) {
                log.error("Invalid file format: Missing filename or buffer");
                continue;
            }

            File tempFile = null;
            try {
                // 1. Create Temp File (Node.js equivalent of handling file stream)
                Path tempPath = Files.createTempFile("ocr_upload_", "_" + originalFilename);
                tempFile = tempPath.toFile();
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(buffer);
                }

                log.info("Processing OCR for file: {} (size {} bytes)", originalFilename, buffer.length);

                // 2. Prepare Resource
                Resource fileResource = new FileSystemResource(tempFile);

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("image", fileResource);   // <-- key must be "image"

                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.set("X-RapidAPI-Key", ocrApiKey);
                requestHeaders.set("X-RapidAPI-Host", ocrApiHost);
// DO NOT set Content-Type (RestTemplate will set multipart/form-data with boundary)

                HttpEntity<MultiValueMap<String, Object>> requestEntity =
                        new HttpEntity<>(body, requestHeaders);

                ResponseEntity<JsonNode> response = restTemplate.exchange(
                        ocrApiUrl,
                        HttpMethod.POST,
                        requestEntity,
                        JsonNode.class
                );


                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    extractTextFromResponse(response.getBody(), imageProcessedData, originalFilename);
                } else {
                    log.error("OCR failed for {}. Status: {}", originalFilename, response.getStatusCode());
                }

            } catch (HttpClientErrorException e) {
                log.error("OCR API Error for file {}: {} - Body: {}",
                        originalFilename, e.getStatusCode(), e.getResponseBodyAsString());
            } catch (Exception ex) {
                log.error("Unexpected error in OCR for file {}: {}", originalFilename, ex.getMessage(), ex);
            } finally {
                // Clean up
                if (tempFile != null && tempFile.exists()) {
                    try { Files.delete(tempFile.toPath()); } catch (IOException ignored) {}
                }
            }
        }

        if (imageProcessedData.isEmpty()) {
            log.warn("No text was successfully extracted from any of the images.");
        }

        return imageProcessedData;
    }

    private void extractTextFromResponse(JsonNode root, List<String> data, String filename) {
        // Same parsing logic as before...
        JsonNode results = root.get("results");
        if (results != null && results.isArray()) {
            for (JsonNode resultNode : results) {
                JsonNode entities = resultNode.get("entities");
                if (entities != null && entities.isArray()) {
                    for (JsonNode entity : entities) {
                        JsonNode objects = entity.get("objects");
                        if (objects != null && objects.isArray()) {
                            for (JsonNode obj : objects) {
                                JsonNode subEntities = obj.get("entities");
                                if (subEntities != null && subEntities.isArray()) {
                                    for (JsonNode sub : subEntities) {
                                        if (sub.has("text") && sub.get("text").isTextual()) {
                                            String text = sub.get("text").asText();
                                            if (text != null && !text.isBlank()) {
                                                log.info("Found text: {}", text);
                                                data.add(text);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
