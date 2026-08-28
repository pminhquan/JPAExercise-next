package com.hcmute.jpa.service;

import com.hcmute.jpa.entity.OtpPurpose;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailServiceImpl implements IEmailService {

    protected String getSmtpConfig(String key) {
        String val = System.getenv(key);
        if (val == null) {
            val = System.getProperty(key);
        }
        return val;
    }

    @Override
    public boolean sendOtpEmail(String to, String otp, OtpPurpose purpose) {
        if (to == null || to.trim().isEmpty() || !isValidEmail(to)) {
            return false;
        }
        if (otp == null || !otp.matches("\\d{6}") || purpose == null) {
            return false;
        }

        try {
            Session session = getSession();
            MimeMessage message = createMessage(session, to, otp, purpose);
            sendMessage(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected Session getSession() {
        String host = getSmtpConfig("SMTP_HOST");
        String port = getSmtpConfig("SMTP_PORT");
        String username = getSmtpConfig("SMTP_USERNAME");
        String password = getSmtpConfig("SMTP_PASSWORD");
        String auth = getSmtpConfig("SMTP_AUTH");
        String starttls = getSmtpConfig("SMTP_STARTTLS");

        Properties props = new Properties();
        if (host != null) props.put("mail.smtp.host", host);
        if (port != null) props.put("mail.smtp.port", port);
        if (auth != null) props.put("mail.smtp.auth", auth);
        if (starttls != null) props.put("mail.smtp.starttls.enable", starttls);

        if (host == null) props.put("mail.smtp.host", "localhost");
        if (port == null) props.put("mail.smtp.port", "25");

        if (Boolean.parseBoolean(auth)) {
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        } else {
            return Session.getInstance(props);
        }
    }

    protected MimeMessage createMessage(Session session, String to, String otp, OtpPurpose purpose) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("no-reply@example.com"));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));

        String subject;
        String body;

        if (purpose == OtpPurpose.REGISTER) {
            subject = "Verify your Registration";
            body = "Welcome! Your registration OTP is " + otp + ". This code expires in 10 minutes.";
        } else {
            subject = "Password Reset Request";
            body = "Hello! Your password reset OTP is " + otp + ". This code expires in 10 minutes.";
        }

        message.setSubject(subject);
        message.setText(body);
        return message;
    }

    protected void sendMessage(MimeMessage message) throws MessagingException {
        Transport.send(message);
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
