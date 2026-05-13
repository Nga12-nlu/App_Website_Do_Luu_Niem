package com.app.app_website_do_luu_niem.controller.auth;

import com.app.app_website_do_luu_niem.dao.UserDao;
import com.app.app_website_do_luu_niem.dao.impl.UserDaoImpl;
import com.app.app_website_do_luu_niem.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "profileServlet", urlPatterns = "/profile")
public class ProfileServlet extends HttpServlet {

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

        // Lấy thông tin mới nhất từ database
        Optional<User> opt = userDao.findById(currentUser.getId());
        if (opt.isPresent()) {
            req.setAttribute("user", opt.get());
        } else {
            req.setAttribute("user", currentUser);
        }

        req.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(req, resp);
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

        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");

        if (fullName == null || fullName.isBlank() || email == null || email.isBlank()) {
            req.setAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
            Optional<User> opt = userDao.findById(currentUser.getId());
            req.setAttribute("user", opt.orElse(currentUser));
            req.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(req, resp);
            return;
        }

        // Kiểm tra email đã tồn tại chưa (trừ email của chính user hiện tại)
        Optional<User> existingUser = userDao.findByEmail(email);
        if (existingUser.isPresent() && existingUser.get().getId() != currentUser.getId()) {
            req.setAttribute("error", "Email này đã được sử dụng bởi tài khoản khác.");
            Optional<User> opt = userDao.findById(currentUser.getId());
            req.setAttribute("user", opt.orElse(currentUser));
            req.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(req, resp);
            return;
        }

        // Cập nhật thông tin (chỉ fullName và email, không cập nhật password)
        userDao.updateProfile(currentUser.getId(), fullName, email);

        // Lấy thông tin mới nhất từ database và cập nhật session
        Optional<User> updatedUser = userDao.findById(currentUser.getId());
        if (updatedUser.isPresent()) {
            session.setAttribute("currentUser", updatedUser.get());
            req.setAttribute("user", updatedUser.get());
            req.setAttribute("success", "Cập nhật thông tin thành công!");
        } else {
            req.setAttribute("user", currentUser);
        }

        req.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(req, resp);
    }
}

