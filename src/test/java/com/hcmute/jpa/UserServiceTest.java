package com.hcmute.jpa;

import com.hcmute.jpa.entity.User;
import com.hcmute.jpa.dao.IUserDao;
import com.hcmute.jpa.service.IUserService;
import com.hcmute.jpa.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private IUserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(new InMemoryUserDao());
    }

    @Test
    public void testUserLifecycleAndSecurity() {
        String baseName = "testuser_" + System.currentTimeMillis();
        String username = baseName;
        String email = baseName + "@example.com";
        String password = "SuperSecretPassword123";

        // 1. New user registration (create + hash)
        User registeredUser = userService.register(username, email, password);
        assertNotNull(registeredUser);
        assertTrue(registeredUser.getId() > 0);
        assertEquals(username, registeredUser.getUsername());
        assertEquals(email, registeredUser.getEmail());

        // Hash differs from plaintext
        assertNotEquals(password, registeredUser.getPasswordHash());
        assertFalse(registeredUser.getPasswordHash().contains(password));

        // New user default status should be inactive
        assertFalse(registeredUser.isActive());

        // 2. Duplicate username/email blocked
        // Duplicate username
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(username, "other_" + email, "somepass");
        });

        // Duplicate email
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register("other_" + username, email, "somepass");
        });

        // 3. Find/Retrieve
        User foundById = userService.findById(registeredUser.getId());
        assertNotNull(foundById);
        assertEquals(username, foundById.getUsername());

        User foundByUsername = userService.findByUsername(username);
        assertNotNull(foundByUsername);
        assertEquals(registeredUser.getId(), foundByUsername.getId());

        User foundByEmail = userService.findByEmail(email);
        assertNotNull(foundByEmail);
        assertEquals(registeredUser.getId(), foundByEmail.getId());

        // 4. Verify password
        // Correct password -> true
        assertTrue(userService.verifyPassword(registeredUser, password));
        // Wrong password -> false
        assertFalse(userService.verifyPassword(registeredUser, "WrongPassword"));
        // Null password / null user -> false
        assertFalse(userService.verifyPassword(null, password));
        assertFalse(userService.verifyPassword(registeredUser, null));

        // 5. Activation succeeds
        boolean activated = userService.activateUser(registeredUser.getId());
        assertTrue(activated);
        User activatedUser = userService.findById(registeredUser.getId());
        assertTrue(activatedUser.isActive());

        // 6. Update password with new hash
        String newPassword = "EvenMoreSecretPassword456";
        boolean passUpdated = userService.updatePassword(registeredUser.getId(), newPassword);
        assertTrue(passUpdated);

        User updatedUser = userService.findById(registeredUser.getId());
        // Verify new password hash works
        assertTrue(userService.verifyPassword(updatedUser, newPassword));
        // Old password no longer works
        assertFalse(userService.verifyPassword(updatedUser, password));
    }

    @Test
    public void testUpdateProfileUpdatesProfileFieldsAndPreservesOtherFields() {
        String baseName = "profileuser_" + System.currentTimeMillis();
        String username = baseName;
        String email = baseName + "@example.com";
        String password = "Password123";

        User registeredUser = userService.register(username, email, password);
        int userId = registeredUser.getId();
        String originalPasswordHash = registeredUser.getPasswordHash();
        java.sql.Timestamp originalCreatedAt = registeredUser.getCreatedAt();

        // Initially nullable fields are null
        assertNull(registeredUser.getFullname());
        assertNull(registeredUser.getPhone());
        assertNull(registeredUser.getImages());

        // Update profile
        String newFullname = "Nguyen Van A";
        String newPhone = "0901234567";
        String newImages = "https://example.com/avatar.jpg";
        boolean result = userService.updateProfile(userId, newFullname, newPhone, newImages);
        assertTrue(result);

        // Verify updated user
        User updatedUser = userService.findById(userId);
        assertNotNull(updatedUser);
        assertEquals(newFullname, updatedUser.getFullname());
        assertEquals(newPhone, updatedUser.getPhone());
        assertEquals(newImages, updatedUser.getImages());

        // Verify preserved fields
        assertEquals(username, updatedUser.getUsername());
        assertEquals(email, updatedUser.getEmail());
        assertEquals(originalPasswordHash, updatedUser.getPasswordHash());
        assertFalse(updatedUser.isActive());
        assertEquals(originalCreatedAt, updatedUser.getCreatedAt());

        // Verify password authentication still works
        assertTrue(userService.verifyPassword(updatedUser, password));

        // Test updating profile fields to null is allowed
        boolean resetNullResult = userService.updateProfile(userId, null, null, null);
        assertTrue(resetNullResult);
        User nullFieldUser = userService.findById(userId);
        assertNull(nullFieldUser.getFullname());
        assertNull(nullFieldUser.getPhone());
        assertNull(nullFieldUser.getImages());
        assertEquals(username, nullFieldUser.getUsername());
        assertEquals(email, nullFieldUser.getEmail());
        assertEquals(originalPasswordHash, nullFieldUser.getPasswordHash());
        assertFalse(nullFieldUser.isActive());
        assertEquals(originalCreatedAt, nullFieldUser.getCreatedAt());
    }

    @Test
    public void testUpdateProfileNonExistentUserFailsSafely() {
        boolean result = userService.updateProfile(999999, "Non Existent", "000", "none.png");
        assertFalse(result);
    }

    private static final class InMemoryUserDao implements IUserDao {
        private final java.util.Map<Integer, User> usersById = new java.util.HashMap<>();
        private int nextId = 1;

        @Override
        public void create(User user) {
            user.setId(nextId++);
            usersById.put(user.getId(), user);
        }

        @Override
        public User findById(int id) {
            return usersById.get(id);
        }

        @Override
        public User findByEmail(String email) {
            return usersById.values().stream()
                    .filter(user -> email.equals(user.getEmail()))
                    .findFirst().orElse(null);
        }

        @Override
        public User findByUsername(String username) {
            return usersById.values().stream()
                    .filter(user -> username.equals(user.getUsername()))
                    .findFirst().orElse(null);
        }

        @Override
        public boolean existsByEmail(String email) {
            return findByEmail(email) != null;
        }

        @Override
        public boolean existsByUsername(String username) {
            return findByUsername(username) != null;
        }

        @Override
        public void update(User user) {
            usersById.put(user.getId(), user);
        }
    }
}
