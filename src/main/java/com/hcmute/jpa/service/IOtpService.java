package com.hcmute.jpa.service;

import com.hcmute.jpa.entity.OtpPurpose;
import com.hcmute.jpa.entity.User;

public interface IOtpService {

    String generateOtp(User user, OtpPurpose purpose);

    boolean verifyOtp(User user, OtpPurpose purpose, String rawOtp);

    void invalidateExistingOtp(User user, OtpPurpose purpose);
}
