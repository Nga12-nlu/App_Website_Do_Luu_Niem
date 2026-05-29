package com.app.app_website_do_luu_niem.controller.shop;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.app.app_website_do_luu_niem.dao.OrderDao;
import com.app.app_website_do_luu_niem.dao.impl.OrderDaoImpl;
import com.app.app_website_do_luu_niem.model.CartItem;
import com.app.app_website_do_luu_niem.model.Order;
import com.app.app_website_do_luu_niem.model.OrderItem;
import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.service.VNPayService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "checkoutServlet", urlPatterns = "/checkout")
public class CheckoutServlet extends HttpServlet {

    private final OrderDao orderDao = new OrderDaoImpl();
    private final VNPayService vnpayService = new VNPayService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + req.getRequestURI());
            return;
        }
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }
        req.setAttribute("cartItems", cart);
        req.setAttribute("totalAmount", calculateTotal(cart));
        setVnpayCheckoutAttributes(req);
        req.getRequestDispatcher("/WEB-INF/views/shop/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + req.getRequestURI());
            return;
        }
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + req.getRequestURI());
            return;
        }

        String address = req.getParameter("address");
        String phone = req.getParameter("phone");
        String paymentMethod = req.getParameter("paymentMethod");
        if (paymentMethod == null || paymentMethod.isBlank()) {
            paymentMethod = "COD";
        }
        boolean useVnpay = "VNPAY".equalsIgnoreCase(paymentMethod);

        if (address == null || address.isBlank() || phone == null || phone.isBlank()) {
            forwardCheckoutError(req, resp, cart, "Vui lòng nhập đầy đủ địa chỉ và số điện thoại.");
            return;
        }

        if (useVnpay && !vnpayService.isConfigured()) {
            forwardCheckoutError(req, resp, cart, "Thanh toán VNPay chưa được cấu hình. Vui lòng chọn COD.");
            return;
        }

        Order order = new Order();
        order.setUser(currentUser);
        order.setShippingAddress(address);
        order.setPhone(phone);
        order.setStatus("PENDING");
        order.setPaymentMethod(useVnpay ? "VNPAY" : "COD");
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (CartItem ci : cart) {
            OrderItem oi = new OrderItem();
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());
            if (ci.getVariant() != null) {
                oi.setVariantId(ci.getVariant().getId());
                oi.setVariantLabel(ci.getVariant().getDisplayName());
            }
            items.add(oi);
            total = total.add(ci.getTotalPrice());
        }
        order.setItems(items);
        order.setTotalAmount(total);

        try {
            orderDao.saveWithItems(order);
        } catch (RuntimeException ex) {
            forwardCheckoutError(req, resp, cart, "Không thể hoàn tất đơn hàng: " + ex.getMessage());
            return;
        }

        if (useVnpay) {
            String txnRef = vnpayService.newTxnRef(order.getId());
            orderDao.updateVnpayTxnRef(order.getId(), txnRef);
            order.setVnpayTxnRef(txnRef);
            session.removeAttribute("cart");
            try {
                String payUrl = vnpayService.buildPaymentUrl(order, txnRef, req);
                resp.sendRedirect(payUrl);
            } catch (Exception e) {
                req.getServletContext().log("VNPay redirect failed: " + e.getMessage(), e);
                resp.sendRedirect(req.getContextPath() + "/order-success?id=" + order.getId()
                        + "&payment=vnpay_error");
            }
            return;
        }

        session.removeAttribute("cart");
        resp.sendRedirect(req.getContextPath() + "/order-success?id=" + order.getId());
    }

    private void forwardCheckoutError(HttpServletRequest req, HttpServletResponse resp, List<CartItem> cart,
                                      String error) throws ServletException, IOException {
        req.setAttribute("error", error);
        req.setAttribute("cartItems", cart);
        req.setAttribute("totalAmount", calculateTotal(cart));
        setVnpayCheckoutAttributes(req);
        req.getRequestDispatcher("/WEB-INF/views/shop/checkout.jsp").forward(req, resp);
    }

    private void setVnpayCheckoutAttributes(HttpServletRequest req) {
        req.setAttribute("vnpayEnabled", AppConfig.isVnpayEnabled());
        req.setAttribute("vnpayFeatureOn", AppConfig.isVnpayFeatureOn());
        req.setAttribute("vnpaySandbox", AppConfig.isVnpaySandbox());
        if (AppConfig.isVnpayFeatureOn() && !AppConfig.isVnpayEnabled()) {
            req.setAttribute("vnpayConfigWarning",
                    "VNPay sandbox đã bật — hãy điền vnpay.tmn.code và vnpay.hash.secret trong db.properties (lấy từ email sau khi đăng ký sandbox.vnpayment.vn).");
        }
    }

    private BigDecimal calculateTotal(List<CartItem> cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }
}
