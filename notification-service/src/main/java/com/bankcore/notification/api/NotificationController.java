package com.bankcore.notification.api;

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
}
