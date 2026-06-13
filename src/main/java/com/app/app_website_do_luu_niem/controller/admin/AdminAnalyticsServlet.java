package com.app.app_website_do_luu_niem.controller.admin;

import com.app.app_website_do_luu_niem.config.DBConnection;
import com.app.app_website_do_luu_niem.dao.InventoryTransactionDao;
import com.app.app_website_do_luu_niem.dao.impl.InventoryTransactionDaoImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "adminAnalyticsServlet", urlPatterns = "/admin/analytics")
public class AdminAnalyticsServlet extends HttpServlet {

    private final InventoryTransactionDao inventoryTransactionDao = new InventoryTransactionDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int deadStockDays = parseIntOrDefault(req.getParameter("deadStockDays"), 30);

        List<DeadStockItem> deadStockList = getDeadStock(deadStockDays);
        BigDecimal totalCapitalTiedUp = BigDecimal.ZERO;
        for (DeadStockItem item : deadStockList) {
            totalCapitalTiedUp = totalCapitalTiedUp.add(item.getCapitalTiedUp());
        }

        BigDecimal totalLossValue = inventoryTransactionDao.getTotalLossValue();
        List<LossBreakdownItem> lossBreakdown = getLossBreakdown();
        List<LossItem> topLossItems = getTopLossItems();

        req.setAttribute("deadStockDays", deadStockDays);
        req.setAttribute("deadStockList", deadStockList);
        req.setAttribute("totalCapitalTiedUp", totalCapitalTiedUp);
        req.setAttribute("totalLossValue", totalLossValue);
        req.setAttribute("lossBreakdown", lossBreakdown);
        req.setAttribute("topLossItems", topLossItems);

