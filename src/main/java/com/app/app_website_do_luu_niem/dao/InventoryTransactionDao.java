package com.app.app_website_do_luu_niem.dao;

import com.app.app_website_do_luu_niem.model.InventoryTransaction;
import java.util.List;

public interface InventoryTransactionDao {
    void save(InventoryTransaction txn);
    List<InventoryTransaction> findAll(int page, int pageSize);
    int countAll();
    java.math.BigDecimal getTotalLossValue();
}
