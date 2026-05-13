package com.app.app_website_do_luu_niem.controller.shop;

import com.app.app_website_do_luu_niem.dao.OrderDao;
import com.app.app_website_do_luu_niem.dao.impl.OrderDaoImpl;
import com.app.app_website_do_luu_niem.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "myOrdersServlet", urlPatterns = "/my-orders")
public class MyOrdersServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDaoImpl();

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

        var orders = orderDao.findByUserId(currentUser.getId());
        req.setAttribute("orders", orders);
        req.getRequestDispatcher("/WEB-INF/views/shop/my-orders.jsp").forward(req, resp);
    }
}

