package com.app.app_website_do_luu_niem.service;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.app.app_website_do_luu_niem.dao.OrderDao;
import com.app.app_website_do_luu_niem.dao.impl.OrderDaoImpl;
import com.app.app_website_do_luu_niem.model.Order;
import com.app.app_website_do_luu_niem.util.VNPayUtil;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class PaymentService {

    public enum VnpayConfirmResult {
        SUCCESS,
        ALREADY_CONFIRMED,
        INVALID_SIGNATURE,
        INVALID_AMOUNT,
        INVALID_TMN,
        ORDER_NOT_FOUND,
        /** Khách hủy / thanh toán thất bại (vnp_ResponseCode != 00) — không cập nhật đơn */
        PAYMENT_DECLINED,
        INVALID_DATA
    }

    private final OrderDao orderDao = new OrderDaoImpl();

    public VnpayConfirmResult confirmVnpayCallback(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return VnpayConfirmResult.INVALID_DATA;
        }

        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null || secureHash.isBlank()) {
            return VnpayConfirmResult.INVALID_SIGNATURE;
        }

        Map<String, String> hashFields = VNPayUtil.extractSignedParams(params);
        String calculated = VNPayUtil.hashAllFields(hashFields, AppConfig.getVnpayHashSecret());
        if (!calculated.equalsIgnoreCase(secureHash)) {
            return VnpayConfirmResult.INVALID_SIGNATURE;
        }

        String tmn = params.get("vnp_TmnCode");
        if (tmn == null || !tmn.equals(AppConfig.getVnpayTmnCode())) {
            return VnpayConfirmResult.INVALID_TMN;
        }

        String txnRef = params.get("vnp_TxnRef");
        if (txnRef == null || txnRef.isBlank()) {
            return VnpayConfirmResult.INVALID_DATA;
        }

        Optional<Order> orderOpt = orderDao.findByVnpayTxnRef(txnRef);
        if (orderOpt.isEmpty()) {
            return VnpayConfirmResult.ORDER_NOT_FOUND;
        }
        Order order = orderOpt.get();

        if (!order.isVnpay()) {
            return VnpayConfirmResult.ORDER_NOT_FOUND;
        }

        String amountRaw = params.get("vnp_Amount");
        if (amountRaw == null) {
            return VnpayConfirmResult.INVALID_DATA;
        }
        long expectedMinor = VNPayUtil.toMinorUnits(order.getTotalAmount());
        long actualMinor;
        try {
            actualMinor = Long.parseLong(amountRaw);
        } catch (NumberFormatException e) {
            return VnpayConfirmResult.INVALID_AMOUNT;
        }
        if (expectedMinor != actualMinor) {
            return VnpayConfirmResult.INVALID_AMOUNT;
        }

        if ("CONFIRMED".equalsIgnoreCase(order.getStatus())
                || "SHIPPED".equalsIgnoreCase(order.getStatus())) {
            return VnpayConfirmResult.ALREADY_CONFIRMED;
        }

        String responseCode = params.get("vnp_ResponseCode");
        if (!"00".equals(responseCode)) {
            return VnpayConfirmResult.PAYMENT_DECLINED;
        }

        String transactionNo = params.get("vnp_TransactionNo");
        if (transactionNo == null) {
            transactionNo = "";
        }

        BigDecimal paidAmount = VNPayService.amountFromVnpayParam(amountRaw);
        if (orderDao.markVnpayPaid(order.getId(), transactionNo, paidAmount)) {
            return VnpayConfirmResult.SUCCESS;
        }
        Optional<Order> refreshed = orderDao.findById(order.getId());
        if (refreshed.isPresent() && ("CONFIRMED".equalsIgnoreCase(refreshed.get().getStatus())
                || "SHIPPED".equalsIgnoreCase(refreshed.get().getStatus()))) {
            return VnpayConfirmResult.ALREADY_CONFIRMED;
        }
        return VnpayConfirmResult.PAYMENT_DECLINED;
    }

    public Optional<Order> findOrderForUser(int orderId, int userId) {
        return orderDao.findById(orderId)
                .filter(o -> o.getUser() != null && o.getUser().getId() == userId);
    }
}
