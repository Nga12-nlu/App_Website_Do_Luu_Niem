package com.app.app_website_do_luu_niem.controller.auth;

import com.app.app_website_do_luu_niem.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@WebServlet(name = "resetPasswordServlet", urlPatterns = "/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String raw = req.getParameter("token");
        if (raw == null || raw.isBlank()) {
            req.setAttribute("error", "Thiếu liên kết đặt lại mật khẩu. Vui lòng dùng đúng URL trong email.");
            req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
            return;
        }
        raw = raw.trim();
        if (!raw.matches("[0-9a-f]{64}")) {
            req.setAttribute("error", "Liên kết không hợp lệ.");
            req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
            return;
        }

        if (authService.peekValidToken(raw).isEmpty()) {
            req.setAttribute("error", "Liên kết đã hết hạn hoặc đã được sử dụng. Bạn có thể yêu cầu gửi lại email từ trang quên mật khẩu.");
            req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);
        String csrf = UUID.randomUUID().toString();
        session.setAttribute(AuthService.SESSION_PWD_RESET_CSRF, csrf);
        req.setAttribute("resetToken", raw);
        req.setAttribute("csrfToken", csrf);
        req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String sessionCsrf = session != null ? (String) session.getAttribute(AuthService.SESSION_PWD_RESET_CSRF) : null;

        String rawToken = req.getParameter("token");
        String formCsrf = req.getParameter("csrf");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        Optional<String> err = authService.completePasswordReset(
                rawToken, newPassword, confirmPassword, sessionCsrf, formCsrf);

        if (session != null) {
            session.removeAttribute(AuthService.SESSION_PWD_RESET_CSRF);
        }

        if (err.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/login?reset=1");
            return;
        }

        switch (err.get()) {
            case "csrf" -> req.setAttribute("error", "Phiên làm việc không hợp lệ. Vui lòng mở lại liên kết từ email.");
            case "mismatch" -> req.setAttribute("error", "Mật khẩu xác nhận không khớp.");
            case "weak" -> req.setAttribute("error", "Mật khẩu mới phải từ 8–128 ký tự, gồm ít nhất một chữ cái và một chữ số.");
            default -> req.setAttribute("error", "Không thể đặt lại mật khẩu. Liên kết có thể đã hết hạn hoặc đã dùng.");
        }
        req.setAttribute("resetToken", rawToken);
        if (rawToken != null && rawToken.matches("[0-9a-f]{64}") && authService.peekValidToken(rawToken).isPresent() && session != null) {
            String csrf = UUID.randomUUID().toString();
            session.setAttribute(AuthService.SESSION_PWD_RESET_CSRF, csrf);
            req.setAttribute("csrfToken", csrf);
        }
        req.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(req, resp);
    }
}
