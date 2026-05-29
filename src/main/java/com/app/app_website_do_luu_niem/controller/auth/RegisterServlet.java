package com.app.app_website_do_luu_niem.controller.auth;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.app.app_website_do_luu_niem.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "registerServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        forwardRegister(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        if (fullName == null || fullName.isBlank() ||
                email == null || email.isBlank() ||
                password == null || password.isBlank()) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
            forwardRegister(req, resp);
            return;
        }

        if (!password.equals(confirmPassword)) {
            req.setAttribute("error", "Mật khẩu nhập lại không khớp.");
            forwardRegister(req, resp);
            return;
        }

        if (!authService.isPasswordStrongEnough(password)) {
            req.setAttribute("error", "Mật khẩu phải từ 8–128 ký tự, gồm ít nhất một chữ cái và một chữ số.");
            forwardRegister(req, resp);
            return;
        }

        boolean success = authService.register(fullName, email, password);
        if (!success) {
            req.setAttribute("error", "Email đã được sử dụng, vui lòng chọn email khác.");
            forwardRegister(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }

    private void forwardRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("googleEnabled", AppConfig.isGoogleOAuthEnabled());
        req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
    }
}


