package com.hcmute.jpa.dao;

import com.hcmute.jpa.entity.User;

public interface IUserDao {

    void create(User user);

    User findById(int id);

    User findByEmail(String email);

    User findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    void update(User user);

    long countAllUsers();
}
