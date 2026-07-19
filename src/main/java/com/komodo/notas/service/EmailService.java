package com.komodo.notas.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${app.resend.api-key}")
    private String apiKey;

    @Value("${app.resend.from-email}")
    private String fromEmail;

    public void sendOtpEmail(String to, String otpCode, boolean isRegistration) {
        Resend resend = new Resend(apiKey);
        
        String subject = isRegistration ? "Bienvenido a Komodo Notas - Código de Verificación" : "Komodo Notas - Recuperación de Contraseña";
        String htmlContent = "<div style='font-family: Arial, sans-serif; text-align: center; padding: 20px;'>" +
                "<h2>Komodo Notas</h2>" +
                "<p>Tu código OTP es:</p>" +
                "<h1 style='color: #8B5CF6; letter-spacing: 5px;'>" + otpCode + "</h1>" +
                "<p>Este código expira en 10 minutos.</p>" +
                "</div>";

        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .from(fromEmail)
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .build();

        try {
            resend.emails().send(sendEmailRequest);
        } catch (ResendException e) {
            e.printStackTrace();
            throw new RuntimeException("Error enviando email OTP", e);
        }
    }
}
