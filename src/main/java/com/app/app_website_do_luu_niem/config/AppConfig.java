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

    private static String nullToEmpty(String s) {
        return s != null ? s.trim() : "";
    }
}
