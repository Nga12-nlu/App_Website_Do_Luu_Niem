package com.app.app_website_do_luu_niem.controller.shop;

import com.app.app_website_do_luu_niem.dao.OrderDao;
import com.app.app_website_do_luu_niem.dao.impl.OrderDaoImpl;
import com.app.app_website_do_luu_niem.model.Order;
import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.service.PaymentService;
import com.app.app_website_do_luu_niem.service.VNPayService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

/**
 * Thanh toán lại đơn VNPay đang chờ (PENDING).
 */
@WebServlet(name = "vnpayPayServlet", urlPatterns = "/payment/vnpay/pay")
public class VNPayPayServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDaoImpl();
    private final PaymentService paymentService = new PaymentService();
    private final VNPayService vnpayService = new VNPayService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if (!vnpayService.isConfigured()) {
            resp.sendRedirect(req.getContextPath() + "/my-orders?error=vnpay_disabled");
            return;
        }

        String idParam = req.getParameter("orderId");
        if (idParam == null) {
            resp.sendRedirect(req.getContextPath() + "/my-orders");
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/my-orders");
            return;
        }

        Optional<Order> orderOpt = paymentService.findOrderForUser(orderId, user.getId());
        if (orderOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Order order = orderOpt.get();
        if (!order.isVnpay() || !"PENDING".equalsIgnoreCase(order.getStatus())) {
            resp.sendRedirect(req.getContextPath() + "/order-success?id=" + orderId);
            return;
        }

        String txnRef = vnpayService.newTxnRef(orderId);
        orderDao.updateVnpayTxnRef(orderId, txnRef);
        order.setVnpayTxnRef(txnRef);

        String payUrl = vnpayService.buildPaymentUrl(order, txnRef, req);
        resp.sendRedirect(payUrl);
    }
}
