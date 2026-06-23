package com.example.be_foodgo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@foodgo.com}")
    private String fromEmail;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void guiEmailXacThuc(String toEmail, String otpCode) {
        String subject = "Ma xac thuc email - FoodGo";
        String body = buildEmailBody(otpCode);
        guiEmail(toEmail, subject, body);
    }

    @Async
    public void guiEmailQuenMatKhau(String toEmail, String otpCode) {
        String subject = "Ma xac thuc dat lai mat khau - FoodGo";
        String body = buildResetPasswordBody(otpCode);
        guiEmail(toEmail, subject, body);
    }

    private void guiEmail(String to, String subject, String htmlBody) {
        if (!emailEnabled) {
            log.info("[Email Disabled] To: {}, Subject: {}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email da gui thanh cong den: {}", to);
        } catch (MessagingException e) {
            log.error("Loi gui email den {}: {}", to, e.getMessage());
        }
    }

    private String buildEmailBody(String otpCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { background: #ffffff; border-radius: 10px; padding: 30px; max-width: 500px; margin: auto; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; color: #FF6B35; font-size: 28px; font-weight: bold; margin-bottom: 10px; }
                    .title { color: #333; font-size: 20px; text-align: center; margin-bottom: 20px; }
                    .otp-box { background: #FFF3E0; border: 2px dashed #FF6B35; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }
                    .otp-code { font-size: 36px; font-weight: bold; color: #FF6B35; letter-spacing: 8px; }
                    .note { color: #666; font-size: 14px; text-align: center; margin-top: 15px; }
                    .footer { text-align: center; color: #999; font-size: 12px; margin-top: 20px; border-top: 1px solid #eee; padding-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">FoodGo</div>
                    <div class="title">Xac thuc email</div>
                    <p>Xin chao,</p>
                    <p>Ma xac thuc cua ban la:</p>
                    <div class="otp-box">
                        <div class="otp-code">%s</div>
                    </div>
                    <p class="note">Ma nay co hieu luc trong 5 phut. Vui long khong chia se ma nay voi bat ky ai.</p>
                    <div class="footer">
                        Neu ban khong yeu cau xac thuc email nay, vui long bo qua email nay.<br>
                        &copy; 2026 FoodGo
                    </div>
                </div>
            </body>
            </html>
            """.formatted(otpCode);
    }

    private String buildResetPasswordBody(String otpCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { background: #ffffff; border-radius: 10px; padding: 30px; max-width: 500px; margin: auto; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; color: #FF6B35; font-size: 28px; font-weight: bold; margin-bottom: 10px; }
                    .title { color: #333; font-size: 20px; text-align: center; margin-bottom: 20px; }
                    .otp-box { background: #FFF3E0; border: 2px dashed #FF6B35; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }
                    .otp-code { font-size: 36px; font-weight: bold; color: #FF6B35; letter-spacing: 8px; }
                    .note { color: #666; font-size: 14px; text-align: center; margin-top: 15px; }
                    .footer { text-align: center; color: #999; font-size: 12px; margin-top: 20px; border-top: 1px solid #eee; padding-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">FoodGo</div>
                    <div class="title">Dat lai mat khau</div>
                    <p>Xin chao,</p>
                    <p>Ma xac thuc dat lai mat khau cua ban la:</p>
                    <div class="otp-box">
                        <div class="otp-code">%s</div>
                    </div>
                    <p class="note">Ma nay co hieu luc trong 5 phut. Vui long khong chia se ma nay voi bat ky ai.</p>
                    <div class="footer">
                        Neu ban khong yeu cau dat lai mat khau, vui long bo qua email nay.<br>
                        &copy; 2026 FoodGo
                    </div>
                </div>
            </body>
            </html>
            """.formatted(otpCode);
    }
}
