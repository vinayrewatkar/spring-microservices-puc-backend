package com.puc.rcVerificationService.controller;

import com.puc.rcVerificationService.service.OcrProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ocr-debug")
@RequiredArgsConstructor
public class QuickController {

    private final OcrProcessingService ocrProcessingService;

    @GetMapping("/local-image")
    public ResponseEntity<String> testLocalImageOcr() {
        ocrProcessingService.debugWithLocalImage();
        return ResponseEntity.ok("Triggered debugWithLocalImage. Check server logs.");
    }
}
