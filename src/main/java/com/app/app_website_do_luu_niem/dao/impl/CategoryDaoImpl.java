package com.app.app_website_do_luu_niem.dao.impl;

import com.app.app_website_do_luu_niem.dao.BaseDao;
import com.app.app_website_do_luu_niem.dao.CategoryDao;
import com.app.app_website_do_luu_niem.model.Category;
import com.app.app_website_do_luu_niem.model.CategoryAdminRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDaoImpl extends BaseDao implements CategoryDao {

    private static final String SELECT_WITH_PRODUCT_COUNT = """
            SELECT c.*, COALESCE(pc.cnt, 0) AS product_count
            FROM categories c
            LEFT JOIN (
                SELECT category_id, COUNT(*) AS cnt FROM products GROUP BY category_id
            ) pc ON pc.category_id = c.id
            """;

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách danh mục", e);
        }
        return categories;
    }

    @Override
    public List<CategoryAdminRow> findAllAdminRows(int page, int pageSize, String search,
                                                   String productFilter, String sortBy, String sortOrder) {
        StringBuilder sql = new StringBuilder(SELECT_WITH_PRODUCT_COUNT);
        sql.append(" WHERE 1=1 ");
        List<Object> params = appendListFilters(sql, search, productFilter);
        appendOrderBy(sql, sortBy, sortOrder);
        sql.append(" LIMIT ? OFFSET ? ");
        int offset = (page - 1) * pageSize;
        params.add(pageSize);
        params.add(offset);

        List<CategoryAdminRow> rows = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapAdminRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách danh mục (admin)", e);
        }
        return rows;
    }

    @Override
    public int countAll(String search, String productFilter) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) FROM categories c
                LEFT JOIN (
                    SELECT category_id, COUNT(*) AS cnt FROM products GROUP BY category_id
                ) pc ON pc.category_id = c.id
                WHERE 1=1
                """);
        List<Object> params = appendListFilters(sql, search, productFilter);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm danh mục", e);
        }
        return 0;
    }

    @Override
    public long countTotal() {
        return countSimple("SELECT COUNT(*) FROM categories");
    }

    @Override
    public long countWithProducts() {
        return countSimple("""
                SELECT COUNT(DISTINCT category_id) FROM products WHERE category_id IS NOT NULL
                """);
    }

    @Override
    public long countEmpty() {
        return countSimple("""
                SELECT COUNT(*) FROM categories c
                WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.category_id = c.id)
                """);
    }

    @Override
    public long countTotalProducts() {
        return countSimple("SELECT COUNT(*) FROM products");
    }

    @Override
    public long countProductsByCategoryId(int categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm sản phẩm theo danh mục", e);
        }
        return 0;
    }

    @Override
    public boolean nameExistsOtherThan(String name, int excludeId) {
        String sql = "SELECT 1 FROM categories WHERE name = ? AND id <> ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra tên danh mục trùng", e);
        }
    }

    @Override
    public Optional<Category> findById(int id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh mục theo id", e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    category.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu danh mục", e);
        }
    }

    @Override
    public void update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật danh mục", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa danh mục", e);
        }
    }

    private long countSimple(String sql) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thống kê danh mục", e);
        }
        return 0;
    }

    private List<Object> appendListFilters(StringBuilder sql, String search, String productFilter) {
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (c.name LIKE ? OR c.description LIKE ?) ");
            String like = "%" + search.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (productFilter != null && !productFilter.isBlank()) {
            switch (productFilter) {
                case "with" -> sql.append(" AND COALESCE(pc.cnt, 0) > 0 ");
                case "empty" -> sql.append(" AND COALESCE(pc.cnt, 0) = 0 ");
                default -> { }
            }
        }
        return params;
    }

    private void appendOrderBy(StringBuilder sql, String sortBy, String sortOrder) {
        String col = switch (sortBy != null ? sortBy : "") {
            case "name" -> "c.name";
            case "products" -> "product_count";
            case "id" -> "c.id";
            default -> "c.name";
        };
        String dir = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(col).append(" ").append(dir).append(" ");
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof String s) {
                ps.setString(i + 1, s);
            } else if (param instanceof Integer n) {
                ps.setInt(i + 1, n);
            }
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        return c;
    }

    private CategoryAdminRow mapAdminRow(ResultSet rs) throws SQLException {
        Category category = mapRow(rs);
        long productCount = rs.getLong("product_count");
        return CategoryAdminRow.from(category, productCount);
    }
}
