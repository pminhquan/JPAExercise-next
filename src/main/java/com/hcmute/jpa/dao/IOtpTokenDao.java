package com.hcmute.jpa.dao;

import com.hcmute.jpa.entity.OtpPurpose;
import com.hcmute.jpa.entity.OtpToken;
import com.hcmute.jpa.entity.User;

public interface IOtpTokenDao {

    void create(OtpToken token);

    void update(OtpToken token);

    OtpToken findById(int id);

    OtpToken findLatestValidByUserAndPurpose(User user, OtpPurpose purpose);

    void invalidateExistingByUserAndPurpose(User user, OtpPurpose purpose);
}
