package com.app.app_website_do_luu_niem.controller.admin;

import com.app.app_website_do_luu_niem.dao.UserDao;
import com.app.app_website_do_luu_niem.dao.impl.UserDaoImpl;
import com.app.app_website_do_luu_niem.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "adminUserServlet", urlPatterns = "/admin/users")
public class AdminUserServlet extends HttpServlet {

    private final UserDao userDao = new UserDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }
        switch (action) {
            case "toggle-active" -> handleToggleActive(req, resp);
            default -> showList(req, resp);
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<User> users = userDao.findAll();
        req.setAttribute("users", users);
        req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
    }

    private void handleToggleActive(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Optional<User> opt = userDao.findById(id);
                if (opt.isPresent()) {
                    User user = opt.get();
                    user.setActive(!user.isActive());
                    userDao.update(user);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }
}

