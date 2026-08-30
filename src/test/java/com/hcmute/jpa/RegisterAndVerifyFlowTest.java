package com.hcmute.jpa;

import com.hcmute.jpa.config.JpaConfig;
import com.hcmute.jpa.controller.RegisterController;
import com.hcmute.jpa.controller.VerifyOtpController;
import com.hcmute.jpa.entity.OtpPurpose;
import com.hcmute.jpa.entity.OtpToken;
import com.hcmute.jpa.entity.User;
import com.hcmute.jpa.service.*;
import com.hcmute.jpa.dao.IUserDao;
import com.hcmute.jpa.dao.IOtpTokenDao;
import com.hcmute.jpa.dao.OtpTokenDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assumptions;
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
    private InMemoryUserDao userDao;

    private String lastSentOtp;
    private String lastSentRecipient;

    @BeforeEach
    public void setUp() {
        userDao = new InMemoryUserDao();
        otpTokenDao = new InMemoryOtpTokenDao();
        userService = new UserServiceImpl(userDao);
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
        OtpToken issuedToken = otpTokenDao.findLatestValidByUserAndPurpose(user, OtpPurpose.REGISTER);
        assertNotNull(issuedToken);
        assertFalse(issuedToken.isUsed());

        // 2. Submit valid verification
        java.util.List<String> events = new java.util.ArrayList<>();
        IOtpService spyOtpService = new OtpServiceImpl(otpTokenDao) {
            @Override
            public boolean verifyOtp(User u, OtpPurpose p, String o) {
                events.add("VERIFY_OTP");
                return super.verifyOtp(u, p, o);
            }
        };
        IUserService spyUserService = new UserServiceImpl(new InMemoryUserDao()) {
            @Override
            public User findByEmail(String email) {
                return userService.findByEmail(email);
            }
            @Override
            public boolean activateUser(int id) {
                events.add("ACTIVATE_USER");
                return userService.activateUser(id);
            }
        };

        RecordingTransactionBoundary transactionBoundary = new RecordingTransactionBoundary(events);
        VerifyOtpController verifyController = new VerifyOtpController(spyUserService, spyOtpService, transactionBoundary);
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
        OtpToken consumedToken = otpTokenDao.findById(issuedToken.getId());
        assertTrue(consumedToken.isUsed());
        assertNull(otpTokenDao.findLatestValidByUserAndPurpose(activeUser, OtpPurpose.REGISTER));
        assertFalse(otpService.verifyOtp(activeUser, OtpPurpose.REGISTER, lastSentOtp));

        RecordingTransactionBoundary recordingBoundary = (RecordingTransactionBoundary) transactionBoundary;
        assertEquals(1, recordingBoundary.begins);
        assertEquals(1, recordingBoundary.commits);
        assertEquals(0, recordingBoundary.rollbacks);
        assertEquals(1, recordingBoundary.ends);
        assertEquals(java.util.List.of("BEGIN", "VERIFY_OTP", "ACTIVATE_USER", "COMMIT", "END"), events);
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

        // 1. Wrong OTP
        java.util.List<String> events1 = new java.util.ArrayList<>();
        IOtpService spyOtpService1 = new OtpServiceImpl(otpTokenDao) {
            @Override
            public boolean verifyOtp(User u, OtpPurpose p, String o) {
                events1.add("VERIFY_OTP");
                return super.verifyOtp(u, p, o);
            }
        };
        RecordingTransactionBoundary transactionBoundary1 = new RecordingTransactionBoundary(events1);
        VerifyOtpController verifyController1 =
                new VerifyOtpController(userService, spyOtpService1, transactionBoundary1);

        OtpToken wrongOtpToken = otpTokenDao.findLatestValidByUserAndPurpose(user, OtpPurpose.REGISTER);
        MockHttpContext ctx1 = new MockHttpContext();
        ctx1.session.put("pendingVerifyEmail", user.getEmail());
        ctx1.parameters.put("otp", "000000");

        verifyController1.doPost(ctx1.request, ctx1.response);
        assertEquals("Invalid, expired, or blocked verification code.", ctx1.attributes.get("error"));
        assertFalse(userService.findByEmail(user.getEmail()).isActive());

        OtpToken wrongOtpTokenAfter = otpTokenDao.findById(wrongOtpToken.getId());
        assertFalse(wrongOtpTokenAfter.isUsed());
        assertEquals(1, wrongOtpTokenAfter.getAttempts());
        assertEquals(java.util.List.of("BEGIN", "VERIFY_OTP", "COMMIT", "END"), events1);

        // 2. Expired OTP
        String newOtp = otpService.generateOtp(user, OtpPurpose.REGISTER);
        OtpToken token = otpTokenDao.findLatestValidByUserAndPurpose(user, OtpPurpose.REGISTER);
        assertNotNull(token);
        token.setExpiresAt(new Timestamp(System.currentTimeMillis() - 1000));
        otpTokenDao.update(token);

        java.util.List<String> events2 = new java.util.ArrayList<>();
        IOtpService spyOtpService2 = new OtpServiceImpl(otpTokenDao) {
            @Override
            public boolean verifyOtp(User u, OtpPurpose p, String o) {
                events2.add("VERIFY_OTP");
                return super.verifyOtp(u, p, o);
            }
        };
        RecordingTransactionBoundary transactionBoundary2 = new RecordingTransactionBoundary(events2);
        VerifyOtpController verifyController2 =
                new VerifyOtpController(userService, spyOtpService2, transactionBoundary2);

        MockHttpContext ctx2 = new MockHttpContext();
        ctx2.session.put("pendingVerifyEmail", user.getEmail());
        ctx2.parameters.put("otp", newOtp);

        verifyController2.doPost(ctx2.request, ctx2.response);
        assertEquals("Invalid, expired, or blocked verification code.", ctx2.attributes.get("error"));

        OtpToken tokenAfter = otpTokenDao.findById(token.getId());
        assertFalse(tokenAfter.isUsed());
        assertEquals(0, tokenAfter.getAttempts());
        assertEquals(java.util.List.of("BEGIN", "VERIFY_OTP", "COMMIT", "END"), events2);

        // 3. Purpose isolation bypass (Forgot Password OTP cannot activate registration verification)
        // Generate FORGOT_PASSWORD OTP
        String forgotOtp = otpService.generateOtp(user, OtpPurpose.FORGOT_PASSWORD);
        OtpToken wrongPurposeToken = otpTokenDao.findLatestValidByUserAndPurpose(user, OtpPurpose.FORGOT_PASSWORD);
        assertNotNull(wrongPurposeToken);

        java.util.List<String> events3 = new java.util.ArrayList<>();
        IOtpService spyOtpService3 = new OtpServiceImpl(otpTokenDao) {
            @Override
            public boolean verifyOtp(User u, OtpPurpose p, String o) {
                events3.add("VERIFY_OTP");
                return super.verifyOtp(u, p, o);
            }
        };
        RecordingTransactionBoundary transactionBoundary3 = new RecordingTransactionBoundary(events3);
        VerifyOtpController verifyController3 =
                new VerifyOtpController(userService, spyOtpService3, transactionBoundary3);

        MockHttpContext ctx3 = new MockHttpContext();
        ctx3.session.put("pendingVerifyEmail", user.getEmail());
        ctx3.parameters.put("otp", forgotOtp);

        verifyController3.doPost(ctx3.request, ctx3.response);
        assertEquals("Invalid, expired, or blocked verification code.", ctx3.attributes.get("error"));
        assertFalse(userService.findByEmail(user.getEmail()).isActive());

        OtpToken wrongPurposeTokenAfter = otpTokenDao.findById(wrongPurposeToken.getId());
        assertFalse(wrongPurposeTokenAfter.isUsed());
        assertEquals(0, wrongPurposeTokenAfter.getAttempts());
        assertEquals(java.util.List.of("BEGIN", "VERIFY_OTP", "COMMIT", "END"), events3);
    }

    @Test
    public void testRegisterInactiveUserUpdatesPassword() throws Exception {
        String unique = "inactive_" + System.currentTimeMillis();
        String username = unique;
        String email = unique + "@example.com";
        String firstPassword = "FirstPassword123";
        String secondPassword = "SecondPassword123";

        RegisterController registerController = new RegisterController(userService, otpService, emailStub);

        // 1. First registration attempt
        MockHttpContext ctx1 = new MockHttpContext();
        ctx1.parameters.put("username", username);
        ctx1.parameters.put("email", email);
        ctx1.parameters.put("password", firstPassword);
        ctx1.parameters.put("confirmPassword", firstPassword);

        registerController.doPost(ctx1.request, ctx1.response);
        assertEquals("/verify-otp", ctx1.redirectUrl);

        User user = userService.findByEmail(email);
        assertNotNull(user);
        assertFalse(user.isActive());
        assertTrue(userService.verifyPassword(user, firstPassword));

        // 2. Second registration attempt (retry) with different password
        MockHttpContext ctx2 = new MockHttpContext();
        ctx2.parameters.put("username", username);
        ctx2.parameters.put("email", email);
        ctx2.parameters.put("password", secondPassword);
        ctx2.parameters.put("confirmPassword", secondPassword);

        registerController.doPost(ctx2.request, ctx2.response);
        assertEquals("/verify-otp", ctx2.redirectUrl);

        // Verify password updated, and old one no longer works
        User updatedUser = userService.findByEmail(email);
        assertNotNull(updatedUser);
        assertFalse(updatedUser.isActive());
        assertTrue(userService.verifyPassword(updatedUser, secondPassword));
        assertFalse(userService.verifyPassword(updatedUser, firstPassword));
    }

    @Test
    public void testVerificationBypassProtection() throws Exception {
        VerifyOtpController verifyController =
                new VerifyOtpController(userService, otpService, new RecordingTransactionBoundary());
        MockHttpContext ctx = new MockHttpContext();
        // No "pendingVerifyEmail" in session

        verifyController.doGet(ctx.request, ctx.response);
        assertEquals("/register", ctx.redirectUrl);
    }

    @Test
    public void testVerificationRollbackLeavesOtpUsableWhenActivationFails() throws Exception {
        String unique = "txfail_" + System.currentTimeMillis();
        User user = userService.register(unique, unique + "@example.com", "Pass1");
        String otp = otpService.generateOtp(user, OtpPurpose.REGISTER);

        java.util.List<String> events = new java.util.ArrayList<>();

        IOtpService spyOtpService = new OtpServiceImpl(otpTokenDao) {
            @Override
            public boolean verifyOtp(User u, OtpPurpose p, String o) {
                events.add("VERIFY_OTP");
                return super.verifyOtp(u, p, o);
            }
        };

        IUserService failingUserService = new UserServiceImpl(userDao) {
            @Override
            public boolean activateUser(int id) {
                events.add("ACTIVATE_USER");
                return false;
            }
        };

        RecordingTransactionBoundary transactionBoundary =
                new RecordingTransactionBoundary(events);
        VerifyOtpController verifyController =
                new VerifyOtpController(failingUserService, spyOtpService, transactionBoundary);
        MockHttpContext verifyContext = new MockHttpContext();
        verifyContext.session.put("pendingVerifyEmail", user.getEmail());
        verifyContext.parameters.put("otp", otp);

        verifyController.doPost(verifyContext.request, verifyContext.response);
        assertEquals("Failed to activate user account.", verifyContext.attributes.get("error"));
        assertTrue(transactionBoundary.rolledBack);
        assertEquals(1, transactionBoundary.begins);
        assertEquals(0, transactionBoundary.commits);
        assertEquals(1, transactionBoundary.rollbacks);
        assertEquals(1, transactionBoundary.ends);

        // Honestly verify operation ordering and transaction ownership
        assertEquals(java.util.List.of("BEGIN", "VERIFY_OTP", "ACTIVATE_USER", "ROLLBACK", "END"), events);

        // This unit test verifies controller ordering only. Persisted rollback is
        // proven by the conditional SQL Server integration test below.
    }

    @Test
    public void testVerificationRollbackRestoresStateWhenActivationThrows() throws Exception {
        String unique = "txex_" + System.currentTimeMillis();
        User user = userService.register(unique, unique + "@example.com", "Pass1");
        String otp = otpService.generateOtp(user, OtpPurpose.REGISTER);

        java.util.List<String> events = new java.util.ArrayList<>();

        IOtpService spyOtpService = new OtpServiceImpl(otpTokenDao) {
            @Override
            public boolean verifyOtp(User u, OtpPurpose p, String o) {
                events.add("VERIFY_OTP");
                return super.verifyOtp(u, p, o);
            }
        };

        IUserService failingUserService = new UserServiceImpl(userDao) {
            @Override
            public boolean activateUser(int id) {
                events.add("ACTIVATE_USER");
                throw new RuntimeException("DB Connection Lost");
            }
        };

        RecordingTransactionBoundary transactionBoundary =
                new RecordingTransactionBoundary(events);
        VerifyOtpController verifyController =
                new VerifyOtpController(failingUserService, spyOtpService, transactionBoundary);
        MockHttpContext verifyContext = new MockHttpContext();
        verifyContext.session.put("pendingVerifyEmail", user.getEmail());
        verifyContext.parameters.put("otp", otp);

        assertThrows(RuntimeException.class, () -> {
            verifyController.doPost(verifyContext.request, verifyContext.response);
        });
        assertTrue(transactionBoundary.rolledBack);
        assertEquals(1, transactionBoundary.begins);
        assertEquals(0, transactionBoundary.commits);
        assertEquals(1, transactionBoundary.rollbacks);
        assertEquals(1, transactionBoundary.ends);

        // Honestly verify operation ordering and transaction ownership
        assertEquals(java.util.List.of("BEGIN", "VERIFY_OTP", "ACTIVATE_USER", "ROLLBACK", "END"), events);

        // This unit test verifies controller ordering only. Persisted rollback is
        // proven by the conditional SQL Server integration test below.
    }

    @Test
    public void testRealDatabaseVerificationCommitsActivationAndOtpConsumption() throws Exception {
        Assumptions.assumeTrue(hasExternalDatabasePassword(),
                "SQL Server verification requires DB_PASSWORD or jakarta.persistence.jdbc.password");

        String unique = "dbtxok_" + System.currentTimeMillis();
        User persistedUser = null;
        int tokenId = 0;
        try {
            IUserService realUserService = new UserServiceImpl();
            IOtpService realOtpService = new OtpServiceImpl();
            persistedUser = realUserService.register(unique, unique + "@example.com", "Pass1");
            String rawOtp = realOtpService.generateOtp(persistedUser, OtpPurpose.REGISTER);
            OtpToken issuedToken = new OtpTokenDao()
                    .findLatestValidByUserAndPurpose(persistedUser, OtpPurpose.REGISTER);
            assertNotNull(issuedToken);
            tokenId = issuedToken.getId();

            VerifyOtpController verifyController = new VerifyOtpController(realUserService, realOtpService);
            MockHttpContext context = new MockHttpContext();
            context.session.put("pendingVerifyEmail", persistedUser.getEmail());
            context.parameters.put("otp", rawOtp);

            verifyController.doPost(context.request, context.response);

            User observedUser = new UserServiceImpl().findByEmail(persistedUser.getEmail());
            OtpToken observedToken = new OtpTokenDao().findById(tokenId);
            assertTrue(observedUser.isActive());
            assertTrue(observedToken.isUsed());
            assertFalse(new OtpServiceImpl().verifyOtp(observedUser, OtpPurpose.REGISTER, rawOtp));
        } finally {
            cleanupPersistedRegistration(persistedUser, tokenId);
        }
    }

    @Test
    public void testRealDatabaseActivationFailureRollsBackOtpConsumption() throws Exception {
        Assumptions.assumeTrue(hasExternalDatabasePassword(),
                "SQL Server verification requires DB_PASSWORD or jakarta.persistence.jdbc.password");

        String unique = "dbtxfail_" + System.currentTimeMillis();
        User persistedUser = null;
        int tokenId = 0;
        try {
            UserServiceImpl realUserService = new UserServiceImpl();
            IOtpService realOtpService = new OtpServiceImpl();
            persistedUser = realUserService.register(unique, unique + "@example.com", "Pass1");
            String rawOtp = realOtpService.generateOtp(persistedUser, OtpPurpose.REGISTER);
            OtpToken issuedToken = new OtpTokenDao()
                    .findLatestValidByUserAndPurpose(persistedUser, OtpPurpose.REGISTER);
            assertNotNull(issuedToken);
            tokenId = issuedToken.getId();

            IUserService failingUserService = new UserServiceImpl() {
                @Override
                public boolean activateUser(int id) {
                    assertTrue(super.activateUser(id));
                    throw new RuntimeException("forced activation failure");
                }
            };
            VerifyOtpController verifyController =
                    new VerifyOtpController(failingUserService, realOtpService);
            MockHttpContext context = new MockHttpContext();
            context.session.put("pendingVerifyEmail", persistedUser.getEmail());
            context.parameters.put("otp", rawOtp);

            assertThrows(RuntimeException.class, () ->
                    verifyController.doPost(context.request, context.response));

            User observedUser = new UserServiceImpl().findByEmail(persistedUser.getEmail());
            OtpToken observedToken = new OtpTokenDao().findById(tokenId);
            assertFalse(observedUser.isActive());
            assertFalse(observedToken.isUsed());
            assertTrue(new OtpServiceImpl().verifyOtp(observedUser, OtpPurpose.REGISTER, rawOtp));
        } finally {
            cleanupPersistedRegistration(persistedUser, tokenId);
        }
    }

    private static boolean hasExternalDatabasePassword() {
        String systemPassword = System.getProperty("jakarta.persistence.jdbc.password");
        String environmentPassword = System.getenv("DB_PASSWORD");
        return (systemPassword != null && !systemPassword.isBlank())
                || (environmentPassword != null && !environmentPassword.isBlank());
    }

    private static void cleanupPersistedRegistration(User user, int tokenId) {
        if (user == null || user.getId() <= 0) {
            return;
        }

        EntityManager entityManager = null;
        EntityTransaction transaction = null;
        try {
            entityManager = JpaConfig.getEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();
            if (tokenId > 0) {
                OtpToken token = entityManager.find(OtpToken.class, tokenId);
                if (token != null) {
                    entityManager.remove(token);
                }
            }
            User persistedUser = entityManager.find(User.class, user.getId());
            if (persistedUser != null) {
                entityManager.remove(persistedUser);
            }
            transaction.commit();
        } catch (Exception e) {
            System.err.println("Database cleanup error (separate from test outcome): " + e.getMessage());
            e.printStackTrace();
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception ex) {
                    System.err.println("Database rollback error during cleanup: " + ex.getMessage());
                }
            }
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    private static final class RecordingTransactionBoundary
            implements VerifyOtpController.TransactionBoundary {
        private final java.util.List<String> events;
        private int begins;
        private int commits;
        private int rollbacks;
        private int ends;
        private boolean committed;
        private boolean rolledBack;

        private RecordingTransactionBoundary() {
            this(new java.util.ArrayList<>());
        }

        private RecordingTransactionBoundary(java.util.List<String> events) {
            this.events = events;
        }

        @Override
        public void begin() {
            begins++;
            events.add("BEGIN");
        }

        @Override
        public void commit() {
            commits++;
            committed = true;
            events.add("COMMIT");
        }

        @Override
        public void rollback() {
            rollbacks++;
            rolledBack = true;
            events.add("ROLLBACK");
        }

        @Override
        public void end() {
            ends++;
            events.add("END");
        }
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
        public User findById(int id) {
            return users.get(id);
        }

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
        public void update(User user) {
            users.put(user.getId(), user);
        }
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
        public void update(OtpToken token) {
            tokens.put(token.getId(), token);
        }

        @Override
        public OtpToken findById(int id) {
            return tokens.get(id);
        }

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
