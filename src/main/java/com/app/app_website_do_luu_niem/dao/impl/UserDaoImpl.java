package com.app.app_website_do_luu_niem.dao.impl;

import com.app.app_website_do_luu_niem.dao.BaseDao;
import com.app.app_website_do_luu_niem.dao.UserDao;
import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.model.UserAdminRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl extends BaseDao implements UserDao {

    private static final String SELECT_WITH_ORDER_COUNT = """
            SELECT u.*, COALESCE(oc.cnt, 0) AS order_count
            FROM users u
            LEFT JOIN (
                SELECT user_id, COUNT(*) AS cnt FROM orders GROUP BY user_id
            ) oc ON oc.user_id = u.id
            """;

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn người dùng theo email", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByGoogleId(String googleId) {
        if (googleId == null || googleId.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT * FROM users WHERE google_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, googleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn người dùng theo Google ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn người dùng theo id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách người dùng", e);
        }
        return users;
    }

    @Override
    public List<UserAdminRow> findAllAdminRows(int page, int pageSize, String search, String role,
                                               Boolean activeOnly, String sortBy, String sortOrder) {
        StringBuilder sql = new StringBuilder(SELECT_WITH_ORDER_COUNT);
        sql.append(" WHERE 1=1 ");
        List<Object> params = appendListFilters(sql, search, role, activeOnly);
        appendOrderBy(sql, sortBy, sortOrder);
        sql.append(" LIMIT ? OFFSET ? ");
        int offset = (page - 1) * pageSize;
        params.add(pageSize);
        params.add(offset);

        List<UserAdminRow> rows = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapAdminRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách người dùng (admin)", e);
        }
        return rows;
    }

    @Override
    public int countAll(String search, String role, Boolean activeOnly) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users u WHERE 1=1 ");
        List<Object> params = appendListFilters(sql, search, role, activeOnly);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm người dùng", e);
        }
        return 0;
    }

    @Override
    public long countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm người dùng theo vai trò", e);
        }
        return 0;
    }

    @Override
    public long countActive() {
        return countByActiveFlag(true);
    }

    @Override
    public long countInactive() {
        return countByActiveFlag(false);
    }

    private long countByActiveFlag(boolean active) {
        String sql = "SELECT COUNT(*) FROM users WHERE active = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm người dùng theo trạng thái", e);
        }
        return 0;
    }

    @Override
    public long countAdmins() {
        return countByRole("ADMIN");
    }

    @Override
    public boolean emailExistsOtherThan(String email, int excludeId) {
        String sql = "SELECT 1 FROM users WHERE email = ? AND id <> ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra email trùng", e);
        }
    }

    @Override
    public long countOrdersByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm đơn hàng của người dùng", e);
        }
        return 0;
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (email, password_hash, google_id, full_name, role, active, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            if (user.getPasswordHash() != null) {
                ps.setString(2, user.getPasswordHash());
            } else {
                ps.setNull(2, java.sql.Types.VARCHAR);
            }
            if (user.getGoogleId() != null && !user.getGoogleId().isBlank()) {
                ps.setString(3, user.getGoogleId());
            } else {
                ps.setNull(3, java.sql.Types.VARCHAR);
            }
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getRole());
            ps.setBoolean(6, user.isActive());
            ps.setTimestamp(7, Timestamp.valueOf(
                    user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()
            ));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu người dùng", e);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET email = ?, password_hash = ?, google_id = ?, full_name = ?, role = ?, active = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            if (user.getPasswordHash() != null) {
                ps.setString(2, user.getPasswordHash());
            } else {
                ps.setNull(2, java.sql.Types.VARCHAR);
            }
            if (user.getGoogleId() != null && !user.getGoogleId().isBlank()) {
                ps.setString(3, user.getGoogleId());
            } else {
                ps.setNull(3, java.sql.Types.VARCHAR);
            }
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getRole());
            ps.setBoolean(6, user.isActive());
            ps.setInt(7, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật người dùng", e);
        }
    }

    @Override
    public void linkGoogleAccount(int userId, String googleId) {
        String sql = "UPDATE users SET google_id = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, googleId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi liên kết tài khoản Google", e);
        }
    }

    @Override
    public void updateProfile(int id, String fullName, String email) {
        String sql = "UPDATE users SET full_name = ?, email = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật thông tin người dùng", e);
        }
    }

    @Override
    public void updatePasswordHash(int userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật mật khẩu", e);
        }
    }

    @Override
    public void updateRoleAndActive(int id, String role, boolean active) {
        String sql = "UPDATE users SET role = ?, active = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setBoolean(2, active);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật vai trò/trạng thái", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa người dùng", e);
        }
    }

    @Override
    public long countActiveCustomers() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER' AND active = 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm khách hàng", e);
        }
        return 0;
    }

    private List<Object> appendListFilters(StringBuilder sql, String search, String role, Boolean activeOnly) {
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (u.full_name LIKE ? OR u.email LIKE ?) ");
            String like = "%" + search.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (role != null && !role.isBlank()) {
            sql.append(" AND u.role = ? ");
            params.add(role.trim().toUpperCase());
        }
        if (activeOnly != null) {
            sql.append(" AND u.active = ? ");
            params.add(activeOnly);
        }
        return params;
    }

    private void appendOrderBy(StringBuilder sql, String sortBy, String sortOrder) {
        String col = switch (sortBy != null ? sortBy : "") {
            case "name" -> "u.full_name";
            case "email" -> "u.email";
            case "role" -> "u.role";
            case "orders" -> "order_count";
            case "id" -> "u.id";
            default -> "u.created_at";
        };
        String dir = "ASC".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(col).append(" ").append(dir).append(" ");
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof String s) {
                ps.setString(i + 1, s);
            } else if (param instanceof Integer n) {
                ps.setInt(i + 1, n);
            } else if (param instanceof Boolean b) {
                ps.setBoolean(i + 1, b);
            }
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        if (hasColumn(rs, "google_id")) {
            user.setGoogleId(rs.getString("google_id"));
        }
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("active"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        return user;
    }

    private static boolean hasColumn(ResultSet rs, String column) throws SQLException {
        java.sql.ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (column.equalsIgnoreCase(meta.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    private UserAdminRow mapAdminRow(ResultSet rs) throws SQLException {
        User user = mapRow(rs);
        long orderCount = rs.getLong("order_count");
        return UserAdminRow.from(user, orderCount);
    }
}
