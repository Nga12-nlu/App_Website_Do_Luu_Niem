package com.app.app_website_do_luu_niem.controller.admin;

import com.app.app_website_do_luu_niem.dao.CategoryDao;
import com.app.app_website_do_luu_niem.dao.impl.CategoryDaoImpl;
import com.app.app_website_do_luu_niem.model.Category;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "adminCategoryServlet", urlPatterns = "/admin/categories")
public class AdminCategoryServlet extends HttpServlet {

    private final CategoryDao categoryDao = new CategoryDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }
        switch (action) {
            case "create" -> showForm(req, resp, new Category());
            case "edit" -> showEditForm(req, resp);
            case "delete" -> handleDelete(req, resp);
            default -> showList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        Category category;
        if (idParam == null || idParam.isBlank()) {
            category = new Category();
        } else {
            int id = Integer.parseInt(idParam);
            Optional<Category> opt = categoryDao.findById(id);
            category = opt.orElseGet(Category::new);
        }

        category.setName(req.getParameter("name"));
        category.setDescription(req.getParameter("description"));

        if (category.getId() == 0) {
            categoryDao.save(category);
        } else {
            categoryDao.update(category);
        }

        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Category> categories = categoryDao.findAll();
        req.setAttribute("categories", categories);
        req.getRequestDispatcher("/WEB-INF/views/admin/categories.jsp").forward(req, resp);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Category category)
            throws ServletException, IOException {
        req.setAttribute("category", category);
        req.getRequestDispatcher("/WEB-INF/views/admin/category-form.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/categories");
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            Optional<Category> opt = categoryDao.findById(id);
            if (opt.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/admin/categories");
                return;
            }
            showForm(req, resp, opt.get());
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/categories");
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                categoryDao.delete(id);
            } catch (NumberFormatException ignored) {
            }
        }
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }
}

