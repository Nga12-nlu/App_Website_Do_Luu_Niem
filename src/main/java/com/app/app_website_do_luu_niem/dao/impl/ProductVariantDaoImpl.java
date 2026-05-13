package com.app.app_website_do_luu_niem.dao.impl;

import com.app.app_website_do_luu_niem.dao.BaseDao;
import com.app.app_website_do_luu_niem.dao.ProductVariantDao;
import com.app.app_website_do_luu_niem.model.ProductVariant;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductVariantDaoImpl extends BaseDao implements ProductVariantDao {

    @Override
    public List<ProductVariant> findByProductId(int productId) {
        String sql = "SELECT * FROM product_variants WHERE product_id = ? ORDER BY sort_order ASC, id ASC";
        List<ProductVariant> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy biến thể sản phẩm", e);
        }
        return list;
    }

    @Override
    public Optional<ProductVariant> findById(int id) {
        String sql = "SELECT * FROM product_variants WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy biến thể theo id", e);
        }
        return Optional.empty();
    }

    @Override
    public void replaceAllForProduct(int productId, List<ProductVariant> variants) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM product_variants WHERE product_id = ?")) {
                    del.setInt(1, productId);
                    del.executeUpdate();
                }
                String ins = "INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(ins)) {
                    for (ProductVariant v : variants) {
                        ps.setInt(1, productId);
                        ps.setString(2, v.getDisplayName());
                        ps.setString(3, v.getSku());
                        ps.setBigDecimal(4, v.getPrice() != null ? v.getPrice() : BigDecimal.ZERO);
                        ps.setInt(5, v.getStock());
                        ps.setString(6, v.getImageUrl());
                        ps.setInt(7, v.getSortOrder());
                        ps.setInt(8, v.isActive() ? 1 : 0);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                syncProductAggregateFromVariants(conn, productId);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Lỗi lưu biến thể sản phẩm", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối khi lưu biến thể", e);
        }
    }

    @Override
    public void syncProductAggregateFromVariants(int productId) {
        try (Connection conn = getConnection()) {
            syncProductAggregateFromVariants(conn, productId);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đồng bộ tổng hợp sản phẩm", e);
        }
    }

    private void syncProductAggregateFromVariants(Connection conn, int productId) throws SQLException {
        String sql = """
                UPDATE products p
                SET p.price = COALESCE((SELECT MIN(v.price) FROM product_variants v WHERE v.product_id = p.id AND v.active = 1), p.price),
                    p.stock = COALESCE((SELECT SUM(v.stock) FROM product_variants v WHERE v.product_id = p.id AND v.active = 1), 0)
                WHERE p.id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        }
    }

    @Override
    public void decrementStock(Connection conn, int variantId, int quantity) throws SQLException {
        String sql = "UPDATE product_variants SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, variantId);
            ps.setInt(3, quantity);
            int n = ps.executeUpdate();
            if (n != 1) {
                throw new SQLException("Không đủ tồn kho biến thể id=" + variantId);
            }
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT product_id FROM product_variants WHERE id = ?")) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    syncProductAggregateFromVariants(conn, rs.getInt(1));
                }
            }
        }
    }

    @Override
    public int getTotalStockForProduct(int productId) {
        String sql = "SELECT COALESCE(SUM(stock), 0) FROM product_variants WHERE product_id = ? AND active = 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tính tồn kho biến thể", e);
        }
        return 0;
    }

    private ProductVariant mapRow(ResultSet rs) throws SQLException {
        ProductVariant v = new ProductVariant();
        v.setId(rs.getInt("id"));
        v.setProductId(rs.getInt("product_id"));
        v.setDisplayName(rs.getString("display_name"));
        v.setSku(rs.getString("sku"));
        v.setPrice(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : BigDecimal.ZERO);
        v.setStock(rs.getInt("stock"));
        v.setImageUrl(rs.getString("image_url"));
        v.setSortOrder(rs.getInt("sort_order"));
        v.setActive(rs.getInt("active") == 1);
        return v;
    }
}
