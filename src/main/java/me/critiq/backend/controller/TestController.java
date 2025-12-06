package me.critiq.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    private final EmailService emailService;

    @GetMapping("/email")
    public ResponseEntity<String> testEmail() {
        var future = emailService.testSend();
        future.thenAccept(log::info);
        return ResponseEntity.ok("async success");
    }

    @GetMapping("/insecure")
    public ResponseEntity<String> insecure() {
        return ResponseEntity.ok("insecure");
    }

    @GetMapping("/secure")
    public ResponseEntity<String> secure() {
        return ResponseEntity.ok("secure");
    }
}
