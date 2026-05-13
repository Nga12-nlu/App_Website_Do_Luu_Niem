package com.app.app_website_do_luu_niem.controller.shop;

import com.app.app_website_do_luu_niem.dao.CategoryDao;
import com.app.app_website_do_luu_niem.dao.ProductDao;
import com.app.app_website_do_luu_niem.dao.impl.CategoryDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.ProductDaoImpl;
import com.app.app_website_do_luu_niem.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "homeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    private final ProductDao productDao = new ProductDaoImpl();
    private final CategoryDao categoryDao = new CategoryDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Product> latestProducts = productDao.findAll(1, 8, null, null, null);
        req.setAttribute("latestProducts", latestProducts);
        req.setAttribute("categories", categoryDao.findAll());
        req.getRequestDispatcher("/WEB-INF/views/shop/home.jsp").forward(req, resp);
    }
}


