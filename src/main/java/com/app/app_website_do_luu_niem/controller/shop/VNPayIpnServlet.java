package com.app.app_website_do_luu_niem.controller.shop;

import com.app.app_website_do_luu_niem.service.PaymentService;
import com.app.app_website_do_luu_niem.service.PaymentService.VnpayConfirmResult;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "vnpayIpnServlet", urlPatterns = "/payment/vnpay/ipn")
public class VNPayIpnServlet extends HttpServlet {

    private final PaymentService paymentService = new PaymentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> params = new HashMap<>();
        req.getParameterMap().forEach((key, values) -> {
            if (key.startsWith("vnp_") && values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });

        VnpayConfirmResult result = paymentService.confirmVnpayCallback(params);
        String rspCode;
        String message;
        switch (result) {
            case SUCCESS, ALREADY_CONFIRMED, PAYMENT_DECLINED -> {
                // 00 = đã nhận IPN (kể cả giao dịch thất bại — tránh VNPay gửi lại nhiều lần)
                rspCode = "00";
                message = "Confirm Success";
            }
            case ORDER_NOT_FOUND -> {
                rspCode = "01";
                message = "Order not Found";
            }
            case INVALID_SIGNATURE -> {
                rspCode = "97";
                message = "Invalid Checksum";
            }
            case INVALID_AMOUNT -> {
                rspCode = "04";
                message = "Invalid Amount";
            }
            case INVALID_TMN -> {
                rspCode = "03";
                message = "Invalid TMN";
            }
            default -> {
                rspCode = "99";
                message = "Unknown error";
            }
        }

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json");
        resp.getWriter().write("{\"RspCode\":\"" + rspCode + "\",\"Message\":\"" + message + "\"}");
    }
}
