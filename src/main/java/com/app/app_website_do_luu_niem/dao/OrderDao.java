package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.model.DashboardRevenuePoint;
import com.app.app_website_do_luu_niem.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderDao {

    void saveWithItems(Order order);

    List<Order> findAll();

    List<Order> findAll(int page, int pageSize, String status, String search);

    int countAll(String status, String search);

    Optional<Order> findById(int id);

    List<Order> findByUserId(int userId);

    void updateStatus(int id, String status);

    Optional<Order> findByVnpayTxnRef(String txnRef);

    void updateVnpayTxnRef(int orderId, String txnRef);

    /**
     * @return true nếu cập nhật thành công (đơn PENDING + VNPAY, đúng số tiền)
     */
    boolean markVnpayPaid(int orderId, String vnpayTransactionNo, java.math.BigDecimal paidAmount);

    java.math.BigDecimal getTotalRevenue();

    long countByStatus(String status);

    List<Order> findRecent(int limit);

    List<DashboardRevenuePoint> getRevenueByLastDays(int days);

    long countOrdersThisMonth();

    BigDecimal getRevenueThisMonth();
}


