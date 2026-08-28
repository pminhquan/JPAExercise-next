package com.hcmute.jpa;

import com.hcmute.jpa.controller.ForgotPasswordController;
import com.hcmute.jpa.controller.ResetPasswordController;
import com.hcmute.jpa.entity.OtpPurpose;
import com.hcmute.jpa.entity.OtpToken;
import com.hcmute.jpa.entity.User;
import com.hcmute.jpa.service.*;
import com.hcmute.jpa.dao.*;
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

public class ForgotPasswordFlowTest {

    private IUserService userService;
    private IOtpService otpService;
    private IOtpTokenDao otpTokenDao;
    private EmailServiceImpl emailStub;

    private User testUser;
    private String password = "OldPassword123";
    private String lastSentOtp;
    private String lastSentRecipient;

    @BeforeEach
    public void setUp() {
        otpTokenDao = new InMemoryOtpTokenDao();
        otpService = new OtpServiceImpl(otpTokenDao);
        userService = new UserServiceImpl(new InMemoryUserDao());

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

        String unique = "forgot_" + System.currentTimeMillis();
        testUser = userService.register(unique, unique + "@example.com", password);
        userService.activateUser(testUser.getId());
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
    public void testForgotPasswordSuccessfulFlow() throws Exception {
        ForgotPasswordController forgotController = new ForgotPasswordController(userService, otpService, emailStub);

        MockHttpContext reqCtx = new MockHttpContext();
        reqCtx.parameters.put("action", "request");
        reqCtx.parameters.put("email", testUser.getEmail());

        forgotController.doPost(reqCtx.request, reqCtx.response);

        assertNotNull(lastSentOtp);
        assertEquals(testUser.getEmail(), lastSentRecipient);
        assertEquals("If the email is registered, a password reset code has been sent.", reqCtx.attributes.get("message"));
        assertEquals(testUser.getEmail(), reqCtx.session.get("pendingResetEmail"));

        MockHttpContext verCtx = new MockHttpContext();
        verCtx.session.put("pendingResetEmail", testUser.getEmail());
        verCtx.parameters.put("action", "verify");
        verCtx.parameters.put("otp", lastSentOtp);

        forgotController.doPost(verCtx.request, verCtx.response);

        assertEquals("/reset-password", verCtx.redirectUrl);
        assertEquals(testUser.getEmail(), verCtx.session.get("resetEmail"));
        assertEquals(true, verCtx.session.get("resetAuthorized"));
        assertNotNull(verCtx.session.get("resetExpiry"));

        ResetPasswordController resetController = new ResetPasswordController(userService);
        MockHttpContext resetCtx = new MockHttpContext();
        resetCtx.session.put("resetEmail", testUser.getEmail());
        resetCtx.session.put("resetAuthorized", true);
        resetCtx.session.put("resetExpiry", System.currentTimeMillis() + 60000);
        resetCtx.parameters.put("password", "NewPassword123");
        resetCtx.parameters.put("confirmPassword", "NewPassword123");

        resetController.doPost(resetCtx.request, resetCtx.response);

        assertTrue(resetCtx.redirectUrl.contains("/login"));
        assertNull(resetCtx.session.get("resetEmail"));
        assertNull(resetCtx.session.get("resetAuthorized"));

        User updatedUser = userService.findByEmail(testUser.getEmail());
        assertTrue(userService.verifyPassword(updatedUser, "NewPassword123"));
        assertFalse(userService.verifyPassword(updatedUser, password));
    }

    @Test
    public void testUnknownEmailResponseNoEnumeration() throws Exception {
        ForgotPasswordController forgotController = new ForgotPasswordController(userService, otpService, emailStub);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "request");
        ctx.parameters.put("email", "unknown_user_email@example.com");

        forgotController.doPost(ctx.request, ctx.response);

        assertEquals("If the email is registered, a password reset code has been sent.", ctx.attributes.get("message"));
        assertEquals("unknown_user_email@example.com", ctx.session.get("pendingResetEmail"));
        assertNull(lastSentOtp);
    }

