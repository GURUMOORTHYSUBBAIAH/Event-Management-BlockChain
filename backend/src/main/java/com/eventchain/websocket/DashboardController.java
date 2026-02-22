package com.eventchain.websocket;

import com.eventchain.dto.EventAnalyticsDto;
import com.eventchain.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final EventService eventService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/event/{eventId}/analytics")
    public void getAnalytics(@DestinationVariable Long eventId) {
        try {
            EventAnalyticsDto analytics = eventService.getAnalytics(eventId, null);
            messagingTemplate.convertAndSend("/topic/event/" + eventId + "/analytics", analytics);
        } catch (Exception ignored) {}
    }
}
