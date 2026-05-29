package com.app.app_website_do_luu_niem.util;

import com.app.app_website_do_luu_niem.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public final class AuthRedirectHelper {

    private AuthRedirectHelper() {
    }

    public static void completeLogin(HttpServletRequest req, HttpServletResponse resp, User user, String redirect)
            throws IOException {
        HttpSession session = req.getSession(true);
        session.setAttribute("currentUser", user);

        if (redirect != null && !redirect.isBlank() && isSafeRedirect(req, redirect)) {
            resp.sendRedirect(redirect);
            return;
        }
        if (user.isAdminRole()) {
            resp.sendRedirect(req.getContextPath() + "/admin");
        } else {
            resp.sendRedirect(req.getContextPath() + "/home");
        }
    }

    /** Chỉ cho phép redirect nội bộ (cùng context path), chặn open redirect. */
    public static boolean isSafeRedirect(HttpServletRequest req, String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return false;
        }
        String ctx = req.getContextPath();
        if (!redirect.startsWith(ctx)) {
            return false;
        }
        if (redirect.equals(ctx)) {
            return true;
        }
        String path = redirect.substring(ctx.length());
        if (!path.startsWith("/") || path.startsWith("//") || path.contains("://")) {
            return false;
        }
        return true;
    }
}
