package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.model.PasswordResetToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenDao {

    void deleteExpired();

    int countCreatedSince(int userId, LocalDateTime since);

    void invalidateUnusedForUser(int userId);

    long insert(int userId, String tokenHash, LocalDateTime expiresAt, String requestIp);

    Optional<PasswordResetToken> findValidByTokenHash(String tokenHash);

    void markUsed(long id);

    void deleteById(long id);
}
