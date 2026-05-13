package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.model.ProductVariant;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface ProductVariantDao {

    List<ProductVariant> findByProductId(int productId);

    Optional<ProductVariant> findById(int id);

    void replaceAllForProduct(int productId, List<ProductVariant> variants);

    /**
     * Cập nhật products.price = MIN(variant), products.stock = SUM(variant) theo biến thể đang active.
     */
    void syncProductAggregateFromVariants(int productId);

    /**
     * Giảm tồn kho biến thể (đã khóa hàng hoặc trong transaction).
     */
    void decrementStock(Connection conn, int variantId, int quantity) throws Exception;

    int getTotalStockForProduct(int productId);
}
