package com.app.app_website_do_luu_niem.controller.auth;

import com.app.app_website_do_luu_niem.dao.UserDao;
import com.app.app_website_do_luu_niem.dao.impl.UserDaoImpl;
import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "changePasswordServlet", urlPatterns = "/change-password")
public class ChangePasswordServlet extends HttpServlet {

    private final UserDao userDao = new UserDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + 
                java.net.URLEncoder.encode(req.getRequestURI(), java.nio.charset.StandardCharsets.UTF_8));
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + 
                java.net.URLEncoder.encode(req.getRequestURI(), java.nio.charset.StandardCharsets.UTF_8));
            return;
        }

        forwardChangePassword(req, resp, currentUser);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String currentPassword = req.getParameter("currentPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        Optional<User> opt = userDao.findById(currentUser.getId());
        if (opt.isEmpty()) {
            req.setAttribute("error", "Không tìm thấy tài khoản.");
            forwardChangePassword(req, resp, currentUser);
            return;
        }

        User user = opt.get();
        boolean oauthOnly = !user.hasLocalPassword();

        if (newPassword == null || newPassword.isBlank() || confirmPassword == null || confirmPassword.isBlank()) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
            forwardChangePassword(req, resp, currentUser);
            return;
        }
        if (!oauthOnly && (currentPassword == null || currentPassword.isBlank())) {
            req.setAttribute("error", "Vui lòng nhập mật khẩu hiện tại.");
            forwardChangePassword(req, resp, currentUser);
            return;
        }
        if (!oauthOnly) {
            if (!BCrypt.checkpw(currentPassword, user.getPasswordHash())) {
                req.setAttribute("error", "Mật khẩu hiện tại không đúng.");
                forwardChangePassword(req, resp, currentUser);
                return;
            }
        }

        // Kiểm tra mật khẩu mới và xác nhận
        if (!newPassword.equals(confirmPassword)) {
            req.setAttribute("error", "Mật khẩu mới và xác nhận không khớp.");
            forwardChangePassword(req, resp, currentUser);
            return;
        }

        AuthService authService = new AuthService();
        if (!authService.isPasswordStrongEnough(newPassword)) {
            req.setAttribute("error", "Mật khẩu mới phải từ 8–128 ký tự, gồm ít nhất một chữ cái và một chữ số.");
            forwardChangePassword(req, resp, currentUser);
            return;
        }

        userDao.updatePasswordHash(user.getId(), BCrypt.hashpw(newPassword, BCrypt.gensalt()));

        req.setAttribute("success", oauthOnly ? "Đã đặt mật khẩu — bạn có thể đăng nhập bằng email và mật khẩu."
                : "Đổi mật khẩu thành công!");
        forwardChangePassword(req, resp, currentUser);
    }

    private void forwardChangePassword(HttpServletRequest req, HttpServletResponse resp, User sessionUser)
            throws ServletException, IOException {
        userDao.findById(sessionUser.getId()).ifPresent(u ->
                req.setAttribute("oauthOnly", !u.hasLocalPassword()));
        req.getRequestDispatcher("/WEB-INF/views/auth/change-password.jsp").forward(req, resp);
    }
}

