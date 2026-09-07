package com.hcmute.jpa.service;

import com.hcmute.jpa.entity.User;

public interface IUserService {

    User register(String username, String email, String plaintextPassword);

    User findById(int id);

    User findByUsername(String username);

    User findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean verifyPassword(User user, String plaintextPassword);

    boolean activateUser(int id);

    boolean updatePassword(int id, String newPlaintextPassword);

    boolean updateProfile(int userId, String fullname, String phone, String images);
}
