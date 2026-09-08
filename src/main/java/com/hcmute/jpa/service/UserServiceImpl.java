package com.hcmute.jpa.service;

import com.hcmute.jpa.dao.IUserDao;
import com.hcmute.jpa.dao.UserDao;
import com.hcmute.jpa.entity.Role;
import com.hcmute.jpa.entity.User;
import org.mindrot.jbcrypt.BCrypt;

public class UserServiceImpl implements IUserService {

    private final IUserDao userDao;

    public UserServiceImpl() {
        this.userDao = new UserDao();
    }

    public UserServiceImpl(IUserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User register(String username, String email, String plaintextPassword) {
        if (existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        String hashedPassword = BCrypt.hashpw(plaintextPassword, BCrypt.gensalt());
        User user = new User(username, email, hashedPassword);
        user.setRole(Role.CUSTOMER);
        userDao.create(user);
        return user;
    }

    @Override
    public User findById(int id) {
        return userDao.findById(id);
    }

    @Override
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userDao.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }

    @Override
    public boolean verifyPassword(User user, String plaintextPassword) {
        if (user == null || user.getPasswordHash() == null || plaintextPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plaintextPassword, user.getPasswordHash());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean activateUser(int id) {
        User user = userDao.findById(id);
        if (user != null) {
            user.setActive(true);
            userDao.update(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean updatePassword(int id, String newPlaintextPassword) {
        User user = userDao.findById(id);
        if (user != null) {
            String hashedPassword = BCrypt.hashpw(newPlaintextPassword, BCrypt.gensalt());
            user.setPasswordHash(hashedPassword);
            userDao.update(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateProfile(int userId, String fullname, String phone, String images) {
        User user = userDao.findById(userId);
        if (user != null) {
            user.setFullname(fullname);
            user.setPhone(phone);
            user.setImages(images);
            userDao.update(user);
            return true;
        }
        return false;
    }
}
