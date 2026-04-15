package com.puneetchhabra.EmailConsumer.service;

import com.puneetchhabra.EmailConsumer.models.EmailRequest;
import com.puneetchhabra.EmailConsumer.models.SendEmailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public SendEmailResponse sendEmail(EmailRequest emailRequest) {
        log.info("Bắt đầu gửi email tới: {}", emailRequest.getEmailId());

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(senderEmail);
            mailMessage.setTo(emailRequest.getEmailId());
            mailMessage.setSubject(emailRequest.getEmailSubject());
            mailMessage.setText(emailRequest.getMessage());

            mailSender.send(mailMessage);

            log.info("Gửi email thành công tới: {}", emailRequest.getEmailId());

            return SendEmailResponse.builder()
                    .status(200) // Trả về 200 cho thành công
                    .message("Email sent successfully to " + emailRequest.getEmailId())
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi gửi mail qua Gmail: {}", e.getMessage());

            return SendEmailResponse.builder()
                    .status(500) // Trả về 500 khi có lỗi
                    .message("Failed to send email: " + e.getMessage())
                    .build();
        }
    }
}