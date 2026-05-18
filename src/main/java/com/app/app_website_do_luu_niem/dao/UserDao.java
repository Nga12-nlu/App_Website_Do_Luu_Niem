package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    Optional<User> findByEmail(String email);

    Optional<User> findById(int id);

    List<User> findAll();

    void save(User user);

    void update(User user);

    void updateProfile(int id, String fullName, String email);

    void updatePasswordHash(int userId, String passwordHash);

    long countByRole(String role);

    long countActiveCustomers();
}


