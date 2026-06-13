package com.app.app_website_do_luu_niem.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HMAC SHA512 và build query theo tài liệu VNPay.
 */
public final class VNPayUtil {

    private VNPayUtil() {
    }

    public static String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Không tạo được chữ ký VNPay", e);
        }
    }

    private static String encode(String val) {
        if (val == null) {
            return "";
        }
        return URLEncoder.encode(val, StandardCharsets.UTF_8);
    }

    public static String hashAllFields(Map<String, String> fields, String secretKey) {
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            String value = fields.get(name);
            if (value == null || value.isEmpty()) {
                continue;
            }
            sb.append(name);
            sb.append('=');
            sb.append(encode(value));
            sb.append('&');
        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        return hmacSha512(secretKey, sb.toString());
    }

    /** Số tiền VNPay (đơn vị xu = VND × 100). */
    public static long toMinorUnits(BigDecimal vndAmount) {
        if (vndAmount == null) {
            return 0L;
        }
        return vndAmount.setScale(0, RoundingMode.HALF_UP).longValue() * 100L;
    }

    public static Map<String, String> extractSignedParams(Map<String, String> params) {
        Map<String, String> hashFields = new HashMap<>();
        if (params == null) {
            return hashFields;
        }
        for (Map.Entry<String, String> e : params.entrySet()) {
            String key = e.getKey();
            if (!key.startsWith("vnp_")) {
                continue;
            }
            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key)) {
                continue;
            }
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                hashFields.put(key, e.getValue());
            }
        }
        return hashFields;
    }

    public static String buildQueryUrl(Map<String, String> fields) {
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            String value = fields.get(name);
            if (value == null || value.isEmpty()) {
                continue;
            }
            sb.append(name);
            sb.append('=');
            sb.append(encode(value));
            sb.append('&');
        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
