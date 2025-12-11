package com.bankcore.notification.api;

import com.bankcore.notification.model.NotificationEvent;
import com.bankcore.notification.model.NotificationMessage;
import com.bankcore.notification.model.NotificationRequest;
import com.bankcore.notification.service.NotificationDispatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationDispatcher dispatcher;

    public NotificationController(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> send(@Valid @RequestBody NotificationRequest request) {
        dispatcher.dispatch(request);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "queued");
        return ResponseEntity.accepted().body(response);
    }

    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> receiveEvent(@Valid @RequestBody NotificationEvent event) {
        Map<String, Object> response = new HashMap<>();
        dispatcher.dispatch(mapEvent(event));
        response.put("status", "queued");
        response.put("referenceId", event.getReferenceId());
        response.put("eventType", event.getEventType());
        return ResponseEntity.accepted().body(response);
    }

    private NotificationMessage mapEvent(NotificationEvent event) {
        NotificationMessage message = new NotificationMessage();
        message.setChannel(event.getChannel());
        message.setDestination(event.getDestination());
        message.setContent(event.getMessage());
        message.setSubject(event.getSubject());
        message.setReferenceId(event.getReferenceId());
        message.setEventType(event.getEventType());
        return message;
    }
}
