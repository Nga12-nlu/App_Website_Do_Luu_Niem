package com.app.app_website_do_luu_niem.controller.shop;

import com.app.app_website_do_luu_niem.dao.CategoryDao;
import com.app.app_website_do_luu_niem.dao.ProductDao;
import com.app.app_website_do_luu_niem.dao.impl.CategoryDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.ProductDaoImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "productListServlet", urlPatterns = "/products")
public class ProductListServlet extends HttpServlet {

    private final ProductDao productDao = new ProductDaoImpl();
    private final CategoryDao categoryDao = new CategoryDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        int size = 9;
        String keyword = req.getParameter("q");
        String sort = req.getParameter("sort");
        Integer categoryId = parseIntOrNull(req.getParameter("category"));

        var products = productDao.findAll(page, size, keyword, categoryId, sort);
        int totalItems = productDao.countAll(keyword, categoryId);
        int totalPages = (int) Math.ceil((double) totalItems / size);

        req.setAttribute("products", products);
        req.setAttribute("categories", categoryDao.findAll());
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("keyword", keyword);
        req.setAttribute("currentCategory", categoryId);
        req.setAttribute("sort", sort);

        req.getRequestDispatcher("/WEB-INF/views/shop/product-list.jsp").forward(req, resp);
    }

    private Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        Integer v = parseIntOrNull(value);
        return v == null ? defaultValue : v;
    }
}


