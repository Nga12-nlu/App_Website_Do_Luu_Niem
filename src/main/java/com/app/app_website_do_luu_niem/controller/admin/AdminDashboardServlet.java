package com.app.app_website_do_luu_niem.controller.admin;

import com.app.app_website_do_luu_niem.dao.OrderDao;
import com.app.app_website_do_luu_niem.dao.ProductDao;
import com.app.app_website_do_luu_niem.dao.impl.OrderDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.ProductDaoImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "adminDashboardServlet", urlPatterns = "/admin")
public class AdminDashboardServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDaoImpl();
    private final ProductDao productDao = new ProductDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int totalProducts = productDao.countAll(null, null);
        int totalOrders = orderDao.findAll().size();
        java.math.BigDecimal totalRevenue = orderDao.getTotalRevenue();
        long pendingOrders = orderDao.countByStatus("PENDING");
        long confirmedOrders = orderDao.countByStatus("CONFIRMED");
        long shippedOrders = orderDao.countByStatus("SHIPPED");
        long cancelledOrders = orderDao.countByStatus("CANCELLED");

        req.setAttribute("totalProducts", totalProducts);
        req.setAttribute("totalOrders", totalOrders);
        req.setAttribute("totalRevenue", totalRevenue);
        req.setAttribute("pendingOrders", pendingOrders);
        req.setAttribute("confirmedOrders", confirmedOrders);
        req.setAttribute("shippedOrders", shippedOrders);
        req.setAttribute("cancelledOrders", cancelledOrders);
        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}


