package com.app.app_website_do_luu_niem.controller.shop;

import com.app.app_website_do_luu_niem.dao.OrderDao;
import com.app.app_website_do_luu_niem.dao.impl.OrderDaoImpl;
import com.app.app_website_do_luu_niem.model.Order;
import com.app.app_website_do_luu_niem.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "orderSuccessServlet", urlPatterns = "/order-success")
public class OrderSuccessServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String payment = req.getParameter("payment");
        String txnRef = req.getParameter("txnRef");

        Optional<Order> orderOpt = Optional.empty();
        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                orderOpt = orderDao.findById(id);
            } catch (NumberFormatException ignored) {
                // bỏ qua
            }
        }
        if (orderOpt.isEmpty() && txnRef != null && !txnRef.isBlank()) {
            orderOpt = orderDao.findByVnpayTxnRef(txnRef);
        }

        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (!canViewOrder(req, order)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            req.setAttribute("order", order);
        }

        if (payment != null) {
            req.setAttribute("paymentNotice", mapPaymentNotice(payment));
        }

        req.getRequestDispatcher("/WEB-INF/views/shop/order-success.jsp").forward(req, resp);
    }

    private boolean canViewOrder(HttpServletRequest req, Order order) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return true;
        }
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return true;
        }
        return order.getUser() != null && order.getUser().getId() == user.getId();
    }

    private static String mapPaymentNotice(String payment) {
        return switch (payment) {
            case "success" -> "Thanh toán VNPay thành công. Đơn hàng đã được xác nhận.";
            case "failed" -> "Thanh toán VNPay không thành công. Đơn hàng vẫn ở trạng thái chờ — bạn có thể thanh toán lại.";
            case "error" -> "Không xác minh được kết quả thanh toán. Vui lòng kiểm tra đơn hàng hoặc liên hệ hỗ trợ.";
            case "vnpay_error" -> "Không chuyển được sang cổng VNPay. Đơn đã tạo — hãy thanh toán lại từ trang đơn hàng.";
            default -> null;
        };
    }
}
