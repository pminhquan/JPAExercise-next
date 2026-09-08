package com.hcmute.jpa;

import com.hcmute.jpa.controller.LoginController;
import com.hcmute.jpa.controller.LogoutController;
import com.hcmute.jpa.entity.Role;
import com.hcmute.jpa.entity.User;
import com.hcmute.jpa.service.IUserService;
import com.hcmute.jpa.service.UserServiceImpl;
import com.hcmute.jpa.dao.IUserDao;
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

public class LoginAndLogoutFlowTest {

    private IUserService userService;
    private User activeUser;
    private User inactiveUser;
    private String password = "TestPassword123";

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(new InMemoryUserDao());

        String unique = "login_" + System.currentTimeMillis();

        activeUser = userService.register(unique + "_act", unique + "_act@example.com", password);
        userService.activateUser(activeUser.getId());

        inactiveUser = userService.register(unique + "_inact", unique + "_inact@example.com", password);
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
        boolean invalidated = false;

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
                } else if (methodName.equals("invalidate")) {
                    invalidated = true;
                    session.clear();
                    return null;
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
    public void testSuccessfulLoginFlowWithEmail() throws Exception {
        LoginController controller = new LoginController(userService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("identifier", activeUser.getEmail());
        ctx.parameters.put("password", password);

        controller.doPost(ctx.request, ctx.response);

        assertEquals("/home", ctx.redirectUrl);
        assertEquals(activeUser.getId(), ctx.session.get("authenticatedUserId"));
        assertEquals("CUSTOMER", ctx.session.get("authenticatedUserRole"));
        assertEquals(activeUser, ctx.session.get("account"));
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

    @Test
    public void testSuccessfulLoginFlowWithUsername() throws Exception {
        LoginController controller = new LoginController(userService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("identifier", activeUser.getUsername());
        ctx.parameters.put("password", password);

        controller.doPost(ctx.request, ctx.response);

        assertEquals("/home", ctx.redirectUrl);
        assertEquals(activeUser.getId(), ctx.session.get("authenticatedUserId"));
        assertEquals("CUSTOMER", ctx.session.get("authenticatedUserRole"));
        assertEquals(activeUser, ctx.session.get("account"));
    }

    @Test
    public void testWrongPasswordFails() throws Exception {
        LoginController controller = new LoginController(userService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("identifier", activeUser.getEmail());
        ctx.parameters.put("password", "IncorrectPass");

        controller.doPost(ctx.request, ctx.response);

        assertTrue(ctx.forwarded);
        assertEquals("/views/login.jsp", ctx.forwardedPath);
        assertEquals("Invalid username/email or password.", ctx.attributes.get("error"));
        assertNull(ctx.session.get("authenticatedUserId"));
    }

    @Test
    public void testInactiveUserFails() throws Exception {
        LoginController controller = new LoginController(userService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("identifier", inactiveUser.getEmail());
        ctx.parameters.put("password", password);

        controller.doPost(ctx.request, ctx.response);

        assertTrue(ctx.forwarded);
        assertEquals("/views/login.jsp", ctx.forwardedPath);
        assertEquals("Invalid username/email or password.", ctx.attributes.get("error"));
        assertNull(ctx.session.get("authenticatedUserId"));
    }

    @Test
    public void testUnknownUserFails() throws Exception {
        LoginController controller = new LoginController(userService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("identifier", "unknown_user_at_test");
        ctx.parameters.put("password", password);

        controller.doPost(ctx.request, ctx.response);

        assertTrue(ctx.forwarded);
        assertEquals("Invalid username/email or password.", ctx.attributes.get("error"));
        assertNull(ctx.session.get("authenticatedUserId"));
    }

    @Test
    public void testBlankFieldsFail() throws Exception {
        LoginController controller = new LoginController(userService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("identifier", "");
        ctx.parameters.put("password", "");

        controller.doPost(ctx.request, ctx.response);

        assertTrue(ctx.forwarded);
        assertEquals("Invalid username/email or password.", ctx.attributes.get("error"));
        assertNull(ctx.session.get("authenticatedUserId"));
    }

    @Test
    public void testLogoutFlow() throws Exception {
        LogoutController controller = new LogoutController();
        MockHttpContext ctx = new MockHttpContext();
        ctx.session.put("authenticatedUserId", activeUser.getId());

        controller.doGet(ctx.request, ctx.response);

        assertTrue(ctx.invalidated);
        assertNull(ctx.session.get("authenticatedUserId"));
        assertEquals("/login", ctx.redirectUrl);
    }

    @Test
    public void testSuccessfulLoginFlowForAdmin() throws Exception {
        String unique = "admin_" + System.currentTimeMillis();
        User adminUser = userService.register(unique, unique + "@example.com", password);
        adminUser.setRole(Role.ADMIN);
        userService.activateUser(adminUser.getId());

        LoginController controller = new LoginController(userService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("identifier", adminUser.getEmail());
        ctx.parameters.put("password", password);

        controller.doPost(ctx.request, ctx.response);

        assertEquals("/categories", ctx.redirectUrl);
        assertEquals(adminUser.getId(), ctx.session.get("authenticatedUserId"));
        assertEquals("ADMIN", ctx.session.get("authenticatedUserRole"));
        assertEquals(adminUser, ctx.session.get("account"));
    }

    @Test
    public void testAuthenticatedGetLoginRedirectsCustomerToHome() throws Exception {
        LoginController controller = new LoginController(userService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.session.put("authenticatedUserId", activeUser.getId());
        ctx.session.put("authenticatedUserRole", "CUSTOMER");

        controller.doGet(ctx.request, ctx.response);

        assertEquals("/home", ctx.redirectUrl);
        assertFalse(ctx.forwarded);
    }

    @Test
    public void testAuthenticatedGetLoginRedirectsAdminToCategories() throws Exception {
        LoginController controller = new LoginController(userService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.session.put("authenticatedUserId", 999);
        ctx.session.put("authenticatedUserRole", "ADMIN");

        controller.doGet(ctx.request, ctx.response);

        assertEquals("/categories", ctx.redirectUrl);
        assertFalse(ctx.forwarded);
    }

    @Test
    public void testUnauthenticatedGetLoginShowsLoginPage() throws Exception {
        LoginController controller = new LoginController(userService);
        MockHttpContext ctx = new MockHttpContext();

        controller.doGet(ctx.request, ctx.response);

        assertTrue(ctx.forwarded);
        assertEquals("/views/login.jsp", ctx.forwardedPath);
        assertNull(ctx.redirectUrl);
    }
}
