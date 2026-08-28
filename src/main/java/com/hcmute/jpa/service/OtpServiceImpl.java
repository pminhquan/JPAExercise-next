package com.hcmute.jpa.service;

import com.hcmute.jpa.dao.IOtpTokenDao;
import com.hcmute.jpa.dao.OtpTokenDao;
import com.hcmute.jpa.entity.OtpPurpose;
import com.hcmute.jpa.entity.OtpToken;
import com.hcmute.jpa.entity.User;
import org.mindrot.jbcrypt.BCrypt;
import java.security.SecureRandom;
import java.sql.Timestamp;

public class OtpServiceImpl implements IOtpService {

    private final IOtpTokenDao otpTokenDao;
    private final SecureRandom random = new SecureRandom();

    public OtpServiceImpl() {
        this.otpTokenDao = new OtpTokenDao();
    }

    public OtpServiceImpl(IOtpTokenDao otpTokenDao) {
        this.otpTokenDao = otpTokenDao;
    }

    @Override
    public String generateOtp(User user, OtpPurpose purpose) {
        if (user == null || purpose == null) {
            throw new IllegalArgumentException("User and purpose must not be null");
        }

        // 1. Invalidate existing OTPs for the same user + purpose
        invalidateExistingOtp(user, purpose);

        // 2. Generate exactly 6-digit OTP
        int number = random.nextInt(900000) + 100000;
        String rawOtp = String.valueOf(number);

        // 3. Hash the raw OTP
        String hashedOtp = BCrypt.hashpw(rawOtp, BCrypt.gensalt());

        // 4. Default expiry of 10 minutes
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + 10 * 60 * 1000);

        // 5. Persist OtpToken
        OtpToken token = new OtpToken(user, purpose, hashedOtp, expiresAt);
        otpTokenDao.create(token);

        return rawOtp;
    }

    @Override
    public boolean verifyOtp(User user, OtpPurpose purpose, String rawOtp) {
        if (user == null || purpose == null || rawOtp == null) {
            return false;
        }

        OtpToken token = otpTokenDao.findLatestValidByUserAndPurpose(user, purpose);
        if (token == null) {
            return false;
        }

        // Validate expiration
        if (token.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            return false;
        }

        // Validate attempts limit
        if (token.getAttempts() >= 5) {
            return false;
        }

        // Validate used status
        if (token.isUsed()) {
            return false;
        }

        // Verify hash
        try {
            if (BCrypt.checkpw(rawOtp, token.getCodeHash())) {
                token.setUsed(true);
                otpTokenDao.update(token);
                return true;
            } else {
                token.setAttempts(token.getAttempts() + 1);
                otpTokenDao.update(token);
                return false;
            }
        } catch (Exception e) {
            token.setAttempts(token.getAttempts() + 1);
            otpTokenDao.update(token);
            return false;
        }
    }

    @Override
    public void invalidateExistingOtp(User user, OtpPurpose purpose) {
        if (user != null && purpose != null) {
            otpTokenDao.invalidateExistingByUserAndPurpose(user, purpose);
        }
    }
}
