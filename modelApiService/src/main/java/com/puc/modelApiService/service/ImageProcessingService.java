package com.puc.modelApiService.service;

import com.puc.modelApiService.dto.ProgressEvent;
import com.puc.modelApiService.dto.VehicleDetailsDto;
import com.puc.modelApiService.external.RcVerificationFeignClient;
import com.puc.modelApiService.kafka.ProgressPublisher;
import com.puc.modelApiService.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    private final RestTemplate restTemplate;

    private final RcVerificationFeignClient rcVerificationFeignClient;

    private final ProgressPublisher progressPublisher;

    private final JwtUtil jwtUtil;

    public byte[] cropImage(MultipartFile image) throws IOException {
        // existing cropping logic using restTemplate
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartInputStreamFileResource(
                image.getInputStream(), image.getOriginalFilename()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        log.info("making model request");
        ResponseEntity<byte[]> response = restTemplate.exchange(
                "http://localhost:5000/predict",
                HttpMethod.POST,
                requestEntity,
                byte[].class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            byte[] zipBytes = response.getBody();
            byte[] croppedJpgBytes = extractFirstJpg(zipBytes);
            return croppedJpgBytes;
        } else {
            String errorMsg = "Failed to call model API: " + response.getStatusCode();
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    public VehicleDetailsDto cropAndVerify(MultipartFile image, String token) throws IOException {
        String jobId = UUID.randomUUID().toString();
        String username = jwtUtil.extractUsername(token); // you must have JwtUtil in this service

        progressPublisher.publish(event(jobId, username, "MODEL_PIPELINE_STARTED", "STARTED", "Started", 0));

        // 1) Crop
        progressPublisher.publish(event(jobId, username, "MODEL_CROP_STARTED", "STARTED", "Cropping", 10));
        byte[] croppedBytes = cropImage(image);
        progressPublisher.publish(event(jobId, username, "MODEL_CROP_DONE", "DONE", "Crop done", 40));

        // 2) Call rcVerification (Feign)
        MultipartFile croppedMultipartFile = new MockMultipartFile(
                "file",
                "cropped_" + image.getOriginalFilename(),
                MediaType.IMAGE_JPEG_VALUE.toString(),
                croppedBytes
        );

        progressPublisher.publish(event(jobId, username, "RC_VERIFY_STARTED", "STARTED", "Calling RC verification", 50));
        VehicleDetailsDto dto = rcVerificationFeignClient.verifyVehicle(
                jobId,
                username,
                List.of(croppedMultipartFile)
        );
        progressPublisher.publish(event(jobId, username, "RC_VERIFY_DONE", "DONE", "RC verification finished", 90));

        progressPublisher.publish(event(jobId, username, "MODEL_PIPELINE_DONE", "DONE", "Completed", 100));
        return dto;
    }


    private static class MultipartInputStreamFileResource extends InputStreamResource {
        private final String filename;

        public MultipartInputStreamFileResource(InputStream inputStream, String filename) {
            super(inputStream);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

        @Override
        public long contentLength() throws IOException {
            return -1;
        }
    }
    private byte[] extractFirstJpg(byte[] zipBytes) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".jpg")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    zis.transferTo(baos);
                    return baos.toByteArray();
                }
            }
        }
        throw new IllegalStateException("No .jpg found in zip response");
    }
    private ProgressEvent event(String jobId, String userId, String step, String status, String msg, Integer progress) {
        return ProgressEvent.builder()
                .jobId(jobId)
                .userId(userId)
                .service("modelApiService")
                .step(step)
                .status(status)
                .message(msg)
                .progress(progress)
                .ts(System.currentTimeMillis())
                .build();
    }

}
