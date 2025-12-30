package com.puc.realTimeUpdateService.dto;

import lombok.Data;

@Data
public class ProgressEvent {
    private String jobId;
    private String userId;   // MUST be username, e.g. "vinay"
    private String step;     // e.g. RC_VERIFY_STARTED
    private String status;   // STARTED, DONE, FAILED
    private String message;  // optional
    private Integer progress; // optional 0..100
}
