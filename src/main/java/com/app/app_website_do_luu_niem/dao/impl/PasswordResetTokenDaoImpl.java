package com.app.app_website_do_luu_niem.dao.impl;

import com.app.app_website_do_luu_niem.dao.BaseDao;
import com.app.app_website_do_luu_niem.dao.PasswordResetTokenDao;
import com.app.app_website_do_luu_niem.model.PasswordResetToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class PasswordResetTokenDaoImpl extends BaseDao implements PasswordResetTokenDao {

    @Override
    public void deleteExpired() {
        String sql = "DELETE FROM password_reset_tokens WHERE expires_at < NOW() OR (used_at IS NOT NULL AND used_at < DATE_SUB(NOW(), INTERVAL 30 DAY))";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi dọn token hết hạn", e);
        }
    }

    @Override
    public int countCreatedSince(int userId, LocalDateTime since) {
        String sql = "SELECT COUNT(*) FROM password_reset_tokens WHERE user_id = ? AND created_at >= ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(since));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm token reset", e);
        }
        return 0;
    }

    @Override
    public void invalidateUnusedForUser(int userId) {
        String sql = "UPDATE password_reset_tokens SET used_at = NOW() WHERE user_id = ? AND used_at IS NULL";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi vô hiệu hóa token cũ", e);
        }
    }

    @Override
    public long insert(int userId, String tokenHash, LocalDateTime expiresAt, String requestIp) {
        String sql = "INSERT INTO password_reset_tokens (user_id, token_hash, expires_at, request_ip) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, tokenHash);
            ps.setTimestamp(3, Timestamp.valueOf(expiresAt));
            ps.setString(4, requestIp);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu token reset", e);
        }
        return 0;
    }

    @Override
    public Optional<PasswordResetToken> findValidByTokenHash(String tokenHash) {
        String sql = "SELECT * FROM password_reset_tokens WHERE token_hash = ? AND used_at IS NULL AND expires_at > NOW()";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tra token reset", e);
        }
        return Optional.empty();
    }

    @Override
    public void markUsed(long id) {
        String sql = "UPDATE password_reset_tokens SET used_at = NOW() WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đánh dấu token đã dùng", e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM password_reset_tokens WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa token reset", e);
        }
    }

    private PasswordResetToken map(ResultSet rs) throws SQLException {
        PasswordResetToken t = new PasswordResetToken();
        t.setId(rs.getLong("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setTokenHash(rs.getString("token_hash"));
        Timestamp ex = rs.getTimestamp("expires_at");
        if (ex != null) {
            t.setExpiresAt(ex.toLocalDateTime());
        }
        Timestamp used = rs.getTimestamp("used_at");
        if (used != null) {
            t.setUsedAt(used.toLocalDateTime());
        }
        Timestamp cr = rs.getTimestamp("created_at");
        if (cr != null) {
            t.setCreatedAt(cr.toLocalDateTime());
        }
        t.setRequestIp(rs.getString("request_ip"));
        return t;
    }
}
