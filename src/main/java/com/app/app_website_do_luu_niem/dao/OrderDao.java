package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.model.Order;

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

    java.math.BigDecimal getTotalRevenue();

    long countByStatus(String status);
}


