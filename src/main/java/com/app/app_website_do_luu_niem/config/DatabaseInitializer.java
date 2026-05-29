package com.app.app_website_do_luu_niem.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Tự động khởi tạo bảng và dữ liệu mẫu khi ứng dụng khởi động nếu chưa tồn tại.
 */
@WebListener
public class DatabaseInitializer implements ServletContextListener {

    private static final String CREATE_PRODUCT_VARIANTS = """
            CREATE TABLE IF NOT EXISTS product_variants (
                id INT AUTO_INCREMENT PRIMARY KEY,
                product_id INT NOT NULL,
                display_name VARCHAR(512) NOT NULL,
                sku VARCHAR(120) NULL,
                price DECIMAL(15,2) NOT NULL,
                stock INT NOT NULL DEFAULT 0,
                image_url LONGTEXT NULL,
                sort_order INT NOT NULL DEFAULT 0,
                active TINYINT(1) NOT NULL DEFAULT 1,
                INDEX idx_pv_product (product_id),
                CONSTRAINT fk_pv_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
            )""";

    private static final String CREATE_PASSWORD_RESET = """
            CREATE TABLE IF NOT EXISTS password_reset_tokens (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                token_hash CHAR(64) NOT NULL,
                expires_at DATETIME NOT NULL,
                used_at DATETIME NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                request_ip VARCHAR(45) NULL,
                INDEX idx_prt_token (token_hash),
                INDEX idx_prt_user_created (user_id, created_at),
                CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )""";

