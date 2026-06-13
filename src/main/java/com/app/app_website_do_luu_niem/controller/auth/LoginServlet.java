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
        String msg = req.getParameter("msg");
        if ("role_changed".equals(msg)) {
            req.setAttribute("message", "Quyền hạn của bạn đã được thay đổi. Vui lòng đăng nhập lại.");
        } else if ("1".equals(req.getParameter("reset"))) {
            req.setAttribute("message", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới.");
        }
        req.setAttribute("googleEnabled", AppConfig.isGoogleOAuthEnabled());
        req.setAttribute("googleError", mapGoogleError(req.getParameter("error")));
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email"); // This can be email, username, or phone
        String password = req.getParameter("password");
        String redirect = req.getParameter("redirect");

        try {
            AuthService.LoginResult result = authService.login(email, password);
            
            if (result.getError() == null) {
                AuthRedirectHelper.completeLogin(req, resp, result.getUser(), redirect);
                return;
            }

            String msg = switch (result.getError()) {
                case "invalid_input" -> "Vui lòng nhập tài khoản và mật khẩu.";
                case "not_found" -> "Tài khoản không tồn tại trên hệ thống.";
                case "locked" -> "Tài khoản của bạn đã bị khóa đăng nhập tạm thời. Vui lòng thử lại sau " 
                        + result.getRemainingLockMinutes() + " phút.";
                case "banned" -> "Tài khoản của bạn đã bị khóa vĩnh viễn do vi phạm điều khoản.";
                case "unverified" -> {
                    // Redirect to OTP verification page
                    resp.sendRedirect(req.getContextPath() + "/verify-otp?userId=" + result.getUser().getId() + 
                            "&email=" + java.net.URLEncoder.encode(result.getUser().getEmail(), java.nio.charset.StandardCharsets.UTF_8) +
                            "&msg=unverified");
                    yield null; // Return from doPost after sendRedirect
                }
                case "no_local_password" -> "Tài khoản này đăng ký bằng Google. Vui lòng nhấn nút \"Đăng nhập bằng Google\".";
                default -> "Tài khoản hoặc mật khẩu không chính xác.";
            };

            if (msg != null) {
                forwardLoginError(req, resp, email, msg);
            }
        } catch (RuntimeException ex) {
            req.getServletContext().log("Login failed: " + ex.getMessage(), ex);
            forwardLoginError(req, resp, email, "Không thể đăng nhập lúc này. Vui lòng thử lại sau.");
        }
    }

    private void forwardLoginError(HttpServletRequest req, HttpServletResponse resp, String email, String message)
            throws ServletException, IOException {
        req.setAttribute("error", message);
        req.setAttribute("email", email);
        req.setAttribute("googleEnabled", AppConfig.isGoogleOAuthEnabled());
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
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


