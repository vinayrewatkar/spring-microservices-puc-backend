package com.puc.modelApiService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressEvent {
    private String jobId;
    private String userId;     // username: "vinay"
    private String service;    // "modelApiService"
    private String step;       // e.g. "MODEL_REQUEST_STARTED"
    private String status;     // STARTED / DONE / FAILED
    private String message;
    private Integer progress;  // 0..100 optional
    private long ts;
}
