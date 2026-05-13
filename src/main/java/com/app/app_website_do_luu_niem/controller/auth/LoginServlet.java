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

@WebServlet(name = "loginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("1".equals(req.getParameter("reset"))) {
            req.setAttribute("message", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới.");
        }
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String redirect = req.getParameter("redirect");

        Optional<User> userOpt = authService.login(email, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", user);
            
            // Nếu có redirect parameter, ưu tiên redirect đến đó
            if (redirect != null && !redirect.isBlank()) {
                resp.sendRedirect(redirect);
            } else {
                // Nếu là admin, redirect đến trang quản trị
                if ("ADMIN".equals(user.getRole())) {
                    resp.sendRedirect(req.getContextPath() + "/admin");
                } else {
                    // Nếu là customer, redirect đến trang chủ
                    resp.sendRedirect(req.getContextPath() + "/home");
                }
            }
        } else {
            req.setAttribute("error", "Email hoặc mật khẩu không đúng, hoặc tài khoản đã bị khóa.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        }
    }
}


