package com.hcmute.jpa;

import com.hcmute.jpa.entity.OtpPurpose;
import com.hcmute.jpa.service.EmailServiceImpl;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EmailServiceTest {

    private TestEmailService emailService;
    private MimeMessage lastSentMessage;

    private class TestEmailService extends EmailServiceImpl {
        @Override
        protected void sendMessage(MimeMessage message) throws MessagingException {
            lastSentMessage = message;
        }

        private String mockHost;
        private String mockPort;
        private String mockUser;
        private String mockPassword;
        private String mockAuth;
        private String mockStarttls;

        public void setSmtpConfig(String host, String port, String user, String pass, String auth, String starttls) {
            this.mockHost = host;
            this.mockPort = port;
            this.mockUser = user;
            this.mockPassword = pass;
            this.mockAuth = auth;
            this.mockStarttls = starttls;
        }

        @Override
        protected String getSmtpConfig(String key) {
            switch (key) {
                case "SMTP_HOST": return mockHost;
                case "SMTP_PORT": return mockPort;
                case "SMTP_USERNAME": return mockUser;
                case "SMTP_PASSWORD": return mockPassword;
                case "SMTP_AUTH": return mockAuth;
                case "SMTP_STARTTLS": return mockStarttls;
                default: return super.getSmtpConfig(key);
            }
        }
    }

    @BeforeEach
    public void setUp() {
        emailService = new TestEmailService();
        lastSentMessage = null;
    }

    @Test
    public void testSendOtpEmailRegister() throws Exception {
        String to = "test@example.com";
        String otp = "987654";

        boolean result = emailService.sendOtpEmail(to, otp, OtpPurpose.REGISTER);

        assertTrue(result);
        assertNotNull(lastSentMessage);

        assertEquals("Verify your Registration", lastSentMessage.getSubject());

        String content = (String) lastSentMessage.getContent();
        assertTrue(content.contains(otp));
        assertTrue(content.contains("10 minutes"));
        assertTrue(content.contains("registration"));

        assertEquals(to, lastSentMessage.getRecipients(Message.RecipientType.TO)[0].toString());
    }

    @Test
    public void testSendOtpEmailForgotPassword() throws Exception {
        String to = "user@domain.com";
        String otp = "123456";

        boolean result = emailService.sendOtpEmail(to, otp, OtpPurpose.FORGOT_PASSWORD);

        assertTrue(result);
        assertNotNull(lastSentMessage);

        assertEquals("Password Reset Request", lastSentMessage.getSubject());

        String content = (String) lastSentMessage.getContent();
        assertTrue(content.contains(otp));
        assertTrue(content.contains("10 minutes"));
        assertTrue(content.contains("password reset"));

        assertEquals(to, lastSentMessage.getRecipients(Message.RecipientType.TO)[0].toString());
    }

    @Test
    public void testInvalidEmailRecipients() {
        assertFalse(emailService.sendOtpEmail(null, "123456", OtpPurpose.REGISTER));
        assertNull(lastSentMessage);

        assertFalse(emailService.sendOtpEmail("   ", "123456", OtpPurpose.REGISTER));
        assertNull(lastSentMessage);

        assertFalse(emailService.sendOtpEmail("invalid-email", "123456", OtpPurpose.REGISTER));
        assertNull(lastSentMessage);

        assertFalse(emailService.sendOtpEmail("user@domain", "123456", OtpPurpose.REGISTER));
        assertNull(lastSentMessage);
    }

    @Test
    public void testInvalidOtpIsRejected() {
        assertFalse(emailService.sendOtpEmail("test@example.com", "12345", OtpPurpose.REGISTER));
        assertNull(lastSentMessage);

        assertFalse(emailService.sendOtpEmail("test@example.com", "abcdef", OtpPurpose.REGISTER));
        assertNull(lastSentMessage);
    }

    @Test
    public void testSmtpConfigurationParsing() {
        emailService.setSmtpConfig("smtp.google.com", "587", "user", "secret", "true", "true");
        boolean result = emailService.sendOtpEmail("test@example.com", "123456", OtpPurpose.REGISTER);
        assertTrue(result);
        assertNotNull(lastSentMessage);
    }
}
