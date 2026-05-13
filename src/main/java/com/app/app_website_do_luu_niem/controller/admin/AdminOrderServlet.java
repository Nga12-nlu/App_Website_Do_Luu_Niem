package com.app.app_website_do_luu_niem.controller.admin;

import com.app.app_website_do_luu_niem.dao.OrderDao;
import com.app.app_website_do_luu_niem.dao.impl.OrderDaoImpl;
import com.app.app_website_do_luu_niem.model.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "adminOrderServlet", urlPatterns = "/admin/orders")
public class AdminOrderServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }
        switch (action) {
            case "detail" -> showDetail(req, resp);
            case "update-status" -> updateStatus(req, resp);
            default -> showList(req, resp);
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Parse parameters
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        int pageSize = parseIntOrDefault(req.getParameter("pageSize"), 10);
        String status = req.getParameter("status");
        String search = req.getParameter("search");
        
        if (status != null && status.isBlank()) {
            status = null;
        }
        if (search != null && search.isBlank()) {
            search = null;
        }
        
        // Get orders with pagination
        List<Order> orders = orderDao.findAll(page, pageSize, status, search);
        int totalOrders = orderDao.countAll(status, search);
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);
        
        req.setAttribute("orders", orders);
        req.setAttribute("currentPage", page);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalOrders", totalOrders);
        req.setAttribute("status", status != null ? status : "");
        req.setAttribute("search", search != null ? search : "");
        req.getRequestDispatcher("/WEB-INF/views/admin/orders.jsp").forward(req, resp);
    }
    
    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            Optional<Order> opt = orderDao.findById(id);
            if (opt.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/admin/orders");
                return;
            }
            req.setAttribute("order", opt.get());
            req.getRequestDispatcher("/WEB-INF/views/admin/order-detail.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
        }
    }

    private void updateStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String status = req.getParameter("status");
        if (idParam != null && status != null && !status.isBlank()) {
            try {
                int id = Integer.parseInt(idParam);
                orderDao.updateStatus(id, status);
                // Redirect về order detail để xem kết quả
                resp.sendRedirect(req.getContextPath() + "/admin/orders?action=detail&id=" + id);
                return;
            } catch (NumberFormatException ignored) {
            }
        }
        resp.sendRedirect(req.getContextPath() + "/admin/orders");
    }
}


