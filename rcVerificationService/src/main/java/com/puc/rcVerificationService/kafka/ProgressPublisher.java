package com.puc.rcVerificationService.kafka;

import com.puc.rcVerificationService.dto.ProgressEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgressPublisher {

    private final KafkaTemplate<String, ProgressEvent> kafkaTemplate;

    @Value("${puc.progress.topic}")
    private String topic;

    public void publish(ProgressEvent event) {
        kafkaTemplate.send(topic, event.getJobId(), event);
    }
}
