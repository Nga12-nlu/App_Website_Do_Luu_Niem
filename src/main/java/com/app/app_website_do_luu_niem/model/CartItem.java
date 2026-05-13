package com.app.app_website_do_luu_niem.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Product product;
    /** null = giỏ cũ / sản phẩm không có biến thể */
    private ProductVariant variant;
    private int quantity;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        if (variant != null && variant.getPrice() != null) {
            return variant.getPrice();
        }
        if (product != null && product.getPrice() != null) {
            return product.getPrice();
        }
        return BigDecimal.ZERO;
    }

    public int getAvailableStock() {
        if (variant != null) {
            return variant.getStock();
        }
        return product != null ? product.getStock() : 0;
    }

    public BigDecimal getTotalPrice() {
        return getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public String getLineKey() {
        int vid = variant != null ? variant.getId() : 0;
        return (product != null ? product.getId() : 0) + "_" + vid;
    }
}


