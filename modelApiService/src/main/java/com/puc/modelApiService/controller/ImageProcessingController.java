package com.puc.modelApiService.controller;

import com.puc.modelApiService.service.ImageProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ImageProcessingController {

    private final ImageProcessingService imageProcessingService;

    @PostMapping(value = "/crop-image", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> cropImage(@RequestParam("image") MultipartFile image) {
        try {
            byte[] croppedImage = imageProcessingService.cropImage(image);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(croppedImage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

