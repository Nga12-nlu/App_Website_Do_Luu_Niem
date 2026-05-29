package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.model.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponDao {

    Optional<Coupon> findByCode(String code);

    Optional<Coupon> findById(int id);

    List<Coupon> findAll();

    void save(Coupon coupon);

    void update(Coupon coupon);

    void incrementUsedCount(int couponId);

    /** Tăng lượt dùng nếu chưa vượt usage_limit. */
    boolean incrementUsedCountIfAllowed(int couponId);

    int countUsageByUser(int couponId, int userId);

    boolean hasUsageForOrder(int orderId);

    void recordUsage(int couponId, int userId, Integer orderId);
}
