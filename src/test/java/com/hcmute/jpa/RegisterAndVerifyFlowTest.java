package com.hcmute.jpa;

import com.hcmute.jpa.controller.RegisterController;
import com.hcmute.jpa.controller.VerifyOtpController;
import com.hcmute.jpa.entity.OtpPurpose;
import com.hcmute.jpa.entity.OtpToken;
import com.hcmute.jpa.entity.User;
import com.hcmute.jpa.service.*;
import com.hcmute.jpa.dao.IUserDao;
import com.hcmute.jpa.dao.IOtpTokenDao;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterAndVerifyFlowTest {

    private IUserService userService;
    private IOtpService otpService;
    private EmailServiceImpl emailStub;
    private IOtpTokenDao otpTokenDao;

    private String lastSentOtp;
    private String lastSentRecipient;

    @BeforeEach
    public void setUp() {
        otpTokenDao = new InMemoryOtpTokenDao();
        userService = new UserServiceImpl(new InMemoryUserDao());
        otpService = new OtpServiceImpl(otpTokenDao);

        emailStub = new EmailServiceImpl() {
            @Override
            protected void sendMessage(jakarta.mail.internet.MimeMessage message) throws jakarta.mail.MessagingException {
                try {
                    String content = (String) message.getContent();
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\d{6}");
                    java.util.regex.Matcher m = p.matcher(content);
                    if (m.find()) {
                        lastSentOtp = m.group();
                    }
                    lastSentRecipient = message.getRecipients(jakarta.mail.Message.RecipientType.TO)[0].toString();
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }
        };

        lastSentOtp = null;
        lastSentRecipient = null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T createMock(Class<T> interfaceType, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                handler
        );
    }

    private static class MockHttpContext {
        Map<String, String> parameters = new HashMap<>();
        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> session = new HashMap<>();
        String redirectUrl = null;
        boolean forwarded = false;
        String forwardedPath = null;

        HttpServletRequest request;
        HttpServletResponse response;
        HttpSession httpSession;
        RequestDispatcher requestDispatcher;

        MockHttpContext() {
            httpSession = createMock(HttpSession.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if (methodName.equals("setAttribute")) {
                    session.put((String) args[0], args[1]);
                    return null;
                } else if (methodName.equals("getAttribute")) {
                    return session.get((String) args[0]);
                } else if (methodName.equals("removeAttribute")) {
                    return session.remove((String) args[0]);
                }
                return null;
            });

            requestDispatcher = createMock(RequestDispatcher.class, (proxy, method, args) -> {
                if (method.getName().equals("forward")) {
                    forwarded = true;
                    return null;
                }
                return null;
            });

            request = createMock(HttpServletRequest.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if (methodName.equals("getParameter")) {
                    return parameters.get((String) args[0]);
                } else if (methodName.equals("setAttribute")) {
                    attributes.put((String) args[0], args[1]);
                    return null;
                } else if (methodName.equals("getAttribute")) {
                    return attributes.get((String) args[0]);
                } else if (methodName.equals("getSession")) {
                    return httpSession;
                } else if (methodName.equals("getRequestDispatcher")) {
                    forwardedPath = (String) args[0];
                    return requestDispatcher;
                } else if (methodName.equals("getContextPath")) {
                    return "";
                }
                return null;
            });

            response = createMock(HttpServletResponse.class, (proxy, method, args) -> {
                if (method.getName().equals("sendRedirect")) {
                    redirectUrl = (String) args[0];
                    return null;
                }
                return null;
            });
        }
    }

    @Test
    public void testSuccessfulRegistrationAndVerificationFlow() throws Exception {
        String unique = "flow_" + System.currentTimeMillis();
        String username = unique;
        String email = unique + "@example.com";
        String password = "SecurePassword123";

        RegisterController registerController = new RegisterController(userService, otpService, emailStub);
        MockHttpContext regContext = new MockHttpContext();
        regContext.parameters.put("username", username);
        regContext.parameters.put("email", email);
        regContext.parameters.put("password", password);
        regContext.parameters.put("confirmPassword", password);

        // 1. Submit registration
        registerController.doPost(regContext.request, regContext.response);

        // Verify registration outputs
        assertEquals("/verify-otp", regContext.redirectUrl);
        assertEquals(email, regContext.session.get("pendingVerifyEmail"));
        assertNotNull(lastSentOtp);
        assertEquals(email, lastSentRecipient);

        // Verify inactive user created
        User user = userService.findByEmail(email);
        assertNotNull(user);
        assertFalse(user.isActive());
        assertNotEquals(password, user.getPasswordHash());

        // 2. Submit valid verification
        VerifyOtpController verifyController = new VerifyOtpController(userService, otpService);
        MockHttpContext verifyContext = new MockHttpContext();
        verifyContext.session.put("pendingVerifyEmail", email);
        verifyContext.parameters.put("otp", lastSentOtp);

        verifyController.doPost(verifyContext.request, verifyContext.response);

        // Verify verification outputs
        assertTrue(verifyContext.forwarded);
        assertEquals("/views/verify-otp.jsp", verifyContext.forwardedPath);
        assertNull(verifyContext.session.get("pendingVerifyEmail")); // Cleared from session
        assertNotNull(verifyContext.attributes.get("success"));

        // Verify user is now active
        User activeUser = userService.findByEmail(email);
        assertTrue(activeUser.isActive());
    }

    @Test
    public void testRegistrationFailures() throws Exception {
        String unique = "flowfail_" + System.currentTimeMillis();
        RegisterController registerController = new RegisterController(userService, otpService, emailStub);

        // Mismatched passwords
        MockHttpContext ctx1 = new MockHttpContext();
        ctx1.parameters.put("username", unique);
        ctx1.parameters.put("email", unique + "@example.com");
        ctx1.parameters.put("password", "Pass1");
        ctx1.parameters.put("confirmPassword", "Pass2");

        registerController.doPost(ctx1.request, ctx1.response);
        assertEquals("Passwords do not match.", ctx1.attributes.get("error"));
        assertNull(userService.findByEmail(unique + "@example.com"));

        // Blank username
        MockHttpContext ctx2 = new MockHttpContext();
        ctx2.parameters.put("username", "");
        ctx2.parameters.put("email", unique + "@example.com");
        ctx2.parameters.put("password", "Pass1");
        ctx2.parameters.put("confirmPassword", "Pass1");

        registerController.doPost(ctx2.request, ctx2.response);
        assertEquals("All fields are required.", ctx2.attributes.get("error"));

        // Duplicate checks
        User existing = userService.register(unique + "_exist", unique + "_exist@example.com", "Pass1");
        MockHttpContext ctx3 = new MockHttpContext();
        ctx3.parameters.put("username", unique + "_exist");
        ctx3.parameters.put("email", unique + "_new@example.com");
        ctx3.parameters.put("password", "Pass1");
        ctx3.parameters.put("confirmPassword", "Pass1");

        registerController.doPost(ctx3.request, ctx3.response);
        assertEquals("Username is already registered.", ctx3.attributes.get("error"));

        MockHttpContext ctx4 = new MockHttpContext();
        ctx4.parameters.put("username", unique + "_invalid_email");
        ctx4.parameters.put("email", "invalid-email");
        ctx4.parameters.put("password", "Pass1");
        ctx4.parameters.put("confirmPassword", "Pass1");

        registerController.doPost(ctx4.request, ctx4.response);
        assertEquals("Please enter a valid email address.", ctx4.attributes.get("error"));
    }

    @Test
    public void testEmailFailureDoesNotActivateOrExposeSecrets() throws Exception {
        String email = "mail-failure@example.com";
        IEmailService failingEmail = (to, otp, purpose) -> false;
        RegisterController registerController = new RegisterController(userService, otpService, failingEmail);
        MockHttpContext context = new MockHttpContext();
        context.parameters.put("username", "mail_failure_user");
        context.parameters.put("email", email);
        context.parameters.put("password", "SecurePassword123");
        context.parameters.put("confirmPassword", "SecurePassword123");

        registerController.doPost(context.request, context.response);

        assertEquals("Failed to send verification email. Please try again.", context.attributes.get("error"));
        assertNull(context.session.get("pendingVerifyEmail"));
        assertNull(context.session.get("password"));
        assertNull(context.session.get("otp"));
        assertFalse(userService.findByEmail(email).isActive());
    }

    @Test
    public void testVerificationFailures() throws Exception {
        String unique = "flowvfail_" + System.currentTimeMillis();
        User user = userService.register(unique, unique + "@example.com", "Pass1");
        String otp = otpService.generateOtp(user, OtpPurpose.REGISTER);

        VerifyOtpController verifyController = new VerifyOtpController(userService, otpService);

        // Wrong OTP
        MockHttpContext ctx1 = new MockHttpContext();
        ctx1.session.put("pendingVerifyEmail", user.getEmail());
        ctx1.parameters.put("otp", "000000");

        verifyController.doPost(ctx1.request, ctx1.response);
        assertEquals("Invalid, expired, or blocked verification code.", ctx1.attributes.get("error"));
        assertFalse(userService.findByEmail(user.getEmail()).isActive());

        // Expired OTP
        String newOtp = otpService.generateOtp(user, OtpPurpose.REGISTER);
        OtpToken token = otpTokenDao.findLatestValidByUserAndPurpose(user, OtpPurpose.REGISTER);
        token.setExpiresAt(new Timestamp(System.currentTimeMillis() - 1000));
        otpTokenDao.update(token);

        MockHttpContext ctx2 = new MockHttpContext();
        ctx2.session.put("pendingVerifyEmail", user.getEmail());
        ctx2.parameters.put("otp", newOtp);

        verifyController.doPost(ctx2.request, ctx2.response);
        assertEquals("Invalid, expired, or blocked verification code.", ctx2.attributes.get("error"));

        // Purpose isolation bypass (Forgot Password OTP cannot activate registration verification)
        // Generate FORGOT_PASSWORD OTP
        String forgotOtp = otpService.generateOtp(user, OtpPurpose.FORGOT_PASSWORD);
        MockHttpContext ctx3 = new MockHttpContext();
        ctx3.session.put("pendingVerifyEmail", user.getEmail());
        ctx3.parameters.put("otp", forgotOtp);

        verifyController.doPost(ctx3.request, ctx3.response);
        assertEquals("Invalid, expired, or blocked verification code.", ctx3.attributes.get("error"));
        assertFalse(userService.findByEmail(user.getEmail()).isActive());
    }

    @Test
    public void testVerificationBypassProtection() throws Exception {
        VerifyOtpController verifyController = new VerifyOtpController(userService, otpService);
        MockHttpContext ctx = new MockHttpContext();
        // No "pendingVerifyEmail" in session

        verifyController.doGet(ctx.request, ctx.response);
        assertEquals("/register", ctx.redirectUrl);
    }

    private static final class InMemoryUserDao implements IUserDao {
        private final Map<Integer, User> users = new HashMap<>();
        private int nextId = 1;

        @Override
        public void create(User user) {
            user.setId(nextId++);
            users.put(user.getId(), user);
        }

        @Override
        public User findById(int id) { return users.get(id); }

        @Override
        public User findByEmail(String email) {
            return users.values().stream().filter(u -> email.equals(u.getEmail())).findFirst().orElse(null);
        }

        @Override
        public User findByUsername(String username) {
            return users.values().stream().filter(u -> username.equals(u.getUsername())).findFirst().orElse(null);
        }

        @Override
        public boolean existsByEmail(String email) { return findByEmail(email) != null; }

        @Override
        public boolean existsByUsername(String username) { return findByUsername(username) != null; }

        @Override
        public void update(User user) { users.put(user.getId(), user); }
    }

    private static final class InMemoryOtpTokenDao implements IOtpTokenDao {
        private final Map<Integer, OtpToken> tokens = new HashMap<>();
        private int nextId = 1;

        @Override
        public void create(OtpToken token) {
            token.setId(nextId++);
            tokens.put(token.getId(), token);
        }

        @Override
        public void update(OtpToken token) { tokens.put(token.getId(), token); }

        @Override
        public OtpToken findById(int id) { return tokens.get(id); }

        @Override
        public OtpToken findLatestValidByUserAndPurpose(User user, OtpPurpose purpose) {
            return tokens.values().stream()
                    .filter(t -> t.getUser().getId() == user.getId())
                    .filter(t -> t.getPurpose() == purpose)
                    .filter(t -> !t.isUsed())
                    .filter(t -> t.getExpiresAt().after(new Timestamp(System.currentTimeMillis())))
                    .filter(t -> t.getAttempts() < 5)
                    .findFirst().orElse(null);
        }

        @Override
        public void invalidateExistingByUserAndPurpose(User user, OtpPurpose purpose) {
            tokens.values().stream()
                    .filter(t -> t.getUser().getId() == user.getId())
                    .filter(t -> t.getPurpose() == purpose)
                    .filter(t -> !t.isUsed())
                    .forEach(t -> t.setUsed(true));
        }
    }
}
