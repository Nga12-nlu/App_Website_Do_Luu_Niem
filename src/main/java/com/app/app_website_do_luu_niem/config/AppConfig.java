package com.app.app_website_do_luu_niem.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Đọc {@code db.properties} (URL DB + tùy chọn mail / app).
 */
public final class AppConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("Không đọc được db.properties", e);
        }
    }

    private AppConfig() {
    }

    public static String get(String key) {
        return PROPS.getProperty(key);
    }

    public static boolean isMailEnabled() {
        return Boolean.parseBoolean(PROPS.getProperty("mail.enabled", "false"));
    }

    /** URL công khai (vd https://shop.example.com) không có dấu / cuối; dùng trong link reset email. */
    public static String getPublicBaseUrl() {
        String u = PROPS.getProperty("app.public.base.url", "").trim();
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }

    public static String getMailHost() {
        return nullToEmpty(PROPS.getProperty("mail.smtp.host"));
    }

    public static int getMailPort() {
        try {
            return Integer.parseInt(PROPS.getProperty("mail.smtp.port", "587"));
        } catch (NumberFormatException e) {
            return 587;
        }
    }

    public static String getMailUsername() {
        return nullToEmpty(PROPS.getProperty("mail.smtp.username"));
    }

    public static String getMailPassword() {
        return nullToEmpty(PROPS.getProperty("mail.smtp.password"));
    }

    public static boolean getMailStartTls() {
        return Boolean.parseBoolean(PROPS.getProperty("mail.smtp.starttls.enable", "true"));
    }

    public static String getMailFromAddress() {
        return nullToEmpty(PROPS.getProperty("mail.from.address"));
    }

    public static String getMailFromName() {
        String n = PROPS.getProperty("mail.from.name", "Souvenir Shop");
        return n != null ? n.trim() : "Souvenir Shop";
    }

    public static boolean isGoogleOAuthEnabled() {
        return !getGoogleClientId().isEmpty() && !getGoogleClientSecret().isEmpty();
    }

    public static String getGoogleClientId() {
        return nullToEmpty(PROPS.getProperty("google.oauth.client.id"));
    }

    public static String getGoogleClientSecret() {
        return nullToEmpty(PROPS.getProperty("google.oauth.client.secret"));
    }

    /** Để trống = {app.public.base.url hoặc request}/auth/google/callback */
    public static String getGoogleRedirectUriOverride() {
        return nullToEmpty(PROPS.getProperty("google.oauth.redirect.uri"));
    }

    /** Đã bật VNPay trong cấu hình (có thể sandbox). */
    public static boolean isVnpayFeatureOn() {
        return Boolean.parseBoolean(PROPS.getProperty("vnpay.enabled", "false"));
    }

    /** Đủ TMN + secret — mới chọn thanh toán / redirect sang VNPay. */
    public static boolean isVnpayEnabled() {
        return isVnpayFeatureOn()
                && !getVnpayTmnCode().isEmpty()
                && !getVnpayHashSecret().isEmpty();
    }

    public static boolean isVnpaySandbox() {
        return getVnpayPayUrl().contains("sandbox.vnpayment.vn");
    }

    public static String getVnpayTmnCode() {
        return nullToEmpty(PROPS.getProperty("vnpay.tmn.code"));
    }

    public static String getVnpayHashSecret() {
        return nullToEmpty(PROPS.getProperty("vnpay.hash.secret"));
    }

    public static String getVnpayPayUrl() {
        String u = PROPS.getProperty("vnpay.pay.url",
                "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        return u != null ? u.trim() : "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    }

    public static String getVnpayVersion() {
        return nullToEmpty(PROPS.getProperty("vnpay.version", "2.1.0"));
    }

    public static String getVnpayReturnUrlOverride() {
        return nullToEmpty(PROPS.getProperty("vnpay.return.url"));
    }

    public static String getVnpayIpnUrlOverride() {
        return nullToEmpty(PROPS.getProperty("vnpay.ipn.url"));
    }

    public static String getAddressApiProvider() {
        String p = nullToEmpty(PROPS.getProperty("address.api.provider", "open-api"));
        return p.isEmpty() ? "open-api" : p;
    }

    public static java.math.BigDecimal getShippingBaseFee() {
        return parseDecimal(PROPS.getProperty("shipping.base.fee"), "30000");
    }

    public static java.math.BigDecimal getShippingFreeThreshold() {
        return parseDecimal(PROPS.getProperty("shipping.free.threshold"), "500000");
    }

    public static java.math.BigDecimal getShippingRemoteSurcharge() {
        return parseDecimal(PROPS.getProperty("shipping.remote.surcharge"), "15000");
    }

    public static boolean isRemoteProvince(String provinceCode) {
        if (provinceCode == null || provinceCode.isBlank()) {
            return false;
        }
        String list = PROPS.getProperty("shipping.remote.provinces", "96,97");
        for (String code : list.split(",")) {
            if (provinceCode.trim().equals(code.trim())) {
                return true;
            }
        }
        return false;
    }

    private static java.math.BigDecimal parseDecimal(String value, String defaultValue) {
        try {
            String v = value != null && !value.isBlank() ? value.trim() : defaultValue;
            return new java.math.BigDecimal(v);
        } catch (Exception e) {
            return new java.math.BigDecimal(defaultValue);
        }
    }

    private static String nullToEmpty(String s) {
        return s != null ? s.trim() : "";
    }
}
