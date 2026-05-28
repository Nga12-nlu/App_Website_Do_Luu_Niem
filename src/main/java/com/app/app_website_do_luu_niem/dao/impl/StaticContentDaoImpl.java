package com.app.app_website_do_luu_niem.dao.impl;

import com.app.app_website_do_luu_niem.dao.BaseDao;
import com.app.app_website_do_luu_niem.dao.StaticContentDao;
import com.app.app_website_do_luu_niem.model.StaticContent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StaticContentDaoImpl extends BaseDao implements StaticContentDao {

    @Override
    public List<StaticContent> findAll(int page, int pageSize, String search, String groupName) {
        StringBuilder sql = new StringBuilder("SELECT * FROM static_contents WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, search, groupName);
        sql.append("ORDER BY group_name ASC, sort_order ASC, id ASC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        List<StaticContent> items = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách nội dung tĩnh", e);
        }
        return items;
    }

    @Override
    public int countAll(String search, String groupName) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM static_contents WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, search, groupName);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi đếm nội dung tĩnh", e);
        }
        return 0;
    }

    @Override
    public List<String> findGroups() {
        List<String> groups = new ArrayList<>();
        String sql = "SELECT DISTINCT group_name FROM static_contents ORDER BY group_name ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                groups.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy nhóm nội dung", e);
        }
        return groups;
    }

    @Override
    public Optional<StaticContent> findById(int id) {
        String sql = "SELECT * FROM static_contents WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy nội dung tĩnh theo id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<StaticContent> findByKey(String key) {
        String sql = "SELECT * FROM static_contents WHERE content_key = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy nội dung tĩnh theo khóa", e);
        }
        return Optional.empty();
    }

    @Override
    public Map<String, String> findActiveMap() {
        Map<String, String> map = new LinkedHashMap<>();
        String sql = "SELECT content_key, content_value FROM static_contents WHERE active = 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("content_key"), rs.getString("content_value"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy map nội dung tĩnh", e);
        }
        return map;
    }

    @Override
    public long countAllItems() {
        return countSimple("SELECT COUNT(*) FROM static_contents");
    }

    @Override
    public long countActiveItems() {
        return countSimple("SELECT COUNT(*) FROM static_contents WHERE active = 1");
    }

    @Override
    public void update(StaticContent item) {
        String sql = """
                UPDATE static_contents
                SET content_value = ?, active = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getValue());
            ps.setBoolean(2, item.isActive());
            ps.setInt(3, item.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật nội dung tĩnh", e);
        }
    }

    private void appendFilters(StringBuilder sql, List<Object> params, String search, String groupName) {
        if (search != null && !search.isBlank()) {
            sql.append("AND (content_key LIKE ? OR label LIKE ? OR content_value LIKE ?) ");
            String like = "%" + search.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (groupName != null && !groupName.isBlank()) {
            sql.append("AND group_name = ? ");
            params.add(groupName);
        }
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof String s) {
                ps.setString(i + 1, s);
            } else if (param instanceof Integer n) {
                ps.setInt(i + 1, n);
            }
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
            throw new RuntimeException("Lỗi thống kê nội dung tĩnh", e);
        }
        return 0;
    }

    private StaticContent mapRow(ResultSet rs) throws SQLException {
        StaticContent item = new StaticContent();
        item.setId(rs.getInt("id"));
        item.setContentKey(rs.getString("content_key"));
        item.setGroupName(rs.getString("group_name"));
        item.setLabel(rs.getString("label"));
        item.setValue(rs.getString("content_value"));
        item.setInputType(rs.getString("input_type"));
        item.setActive(rs.getBoolean("active"));
        item.setSortOrder(rs.getInt("sort_order"));
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            item.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return item;
    }
}