    private static final String CREATE_STATIC_CONTENTS = """
            CREATE TABLE IF NOT EXISTS static_contents (
                id INT AUTO_INCREMENT PRIMARY KEY,
                content_key VARCHAR(120) NOT NULL UNIQUE,
                group_name VARCHAR(60) NOT NULL,
                label VARCHAR(255) NOT NULL,
                content_value TEXT NOT NULL,
                input_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
                active TINYINT(1) NOT NULL DEFAULT 1,
                sort_order INT NOT NULL DEFAULT 0,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )""";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            if (!tableExists("products")) {
                runSchemaInit();
            }
            migrateProductVariantsIfNeeded(sce);
            migratePasswordResetIfNeeded(sce);
            migrateGoogleOAuthIfNeeded(sce);
            migrateVnpayIfNeeded(sce);
            migrateCheckoutEnhancementsIfNeeded(sce);
            migrateStaticContentsIfNeeded(sce);
        } catch (Exception e) {
            sce.getServletContext().log("Không thể khởi tạo database: " + e.getMessage(), e);
        }
    }

    private void migrateProductVariantsIfNeeded(ServletContextEvent sce) throws Exception {
        if (!tableExists("products")) {
            return;
        }
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            if (!tableExists("product_variants")) {
                st.execute(CREATE_PRODUCT_VARIANTS);
                sce.getServletContext().log("Đã tạo bảng product_variants.");
            }
            if (!columnExists(conn, "order_items", "variant_id")) {
                st.execute("ALTER TABLE order_items ADD COLUMN variant_id INT NULL");
            }
            if (!columnExists(conn, "order_items", "variant_label")) {
                st.execute("ALTER TABLE order_items ADD COLUMN variant_label VARCHAR(512) NULL");
            }
            try {
                st.execute("""
                        ALTER TABLE order_items
                        ADD CONSTRAINT fk_order_items_variant
                        FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL
                        """);
            } catch (Exception ignored) {
                // Ràng buộc đã tồn tại hoặc engine không hỗ trợ — bỏ qua
            }

            st.execute("""
                    INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
                    SELECT p.id, 'Mặc định', NULL, p.price, p.stock, p.image_url, 0, 1
                    FROM products p
                    WHERE NOT EXISTS (SELECT 1 FROM product_variants v WHERE v.product_id = p.id)
                    """);

            st.execute("""
                    UPDATE products p
                    SET p.price = COALESCE((SELECT MIN(v.price) FROM product_variants v WHERE v.product_id = p.id AND v.active = 1), p.price),
                        p.stock = COALESCE((SELECT SUM(v.stock) FROM product_variants v WHERE v.product_id = p.id AND v.active = 1), p.stock)
                    WHERE EXISTS (SELECT 1 FROM product_variants v2 WHERE v2.product_id = p.id)
                    """);
        }
    }

    private void migratePasswordResetIfNeeded(ServletContextEvent sce) throws Exception {
        if (!tableExists("users")) {
            return;
        }
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            if (!tableExists("password_reset_tokens")) {
                st.execute(CREATE_PASSWORD_RESET);
                sce.getServletContext().log("Đã tạo bảng password_reset_tokens.");
            }
        }
    }

    private void migrateGoogleOAuthIfNeeded(ServletContextEvent sce) throws Exception {
        if (!tableExists("users")) {
            return;
        }
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            if (!columnExists(conn, "users", "google_id")) {
                st.execute("ALTER TABLE users ADD COLUMN google_id VARCHAR(255) NULL");
                sce.getServletContext().log("Đã thêm cột users.google_id.");
            }
            try {
                st.execute("CREATE UNIQUE INDEX idx_users_google_id ON users (google_id)");
            } catch (Exception ignored) {
                // Index đã tồn tại
            }
            try {
                st.execute("ALTER TABLE users MODIFY COLUMN password_hash VARCHAR(255) NULL");
            } catch (Exception ignored) {
                // Cột đã nullable hoặc engine không hỗ trợ
            }
        }
    }

    private void migrateVnpayIfNeeded(ServletContextEvent sce) throws Exception {
        if (!tableExists("orders")) {
            return;
        }
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            if (!columnExists(conn, "orders", "payment_method")) {
                st.execute("ALTER TABLE orders ADD COLUMN payment_method VARCHAR(20) NOT NULL DEFAULT 'COD'");
                sce.getServletContext().log("Đã thêm cột orders.payment_method.");
            }
            if (!columnExists(conn, "orders", "vnpay_txn_ref")) {
                st.execute("ALTER TABLE orders ADD COLUMN vnpay_txn_ref VARCHAR(64) NULL");
                sce.getServletContext().log("Đã thêm cột orders.vnpay_txn_ref.");
            }
            if (!columnExists(conn, "orders", "vnpay_transaction_no")) {
                st.execute("ALTER TABLE orders ADD COLUMN vnpay_transaction_no VARCHAR(64) NULL");
                sce.getServletContext().log("Đã thêm cột orders.vnpay_transaction_no.");
            }
            if (!columnExists(conn, "orders", "paid_at")) {
                st.execute("ALTER TABLE orders ADD COLUMN paid_at DATETIME NULL");
                sce.getServletContext().log("Đã thêm cột orders.paid_at.");
            }
            try {
                st.execute("CREATE UNIQUE INDEX idx_orders_vnpay_txn ON orders (vnpay_txn_ref)");
            } catch (Exception ignored) {
                // Index đã tồn tại
            }
        }
    }

    private void migrateCheckoutEnhancementsIfNeeded(ServletContextEvent sce) throws Exception {
        if (!tableExists("orders")) {
            return;
        }
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            if (!tableExists("coupons")) {
                st.execute("""
                        CREATE TABLE IF NOT EXISTS coupons (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            code VARCHAR(50) NOT NULL UNIQUE,
                            description VARCHAR(255) NULL,
                            discount_type VARCHAR(20) NOT NULL,
                            discount_value DECIMAL(15,2) NOT NULL,
                            min_order_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
                            max_discount DECIMAL(15,2) NULL,
                            usage_limit INT NULL,
                            used_count INT NOT NULL DEFAULT 0,
                            per_user_limit INT NOT NULL DEFAULT 1,
                            starts_at DATETIME NULL,
                            expires_at DATETIME NULL,
                            active TINYINT(1) NOT NULL DEFAULT 1,
                            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                        )
                        """);
                st.execute("""
                        CREATE TABLE IF NOT EXISTS coupon_usages (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            coupon_id INT NOT NULL,
                            user_id INT NOT NULL,
                            order_id INT NULL,
                            used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            INDEX idx_cu_coupon_user (coupon_id, user_id),
                            CONSTRAINT fk_cu_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id),
                            CONSTRAINT fk_cu_user FOREIGN KEY (user_id) REFERENCES users(id)
                        )
                        """);
                st.execute("""
                        INSERT INTO coupons (code, description, discount_type, discount_value, min_order_amount,
                        max_discount, usage_limit, per_user_limit, active) VALUES
                        ('WELCOME10', 'Giảm 10% cho đơn đầu (tối đa 50k)', 'PERCENT', 10, 200000, 50000, 1000, 1, 1),
                        ('GIAM50K', 'Giảm 50.000đ cho đơn từ 300k', 'FIXED', 50000, 300000, NULL, 500, 2, 1),
                        ('FREESHIP', 'Miễn phí ship (giảm 30k phí ship)', 'FIXED', 30000, 150000, NULL, 200, 1, 1)
                        ON DUPLICATE KEY UPDATE code = code
                        """);
                sce.getServletContext().log("Đã tạo bảng coupons và mã mẫu.");
            } else {
                seedCouponsIfEmpty(st);
            }
            addOrderColumnIfMissing(conn, st, sce, "subtotal", "DECIMAL(15,2) NULL");
            addOrderColumnIfMissing(conn, st, sce, "discount_amount", "DECIMAL(15,2) NOT NULL DEFAULT 0");
            addOrderColumnIfMissing(conn, st, sce, "shipping_fee", "DECIMAL(15,2) NOT NULL DEFAULT 0");
            addOrderColumnIfMissing(conn, st, sce, "coupon_id", "INT NULL");
            addOrderColumnIfMissing(conn, st, sce, "coupon_code", "VARCHAR(50) NULL");
            addOrderColumnIfMissing(conn, st, sce, "receiver_name", "VARCHAR(255) NULL");
            addOrderColumnIfMissing(conn, st, sce, "customer_note", "VARCHAR(500) NULL");
            addOrderColumnIfMissing(conn, st, sce, "province_code", "VARCHAR(20) NULL");
            addOrderColumnIfMissing(conn, st, sce, "province_name", "VARCHAR(120) NULL");
            addOrderColumnIfMissing(conn, st, sce, "district_code", "VARCHAR(20) NULL");
            addOrderColumnIfMissing(conn, st, sce, "district_name", "VARCHAR(120) NULL");
            addOrderColumnIfMissing(conn, st, sce, "ward_code", "VARCHAR(20) NULL");
            addOrderColumnIfMissing(conn, st, sce, "ward_name", "VARCHAR(120) NULL");
            addOrderColumnIfMissing(conn, st, sce, "address_detail", "VARCHAR(500) NULL");
        }
    }

    private void seedCouponsIfEmpty(Statement st) throws Exception {
        try (var rs = st.executeQuery("SELECT COUNT(*) FROM coupons")) {
            if (rs.next() && rs.getInt(1) == 0) {
                st.execute("""
                        INSERT INTO coupons (code, description, discount_type, discount_value, min_order_amount,
                        max_discount, usage_limit, per_user_limit, active) VALUES
                        ('WELCOME10', 'Giảm 10% cho đơn đầu (tối đa 50k)', 'PERCENT', 10, 200000, 50000, 1000, 1, 1),
                        ('GIAM50K', 'Giảm 50.000đ cho đơn từ 300k', 'FIXED', 50000, 300000, NULL, 500, 2, 1),
                        ('FREESHIP', 'Giảm 30k phí ship', 'FIXED', 30000, 150000, NULL, 200, 1, 1)
                        """);
            }
        }
    }

    private void addOrderColumnIfMissing(Connection conn, Statement st, ServletContextEvent sce,
                                         String column, String ddl) throws Exception {
        if (!columnExists(conn, "orders", column)) {
            st.execute("ALTER TABLE orders ADD COLUMN " + column + " " + ddl);
            sce.getServletContext().log("Đã thêm cột orders." + column);
        }
    }

    private void migrateStaticContentsIfNeeded(ServletContextEvent sce) throws Exception {
        if (!tableExists("products")) {
            return;
        }
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            if (!tableExists("static_contents")) {
                st.execute(CREATE_STATIC_CONTENTS);
                sce.getServletContext().log("Đã tạo bảng static_contents.");
            }
            st.execute("""
                    INSERT INTO static_contents (content_key, group_name, label, content_value, input_type, active, sort_order) VALUES
                    ('home.hero.title', 'Trang chủ', 'Tiêu đề hero', 'Chào mừng đến với Souvenir Shop', 'TEXT', 1, 10),
                    ('home.hero.subtitle', 'Trang chủ', 'Mô tả hero', 'Khám phá những món quà lưu niệm độc đáo, mang đậm dấu ấn văn hóa Việt Nam', 'TEXTAREA', 1, 20),
                    ('home.hero.badge1', 'Trang chủ', 'Badge hero 1', 'Giao hàng toàn quốc', 'TEXT', 1, 30),
                    ('home.hero.badge2', 'Trang chủ', 'Badge hero 2', 'Thanh toán an toàn', 'TEXT', 1, 40),
                    ('home.hero.badge3', 'Trang chủ', 'Badge hero 3', 'Nhiều mẫu và biến thể', 'TEXT', 1, 50),
                    ('home.latest.title', 'Trang chủ', 'Tiêu đề block sản phẩm mới', 'Sản phẩm mới nhất', 'TEXT', 1, 60),
                    ('home.latest.cta', 'Trang chủ', 'Nút xem tất cả sản phẩm', 'Xem tất cả sản phẩm', 'TEXT', 1, 70),
                    ('footer.brand.title', 'Footer', 'Tên thương hiệu footer', 'Souvenir Shop', 'TEXT', 1, 80),
                    ('footer.brand.description', 'Footer', 'Mô tả thương hiệu footer', 'Cửa hàng đồ lưu niệm Việt Nam với sản phẩm tinh tế, phù hợp làm quà tặng và lưu giữ kỷ niệm.', 'TEXTAREA', 1, 90),
                    ('footer.contact.address', 'Footer', 'Địa chỉ liên hệ', 'Hà Nội, Việt Nam', 'TEXT', 1, 100),
                    ('footer.contact.phone', 'Footer', 'Số điện thoại liên hệ', 'Hotline: 09xx xxx xxx', 'TEXT', 1, 110),
                    ('footer.contact.email', 'Footer', 'Email liên hệ', 'support@souvenirshop.vn', 'TEXT', 1, 120),
                    ('footer.copyright', 'Footer', 'Dòng bản quyền', 'Souvenir Shop - Website bán đồ lưu niệm Việt Nam', 'TEXT', 1, 130)
                    ON DUPLICATE KEY UPDATE content_key = content_key
                    """);
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws Exception {
        DatabaseMetaData md = conn.getMetaData();
        try (ResultSet rs = md.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private boolean tableExists(String tableName) {
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    private void runSchemaInit() throws Exception {
        String sql = new BufferedReader(
                new InputStreamReader(
                        getClass().getClassLoader().getResourceAsStream("schema-init.sql"),
                        StandardCharsets.UTF_8
                )
        ).lines().collect(Collectors.joining("\n"));

        String[] statements = sql.split(";");
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String s : statements) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    try {
                        stmt.execute(trimmed);
                    } catch (Exception e) {
                        if (!e.getMessage().contains("Duplicate")
                                && !e.getMessage().contains("already exists")) {
                            throw e;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
