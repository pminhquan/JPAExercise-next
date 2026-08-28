package com.hcmute.jpa.service;

import com.hcmute.jpa.entity.OtpPurpose;

public interface IEmailService {

    boolean sendOtpEmail(String to, String otp, OtpPurpose purpose);
}
