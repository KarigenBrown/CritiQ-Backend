package me.critiq.backend.service;

public interface EmailService {
    void sendCode(String email, String code);
}
