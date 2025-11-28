package com.puc.modelApiService.service;

import com.puc.modelApiService.dto.VehicleDetailsDto;
import com.puc.modelApiService.external.RcVerificationFeignClient;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    private final RestTemplate restTemplate;

    private final RcVerificationFeignClient rcVerificationFeignClient;

    public byte[] cropImage(MultipartFile image) throws IOException {
        // existing cropping logic using restTemplate
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new MultipartInputStreamFileResource(
                image.getInputStream(), image.getOriginalFilename()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                "http://3.109.124.158:5000/predict",
                HttpMethod.POST,
                requestEntity,
                byte[].class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            log.info("Successfully cropped image, size: {} bytes", response.getBody().length);
            return response.getBody();
        } else {
            String errorMsg = "Failed to call model API: " + response.getStatusCode();
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    public VehicleDetailsDto cropAndVerify(MultipartFile image) throws IOException {
        // 1) Crop the image
        byte[] croppedBytes = cropImage(image);

        // 2) Wrap cropped bytes into MultipartFile
        MultipartFile croppedMultipartFile = new MockMultipartFile(
                "images",               // parameter name expected by rcVerificationService
                "cropped_" + image.getOriginalFilename(),
                MediaType.IMAGE_JPEG_VALUE.toString(),
                croppedBytes);

        // 3) Call rcVerificationService via Feign client with cropped image wrapped in a list
        return rcVerificationFeignClient.verifyVehicle(List.of(croppedMultipartFile));
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
}
