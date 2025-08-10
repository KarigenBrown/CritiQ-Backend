package me.critiq.backend.service.impl;

import lombok.RequiredArgsConstructor;
import me.critiq.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    @Value("${spring.mail.username}")
    private String source;
    private final JavaMailSender mailSender;

    @Override
    public void sendCode(String email, String code) {
        var mail = new SimpleMailMessage();

        mail.setSubject("CritiQ验证码");
        mail.setText("""
                亲爱的用户,
                
                感谢您使用CritiQ!
                
                您的验证码是:
                
                %s
                
                祝好,
                CritiQ团队
                """.formatted(code));
        mail.setTo(email);
        mail.setFrom(source);

        mailSender.send(mail);
    }
}
