package com.app.app_website_do_luu_niem.model;

import java.time.LocalDateTime;

/**
 * Dòng người dùng hiển thị trên trang quản trị (kèm số đơn hàng).
 */
public class UserAdminRow {
    private int id;
    private String email;
    private String fullName;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;
    private long orderCount;

    public static UserAdminRow from(User user, long orderCount) {
        UserAdminRow row = new UserAdminRow();
        row.setId(user.getId());
        row.setEmail(user.getEmail());
        row.setFullName(user.getFullName());
        row.setRole(user.getRole());
        row.setActive(user.isActive());
        row.setCreatedAt(user.getCreatedAt());
        row.setOrderCount(orderCount);
        return row;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