    @Test
    public void testIncorrectOtpRejects() throws Exception {
        ForgotPasswordController forgotController = new ForgotPasswordController(userService, otpService, emailStub);

        MockHttpContext reqCtx = new MockHttpContext();
        reqCtx.parameters.put("action", "request");
        reqCtx.parameters.put("email", testUser.getEmail());
        forgotController.doPost(reqCtx.request, reqCtx.response);

        MockHttpContext verCtx = new MockHttpContext();
        verCtx.session.put("pendingResetEmail", testUser.getEmail());
        verCtx.parameters.put("action", "verify");
        verCtx.parameters.put("otp", "000000");

        forgotController.doPost(verCtx.request, verCtx.response);

        assertTrue(verCtx.forwarded);
        assertEquals("/views/forgot-password.jsp", verCtx.forwardedPath);
        assertEquals("Invalid or expired verification code.", verCtx.attributes.get("error"));
        assertNull(verCtx.session.get("resetAuthorized"));
    }

    @Test
    public void testRegisterOtpCannotAuthorizeReset() throws Exception {
        String regOtp = otpService.generateOtp(testUser, OtpPurpose.REGISTER);

        ForgotPasswordController forgotController = new ForgotPasswordController(userService, otpService, emailStub);
        MockHttpContext ctx = new MockHttpContext();
        ctx.session.put("pendingResetEmail", testUser.getEmail());
        ctx.parameters.put("action", "verify");
        ctx.parameters.put("otp", regOtp);

        forgotController.doPost(ctx.request, ctx.response);

        assertEquals("Invalid or expired verification code.", ctx.attributes.get("error"));
        assertNull(ctx.session.get("resetAuthorized"));
    }

    @Test
    public void testDirectResetBypassBlocked() throws Exception {
        ResetPasswordController resetController = new ResetPasswordController(userService);
        MockHttpContext ctx = new MockHttpContext();

        resetController.doGet(ctx.request, ctx.response);

        assertEquals("/forgot-password", ctx.redirectUrl);
    }

    private static final class InMemoryUserDao implements IUserDao {
        private final Map<Integer, User> users = new HashMap<>();
        private int nextId = 1;

        @Override public void create(User user) { user.setId(nextId++); users.put(user.getId(), user); }
        @Override public User findById(int id) { return users.get(id); }
        @Override public User findByEmail(String email) { return users.values().stream().filter(u -> email.equals(u.getEmail())).findFirst().orElse(null); }
        @Override public User findByUsername(String username) { return users.values().stream().filter(u -> username.equals(u.getUsername())).findFirst().orElse(null); }
        @Override public boolean existsByEmail(String email) { return findByEmail(email) != null; }
        @Override public boolean existsByUsername(String username) { return findByUsername(username) != null; }
        @Override public void update(User user) { users.put(user.getId(), user); }
    }

    private static final class InMemoryOtpTokenDao implements IOtpTokenDao {
        private final Map<Integer, OtpToken> tokens = new HashMap<>();
        private int nextId = 1;

        @Override public void create(OtpToken token) { token.setId(nextId++); tokens.put(token.getId(), token); }
        @Override public void update(OtpToken token) { tokens.put(token.getId(), token); }
        @Override public OtpToken findById(int id) { return tokens.get(id); }
        @Override public OtpToken findLatestValidByUserAndPurpose(User user, OtpPurpose purpose) {
            return tokens.values().stream()
                    .filter(t -> t.getUser().getId() == user.getId() && t.getPurpose() == purpose)
                    .filter(t -> !t.isUsed() && t.getAttempts() < 5)
                    .filter(t -> t.getExpiresAt().after(new Timestamp(System.currentTimeMillis())))
                    .findFirst().orElse(null);
        }
        @Override public void invalidateExistingByUserAndPurpose(User user, OtpPurpose purpose) {
            tokens.values().stream()
                    .filter(t -> t.getUser().getId() == user.getId() && t.getPurpose() == purpose)
                    .forEach(t -> t.setUsed(true));
        }
    }
}
