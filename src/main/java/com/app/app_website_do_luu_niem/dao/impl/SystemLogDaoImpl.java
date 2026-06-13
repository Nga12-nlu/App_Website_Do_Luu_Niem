package com.app.app_website_do_luu_niem.dao.impl;

import com.app.app_website_do_luu_niem.dao.BaseDao;
import com.app.app_website_do_luu_niem.dao.SystemLogDao;
import com.app.app_website_do_luu_niem.model.SystemLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SystemLogDaoImpl extends BaseDao implements SystemLogDao {

    @Override
    public void save(SystemLog log) {
        String sql = "INSERT INTO system_logs (user_id, action, target, details, ip_address, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, log.getUserId());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getTarget());
            ps.setString(4, log.getDetails());
            ps.setString(5, log.getIpAddress());
            ps.setTimestamp(6, Timestamp.valueOf(
                    log.getCreatedAt() != null ? log.getCreatedAt() : LocalDateTime.now()
            ));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu nhật ký hệ thống", e);
        }
    }

    @Override
    public List<SystemLog> findAll(int page, int pageSize) {
        String sql = "SELECT l.*, u.email, u.full_name FROM system_logs l "
                + "JOIN users u ON l.user_id = u.id "
                + "ORDER BY l.created_at DESC LIMIT ? OFFSET ?";
        List<SystemLog> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SystemLog log = new SystemLog();
                    log.setId(rs.getInt("id"));
                    log.setUserId(rs.getInt("user_id"));
                    log.setUserEmail(rs.getString("email"));
                    log.setUserFullName(rs.getString("full_name"));
                    log.setAction(rs.getString("action"));
                    log.setTarget(rs.getString("target"));
                    log.setDetails(rs.getString("details"));
                    log.setIpAddress(rs.getString("ip_address"));
                    Timestamp ca = rs.getTimestamp("created_at");
                    if (ca != null) {
                        log.setCreatedAt(ca.toLocalDateTime());
                    }
                    list.add(log);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách nhật ký hệ thống", e);
        }
        return list;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM system_logs";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm số dòng nhật ký", e);
        }
        return 0;
    }
}
