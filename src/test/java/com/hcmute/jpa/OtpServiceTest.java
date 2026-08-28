package com.hcmute.jpa;

import com.hcmute.jpa.dao.IOtpTokenDao;
import com.hcmute.jpa.entity.OtpPurpose;
import com.hcmute.jpa.entity.OtpToken;
import com.hcmute.jpa.entity.User;
import com.hcmute.jpa.service.IOtpService;
import com.hcmute.jpa.service.OtpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OtpServiceTest {

    private IOtpService otpService;
    private IOtpTokenDao otpTokenDao;
    private User testUser;

    @BeforeEach
    public void setUp() {
        otpTokenDao = new InMemoryOtpTokenDao();
        otpService = new OtpServiceImpl(otpTokenDao);
        testUser = new User("otpuser", "otp@example.com", "unused-hash");
        testUser.setId(1);
    }

    @Test
    public void testOtpFlowAndValidation() {
        String rawOtp = otpService.generateOtp(testUser, OtpPurpose.REGISTER);
        assertNotNull(rawOtp);
        assertEquals(6, rawOtp.length());
        assertTrue(rawOtp.matches("\\d{6}"));

        OtpToken token = otpTokenDao.findLatestValidByUserAndPurpose(testUser, OtpPurpose.REGISTER);
        assertNotNull(token);
        assertNotEquals(rawOtp, token.getCodeHash());
        assertFalse(token.getCodeHash().contains(rawOtp));
        assertFalse(token.isUsed());
        assertEquals(0, token.getAttempts());
        assertNotNull(token.getUser());
        assertEquals(testUser.getId(), token.getUser().getId());

        assertFalse(otpService.verifyOtp(testUser, OtpPurpose.FORGOT_PASSWORD, rawOtp));

        String wrongOtp = "000000";
        if (wrongOtp.equals(rawOtp)) {
            wrongOtp = "111111";
        }
        assertFalse(otpService.verifyOtp(testUser, OtpPurpose.REGISTER, wrongOtp));

        OtpToken updatedToken = otpTokenDao.findById(token.getId());
        assertEquals(1, updatedToken.getAttempts());

        assertTrue(otpService.verifyOtp(testUser, OtpPurpose.REGISTER, rawOtp));

        OtpToken verifiedToken = otpTokenDao.findById(token.getId());
        assertTrue(verifiedToken.isUsed());

        assertFalse(otpService.verifyOtp(testUser, OtpPurpose.REGISTER, rawOtp));
    }

    @Test
    public void testOtpExpiry() {
        String rawOtp = otpService.generateOtp(testUser, OtpPurpose.REGISTER);
        OtpToken token = otpTokenDao.findLatestValidByUserAndPurpose(testUser, OtpPurpose.REGISTER);

        token.setExpiresAt(new Timestamp(System.currentTimeMillis() - 1000));
        otpTokenDao.update(token);

        assertFalse(otpService.verifyOtp(testUser, OtpPurpose.REGISTER, rawOtp));
    }

    @Test
    public void testOtpMaxAttempts() {
        String rawOtp = otpService.generateOtp(testUser, OtpPurpose.REGISTER);
        OtpToken token = otpTokenDao.findLatestValidByUserAndPurpose(testUser, OtpPurpose.REGISTER);

        String wrongOtp = "000000";
        if (wrongOtp.equals(rawOtp)) {
            wrongOtp = "111111";
        }

        for (int i = 0; i < 5; i++) {
            assertFalse(otpService.verifyOtp(testUser, OtpPurpose.REGISTER, wrongOtp));
        }

        assertFalse(otpService.verifyOtp(testUser, OtpPurpose.REGISTER, rawOtp));

        OtpToken finalToken = otpTokenDao.findById(token.getId());
        assertTrue(finalToken.getAttempts() >= 5);
    }

    @Test
    public void testNewTokenInvalidatesOld() {
        String firstOtp = otpService.generateOtp(testUser, OtpPurpose.REGISTER);
        OtpToken firstToken = otpTokenDao.findLatestValidByUserAndPurpose(testUser, OtpPurpose.REGISTER);

        String secondOtp = otpService.generateOtp(testUser, OtpPurpose.REGISTER);
        OtpToken secondToken = otpTokenDao.findLatestValidByUserAndPurpose(testUser, OtpPurpose.REGISTER);

        assertNotEquals(firstToken.getId(), secondToken.getId());

        OtpToken firstTokenCheck = otpTokenDao.findById(firstToken.getId());
        assertTrue(firstTokenCheck.isUsed());

        assertFalse(otpService.verifyOtp(testUser, OtpPurpose.REGISTER, firstOtp));
        assertTrue(otpService.verifyOtp(testUser, OtpPurpose.REGISTER, secondOtp));
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
                    .filter(token -> token.getUser().getId() == user.getId())
                    .filter(token -> token.getPurpose() == purpose)
                    .filter(token -> !token.isUsed())
                    .filter(token -> token.getExpiresAt().after(new Timestamp(System.currentTimeMillis())))
                    .filter(token -> token.getAttempts() < 5)
                    .max((left, right) -> left.getCreatedAt().compareTo(right.getCreatedAt()))
                    .orElse(null);
        }

        @Override
        public void invalidateExistingByUserAndPurpose(User user, OtpPurpose purpose) {
            tokens.values().stream()
                    .filter(token -> token.getUser().getId() == user.getId())
                    .filter(token -> token.getPurpose() == purpose)
                    .filter(token -> !token.isUsed())
                    .forEach(token -> token.setUsed(true));
        }
    }
}
