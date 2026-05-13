package com.app.app_website_do_luu_niem.dao.impl;

import com.app.app_website_do_luu_niem.dao.BaseDao;
import com.app.app_website_do_luu_niem.dao.OrderDao;
import com.app.app_website_do_luu_niem.model.Order;
import com.app.app_website_do_luu_niem.model.OrderItem;
import com.app.app_website_do_luu_niem.model.Product;
import com.app.app_website_do_luu_niem.model.User;

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

public class OrderDaoImpl extends BaseDao implements OrderDao {

    @Override
    public void saveWithItems(Order order) {
        String orderSql = "INSERT INTO orders (user_id, total_amount, status, shipping_address, phone, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, variant_id, variant_label) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (OrderItem item : order.getItems()) {
                    if (item.getVariantId() != null) {
                        lockAndValidateVariantStock(conn, item.getVariantId(), item.getQuantity());
                    } else {
                        lockAndValidateProductStock(conn, item.getProduct().getId(), item.getQuantity());
                    }
                }

                try (PreparedStatement orderPs = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    orderPs.setInt(1, order.getUser().getId());
                    orderPs.setBigDecimal(2, order.getTotalAmount());
                    orderPs.setString(3, order.getStatus());
                    orderPs.setString(4, order.getShippingAddress());
                    orderPs.setString(5, order.getPhone());
                    orderPs.setTimestamp(6, Timestamp.valueOf(
                            order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now()
                    ));
                    orderPs.executeUpdate();

                    try (ResultSet rs = orderPs.getGeneratedKeys()) {
                        if (rs.next()) {
                            order.setId(rs.getInt(1));
                        }
                    }
                }

                try (PreparedStatement itemPs = conn.prepareStatement(itemSql)) {
                    for (OrderItem item : order.getItems()) {
                        itemPs.setInt(1, order.getId());
                        itemPs.setInt(2, item.getProduct().getId());
                        itemPs.setInt(3, item.getQuantity());
                        itemPs.setBigDecimal(4, item.getUnitPrice());
                        if (item.getVariantId() != null) {
                            itemPs.setInt(5, item.getVariantId());
                        } else {
                            itemPs.setNull(5, java.sql.Types.INTEGER);
                        }
                        itemPs.setString(6, item.getVariantLabel());
                        itemPs.addBatch();
                    }
                    itemPs.executeBatch();
                }

                for (OrderItem item : order.getItems()) {
                    if (item.getVariantId() != null) {
                        decrementVariantStock(conn, item.getVariantId(), item.getQuantity());
                    } else {
                        decrementProductStock(conn, item.getProduct().getId(), item.getQuantity());
                    }
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Lỗi lưu đơn hàng: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu đơn hàng", e);
        }
    }

    private void lockAndValidateVariantStock(Connection conn, int variantId, int quantity) throws SQLException {
        String sql = "SELECT stock, product_id FROM product_variants WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Biến thể không tồn tại (id=" + variantId + ")");
                }
                int stock = rs.getInt("stock");
                if (stock < quantity) {
                    throw new SQLException("Không đủ tồn kho cho biến thể đã chọn.");
                }
            }
        }
    }

    private void lockAndValidateProductStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "SELECT stock FROM products WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Sản phẩm không tồn tại.");
                }
                int stock = rs.getInt("stock");
                if (stock < quantity) {
                    throw new SQLException("Không đủ tồn kho sản phẩm.");
                }
            }
        }
    }

    private void decrementVariantStock(Connection conn, int variantId, int quantity) throws SQLException {
        String sql = "UPDATE product_variants SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, variantId);
            ps.setInt(3, quantity);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Không thể trừ tồn biến thể.");
            }
        }
        syncProductAggregate(conn, variantId);
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

    private void decrementProductStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Không thể trừ tồn sản phẩm.");
            }
        }
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT o.*, u.full_name, u.email " +
                "FROM orders o JOIN users u ON o.user_id = u.id ORDER BY o.created_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách đơn hàng", e);
        }
        return orders;
    }

    @Override
    public List<Order> findAll(int page, int pageSize, String status, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT o.*, u.full_name, u.email " +
                "FROM orders o JOIN users u ON o.user_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }

        if (search != null && !search.isBlank()) {
            sql.append(" AND (u.full_name LIKE ? OR u.email LIKE ? OR o.id LIKE ?)");
            String searchPattern = "%" + search + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        sql.append(" ORDER BY o.created_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        List<Order> orders = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách đơn hàng", e);
        }
        return orders;
    }

    @Override
    public int countAll(String status, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) as count FROM orders o JOIN users u ON o.user_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }

        if (search != null && !search.isBlank()) {
            sql.append(" AND (u.full_name LIKE ? OR u.email LIKE ? OR o.id LIKE ?)");
            String searchPattern = "%" + search + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm đơn hàng", e);
        }
        return 0;
    }

    @Override
    public Optional<Order> findById(int id) {
        String orderSql = "SELECT o.*, u.full_name, u.email " +
                "FROM orders o JOIN users u ON o.user_id = u.id WHERE o.id = ?";
        String itemSql = "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, oi.variant_id, oi.variant_label, "
                + "p.name, p.image_url FROM order_items oi "
                + "JOIN products p ON oi.product_id = p.id WHERE oi.order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement orderPs = conn.prepareStatement(orderSql);
             PreparedStatement itemPs = conn.prepareStatement(itemSql)) {

            orderPs.setInt(1, id);
            Order order = null;
            try (ResultSet rs = orderPs.executeQuery()) {
                if (rs.next()) {
                    order = mapOrder(rs);
                } else {
                    return Optional.empty();
                }
            }

            itemPs.setInt(1, id);
            List<OrderItem> items = new ArrayList<>();
            try (ResultSet rs = itemPs.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrder(order);

                    Product p = new Product();
                    p.setId(rs.getInt("product_id"));
                    p.setName(rs.getString("name"));
                    p.setImageUrl(rs.getString("image_url"));
                    item.setProduct(p);

                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    int vid = rs.getInt("variant_id");
                    if (!rs.wasNull()) {
                        item.setVariantId(vid);
                    }
                    item.setVariantLabel(rs.getString("variant_label"));
                    items.add(item);
                }
            }
            order.setItems(items);
            return Optional.of(order);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy chi tiết đơn hàng", e);
        }
    }

    @Override
    public List<Order> findByUserId(int userId) {
        String sql = "SELECT o.*, u.full_name, u.email " +
                "FROM orders o JOIN users u ON o.user_id = u.id WHERE o.user_id = ? ORDER BY o.created_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy đơn hàng của user", e);
        }
        return orders;
    }

    @Override
    public void updateStatus(int id, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật trạng thái đơn hàng", e);
        }
    }

    @Override
    public java.math.BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total FROM orders WHERE status IN ('CONFIRMED', 'SHIPPED')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tính tổng doanh thu", e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) as count FROM orders WHERE status = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("count");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm đơn hàng theo trạng thái", e);
        }
        return 0;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        order.setUser(user);
        order.setTotalAmount(rs.getBigDecimal("total_amount") != null
                ? rs.getBigDecimal("total_amount") : BigDecimal.ZERO);
        order.setStatus(rs.getString("status"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setPhone(rs.getString("phone"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            order.setCreatedAt(createdAt.toLocalDateTime());
        }
        return order;
    }
}
