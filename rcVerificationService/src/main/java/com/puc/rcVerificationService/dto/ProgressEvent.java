package com.puc.rcVerificationService.dto;

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
    private String userId;
    private String service;
    private String step;
    private String status;
    private String message;
    private Integer progress;
    private long ts;
}
