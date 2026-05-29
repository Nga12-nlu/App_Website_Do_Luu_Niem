package com.app.app_website_do_luu_niem.controller.auth;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.service.AuthService;
import com.app.app_website_do_luu_niem.util.AuthRedirectHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "loginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("1".equals(req.getParameter("reset"))) {
            req.setAttribute("message", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới.");
        }
        req.setAttribute("googleEnabled", AppConfig.isGoogleOAuthEnabled());
        req.setAttribute("googleError", mapGoogleError(req.getParameter("error")));
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String redirect = req.getParameter("redirect");

        Optional<User> userOpt = authService.login(email, password);
        if (userOpt.isPresent()) {
            AuthRedirectHelper.completeLogin(req, resp, userOpt.get(), redirect);
        } else {
            req.setAttribute("error", "Email hoặc mật khẩu không đúng, hoặc tài khoản đã bị khóa.");
            req.setAttribute("email", email);
            req.setAttribute("googleEnabled", AppConfig.isGoogleOAuthEnabled());
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        }
    }

    private static String mapGoogleError(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return switch (code) {
            case "google_disabled" -> "Đăng nhập Google chưa được cấu hình trên hệ thống.";
            case "google_denied" -> "Bạn đã hủy đăng nhập với Google.";
            case "google_state" -> "Phiên đăng nhập không hợp lệ. Vui lòng thử lại.";
            case "google_inactive" -> "Tài khoản của bạn đã bị khóa.";
            case "google_email_conflict" -> "Email này đã liên kết với tài khoản Google khác.";
            case "google_email_unverified" -> "Email Google chưa được xác minh.";
            default -> "Không thể đăng nhập bằng Google. Vui lòng thử lại.";
        };
    }
}


