package com.hcmute.jpa;

import com.hcmute.jpa.controller.ForgotPasswordController;
import com.hcmute.jpa.controller.RegisterController;
import com.hcmute.jpa.controller.ResetPasswordController;
import com.hcmute.jpa.entity.OtpPurpose;
import com.hcmute.jpa.entity.OtpToken;
import com.hcmute.jpa.entity.User;
import com.hcmute.jpa.entity.Category;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuditSpecificSecurityTests {

    private IUserService userService;
    private IOtpService otpService;
    private EmailServiceImpl emailStub;
    private IOtpTokenDao otpTokenDao;

    private User userA;
    private User userB;
    private String lastSentOtp;

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(new InMemoryUserDao());
        otpTokenDao = new InMemoryOtpTokenDao();
        otpService = new OtpServiceImpl(otpTokenDao);

        emailStub = new EmailServiceImpl() {
            @Override
            protected void sendMessage(jakarta.mail.internet.MimeMessage message) {
                try {
                    String content = (String) message.getContent();
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\d{6}");
                    java.util.regex.Matcher m = p.matcher(content);
                    if (m.find()) {
                        lastSentOtp = m.group();
                    }
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }
        };

        lastSentOtp = null;

        String unique = "audit_" + System.currentTimeMillis();
        userA = userService.register(unique + "a", unique + "a@example.com", "Password123");
        userService.activateUser(userA.getId());

        userB = userService.register(unique + "b", unique + "b@example.com", "Password123");
        userService.activateUser(userB.getId());
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
    public void testResetExpiryEnforcement() throws Exception {
        ResetPasswordController resetController = new ResetPasswordController(userService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.session.put("resetEmail", userA.getEmail());
        ctx.session.put("resetAuthorized", true);
        ctx.session.put("resetExpiry", System.currentTimeMillis() - 1);
        ctx.parameters.put("password", "NewPass123");
        ctx.parameters.put("confirmPassword", "NewPass123");

        resetController.doPost(ctx.request, ctx.response);

        assertEquals("/forgot-password", ctx.redirectUrl);
    }

    @Test
    public void testUserIsolationOnVerification() throws Exception {
        String otpA = otpService.generateOtp(userA, OtpPurpose.FORGOT_PASSWORD);

        ForgotPasswordController forgotController = new ForgotPasswordController(userService, otpService, emailStub);
        MockHttpContext ctx = new MockHttpContext();

        ctx.session.put("pendingResetEmail", userB.getEmail());
        ctx.parameters.put("action", "verify");
        ctx.parameters.put("otp", otpA);

        forgotController.doPost(ctx.request, ctx.response);

        assertEquals("Invalid or expired verification code.", ctx.attributes.get("error"));
        assertNull(ctx.session.get("resetAuthorized"));
    }

    @Test
    public void testResetAuthOneTimeUse() throws Exception {
        ResetPasswordController resetController = new ResetPasswordController(userService);

        MockHttpContext ctx1 = new MockHttpContext();
        ctx1.session.put("resetEmail", userA.getEmail());
        ctx1.session.put("resetAuthorized", true);
        ctx1.session.put("resetExpiry", System.currentTimeMillis() + 60000);
        ctx1.parameters.put("password", "NewPass123");
        ctx1.parameters.put("confirmPassword", "NewPass123");

        resetController.doPost(ctx1.request, ctx1.response);
        assertTrue(ctx1.redirectUrl.contains("/login"));

        MockHttpContext ctx2 = new MockHttpContext();
        ctx2.session = ctx1.session;
        ctx2.parameters.put("password", "HackedPass123");
        ctx2.parameters.put("confirmPassword", "HackedPass123");

        resetController.doPost(ctx2.request, ctx2.response);
        assertEquals("/forgot-password", ctx2.redirectUrl);
    }

    @Test
    public void testLeadingZeroOtpHashingAndVerification() {
        String zeroOtp = "012345";
        String hashedOtp = org.mindrot.jbcrypt.BCrypt.hashpw(zeroOtp, org.mindrot.jbcrypt.BCrypt.gensalt());

        java.sql.Timestamp expiresAt = new java.sql.Timestamp(System.currentTimeMillis() + 600000);
        OtpToken token = new OtpToken(userA, OtpPurpose.REGISTER, hashedOtp, expiresAt);
        otpTokenDao.create(token);

        assertTrue(otpService.verifyOtp(userA, OtpPurpose.REGISTER, "012345"));
    }

    @Test
    public void testEmailFailureRegistrationState() throws Exception {
        EmailServiceImpl failingEmailService = new EmailServiceImpl() {
            @Override
            public boolean sendOtpEmail(String to, String otp, OtpPurpose purpose) {
                return false;
            }
        };

        String unique = "failreg_" + System.currentTimeMillis();
        RegisterController registerController = new RegisterController(userService, otpService, failingEmailService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("username", unique);
        ctx.parameters.put("email", unique + "@example.com");
        ctx.parameters.put("password", "Pass12345");
        ctx.parameters.put("confirmPassword", "Pass12345");

        registerController.doPost(ctx.request, ctx.response);

        assertEquals("Failed to send verification email. Please try again.", ctx.attributes.get("error"));

        User user = userService.findByEmail(unique + "@example.com");
        assertNotNull(user);
        assertFalse(user.isActive());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void testDatabaseCredentialsNotHardcoded() throws Exception {
        java.io.File file = new java.io.File("src/main/resources/META-INF/persistence.xml");
        assertTrue(file.exists());
        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertFalse(content.contains("jakarta.persistence.jdbc.password"));
        assertFalse(content.contains("jakarta.persistence.jdbc.user"));
    }

    @Test
    public void testNoPasswordLiteralsInSourceCode() throws Exception {
        java.io.File file = new java.io.File("src/main/java/com/hcmute/jpa/config/JpaConfig.java");
        assertTrue(file.exists());
        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertFalse(content.contains("\"123\""));
    }

    @Test
    public void testCategoryDeletionPostOnly() throws Exception {
        com.hcmute.jpa.controller.CategoryController controller = new com.hcmute.jpa.controller.CategoryController();

        final boolean[] deleteCalled = {false};
        ICategoryService mockService = createMock(ICategoryService.class, (proxy, method, args) -> {
            String methodName = method.getName();
            if (methodName.equals("findById")) {
                Category cat = new Category();
                cat.setCategoryid(123);
                return cat;
            } else if (methodName.equals("isCategoryInUse")) {
                return false;
            } else if (methodName.equals("delete")) {
                deleteCalled[0] = true;
                assertEquals(123, args[0]);
                return true;
            }
            return null;
        });
        setField(controller, "categoryService", mockService);

        // Test GET delete -> Should redirect to list view and NOT call delete
        MockHttpContext getCtx = new MockHttpContext();
        getCtx.parameters.put("action", "delete");
        getCtx.parameters.put("id", "123");
        controller.doGet(getCtx.request, getCtx.response);

        assertFalse(deleteCalled[0]);
        assertEquals("/categories", getCtx.redirectUrl);

        // Test POST delete -> Should call delete and redirect
        MockHttpContext postCtx = new MockHttpContext();
        postCtx.parameters.put("action", "delete");
        postCtx.parameters.put("id", "123");
        controller.doPost(postCtx.request, postCtx.response);

        assertTrue(deleteCalled[0]);
        assertTrue(postCtx.redirectUrl.contains("/categories?message=delete_success"));
    }

    @Test
    public void testMalformedCategoryParameters() throws Exception {
        com.hcmute.jpa.controller.CategoryController controller = new com.hcmute.jpa.controller.CategoryController();
        ICategoryService mockService = createMock(ICategoryService.class, (proxy, method, args) -> {
            if (method.getName().equals("findById")) {
                return null;
            }
            return null;
        });
        setField(controller, "categoryService", mockService);

        // Test malformed edit ID
        MockHttpContext editCtx = new MockHttpContext();
        editCtx.parameters.put("action", "edit");
        editCtx.parameters.put("id", "not-an-integer");
        controller.doGet(editCtx.request, editCtx.response);
        assertEquals("/categories", editCtx.redirectUrl);

        // Test malformed delete ID
        MockHttpContext deleteCtx = new MockHttpContext();
        deleteCtx.parameters.put("action", "delete");
        deleteCtx.parameters.put("id", "not-an-integer");
        controller.doPost(deleteCtx.request, deleteCtx.response);
        assertEquals("/categories?error=invalid_id", deleteCtx.redirectUrl);

        // Test malformed insert status
        MockHttpContext insertCtx = new MockHttpContext();
        insertCtx.parameters.put("action", "insert");
        insertCtx.parameters.put("status", "not-an-integer");
        insertCtx.parameters.put("categoryname", "Valid Name");
        controller.doPost(insertCtx.request, insertCtx.response);
        assertEquals("/views/category-add.jsp", insertCtx.forwardedPath);
        assertEquals("Invalid status value.", insertCtx.attributes.get("error"));

        // Test malformed update status
        MockHttpContext updateCtx = new MockHttpContext();
        updateCtx.parameters.put("action", "update");
        updateCtx.parameters.put("categoryid", "123");
        updateCtx.parameters.put("status", "not-an-integer");
        updateCtx.parameters.put("categoryname", "Valid Name");
        controller.doPost(updateCtx.request, updateCtx.response);
        assertEquals("/views/category-edit.jsp", updateCtx.forwardedPath);
        assertEquals("Invalid status value.", updateCtx.attributes.get("error"));
    }

    @Test
    public void testAuthenticationFilter() throws Exception {
        com.hcmute.jpa.filter.AuthenticationFilter filter = new com.hcmute.jpa.filter.AuthenticationFilter();

        // 1. Without session, should redirect to /login
        MockHttpContext unauthCtx = new MockHttpContext();
        unauthCtx.session.clear(); // Ensure authenticatedUserId is not set

        final boolean[] chainCalled = {false};
        jakarta.servlet.FilterChain mockChain = createMock(jakarta.servlet.FilterChain.class, (proxy, method, args) -> {
            chainCalled[0] = true;
            return null;
        });

        filter.doFilter(unauthCtx.request, unauthCtx.response, mockChain);
        assertFalse(chainCalled[0]);
        assertTrue(unauthCtx.redirectUrl.endsWith("/login"));

        // 2. With session containing authenticatedUserId, should call chain.doFilter
        MockHttpContext authCtx = new MockHttpContext();
        authCtx.session.put("authenticatedUserId", 1);

        final boolean[] authChainCalled = {false};
        jakarta.servlet.FilterChain mockAuthChain = createMock(jakarta.servlet.FilterChain.class, (proxy, method, args) -> {
            authChainCalled[0] = true;
            return null;
        });

        filter.doFilter(authCtx.request, authCtx.response, mockAuthChain);
        assertTrue(authChainCalled[0]);
        assertNull(authCtx.redirectUrl);
    }

    @Test
    public void testDeleteEmptyCategorySuccess() throws Exception {
        com.hcmute.jpa.controller.CategoryController controller = new com.hcmute.jpa.controller.CategoryController();

        final boolean[] deleteCalled = {false};
        final boolean[] isCategoryInUseCalled = {false};
        ICategoryService mockService = createMock(ICategoryService.class, (proxy, method, args) -> {
            String methodName = method.getName();
            if (methodName.equals("findById")) {
                Category cat = new Category();
                cat.setCategoryid(123);
                return cat;
            } else if (methodName.equals("isCategoryInUse")) {
                isCategoryInUseCalled[0] = true;
                assertEquals(123, args[0]);
                return false; // not in use
            } else if (methodName.equals("delete")) {
                deleteCalled[0] = true;
                assertEquals(123, args[0]);
                return true;
            }
            return null;
        });
        setField(controller, "categoryService", mockService);

        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "delete");
        ctx.parameters.put("id", "123");
        controller.doPost(ctx.request, ctx.response);

        assertTrue(isCategoryInUseCalled[0]);
        assertTrue(deleteCalled[0]);
        assertTrue(ctx.redirectUrl.contains("message=delete_success"));
    }

    @Test
    public void testDeleteInUseCategoryFails() throws Exception {
        com.hcmute.jpa.controller.CategoryController controller = new com.hcmute.jpa.controller.CategoryController();

        final boolean[] deleteCalled = {false};
        final boolean[] isCategoryInUseCalled = {false};
        ICategoryService mockService = createMock(ICategoryService.class, (proxy, method, args) -> {
            String methodName = method.getName();
            if (methodName.equals("findById")) {
                Category cat = new Category();
                cat.setCategoryid(123);
                return cat;
            } else if (methodName.equals("isCategoryInUse")) {
                isCategoryInUseCalled[0] = true;
                assertEquals(123, args[0]);
                return true; // in use!
            } else if (methodName.equals("delete")) {
                deleteCalled[0] = true;
                return null;
            }
            return null;
        });
        setField(controller, "categoryService", mockService);

        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "delete");
        ctx.parameters.put("id", "123");
        controller.doPost(ctx.request, ctx.response);

        assertTrue(isCategoryInUseCalled[0]);
        assertFalse(deleteCalled[0]); // should NOT be called!
        assertTrue(ctx.redirectUrl.contains("error=in_use"));
    }

    @Test
    public void testDeleteDaoFailureRedirectsToError() throws Exception {
        com.hcmute.jpa.controller.CategoryController controller = new com.hcmute.jpa.controller.CategoryController();

        ICategoryService mockService = createMock(ICategoryService.class, (proxy, method, args) -> {
            String methodName = method.getName();
            if (methodName.equals("findById")) {
                Category cat = new Category();
                cat.setCategoryid(123);
                return cat;
            } else if (methodName.equals("isCategoryInUse")) {
                return false;
            } else if (methodName.equals("delete")) {
                throw new RuntimeException("DB Connection failure");
            }
            return null;
        });
        setField(controller, "categoryService", mockService);

        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "delete");
        ctx.parameters.put("id", "123");
        controller.doPost(ctx.request, ctx.response);

        assertTrue(ctx.redirectUrl.contains("error=delete_failed"));
    }

    @Test
    public void testDeleteInvalidMissingOrNonExistingId() throws Exception {
        com.hcmute.jpa.controller.CategoryController controller = new com.hcmute.jpa.controller.CategoryController();

        ICategoryService mockService = createMock(ICategoryService.class, (proxy, method, args) -> {
            String methodName = method.getName();
            if (methodName.equals("findById")) {
                // Return null to simulate non-existing category
                return null;
            }
            return null;
        });
        setField(controller, "categoryService", mockService);

        // Case 1: Missing ID
        MockHttpContext missingCtx = new MockHttpContext();
        missingCtx.parameters.put("action", "delete");
        controller.doPost(missingCtx.request, missingCtx.response);
        assertTrue(missingCtx.redirectUrl.contains("error=invalid_id"));

        // Case 2: Invalid (non-numeric) ID
        MockHttpContext invalidCtx = new MockHttpContext();
        invalidCtx.parameters.put("action", "delete");
        invalidCtx.parameters.put("id", "abc");
        controller.doPost(invalidCtx.request, invalidCtx.response);
        assertTrue(invalidCtx.redirectUrl.contains("error=invalid_id"));

        // Case 3: Non-existing ID
        MockHttpContext nonExistingCtx = new MockHttpContext();
        nonExistingCtx.parameters.put("action", "delete");
        nonExistingCtx.parameters.put("id", "999");
        controller.doPost(nonExistingCtx.request, nonExistingCtx.response);
        assertTrue(nonExistingCtx.redirectUrl.contains("error=not_found"));
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
                    .filter(t -> t.getExpiresAt().after(new java.sql.Timestamp(System.currentTimeMillis())))
                    .findFirst().orElse(null);
        }
        @Override public void invalidateExistingByUserAndPurpose(User user, OtpPurpose purpose) {
            tokens.values().stream()
                    .filter(t -> t.getUser().getId() == user.getId() && t.getPurpose() == purpose)
                    .forEach(t -> t.setUsed(true));
        }
    }
}
