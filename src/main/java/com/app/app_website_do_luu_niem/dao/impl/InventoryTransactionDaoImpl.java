package com.app.app_website_do_luu_niem.dao.impl;

import com.app.app_website_do_luu_niem.dao.BaseDao;
import com.app.app_website_do_luu_niem.dao.InventoryTransactionDao;
import com.app.app_website_do_luu_niem.model.InventoryTransaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryTransactionDaoImpl extends BaseDao implements InventoryTransactionDao {

    @Override
    public void save(InventoryTransaction txn) {
        String insertSql = "INSERT INTO inventory_transactions (product_id, variant_id, type, quantity, note, user_id, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, txn.getProductId());
                    if (txn.getVariantId() != null) {
                        ps.setInt(2, txn.getVariantId());
                    } else {
                        ps.setNull(2, java.sql.Types.INTEGER);
                    }
                    ps.setString(3, txn.getType());
                    ps.setInt(4, txn.getQuantity());
                    ps.setString(5, txn.getNote());
                    ps.setInt(6, txn.getUserId());
                    ps.setTimestamp(7, Timestamp.valueOf(
                            txn.getCreatedAt() != null ? txn.getCreatedAt() : LocalDateTime.now()
                      ));
                    ps.executeUpdate();
                }

                int delta = txn.getQuantity();
                if (!"IMPORT".equalsIgnoreCase(txn.getType())) {
                    delta = -delta;
                }

                if (txn.getVariantId() != null) {
                    String updateVar = "UPDATE product_variants SET stock = stock + ? WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateVar)) {
                        ps.setInt(1, delta);
                        ps.setInt(2, txn.getVariantId());
                        ps.executeUpdate();
                    }
                    syncProductAggregate(conn, txn.getVariantId());
                } else {
                    String updateProd = "UPDATE products SET stock = stock + ? WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateProd)) {
                        ps.setInt(1, delta);
                        ps.setInt(2, txn.getProductId());
                        ps.executeUpdate();
                    }
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Lỗi lưu giao dịch kho", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối lưu giao dịch kho", e);
        }
    }

    private void syncProductAggregate(Connection conn, int variantId) throws SQLException {
        int productId;
        try (PreparedStatement ps = conn.prepareStatement("SELECT product_id FROM product_variants WHERE id = ?")) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return;
                }
                productId = rs.getInt(1);
            }
        }
        String sync = "UPDATE products p SET "
                + "p.price = COALESCE((SELECT MIN(v.price) FROM product_variants v WHERE v.product_id = p.id AND v.active = 1), p.price), "
                + "p.stock = COALESCE((SELECT SUM(v.stock) FROM product_variants v WHERE v.product_id = p.id AND v.active = 1), 0) "
                + "WHERE p.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sync)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<InventoryTransaction> findAll(int page, int pageSize) {
        String sql = "SELECT t.*, p.name AS p_name, v.display_name AS v_label, u.full_name AS u_name "
                + "FROM inventory_transactions t "
                + "JOIN products p ON t.product_id = p.id "
                + "LEFT JOIN product_variants v ON t.variant_id = v.id "
                + "JOIN users u ON t.user_id = u.id "
                + "ORDER BY t.created_at DESC LIMIT ? OFFSET ?";
        List<InventoryTransaction> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryTransaction txn = new InventoryTransaction();
                    txn.setId(rs.getInt("id"));
                    txn.setProductId(rs.getInt("product_id"));
                    txn.setProductName(rs.getString("p_name"));
                    int vid = rs.getInt("variant_id");
                    if (!rs.wasNull()) {
                        txn.setVariantId(vid);
                    }
                    txn.setVariantLabel(rs.getString("v_label"));
                    txn.setType(rs.getString("type"));
                    txn.setQuantity(rs.getInt("quantity"));
                    txn.setNote(rs.getString("note"));
                    txn.setUserId(rs.getInt("user_id"));
                    txn.setUserFullName(rs.getString("u_name"));
                    Timestamp ca = rs.getTimestamp("created_at");
                    if (ca != null) {
                        txn.setCreatedAt(ca.toLocalDateTime());
                    }
                    list.add(txn);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách giao dịch kho", e);
        }
        return list;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM inventory_transactions";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm giao dịch kho", e);
        }
        return 0;
    }

    @Override
    public BigDecimal getTotalLossValue() {
        String sql = "SELECT SUM(t.quantity * COALESCE(v.price, p.price)) AS total_loss "
                + "FROM inventory_transactions t "
                + "JOIN products p ON t.product_id = p.id "
                + "LEFT JOIN product_variants v ON t.variant_id = v.id "
                + "WHERE t.type IN ('DAMAGE', 'LOSS', 'DESTROYED')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total_loss");
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thống kê tổn thất kho", e);
        }
        return BigDecimal.ZERO;
    }
}
