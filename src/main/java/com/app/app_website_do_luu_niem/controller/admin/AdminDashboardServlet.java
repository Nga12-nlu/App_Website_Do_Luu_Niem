package com.app.app_website_do_luu_niem.controller.admin;

import com.app.app_website_do_luu_niem.dao.CategoryDao;
import com.app.app_website_do_luu_niem.dao.OrderDao;
import com.app.app_website_do_luu_niem.dao.ProductDao;
import com.app.app_website_do_luu_niem.dao.UserDao;
import com.app.app_website_do_luu_niem.dao.impl.CategoryDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.OrderDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.ProductDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.UserDaoImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet(name = "adminDashboardServlet", urlPatterns = "/admin")
public class AdminDashboardServlet extends HttpServlet {

    private static final int LOW_STOCK_THRESHOLD = 15;
    private static final int REVENUE_CHART_DAYS = 7;

    private final OrderDao orderDao = new OrderDaoImpl();
    private final ProductDao productDao = new ProductDaoImpl();
    private final UserDao userDao = new UserDaoImpl();
    private final CategoryDao categoryDao = new CategoryDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int totalProducts = productDao.countAll(null, null);
        int totalOrders = orderDao.countAll(null, null);
        BigDecimal totalRevenue = orderDao.getTotalRevenue();
        long pendingOrders = orderDao.countByStatus("PENDING");
        long confirmedOrders = orderDao.countByStatus("CONFIRMED");
        long shippedOrders = orderDao.countByStatus("SHIPPED");
        long cancelledOrders = orderDao.countByStatus("CANCELLED");

        long ordersThisMonth = orderDao.countOrdersThisMonth();
        BigDecimal revenueThisMonth = orderDao.getRevenueThisMonth();
        long totalCustomers = userDao.countActiveCustomers();
        int totalCategories = categoryDao.findAll().size();
        int lowStockCount = productDao.countLowStock(LOW_STOCK_THRESHOLD);

        long completedOrders = confirmedOrders + shippedOrders;
        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (completedOrders > 0 && totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(completedOrders), 0, RoundingMode.HALF_UP);
        }

        req.setAttribute("totalProducts", totalProducts);
        req.setAttribute("totalOrders", totalOrders);
        req.setAttribute("totalRevenue", totalRevenue);
        req.setAttribute("pendingOrders", pendingOrders);
        req.setAttribute("confirmedOrders", confirmedOrders);
        req.setAttribute("shippedOrders", shippedOrders);
        req.setAttribute("cancelledOrders", cancelledOrders);
        req.setAttribute("ordersThisMonth", ordersThisMonth);
        req.setAttribute("revenueThisMonth", revenueThisMonth);
        req.setAttribute("totalCustomers", totalCustomers);
        req.setAttribute("totalCategories", totalCategories);
        req.setAttribute("lowStockCount", lowStockCount);
        req.setAttribute("averageOrderValue", averageOrderValue);
        req.setAttribute("lowStockThreshold", LOW_STOCK_THRESHOLD);

        req.setAttribute("recentOrders", orderDao.findRecent(8));
        req.setAttribute("lowStockProducts", productDao.findLowStock(LOW_STOCK_THRESHOLD, 6));
        req.setAttribute("revenueChart", orderDao.getRevenueByLastDays(REVENUE_CHART_DAYS));

        req.setAttribute("todayFormatted", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", java.util.Locale.forLanguageTag("vi"))));
        req.setAttribute("dashboardPage", true);

        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}
