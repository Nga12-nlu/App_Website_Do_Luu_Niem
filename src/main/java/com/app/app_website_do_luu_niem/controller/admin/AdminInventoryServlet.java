package com.app.app_website_do_luu_niem.controller.admin;

import com.app.app_website_do_luu_niem.config.DBConnection;
import com.app.app_website_do_luu_niem.dao.InventoryTransactionDao;
import com.app.app_website_do_luu_niem.dao.ProductDao;
import com.app.app_website_do_luu_niem.dao.ProductVariantDao;
import com.app.app_website_do_luu_niem.dao.impl.InventoryTransactionDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.ProductDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.ProductVariantDaoImpl;
import com.app.app_website_do_luu_niem.model.InventoryTransaction;
import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.util.SystemLogHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "adminInventoryServlet", urlPatterns = "/admin/inventory")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class AdminInventoryServlet extends HttpServlet {

    private final InventoryTransactionDao inventoryTransactionDao = new InventoryTransactionDaoImpl();
    private final ProductDao productDao = new ProductDaoImpl();
    private final ProductVariantDao productVariantDao = new ProductVariantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("export".equals(action)) {
            handleExportCsv(resp);
            return;
        }
        showList(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if ("adjust".equals(action)) {
            handleManualAdjustment(req, resp, currentUser);
        } else if ("import".equals(action)) {
            handleCsvImport(req, resp, currentUser);
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/inventory");
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        int pageSize = parseIntOrDefault(req.getParameter("pageSize"), 15);

        List<InventoryTransaction> transactions = inventoryTransactionDao.findAll(page, pageSize);
        int totalTxns = inventoryTransactionDao.countAll();
        int totalPages = (int) Math.ceil((double) totalTxns / pageSize);

        List<ProductInventoryOption> options = getProductInventoryOptions();

        req.setAttribute("transactions", transactions);
        req.setAttribute("currentPage", page);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalTransactions", totalTxns);
        req.setAttribute("productOptions", options);

        // Flash message handling
        HttpSession session = req.getSession();
        String successMsg = (String) session.getAttribute("successMsg");
        String errorMsg = (String) session.getAttribute("errorMsg");
        List<String> importErrors = (List<String>) session.getAttribute("importErrors");

        if (successMsg != null) {
            req.setAttribute("successMsg", successMsg);
            session.removeAttribute("successMsg");
        }
        if (errorMsg != null) {
            req.setAttribute("errorMsg", errorMsg);
            session.removeAttribute("errorMsg");
        }
        if (importErrors != null) {
            req.setAttribute("importErrors", importErrors);
            session.removeAttribute("importErrors");
        }

        req.getRequestDispatcher("/WEB-INF/views/admin/inventory.jsp").forward(req, resp);
    }

    private void handleManualAdjustment(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        HttpSession session = req.getSession();
        try {
            int productId = Integer.parseInt(req.getParameter("productId"));
            String variantIdStr = req.getParameter("variantId");
            Integer variantId = (variantIdStr == null || variantIdStr.isBlank() || "null".equals(variantIdStr)) 
                    ? null : Integer.parseInt(variantIdStr);
            String type = req.getParameter("type");
            int quantity = Integer.parseInt(req.getParameter("quantity"));
            String note = req.getParameter("note");

            if (quantity <= 0) {
                session.setAttribute("errorMsg", "Số lượng điều chỉnh phải lớn hơn 0.");
                resp.sendRedirect(req.getContextPath() + "/admin/inventory");
                return;
            }

            // Verify stock level before decreasing
            if (!"IMPORT".equalsIgnoreCase(type)) {
                int currentStock = 0;
                if (variantId != null) {
                    var variantOpt = productVariantDao.findById(variantId);
                    if (variantOpt.isPresent()) {
                        currentStock = variantOpt.get().getStock();
                    }
                } else {
                    var prodOpt = productDao.findById(productId);
                    if (prodOpt.isPresent()) {
                        currentStock = prodOpt.get().getStock();
                    }
                }

                if (currentStock < quantity) {
                    session.setAttribute("errorMsg", "Lỗi: Số lượng tồn kho hiện tại (" + currentStock + ") không đủ để xuất/hao hụt " + quantity + " sản phẩm.");
                    resp.sendRedirect(req.getContextPath() + "/admin/inventory");
                    return;
                }
            }

            InventoryTransaction txn = new InventoryTransaction();
            txn.setProductId(productId);
            txn.setVariantId(variantId);
            txn.setType(type.toUpperCase());
            txn.setQuantity(quantity);
            txn.setNote(note);
            txn.setUserId(currentUser.getId());
            txn.setCreatedAt(LocalDateTime.now());

            inventoryTransactionDao.save(txn);
            SystemLogHelper.log(req, "INVENTORY_ADJUST", "INVENTORY", 
                    "Điều chỉnh kho thủ công: Loại=" + type.toUpperCase() + ", SL=" + quantity + ", ProductID=" + productId + (variantId != null ? ", VariantID=" + variantId : "") + ", Note=" + note);

            session.setAttribute("successMsg", "Đã ghi nhận giao dịch kho thành công.");
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Lỗi dữ liệu điều chỉnh kho: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/admin/inventory");
    }

    private void handleExportCsv(HttpServletResponse resp) throws IOException {
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"danh_sach_ton_kho_" + System.currentTimeMillis() + ".csv\"");

        try (OutputStream os = resp.getOutputStream()) {
            // Write UTF-8 BOM so Excel opens it correctly with Vietnamese accents
            os.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));

            writer.println("Mã sản phẩm (ProductId),Mã biến thể (VariantId - để trống nếu không có),Tên sản phẩm,Tên biến thể,SKU,Số lượng tồn hiện tại (CurrentStock)");

            List<ProductInventoryOption> options = getProductInventoryOptions();
            for (ProductInventoryOption opt : options) {
                String varId = opt.getVariantId() != null ? String.valueOf(opt.getVariantId()) : "";
                String varName = opt.getVariantName() != null ? opt.getVariantName() : "";
                String sku = opt.getSku() != null ? opt.getSku() : "";
                
                writer.println(String.format("%d,%s,\"%s\",\"%s\",\"%s\",%d",
                        opt.getProductId(),
                        varId,
                        escapeCsvValue(opt.getProductName()),
                        escapeCsvValue(varName),
                        escapeCsvValue(sku),
                        opt.getStock()
                ));
            }
            writer.flush();
        }
    }

    private void handleCsvImport(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws ServletException, IOException {
        HttpSession session = req.getSession();
        Part filePart = req.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            session.setAttribute("errorMsg", "Vui lòng chọn file CSV hợp lệ để import.");
            resp.sendRedirect(req.getContextPath() + "/admin/inventory");
            return;
        }

        String importMode = req.getParameter("importMode"); // "ADD" or "OVERWRITE"
        if (importMode == null || importMode.isBlank()) {
            importMode = "ADD";
        }

        int successCount = 0;
        int errorCount = 0;
        List<String> importErrors = new ArrayList<>();

        try (InputStream is = filePart.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Strip UTF-8 BOM if present
                if (isFirstLine) {
                    isFirstLine = false;
                    if (line.startsWith("\uFEFF")) {
                        line = line.substring(1);
                    }
                    // Skip header line
                    if (line.toLowerCase().contains("productid") || line.toLowerCase().contains("mã sản phẩm")) {
                        continue;
                    }
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] tokens = parseCsvLine(line);
                if (tokens.length < 6) {
                    errorCount++;
                    importErrors.add("Dòng không hợp lệ (thiếu cột): " + line);
                    continue;
                }

                try {
                    int productId = Integer.parseInt(tokens[0].trim());
                    String variantIdStr = tokens[1].trim();
                    Integer variantId = (variantIdStr.isEmpty() || "null".equalsIgnoreCase(variantIdStr)) 
                            ? null : Integer.parseInt(variantIdStr);
                    int quantity = Integer.parseInt(tokens[5].trim());

                    // Find current stock
                    int currentStock = 0;
                    if (variantId != null) {
                        var variantOpt = productVariantDao.findById(variantId);
                        if (variantOpt.isEmpty()) {
                            errorCount++;
                            importErrors.add("Không tìm thấy biến thể ID=" + variantId);
                            continue;
                        }
                        currentStock = variantOpt.get().getStock();
                    } else {
                        var prodOpt = productDao.findById(productId);
                        if (prodOpt.isEmpty()) {
                            errorCount++;
                            importErrors.add("Không tìm thấy sản phẩm ID=" + productId);
                            continue;
                        }
                        currentStock = prodOpt.get().getStock();
                    }

                    int delta = 0;
                    String note = "";
                    if ("OVERWRITE".equalsIgnoreCase(importMode)) {
                        delta = quantity - currentStock;
                        note = "Import CSV (Ghi đè: " + currentStock + " -> " + quantity + ")";
                    } else {
                        delta = quantity;
                        note = "Import CSV (Cộng dồn: +" + quantity + ")";
                    }

                    if (delta == 0) {
                        successCount++;
                        continue;
                    }

                    if (currentStock + delta < 0) {
                        errorCount++;
                        importErrors.add("Sản phẩm ID=" + productId + (variantId != null ? " biến thể ID=" + variantId : "") + " có tồn kho mới âm (" + (currentStock + delta) + ") nên không cập nhật.");
                        continue;
                    }

                    InventoryTransaction txn = new InventoryTransaction();
                    txn.setProductId(productId);
                    txn.setVariantId(variantId);
                    txn.setType(delta > 0 ? "IMPORT" : "EXPORT");
                    txn.setQuantity(Math.abs(delta));
                    txn.setNote(note);
                    txn.setUserId(currentUser.getId());
                    txn.setCreatedAt(LocalDateTime.now());

                    inventoryTransactionDao.save(txn);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    importErrors.add("Lỗi xử lý dòng [" + line + "]: " + e.getMessage());
                }
            }

            SystemLogHelper.log(req, "INVENTORY_IMPORT", "INVENTORY", 
                    "Nhập kho hàng loạt qua file CSV: Thành công=" + successCount + ", Thất bại=" + errorCount + ", Chế độ=" + importMode);

            session.setAttribute("successMsg", "Đã hoàn thành xử lý file CSV. Thành công: " + successCount + ", Thất bại: " + errorCount + ".");
            if (!importErrors.isEmpty()) {
                session.setAttribute("importErrors", importErrors);
            }
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Lỗi đọc file CSV: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/inventory");
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }

    private String escapeCsvValue(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private List<ProductInventoryOption> getProductInventoryOptions() {
        List<ProductInventoryOption> list = new ArrayList<>();
        String sql = "SELECT p.id AS p_id, p.name AS p_name, v.id AS v_id, v.display_name AS v_name, v.sku AS v_sku, "
                   + "CASE WHEN v.id IS NOT NULL THEN v.stock ELSE p.stock END AS current_stock "
                   + "FROM products p "
                   + "LEFT JOIN product_variants v ON p.id = v.product_id AND v.active = 1 "
                   + "ORDER BY p.name ASC, v.sort_order ASC, v.id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProductInventoryOption opt = new ProductInventoryOption();
                opt.setProductId(rs.getInt("p_id"));
                opt.setProductName(rs.getString("p_name"));
                int vid = rs.getInt("v_id");
                if (!rs.wasNull()) {
                    opt.setVariantId(vid);
                }
                opt.setVariantName(rs.getString("v_name"));
                opt.setSku(rs.getString("v_sku"));
                opt.setStock(rs.getInt("current_stock"));
                list.add(opt);
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

    public static class ProductInventoryOption implements Serializable {
        private static final long serialVersionUID = 1L;
        private int productId;
        private String productName;
        private Integer variantId;
        private String variantName;
        private String sku;
        private int stock;

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

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }
    }
}
