package com.app.app_website_do_luu_niem.service;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.app.app_website_do_luu_niem.dao.ProductDao;
import com.app.app_website_do_luu_niem.dao.ProductVariantDao;
import com.app.app_website_do_luu_niem.dao.impl.ProductDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.ProductVariantDaoImpl;
import com.app.app_website_do_luu_niem.model.CartItem;
import com.app.app_website_do_luu_niem.model.CheckoutQuote;
import com.app.app_website_do_luu_niem.model.Coupon;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class CheckoutService {

    private final CouponService couponService = new CouponService();
    private final ProductDao productDao = new ProductDaoImpl();
    private final ProductVariantDao variantDao = new ProductVariantDaoImpl();

    /** Đồng bộ giá/tồn từ DB trước khi tính tiền (tránh giá cũ trong session). */
    public void refreshCartFromDatabase(List<CartItem> cart) {
        if (cart == null) {
            return;
        }
        for (CartItem item : cart) {
            if (item.getProduct() == null) {
                continue;
            }
            productDao.findById(item.getProduct().getId()).ifPresent(item::setProduct);
            if (item.getVariant() != null) {
                int variantId = item.getVariant().getId();
                variantDao.findById(variantId).ifPresent(v -> {
                    if (v.getProductId() == item.getProduct().getId() && v.isActive()) {
                        item.setVariant(v);
                    }
                });
            }
        }
    }

    public BigDecimal calculateSubtotal(List<CartItem> cart) {
        BigDecimal total = BigDecimal.ZERO;
        if (cart == null) {
            return total;
        }
        for (CartItem item : cart) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }

    public BigDecimal calculateShippingFee(BigDecimal subtotalAfterDiscount, String provinceCode) {
        return calculateShippingFee(subtotalAfterDiscount, provinceCode, null, null);
    }

    public BigDecimal calculateShippingFee(BigDecimal subtotalAfterDiscount, String provinceCode, String districtCode, String wardCode) {
        BigDecimal freeThreshold = AppConfig.getShippingFreeThreshold();
        if (subtotalAfterDiscount.compareTo(freeThreshold) >= 0) {
            return BigDecimal.ZERO;
        }

        if (AppConfig.isGhnEnabled() && districtCode != null && !districtCode.isBlank() && wardCode != null && !wardCode.isBlank()) {
            try {
                int toDistrictId = Integer.parseInt(districtCode.trim());
                GhnShippingService ghnService = new GhnShippingService();
                int weight = AppConfig.getGhnDefaultWeight();
                return ghnService.calculateShippingFee(toDistrictId, wardCode.trim(), weight, subtotalAfterDiscount);
            } catch (Exception e) {
                // System fallback on any exception
                System.err.println("GHN Shipping Fee calculation failed, falling back to local: " + e.getMessage());
            }
        }

        // Fallback to local flat fee
        BigDecimal base = AppConfig.getShippingBaseFee();
        BigDecimal fee = base;
        if (provinceCode != null && AppConfig.isRemoteProvince(provinceCode)) {
            fee = fee.add(AppConfig.getShippingRemoteSurcharge());
        }
        return fee.setScale(0, RoundingMode.HALF_UP);
    }

    public CheckoutQuote buildQuote(List<CartItem> cart, String couponCode, Integer userId, String provinceCode) {
        return buildQuote(cart, couponCode, userId, provinceCode, null, null);
    }

    public CheckoutQuote buildQuote(List<CartItem> cart, String couponCode, Integer userId, String provinceCode, String districtCode, String wardCode) {
        CheckoutQuote quote = new CheckoutQuote();
        quote.setSubtotal(calculateSubtotal(cart));

        if (couponCode != null && !couponCode.isBlank()) {
            CouponService.CouponValidation v = couponService.validateAndCalculate(couponCode, quote.getSubtotal(), userId);
            if (v.valid()) {
                quote.setCouponApplied(true);
                quote.setCouponCode(v.code());
                quote.setCouponMessage(v.message());
                quote.setDiscountAmount(v.discount());
            } else {
                quote.setCouponApplied(false);
                quote.setCouponCode(null);
                quote.setCouponMessage(v.message());
                quote.setDiscountAmount(BigDecimal.ZERO);
            }
        }

        BigDecimal afterDiscount = quote.getSubtotal().subtract(quote.getDiscountAmount());
        if (afterDiscount.compareTo(BigDecimal.ZERO) < 0) {
            afterDiscount = BigDecimal.ZERO;
        }
        BigDecimal shipping = calculateShippingFee(afterDiscount, provinceCode, districtCode, wardCode);
        if (quote.isCouponApplied() && quote.getCouponCode() != null
                && "FREESHIP".equalsIgnoreCase(quote.getCouponCode().trim())) {
            shipping = BigDecimal.ZERO;
        }
        quote.setShippingFee(shipping);
        quote.recalculateTotal();
        return quote;
    }

    public Optional<Coupon> resolveCouponForOrder(String couponCode, BigDecimal subtotal, int userId) {
        if (couponCode == null || couponCode.isBlank()) {
            return Optional.empty();
        }
        CouponService.CouponValidation v = couponService.validateAndCalculate(couponCode, subtotal, userId);
        if (!v.valid()) {
            return Optional.empty();
        }
        return couponService.findByCode(v.code());
    }

    public static String buildFullAddress(String detail, String wardName, String districtName, String provinceName) {
        StringBuilder sb = new StringBuilder();
        appendPart(sb, detail);
        appendPart(sb, wardName);
        appendPart(sb, districtName);
        appendPart(sb, provinceName);
        return sb.toString().trim();
    }

    private static void appendPart(StringBuilder sb, String part) {
        if (part == null || part.isBlank()) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append(", ");
        }
        sb.append(part.trim());
    }
}
