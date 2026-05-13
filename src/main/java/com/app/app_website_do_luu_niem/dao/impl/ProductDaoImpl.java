package com.app.app_website_do_luu_niem.dao.impl;

import com.app.app_website_do_luu_niem.dao.BaseDao;
import com.app.app_website_do_luu_niem.dao.ProductDao;
import com.app.app_website_do_luu_niem.model.Category;
import com.app.app_website_do_luu_niem.model.Product;

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

public class ProductDaoImpl extends BaseDao implements ProductDao {

    @Override
    public List<Product> findAll(int page, int size, String keyword, Integer categoryId, String sort) {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.id AS c_id, c.name AS c_name, c.description AS c_description " +
                "FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (p.name LIKE ? OR p.description LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (categoryId != null) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }

        appendOrderBy(sql, sort);

        sql.append("LIMIT ? OFFSET ?");
        int offset = (page - 1) * size;
        params.add(size);
        params.add(offset);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    ps.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    ps.setInt(i + 1, (Integer) param);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách sản phẩm", e);
        }
        return products;
    }

    @Override
    public int countAll(String keyword, Integer categoryId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (name LIKE ? OR description LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (categoryId != null) {
            sql.append("AND category_id = ? ");
            params.add(categoryId);
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    ps.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    ps.setInt(i + 1, (Integer) param);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm số lượng sản phẩm", e);
        }
        return 0;
    }

    @Override
    public Optional<Product> findById(int id) {
        String sql = "SELECT p.*, c.id AS c_id, c.name AS c_name, c.description AS c_description " +
                "FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE p.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy sản phẩm theo id", e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Product product) {
        String sql = "INSERT INTO products (name, description, price, stock, image_url, category_id, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStock());
            ps.setString(5, product.getImageUrl());
            if (product.getCategory() != null) {
                ps.setInt(6, product.getCategory().getId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setTimestamp(7, Timestamp.valueOf(
                    product.getCreatedAt() != null ? product.getCreatedAt() : LocalDateTime.now()
            ));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu sản phẩm", e);
        }
    }

    @Override
    public void update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock = ?, image_url = ?, category_id = ? " +
                "WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStock());
            ps.setString(5, product.getImageUrl());
            if (product.getCategory() != null) {
                ps.setInt(6, product.getCategory().getId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setInt(7, product.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật sản phẩm", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xoá sản phẩm", e);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : BigDecimal.ZERO);
        p.setStock(rs.getInt("stock"));
        p.setImageUrl(rs.getString("image_url"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            p.setCreatedAt(createdAt.toLocalDateTime());
        }

        int categoryId = rs.getInt("c_id");
        if (categoryId > 0) {
            Category c = new Category();
            c.setId(categoryId);
            c.setName(rs.getString("c_name"));
            c.setDescription(rs.getString("c_description"));
            p.setCategory(c);
        }

        return p;
    }

    private void appendOrderBy(StringBuilder sql, String sort) {
        if (sort == null || sort.isBlank()) {
            sql.append("ORDER BY p.created_at DESC ");
            return;
        }
        String normalized = sort.trim().replaceAll("\\s+", " ");
        if ("price_asc".equalsIgnoreCase(normalized)) {
            sql.append("ORDER BY p.price ASC ");
            return;
        }
        if ("price_desc".equalsIgnoreCase(normalized)) {
            sql.append("ORDER BY p.price DESC ");
            return;
        }
        String[] parts = normalized.split(" ", 2);
        String col = parts[0].toLowerCase();
        String dir = parts.length > 1 ? parts[1].toUpperCase() : "DESC";
        if (!"ASC".equals(dir) && !"DESC".equals(dir)) {
            dir = "DESC";
        }
        String orderCol = switch (col) {
            case "id" -> "p.id";
            case "name" -> "p.name";
            case "price" -> "p.price";
            case "stock" -> "p.stock";
            case "created_at" -> "p.created_at";
            default -> null;
        };
        if (orderCol != null) {
            sql.append("ORDER BY ").append(orderCol).append(" ").append(dir).append(" ");
        } else {
            sql.append("ORDER BY p.created_at DESC ");
        }
    }
}


