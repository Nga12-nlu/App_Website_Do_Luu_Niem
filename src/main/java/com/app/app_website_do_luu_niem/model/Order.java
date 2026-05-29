package com.app.app_website_do_luu_niem.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private int id;
    private User user;
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;
    private String vnpayTxnRef;
    private String vnpayTransactionNo;
    private LocalDateTime paidAt;
    private String shippingAddress;
    private String phone;
    private LocalDateTime createdAt;
    private List<OrderItem> items;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isVnpay() {
        return "VNPAY".equalsIgnoreCase(paymentMethod);
    }

    public boolean isCod() {
        return paymentMethod == null || paymentMethod.isBlank() || "COD".equalsIgnoreCase(paymentMethod);
    }

    public String getVnpayTxnRef() {
        return vnpayTxnRef;
    }

    public void setVnpayTxnRef(String vnpayTxnRef) {
        this.vnpayTxnRef = vnpayTxnRef;
    }

    public String getVnpayTransactionNo() {
        return vnpayTransactionNo;
    }

    public void setVnpayTransactionNo(String vnpayTransactionNo) {
        this.vnpayTransactionNo = vnpayTransactionNo;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public boolean isPaid() {
        return paidAt != null || "CONFIRMED".equalsIgnoreCase(status) || "SHIPPED".equalsIgnoreCase(status);
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}


