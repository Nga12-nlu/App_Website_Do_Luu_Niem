package com.app.app_website_do_luu_niem.service;

import com.app.app_website_do_luu_niem.dao.CouponDao;
import com.app.app_website_do_luu_niem.dao.impl.CouponDaoImpl;
import com.app.app_website_do_luu_niem.model.Coupon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class CouponService {

    public static final String SESSION_APPLIED_COUPON = "appliedCoupon";

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{3,32}$");

    private final CouponDao couponDao = new CouponDaoImpl();

    public record CouponValidation(String code, BigDecimal discount, String message, boolean valid) {
    }

    public CouponValidation validateAndCalculate(String rawCode, BigDecimal subtotal, Integer userId) {
        if (rawCode == null || rawCode.isBlank()) {
            return new CouponValidation("", BigDecimal.ZERO, "Vui lòng nhập mã giảm giá.", false);
        }
        String code = rawCode.trim().toUpperCase(Locale.ROOT);
        if (!CODE_PATTERN.matcher(code).matches()) {
            return new CouponValidation(code, BigDecimal.ZERO,
                    "Mã chỉ gồm chữ, số, gạch ngang (3–32 ký tự).", false);
        }
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return new CouponValidation(code, BigDecimal.ZERO, "Giỏ hàng trống.", false);
        }

        Optional<Coupon> opt = couponDao.findByCode(code);
        if (opt.isEmpty()) {
            return new CouponValidation(code, BigDecimal.ZERO, "Mã giảm giá không tồn tại.", false);
        }
        Coupon c = opt.get();
        if (!c.isActive()) {
            return new CouponValidation(code, BigDecimal.ZERO, "Mã giảm giá đã ngừng hoạt động.", false);
        }
        LocalDateTime now = LocalDateTime.now();
        if (c.getStartsAt() != null && now.isBefore(c.getStartsAt())) {
            return new CouponValidation(code, BigDecimal.ZERO, "Mã giảm giá chưa có hiệu lực.", false);
        }
        if (c.getExpiresAt() != null && now.isAfter(c.getExpiresAt())) {
            return new CouponValidation(code, BigDecimal.ZERO, "Mã giảm giá đã hết hạn.", false);
        }
        if (c.getMinOrderAmount() != null && subtotal.compareTo(c.getMinOrderAmount()) < 0) {
            return new CouponValidation(code, BigDecimal.ZERO,
                    "Đơn tối thiểu " + formatMoney(c.getMinOrderAmount()) + " để dùng mã này.", false);
        }
        if (c.getUsageLimit() != null && c.getUsedCount() >= c.getUsageLimit()) {
            return new CouponValidation(code, BigDecimal.ZERO, "Mã đã hết lượt sử dụng.", false);
        }
        if (userId != null && c.getPerUserLimit() > 0) {
            int used = couponDao.countUsageByUser(c.getId(), userId);
            if (used >= c.getPerUserLimit()) {
                return new CouponValidation(code, BigDecimal.ZERO, "Bạn đã dùng hết lượt cho mã này.", false);
            }
        }

        BigDecimal discount = calculateDiscount(c, subtotal);
        String msg = c.isPercent()
                ? "Giảm " + c.getDiscountValue().stripTrailingZeros().toPlainString() + "%"
                : "Giảm " + formatMoney(c.getDiscountValue());
        if (c.getDescription() != null && !c.getDescription().isBlank()) {
            msg += " — " + c.getDescription();
        }
        return new CouponValidation(code, discount, msg, true);
    }

    public BigDecimal calculateDiscount(Coupon c, BigDecimal subtotal) {
        BigDecimal discount;
        if (c.isPercent()) {
            discount = subtotal.multiply(c.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (c.getMaxDiscount() != null && discount.compareTo(c.getMaxDiscount()) > 0) {
                discount = c.getMaxDiscount();
            }
        } else {
            discount = c.getDiscountValue();
        }
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }
        return discount.setScale(0, RoundingMode.HALF_UP);
    }

    public void recordOrderUsage(int couponId, int userId, int orderId) {
        if (couponDao.hasUsageForOrder(orderId)) {
            return;
        }
        if (!couponDao.incrementUsedCountIfAllowed(couponId)) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng.");
        }
        couponDao.recordUsage(couponId, userId, orderId);
    }

    public Optional<Coupon> findByCode(String code) {
        return couponDao.findByCode(code);
    }

    private static String formatMoney(BigDecimal amount) {
        return new java.text.DecimalFormat("#,###").format(amount) + "đ";
    }
}
