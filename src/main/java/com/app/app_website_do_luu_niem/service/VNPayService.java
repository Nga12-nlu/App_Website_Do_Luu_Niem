package com.app.app_website_do_luu_niem.service;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.app.app_website_do_luu_niem.model.Order;
import com.app.app_website_do_luu_niem.util.AppUrlHelper;
import com.app.app_website_do_luu_niem.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class VNPayService {

    private static final DateTimeFormatter VNPAY_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public boolean isConfigured() {
        return AppConfig.isVnpayEnabled();
    }

    public String newTxnRef(int orderId) {
        int suffix = ThreadLocalRandom.current().nextInt(100_000, 999_999);
        return "DH" + orderId + "T" + suffix;
    }

    public String buildPaymentUrl(Order order, String txnRef, HttpServletRequest req) {
        if (!isConfigured()) {
            throw new IllegalStateException("VNPay chưa được cấu hình");
        }
        if (txnRef == null || txnRef.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã giao dịch VNPay");
        }

        long amount = VNPayUtil.toMinorUnits(order.getTotalAmount());

        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", AppConfig.getVnpayVersion());
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", AppConfig.getVnpayTmnCode());
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan don hang #" + order.getId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", resolveReturnUrl(req));
        params.put("vnp_IpAddr", AuthService.clientIpForVnpay(req));
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        params.put("vnp_CreateDate", now.format(VNPAY_DATE));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(VNPAY_DATE));

        String hash = VNPayUtil.hashAllFields(params, AppConfig.getVnpayHashSecret());
        params.put("vnp_SecureHash", hash);

        String query = VNPayUtil.buildQueryUrl(params);
        return AppConfig.getVnpayPayUrl() + "?" + query;
    }

    public String resolveReturnUrl(HttpServletRequest req) {
        String override = AppConfig.getVnpayReturnUrlOverride();
        if (!override.isEmpty()) {
            return override;
        }
        return AppUrlHelper.absolutePath(req, "/payment/vnpay/return");
    }

    public String resolveIpnUrl(HttpServletRequest req) {
        String override = AppConfig.getVnpayIpnUrlOverride();
        if (!override.isEmpty()) {
            return override;
        }
        return AppUrlHelper.absolutePath(req, "/payment/vnpay/ipn");
    }

    public static BigDecimal amountFromVnpayParam(String vnpAmount) {
        long minor = Long.parseLong(vnpAmount);
        return BigDecimal.valueOf(minor).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public static String encodeOrderInfo(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
