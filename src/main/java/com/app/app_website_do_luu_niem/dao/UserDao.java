package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.model.UserAdminRow;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    Optional<User> findById(int id);

    List<User> findAll();

    List<UserAdminRow> findAllAdminRows(int page, int pageSize, String search, String role,
                                       Boolean activeOnly, String sortBy, String sortOrder);

    int countAll(String search, String role, Boolean activeOnly);

    long countByRole(String role);

    long countActive();

    long countInactive();

    long countAdmins();

    boolean emailExistsOtherThan(String email, int excludeId);

    long countOrdersByUserId(int userId);

    void save(User user);

    void update(User user);

    void updateProfile(int id, String fullName, String email);

    void updatePasswordHash(int userId, String passwordHash);

    void linkGoogleAccount(int userId, String googleId);

    void updateRoleAndActive(int id, String role, boolean active);

    void delete(int id);

    long countActiveCustomers();

    Optional<User> findByEmailOrUsernameOrPhone(String input);

    void incrementFailedLogins(int userId);

    void resetFailedLogins(int userId);

    void lockUser(int userId, java.time.LocalDateTime lockTime);

    Optional<String> getOtpCode(int userId);

    void saveOtpCode(int userId, String otpCode, java.time.LocalDateTime expiresAt);

    void clearOtpCode(int userId);

    void updateStatus(int userId, String status);

    void logRoleUpdate(int userId, String newRole);

    java.time.LocalDateTime getLastRoleUpdate(int userId);
    
    boolean usernameExists(String username);
    
    boolean phoneExists(String phone);
}
