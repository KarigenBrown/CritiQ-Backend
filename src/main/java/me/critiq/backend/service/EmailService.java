package me.critiq.backend.service;

import java.util.concurrent.CompletableFuture;

public interface EmailService {
    void sendCode(String email, String code);
    CompletableFuture<String> testSend();
}
