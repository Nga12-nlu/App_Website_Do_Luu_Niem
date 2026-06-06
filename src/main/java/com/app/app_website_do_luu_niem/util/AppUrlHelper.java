package com.app.app_website_do_luu_niem.util;

import com.app.app_website_do_luu_niem.config.AppConfig;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Ghép URL công khai (base + context path).
 */
public final class AppUrlHelper {

    private AppUrlHelper() {
    }

    public static String resolveBaseUrl(HttpServletRequest req) {
        String base = AppConfig.getPublicBaseUrl();
        String ctx = req.getContextPath();
        if (base.isEmpty()) {
            int port = req.getServerPort();
            String scheme = req.getScheme();
            String host = req.getServerName();
            boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
            return scheme + "://" + host + (defaultPort ? "" : ":" + port) + ctx;
        }
        if (ctx != null && !ctx.isBlank() && !"/".equals(ctx)) {
            try {
                java.net.URI uri = java.net.URI.create(base);
                String path = uri.getPath();
                if (path == null || path.isEmpty() || "/".equals(path)) {
                    return base + ctx;
                } else if (!path.equals(ctx) && !path.startsWith(ctx + "/")) {
                    String schemeHostPort = uri.getScheme() + "://" + uri.getAuthority();
                    return schemeHostPort + ctx;
                }
            } catch (Exception e) {
                if (!base.endsWith(ctx) && !base.contains(ctx + "/")) {
                    return base + ctx;
                }
            }
        }
        return base;
    }

    public static String absolutePath(HttpServletRequest req, String path) {
        return resolveBaseUrl(req) + path;
    }
}
