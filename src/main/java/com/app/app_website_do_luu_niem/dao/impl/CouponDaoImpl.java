package com.app.app_website_do_luu_niem.dao.impl;

import com.app.app_website_do_luu_niem.dao.BaseDao;
import com.app.app_website_do_luu_niem.dao.CouponDao;
import com.app.app_website_do_luu_niem.model.Coupon;

import java.math.BigDecimal;
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

public class CouponDaoImpl extends BaseDao implements CouponDao {

    @Override
    public Optional<Coupon> findByCode(String code) {
        String sql = "SELECT * FROM coupons WHERE UPPER(code) = UPPER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn mã giảm giá", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Coupon> findById(int id) {
        String sql = "SELECT * FROM coupons WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn mã giảm giá", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Coupon> findAll() {
        String sql = "SELECT * FROM coupons ORDER BY created_at DESC";
        List<Coupon> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách mã giảm giá", e);
        }
        return list;
    }

    @Override
    public void save(Coupon coupon) {
        String sql = """
                INSERT INTO coupons (code, description, discount_type, discount_value, min_order_amount,
                max_discount, usage_limit, used_count, per_user_limit, starts_at, expires_at, active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindCoupon(ps, coupon);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    coupon.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu mã giảm giá", e);
        }
    }

    @Override
    public void update(Coupon coupon) {
        String sql = """
                UPDATE coupons SET code=?, description=?, discount_type=?, discount_value=?,
                min_order_amount=?, max_discount=?, usage_limit=?, per_user_limit=?, starts_at=?,
                expires_at=?, active=? WHERE id=?
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindCouponForUpdate(ps, coupon);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật mã giảm giá", e);
        }
    }

    @Override
    public void incrementUsedCount(int couponId) {
        if (!incrementUsedCountIfAllowed(couponId)) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng.");
        }
    }

    @Override
    public boolean incrementUsedCountIfAllowed(int couponId) {
        String sql = """
                UPDATE coupons SET used_count = used_count + 1
                WHERE id = ? AND (usage_limit IS NULL OR used_count < usage_limit)
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, couponId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật lượt dùng mã", e);
        }
    }

    @Override
    public boolean hasUsageForOrder(int orderId) {
        String sql = "SELECT 1 FROM coupon_usages WHERE order_id = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra sử dụng mã theo đơn", e);
        }
    }

    @Override
    public int countUsageByUser(int couponId, int userId) {
        String sql = "SELECT COUNT(*) FROM coupon_usages WHERE coupon_id = ? AND user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, couponId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm lượt dùng mã theo user", e);
        }
        return 0;
    }

    @Override
    public void recordUsage(int couponId, int userId, Integer orderId) {
        String sql = "INSERT INTO coupon_usages (coupon_id, user_id, order_id) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, couponId);
            ps.setInt(2, userId);
            if (orderId != null) {
                ps.setInt(3, orderId);
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi ghi nhận sử dụng mã", e);
        }
    }

    private void bindCoupon(PreparedStatement ps, Coupon c) throws SQLException {
        int i = bindCouponFields(ps, c, 1);
        ps.setInt(i++, c.getUsedCount());
        ps.setInt(i++, c.getPerUserLimit() > 0 ? c.getPerUserLimit() : 1);
        ps.setTimestamp(i++, c.getStartsAt() != null ? Timestamp.valueOf(c.getStartsAt()) : null);
        ps.setTimestamp(i++, c.getExpiresAt() != null ? Timestamp.valueOf(c.getExpiresAt()) : null);
        ps.setBoolean(i, c.isActive());
    }

    private void bindCouponForUpdate(PreparedStatement ps, Coupon c) throws SQLException {
        int i = bindCouponFields(ps, c, 1);
        ps.setInt(i++, c.getPerUserLimit() > 0 ? c.getPerUserLimit() : 1);
        ps.setTimestamp(i++, c.getStartsAt() != null ? Timestamp.valueOf(c.getStartsAt()) : null);
        ps.setTimestamp(i++, c.getExpiresAt() != null ? Timestamp.valueOf(c.getExpiresAt()) : null);
        ps.setBoolean(i++, c.isActive());
        ps.setInt(i, c.getId());
    }

    private int bindCouponFields(PreparedStatement ps, Coupon c, int startIndex) throws SQLException {
        int i = startIndex;
        ps.setString(i++, c.getCode().trim().toUpperCase());
        ps.setString(i++, c.getDescription());
        ps.setString(i++, c.getDiscountType().toUpperCase());
        ps.setBigDecimal(i++, c.getDiscountValue());
        ps.setBigDecimal(i++, c.getMinOrderAmount() != null ? c.getMinOrderAmount() : BigDecimal.ZERO);
        if (c.getMaxDiscount() != null) {
            ps.setBigDecimal(i++, c.getMaxDiscount());
        } else {
            ps.setNull(i++, java.sql.Types.DECIMAL);
        }
        if (c.getUsageLimit() != null) {
            ps.setInt(i++, c.getUsageLimit());
        } else {
            ps.setNull(i++, java.sql.Types.INTEGER);
        }
        return i;
    }

    private Coupon mapRow(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setDescription(rs.getString("description"));
        c.setDiscountType(rs.getString("discount_type"));
        c.setDiscountValue(rs.getBigDecimal("discount_value"));
        c.setMinOrderAmount(rs.getBigDecimal("min_order_amount"));
        BigDecimal max = rs.getBigDecimal("max_discount");
        if (!rs.wasNull()) {
            c.setMaxDiscount(max);
        }
        int limit = rs.getInt("usage_limit");
        if (!rs.wasNull()) {
            c.setUsageLimit(limit);
        }
        c.setUsedCount(rs.getInt("used_count"));
        c.setPerUserLimit(rs.getInt("per_user_limit"));
        Timestamp starts = rs.getTimestamp("starts_at");
        if (starts != null) {
            c.setStartsAt(starts.toLocalDateTime());
        }
        Timestamp expires = rs.getTimestamp("expires_at");
        if (expires != null) {
            c.setExpiresAt(expires.toLocalDateTime());
        }
        c.setActive(rs.getBoolean("active"));
        return c;
    }
}
