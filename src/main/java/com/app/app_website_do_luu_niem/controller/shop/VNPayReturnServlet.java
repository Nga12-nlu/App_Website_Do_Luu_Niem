package com.app.app_website_do_luu_niem.controller.shop;

import com.app.app_website_do_luu_niem.dao.OrderDao;
import com.app.app_website_do_luu_niem.dao.impl.OrderDaoImpl;
import com.app.app_website_do_luu_niem.model.Order;
import com.app.app_website_do_luu_niem.service.PaymentService;
import com.app.app_website_do_luu_niem.service.PaymentService.VnpayConfirmResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet(name = "vnpayReturnServlet", urlPatterns = "/payment/vnpay/return")
public class VNPayReturnServlet extends HttpServlet {

    private final PaymentService paymentService = new PaymentService();
    private final OrderDao orderDao = new OrderDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> params = extractVnpParams(req);
        VnpayConfirmResult result = paymentService.confirmVnpayCallback(params);

        String txnRef = params.get("vnp_TxnRef");
        StringBuilder redirect = new StringBuilder(req.getContextPath()).append("/order-success");

        Optional<Order> orderOpt = Optional.empty();
        if (txnRef != null && !txnRef.isBlank()) {
            orderOpt = orderDao.findByVnpayTxnRef(txnRef);
        }

        switch (result) {
            case SUCCESS, ALREADY_CONFIRMED -> appendQuery(redirect, "payment=success", txnRef, orderOpt);
            case PAYMENT_DECLINED -> appendQuery(redirect, "payment=failed", txnRef, orderOpt);
            case INVALID_SIGNATURE, INVALID_AMOUNT, INVALID_TMN, ORDER_NOT_FOUND, INVALID_DATA ->
                    appendQuery(redirect, "payment=error", txnRef, orderOpt);
        }

        resp.sendRedirect(redirect.toString());
    }

    private static void appendQuery(StringBuilder url, String paymentStatus, String txnRef,
                                    Optional<Order> orderOpt) {
        url.append("?").append(paymentStatus);
        orderOpt.ifPresent(o -> url.append("&id=").append(o.getId()));
        if (txnRef != null && !txnRef.isBlank()) {
            url.append("&txnRef=").append(enc(txnRef));
        }
    }

    private static Map<String, String> extractVnpParams(HttpServletRequest req) {
        Map<String, String> params = new HashMap<>();
        req.getParameterMap().forEach((key, values) -> {
            if (key.startsWith("vnp_") && values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }

    private static String enc(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
