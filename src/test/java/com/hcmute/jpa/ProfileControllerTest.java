package com.hcmute.jpa;

import com.hcmute.jpa.controller.ProfileController;
import com.hcmute.jpa.entity.User;
import com.hcmute.jpa.filter.AuthenticationFilter;
import com.hcmute.jpa.service.IUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ProfileControllerTest {

    @TempDir
    Path tempDir;

    private Path webRootDir;
    private Path uploadDir;

    private IUserService mockUserService;
    private Map<Integer, User> usersDb;
    private boolean updateProfileCalled;
    private int lastUpdatedUserId;
    private String lastUpdatedFullname;
    private String lastUpdatedPhone;
    private String lastUpdatedImages;
    private boolean updateProfileReturnStatus;

    @BeforeEach
    public void setUp() throws Exception {
        webRootDir = tempDir.resolve("webapp");
        uploadDir = webRootDir.resolve("uploads");
        Files.createDirectories(uploadDir);

        usersDb = new HashMap<>();
        updateProfileCalled = false;
        updateProfileReturnStatus = true;

        User user1 = new User("alice", "alice@example.com", "hash123");
        user1.setActive(true);
        user1.setId(1);
        user1.setFullname("Alice Wonder");
        user1.setPhone("0901112233");
        user1.setImages("alice_old.png");
        usersDb.put(1, user1);

        User user2 = new User("bob", "bob@example.com", "hash456");
        user2.setActive(true);
        user2.setId(2);
        user2.setFullname("Bob Builder");
        user2.setPhone("0904445566");
        user2.setImages("bob_old.jpg");
        usersDb.put(2, user2);

        mockUserService = createMock(IUserService.class, (proxy, method, args) -> {
            String name = method.getName();
            if ("findById".equals(name)) {
                int id = (Integer) args[0];
                return usersDb.get(id);
            } else if ("updateProfile".equals(name)) {
                updateProfileCalled = true;
                lastUpdatedUserId = (Integer) args[0];
                lastUpdatedFullname = (String) args[1];
                lastUpdatedPhone = (String) args[2];
                lastUpdatedImages = (String) args[3];

                if (updateProfileReturnStatus && usersDb.containsKey(lastUpdatedUserId)) {
                    User u = usersDb.get(lastUpdatedUserId);
                    u.setFullname(lastUpdatedFullname);
                    u.setPhone(lastUpdatedPhone);
                    u.setImages(lastUpdatedImages);
                    return true;
                }
                return updateProfileReturnStatus;
            }
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T createMock(Class<T> interfaceType, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                handler
        );
    }

    private class MockHttpContext {
        Map<String, String> parameters = new HashMap<>();
        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> sessionAttributes = new HashMap<>();
        Map<String, Part> parts = new HashMap<>();
        boolean sessionExists = false;
        boolean sessionInvalidated = false;
        String contentType = "multipart/form-data; boundary=----WebKitFormBoundaryXYZ";
        String redirectUrl = null;
        boolean forwarded = false;
        String forwardedPath = null;
        String servletPath = "/profile";
        boolean multipartSizeExceeded = false;

        HttpServletRequest request;
        HttpServletResponse response;
        HttpSession session;
        ServletContext servletContext;
        ServletConfig servletConfig;
        RequestDispatcher requestDispatcher;

        MockHttpContext(String servletPath) {
            this.servletPath = servletPath;

            requestDispatcher = createMock(RequestDispatcher.class, (proxy, method, args) -> {
                if ("forward".equals(method.getName())) {
                    forwarded = true;
                    return null;
                }
                return null;
            });

            servletContext = createMock(ServletContext.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if ("getRealPath".equals(methodName)) {
                    String path = (String) args[0];
                    if ("/".equals(path) || "".equals(path)) {
                        return webRootDir.toAbsolutePath().toString();
                    } else if ("/uploads".equals(path) || "uploads".equals(path)) {
                        return uploadDir.toAbsolutePath().toString();
                    }
                    return webRootDir.resolve(path.startsWith("/") ? path.substring(1) : path).toAbsolutePath().toString();
                } else if ("getRequestDispatcher".equals(methodName)) {
                    forwardedPath = (String) args[0];
                    return requestDispatcher;
                }
                return null;
            });

            servletConfig = createMock(ServletConfig.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if ("getServletContext".equals(methodName)) {
                    return servletContext;
                } else if ("getServletName".equals(methodName)) {
                    return "ProfileController";
                }
                return null;
            });

            session = createMock(HttpSession.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if ("getAttribute".equals(methodName)) {
                    return sessionAttributes.get((String) args[0]);
                } else if ("setAttribute".equals(methodName)) {
                    sessionAttributes.put((String) args[0], args[1]);
                    return null;
                } else if ("removeAttribute".equals(methodName)) {
                    sessionAttributes.remove((String) args[0]);
                    return null;
                } else if ("invalidate".equals(methodName)) {
                    sessionInvalidated = true;
                    sessionAttributes.clear();
                    sessionExists = false;
                    return null;
                }
                return null;
            });

            request = createMock(HttpServletRequest.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if ("getParameter".equals(methodName)) {
                    return parameters.get((String) args[0]);
                } else if ("setAttribute".equals(methodName)) {
                    attributes.put((String) args[0], args[1]);
                    return null;
                } else if ("getAttribute".equals(methodName)) {
                    return attributes.get((String) args[0]);
                } else if ("getRequestDispatcher".equals(methodName)) {
                    forwardedPath = (String) args[0];
                    return requestDispatcher;
                } else if ("getContextPath".equals(methodName)) {
                    return "/JPAExercise-next";
                } else if ("getServletPath".equals(methodName)) {
                    return servletPath;
                } else if ("getContentType".equals(methodName)) {
                    return contentType;
                } else if ("setCharacterEncoding".equals(methodName)) {
                    return null;
                } else if ("getSession".equals(methodName)) {
                    boolean create = args.length == 0 || (Boolean) args[0];
                    if (create) {
                        sessionExists = true;
                        return session;
                    }
                    return sessionExists ? session : null;
                } else if ("getPart".equals(methodName)) {
                    if (multipartSizeExceeded) {
                        throw new IllegalStateException("SizeLimitExceededException: request exceeds 6MB or file exceeds 5MB");
                    }
                    return parts.get((String) args[0]);
                } else if ("getServletContext".equals(methodName)) {
                    return servletContext;
                }
                return null;
            });

            response = createMock(HttpServletResponse.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if ("sendRedirect".equals(methodName)) {
                    redirectUrl = (String) args[0];
                    return null;
                } else if ("setCharacterEncoding".equals(methodName)) {
                    return null;
                }
                return null;
            });
        }

        void setAuthenticatedUser(int userId) {
            sessionExists = true;
            sessionAttributes.put("authenticatedUserId", userId);
        }

        void addFilePart(String partName, String submittedFileName, byte[] content) {
            contentType = "multipart/form-data; boundary=----WebKitFormBoundaryXYZ";
            Part part = createMock(Part.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if ("getSubmittedFileName".equals(methodName)) {
                    return submittedFileName;
                } else if ("getSize".equals(methodName)) {
                    return (long) content.length;
                } else if ("getInputStream".equals(methodName)) {
                    return new ByteArrayInputStream(content);
                } else if ("delete".equals(methodName)) {
                    return null;
                }
                return null;
            });
            parts.put(partName, part);
        }
    }

    private ProfileController createController(MockHttpContext ctx) throws Exception {
        ProfileController controller = new ProfileController(mockUserService);
        controller.init(ctx.servletConfig);
        return controller;
    }

    // --- AuthenticationFilter tests ---

    @Test
    public void testAuthenticationFilterBlocksAnonymousProfileGet() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter();
        MockHttpContext ctx = new MockHttpContext("/profile");
        boolean[] chainCalled = new boolean[]{false};
        FilterChain chain = (req, res) -> chainCalled[0] = true;

        filter.doFilter(ctx.request, ctx.response, chain);

        assertFalse(chainCalled[0]);
        assertEquals("/JPAExercise-next/login", ctx.redirectUrl);
    }

    @Test
    public void testAuthenticationFilterBlocksAnonymousProfilePost() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter();
        MockHttpContext ctx = new MockHttpContext("/profile");
        boolean[] chainCalled = new boolean[]{false};
        FilterChain chain = (req, res) -> chainCalled[0] = true;

        filter.doFilter(ctx.request, ctx.response, chain);

        assertFalse(chainCalled[0]);
        assertEquals("/JPAExercise-next/login", ctx.redirectUrl);
    }

    @Test
    public void testAuthenticationFilterAllowsAuthenticatedProfile() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter();
        MockHttpContext ctx = new MockHttpContext("/profile");
        ctx.setAuthenticatedUser(1);
        boolean[] chainCalled = new boolean[]{false};
        FilterChain chain = (req, res) -> chainCalled[0] = true;

        filter.doFilter(ctx.request, ctx.response, chain);

        assertTrue(chainCalled[0]);
        assertNull(ctx.redirectUrl);
    }

    // --- ProfileController Authentication & Session tests ---

    @Test
    public void testControllerAnonymousGetRedirectsToLogin() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);

        controller.doGet(ctx.request, ctx.response);

        assertFalse(ctx.forwarded);
        assertEquals("/JPAExercise-next/login", ctx.redirectUrl);
    }

    @Test
    public void testControllerAnonymousPostRedirectsToLogin() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        // Anonymous/missing-session POST
        ctx.parameters.put("fullname", "Alice Attacker");
        ctx.parameters.put("phone", "0911223344");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProfileCalled);
        assertFalse(ctx.forwarded);
        assertEquals("/JPAExercise-next/login", ctx.redirectUrl);
    }

    @Test
    public void testControllerInvalidSessionUserIdRedirectsToLogin() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.sessionExists = true;
        ctx.sessionAttributes.put("authenticatedUserId", "not-a-number");

        controller.doGet(ctx.request, ctx.response);

        assertFalse(ctx.forwarded);
        assertEquals("/JPAExercise-next/login", ctx.redirectUrl);
    }

    @Test
    public void testControllerOutOfRangeNumberSessionRedirectsToLogin() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.sessionExists = true;
        ctx.sessionAttributes.put("authenticatedUserId", Long.valueOf(3_000_000_000L));

        controller.doGet(ctx.request, ctx.response);

        assertFalse(ctx.forwarded);
        assertEquals("/JPAExercise-next/login", ctx.redirectUrl);
    }

    @Test
    public void testControllerNonexistentUserSessionRedirectsToLogin() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(9999); // nonexistent in usersDb

        controller.doGet(ctx.request, ctx.response);

        assertFalse(ctx.forwarded);
        assertEquals("/JPAExercise-next/login", ctx.redirectUrl);
        assertNull(ctx.sessionAttributes.get("authenticatedUserId"));
    }

    @Test
    public void testAuthenticatedGetSuccess() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(1);

        controller.doGet(ctx.request, ctx.response);

        assertTrue(ctx.forwarded);
        assertEquals("/WEB-INF/views/profile.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("user"));
        User user = (User) ctx.attributes.get("user");
        assertEquals(1, user.getId());
        assertEquals("Alice Wonder", user.getFullname());
        assertNull(ctx.sessionAttributes.get("user")); // Never store User in session
    }

    // --- IDOR & Spoofing defense tests ---

    @Test
    public void testPostIgnoresSpoofedIdParameters() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(1);

        // Attacker attempts to spoof ID to update user 2
        ctx.parameters.put("userId", "2");
        ctx.parameters.put("id", "2");
        ctx.parameters.put("accountId", "2");
        ctx.parameters.put("fullname", "Alice Attacker");
        ctx.parameters.put("phone", "0911223344");

        controller.doPost(ctx.request, ctx.response);

        assertTrue(updateProfileCalled);
        assertEquals(1, lastUpdatedUserId); // Must update authenticated user 1, NOT 2
        assertNotEquals(2, lastUpdatedUserId);
        assertEquals("Alice Attacker", lastUpdatedFullname);
        assertEquals("/JPAExercise-next/profile", ctx.redirectUrl);
        assertNull(ctx.sessionAttributes.get("user")); // Never store User in session
    }

    // --- Validation tests ---

    @Test
    public void testPostFullnameExceeds100CharsFails() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(1);

        String longName = "A".repeat(101);
        ctx.parameters.put("fullname", longName);
        ctx.parameters.put("phone", "0912345678");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProfileCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/WEB-INF/views/profile.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
        assertTrue(ctx.attributes.get("error").toString().contains("100"));
    }

    @Test
    public void testPostPhoneExceeds30CharsFails() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(1);

        String longPhone = "0".repeat(31);
        ctx.parameters.put("fullname", "Alice Valid");
        ctx.parameters.put("phone", longPhone);

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProfileCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/WEB-INF/views/profile.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
    }

    @Test
    public void testPostInvalidPhoneLettersFails() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(1);

        ctx.parameters.put("fullname", "Alice Valid");
        ctx.parameters.put("phone", "invalid-phone");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProfileCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/WEB-INF/views/profile.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
    }

    @Test
    public void testPostValidPhoneFormatsAccepted() throws Exception {
        String[] validPhones = {"+84901234567", "0901234567", "090-123-4567", "(028) 3896 8641"};
        for (String phone : validPhones) {
            updateProfileCalled = false;
            MockHttpContext ctx = new MockHttpContext("/profile");
            ProfileController controller = createController(ctx);
            ctx.setAuthenticatedUser(1);
            ctx.parameters.put("fullname", "Alice");
            ctx.parameters.put("phone", phone);

            controller.doPost(ctx.request, ctx.response);

            assertTrue(updateProfileCalled, "Should accept valid phone: " + phone);
            assertEquals("/JPAExercise-next/profile", ctx.redirectUrl);
        }
    }

    // --- Upload tests ---

    @Test
    public void testPostEmptyUploadPreservesOldImage() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(1);
        ctx.parameters.put("fullname", "Alice Updated");
        ctx.parameters.put("phone", "0901112233");
        // Empty upload with empty filename and 0 bytes
        ctx.addFilePart("images", "", new byte[0]);

        controller.doPost(ctx.request, ctx.response);

        assertTrue(updateProfileCalled);
        assertEquals("alice_old.png", lastUpdatedImages); // Preserved old image!
        assertEquals("/JPAExercise-next/profile", ctx.redirectUrl);
    }

    @Test
    public void testPostMultipartNullImagePartPreservesExistingImageAndUpdatesInfo() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(1);
        ctx.contentType = "multipart/form-data; boundary=----WebKitFormBoundaryXYZ";
        // request.getPart("images") returns null because no "images" part is added
        ctx.parameters.put("fullname", "Alice Updated Name");
        ctx.parameters.put("phone", "0909998877");

        controller.doPost(ctx.request, ctx.response);

        assertTrue(updateProfileCalled);
        assertEquals(1, lastUpdatedUserId);
        assertEquals("Alice Updated Name", lastUpdatedFullname);
        assertEquals("0909998877", lastUpdatedPhone);
        assertEquals("alice_old.png", lastUpdatedImages); // Preserved existing image!
        assertEquals("/JPAExercise-next/profile", ctx.redirectUrl);
    }

    @Test
    public void testPostValidImageUploadWritesFileAndUpdatesProfile() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(1);
        ctx.parameters.put("fullname", "Alice New Pic");
        ctx.parameters.put("phone", "0901112233");
        byte[] dummyPng = "PNG dummy image content".getBytes(StandardCharsets.UTF_8);
        ctx.addFilePart("images", "avatar.png", dummyPng);

        controller.doPost(ctx.request, ctx.response);

        assertTrue(updateProfileCalled);
        assertNotNull(lastUpdatedImages);
        assertTrue(lastUpdatedImages.endsWith(".png"));
        assertNotEquals("avatar.png", lastUpdatedImages); // Must be UUID generated
        assertNotEquals("alice_old.png", lastUpdatedImages);

        // Verify file actually written to upload directory
        Path uploadedFilePath = uploadDir.resolve(lastUpdatedImages);
        assertTrue(Files.exists(uploadedFilePath));
        assertArrayEquals(dummyPng, Files.readAllBytes(uploadedFilePath));

        assertEquals("/JPAExercise-next/profile", ctx.redirectUrl);
    }

    @Test
    public void testPostInvalidImageExtensionRejected() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(1);
        ctx.parameters.put("fullname", "Alice");
        ctx.addFilePart("images", "malicious.sh", "#!/bin/sh".getBytes(StandardCharsets.UTF_8));

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProfileCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/WEB-INF/views/profile.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
        assertTrue(ctx.attributes.get("error").toString().contains("Only JPG, JPEG, PNG and WEBP images are allowed"));
    }

    @Test
    public void testPostPathTraversalFilenameRejected() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(1);
        ctx.parameters.put("fullname", "Alice");
        ctx.addFilePart("images", "../../evil.png", "fake".getBytes(StandardCharsets.UTF_8));

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProfileCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/WEB-INF/views/profile.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
        assertTrue(ctx.attributes.get("error").toString().contains("Invalid image filename"));
    }

    @Test
    public void testPostZeroByteFileWithSubmittedFileNamePreservesExistingImage() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(1);
        ctx.parameters.put("fullname", "Alice Updated");
        ctx.parameters.put("phone", "0901112233");
        ctx.addFilePart("images", "empty.jpg", new byte[0]);

        controller.doPost(ctx.request, ctx.response);

        assertTrue(updateProfileCalled);
        assertEquals("alice_old.png", lastUpdatedImages); // Preserves existing image on zero-byte upload
        assertEquals("/JPAExercise-next/profile", ctx.redirectUrl);
    }

    @Test
    public void testPostPersistenceFailureCleansUpNewlyWrittenFile() throws Exception {
        updateProfileReturnStatus = false; // Simulate database update failure

        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(1);
        ctx.parameters.put("fullname", "Alice New Pic");
        byte[] dummyPng = "PNG dummy image".getBytes(StandardCharsets.UTF_8);
        ctx.addFilePart("images", "new_avatar.png", dummyPng);

        controller.doPost(ctx.request, ctx.response);

        assertTrue(updateProfileCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/WEB-INF/views/profile.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));

        // Verify that the new image was deleted because persistence failed
        Path uploadedFilePath = uploadDir.resolve(lastUpdatedImages);
        assertFalse(Files.exists(uploadedFilePath), "Newly written file must be deleted on persistence failure");
    }

    @Test
    public void testPostMultipartSizeLimitExceededHandledSafely() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/profile");
        ProfileController controller = createController(ctx);
        ctx.setAuthenticatedUser(1);
        ctx.contentType = "multipart/form-data; boundary=----XYZ";
        ctx.multipartSizeExceeded = true; // Simulates Tomcat maxFileSize / maxRequestSize violation

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProfileCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/WEB-INF/views/profile.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
        assertTrue(ctx.attributes.get("error").toString().contains("5 MB"));
    }
}
