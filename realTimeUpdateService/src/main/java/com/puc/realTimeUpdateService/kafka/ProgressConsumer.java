package com.puc.realTimeUpdateService.kafka;

import com.puc.realTimeUpdateService.dto.ProgressEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProgressConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "puc.processing.progress", groupId = "realtime-updates")
    public void consume(ProgressEvent event) {
        messagingTemplate.convertAndSendToUser(
                event.getUserId(),
                "/queue/progress",
                event
        );
    }
}