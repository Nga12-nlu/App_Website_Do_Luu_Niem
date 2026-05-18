package com.app.app_website_do_luu_niem.model;

import java.math.BigDecimal;

public class DashboardRevenuePoint {
    private String label;
    private BigDecimal revenue;
    private long orderCount;

    public DashboardRevenuePoint() {
    }

    public DashboardRevenuePoint(String label, BigDecimal revenue, long orderCount) {
        this.label = label;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
        this.orderCount = orderCount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }
}