        req.getRequestDispatcher("/WEB-INF/views/admin/analytics.jsp").forward(req, resp);
    }

    private List<DeadStockItem> getDeadStock(int days) {
        List<DeadStockItem> list = new ArrayList<>();
        String sql = """
                SELECT p.id AS product_id, p.name AS product_name, v.id AS variant_id, v.display_name AS variant_name, v.sku AS variant_sku, v.stock AS stock_qty, v.price AS price, (v.stock * v.price) AS capital_tied_up
                FROM product_variants v
                JOIN products p ON v.product_id = p.id
                WHERE v.stock > 0 AND v.active = 1
                  AND NOT EXISTS (
                      SELECT 1
                      FROM order_items oi
                      JOIN orders o ON oi.order_id = o.id
                      WHERE oi.variant_id = v.id
                        AND o.created_at >= DATE_SUB(NOW(), INTERVAL ? DAY)
                        AND o.status <> 'CANCELLED'
                  )
                UNION ALL
                SELECT p.id AS product_id, p.name AS product_name, NULL AS variant_id, NULL AS variant_name, NULL AS variant_sku, p.stock AS stock_qty, p.price AS price, (p.stock * p.price) AS capital_tied_up
                FROM products p
                WHERE p.stock > 0
                  AND NOT EXISTS (SELECT 1 FROM product_variants v WHERE v.product_id = p.id AND v.active = 1)
                  AND NOT EXISTS (
                      SELECT 1
                      FROM order_items oi
                      JOIN orders o ON oi.order_id = o.id
                      WHERE oi.product_id = p.id
                        AND o.created_at >= DATE_SUB(NOW(), INTERVAL ? DAY)
                        AND o.status <> 'CANCELLED'
                  )
                ORDER BY capital_tied_up DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            ps.setInt(2, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DeadStockItem item = new DeadStockItem();
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    int vid = rs.getInt("variant_id");
                    if (!rs.wasNull()) {
                        item.setVariantId(vid);
                    }
                    item.setVariantName(rs.getString("variant_name"));
                    item.setSku(rs.getString("variant_sku"));
                    item.setStockQty(rs.getInt("stock_qty"));
                    item.setPrice(rs.getBigDecimal("price"));
                    item.setCapitalTiedUp(rs.getBigDecimal("capital_tied_up"));
                    list.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<LossBreakdownItem> getLossBreakdown() {
        List<LossBreakdownItem> list = new ArrayList<>();
        String sql = """
                SELECT t.type, SUM(t.quantity) AS total_qty, SUM(t.quantity * COALESCE(v.price, p.price)) AS total_val
                FROM inventory_transactions t
                JOIN products p ON t.product_id = p.id
                LEFT JOIN product_variants v ON t.variant_id = v.id
                WHERE t.type IN ('DAMAGE', 'LOSS', 'DESTROYED')
                GROUP BY t.type
                ORDER BY total_val DESC
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LossBreakdownItem item = new LossBreakdownItem();
                item.setType(rs.getString("type"));
                item.setTotalQty(rs.getInt("total_qty"));
                item.setTotalVal(rs.getBigDecimal("total_val"));
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<LossItem> getTopLossItems() {
        List<LossItem> list = new ArrayList<>();
        String sql = """
                SELECT p.id AS product_id, p.name AS product_name, v.id AS variant_id, v.display_name AS variant_name,
                       SUM(t.quantity) AS total_qty, SUM(t.quantity * COALESCE(v.price, p.price)) AS total_val
                FROM inventory_transactions t
                JOIN products p ON t.product_id = p.id
                LEFT JOIN product_variants v ON t.variant_id = v.id
                WHERE t.type IN ('DAMAGE', 'LOSS', 'DESTROYED')
                GROUP BY p.id, p.name, v.id, v.display_name
                ORDER BY total_val DESC
                LIMIT 10
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LossItem item = new LossItem();
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                int vid = rs.getInt("variant_id");
                if (!rs.wasNull()) {
                    item.setVariantId(vid);
                }
                item.setVariantName(rs.getString("variant_name"));
                item.setTotalQty(rs.getInt("total_qty"));
                item.setTotalVal(rs.getBigDecimal("total_val"));
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static class DeadStockItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private int productId;
        private String productName;
        private Integer variantId;
        private String variantName;
        private String sku;
        private int stockQty;
        private BigDecimal price;
        private BigDecimal capitalTiedUp;

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getVariantId() {
            return variantId;
        }

        public void setVariantId(Integer variantId) {
            this.variantId = variantId;
        }

        public String getVariantName() {
            return variantName;
        }

        public void setVariantName(String variantName) {
            this.variantName = variantName;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public int getStockQty() {
            return stockQty;
        }

        public void setStockQty(int stockQty) {
            this.stockQty = stockQty;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getCapitalTiedUp() {
            return capitalTiedUp;
        }

        public void setCapitalTiedUp(BigDecimal capitalTiedUp) {
            this.capitalTiedUp = capitalTiedUp;
        }
    }

    public static class LossBreakdownItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String type;
        private int totalQty;
        private BigDecimal totalVal;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getTotalQty() {
            return totalQty;
        }

        public void setTotalQty(int totalQty) {
            this.totalQty = totalQty;
        }

        public BigDecimal getTotalVal() {
            return totalVal;
        }

        public void setTotalVal(BigDecimal totalVal) {
            this.totalVal = totalVal;
        }
    }

    public static class LossItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private int productId;
        private String productName;
        private Integer variantId;
        private String variantName;
        private int totalQty;
        private BigDecimal totalVal;

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getVariantId() {
            return variantId;
        }

        public void setVariantId(Integer variantId) {
            this.variantId = variantId;
        }

        public String getVariantName() {
            return variantName;
        }

        public void setVariantName(String variantName) {
            this.variantName = variantName;
        }

        public int getTotalQty() {
            return totalQty;
        }

        public void setTotalQty(int totalQty) {
            this.totalQty = totalQty;
        }

        public BigDecimal getTotalVal() {
            return totalVal;
        }

        public void setTotalVal(BigDecimal totalVal) {
            this.totalVal = totalVal;
        }
    }
}
