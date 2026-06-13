package com.app.app_website_do_luu_niem.controller.auth;

import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "verifyOtpServlet", urlPatterns = "/verify-otp")
public class VerifyOtpServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userIdParam = req.getParameter("userId");
        if (userIdParam == null || userIdParam.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/register");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/auth/verify-otp.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userIdParam = req.getParameter("userId");
        String email = req.getParameter("email");
        String action = req.getParameter("action");

        if (userIdParam == null || userIdParam.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/register");
            return;
        }

        if ("resend".equals(action)) {
            try {
                int userId = Integer.parseInt(userIdParam);
                Optional<User> optUser = authService.findByEmailNormalized(email);
                if (optUser.isPresent()) {
                    authService.sendRegistrationOtp(optUser.get());
                    req.setAttribute("message", "Mã OTP mới đã được gửi thành công.");
                } else {
                    req.setAttribute("error", "Không tìm thấy người dùng.");
                }
            } catch (NumberFormatException e) {
                req.setAttribute("error", "Mã ID không hợp lệ.");
            }
            req.getRequestDispatcher("/WEB-INF/views/auth/verify-otp.jsp").forward(req, resp);
            return;
        }

        String otp = req.getParameter("otp");
        if (otp == null || otp.isBlank()) {
            req.setAttribute("error", "Vui lòng nhập mã OTP.");
            req.getRequestDispatcher("/WEB-INF/views/auth/verify-otp.jsp").forward(req, resp);
            return;
        }

        try {
            int userId = Integer.parseInt(userIdParam);
            boolean verified = authService.verifyRegistrationOtp(userId, otp);
            if (verified) {
                Optional<User> optUser = authService.findById(userId);
                if (optUser.isPresent()) {
                    HttpSession session = req.getSession(true);
                    session.setAttribute("currentUser", optUser.get());
                    session.setAttribute("loginTime", java.time.LocalDateTime.now());
                }
                resp.sendRedirect(req.getContextPath() + "/home");
            } else {
                req.setAttribute("error", "Mã OTP không chính xác hoặc đã hết hạn.");
                req.getRequestDispatcher("/WEB-INF/views/auth/verify-otp.jsp").forward(req, resp);
            }
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/register");
        }
    }
}
