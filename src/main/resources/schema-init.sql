CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NULL,
    google_id VARCHAR(255) NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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
);

CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(15,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    image_url LONGTEXT,
    category_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

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
);

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
);

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
);

CREATE TABLE IF NOT EXISTS coupon_usages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    coupon_id INT NOT NULL,
    user_id INT NOT NULL,
    order_id INT NULL,
    used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cu_coupon_user (coupon_id, user_id),
    CONSTRAINT fk_cu_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    CONSTRAINT fk_cu_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    subtotal DECIMAL(15,2) NULL,
    discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    shipping_fee DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20) NOT NULL DEFAULT 'COD',
    vnpay_txn_ref VARCHAR(64) NULL,
    vnpay_transaction_no VARCHAR(64) NULL,
    paid_at DATETIME NULL,
    coupon_id INT NULL,
    coupon_code VARCHAR(50) NULL,
    receiver_name VARCHAR(255) NULL,
    customer_note VARCHAR(500) NULL,
    province_code VARCHAR(20) NULL,
    province_name VARCHAR(120) NULL,
    district_code VARCHAR(20) NULL,
    district_name VARCHAR(120) NULL,
    ward_code VARCHAR(20) NULL,
    ward_name VARCHAR(120) NULL,
    address_detail VARCHAR(500) NULL,
    shipping_address VARCHAR(500) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT IGNORE INTO coupons (code, description, discount_type, discount_value, min_order_amount,
    max_discount, usage_limit, per_user_limit, active) VALUES
('WELCOME10', 'Giảm 10% cho đơn đầu (tối đa 50k)', 'PERCENT', 10, 200000, 50000, 1000, 1, 1),
('GIAM50K', 'Giảm 50.000đ cho đơn từ 300k', 'FIXED', 50000, 300000, NULL, 500, 2, 1),
('FREESHIP', 'Giảm 30k phí ship', 'FIXED', 30000, 150000, NULL, 200, 1, 1);

CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    variant_id INT NULL,
    variant_label VARCHAR(512) NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_order_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL
);

-- Tài khoản: admin@example.com / 123456 (ADMIN). Khách demo id 2–8: cùng mật khẩu admin (chỉ môi trường dev).
INSERT IGNORE INTO users (id, email, password_hash, full_name, role, active) VALUES
(1, 'admin@example.com', '$2a$10$3i0lHh72S.91Cki.9CAwgOiHivOTHVbhs4cl6tn0eC/19cUNnA4nm', 'Quản trị viên', 'ADMIN', 1),
(2, 'minh.an@example.com', '$2a$10$3i0lHh72S.91Cki.9CAwgOiHivOTHVbhs4cl6tn0eC/19cUNnA4nm', 'Nguyễn Minh An', 'CUSTOMER', 1),
(3, 'thanh.ha@example.com', '$2a$10$3i0lHh72S.91Cki.9CAwgOiHivOTHVbhs4cl6tn0eC/19cUNnA4nm', 'Trần Thanh Hà', 'CUSTOMER', 1),
(4, 'quang.vu@example.com', '$2a$10$3i0lHh72S.91Cki.9CAwgOiHivOTHVbhs4cl6tn0eC/19cUNnA4nm', 'Lê Quang Vũ', 'CUSTOMER', 1),
(5, 'mai.huong@example.com', '$2a$10$3i0lHh72S.91Cki.9CAwgOiHivOTHVbhs4cl6tn0eC/19cUNnA4nm', 'Phạm Mai Hương', 'CUSTOMER', 1),
(6, 'duc.tien@example.com', '$2a$10$3i0lHh72S.91Cki.9CAwgOiHivOTHVbhs4cl6tn0eC/19cUNnA4nm', 'Hoàng Đức Tiến', 'CUSTOMER', 1),
(7, 'linh.chi@example.com', '$2a$10$3i0lHh72S.91Cki.9CAwgOiHivOTHVbhs4cl6tn0eC/19cUNnA4nm', 'Võ Linh Chi', 'CUSTOMER', 0),
(8, 'khoa.nam@example.com', '$2a$10$3i0lHh72S.91Cki.9CAwgOiHivOTHVbhs4cl6tn0eC/19cUNnA4nm', 'Đặng Khoa Nam', 'CUSTOMER', 1);

INSERT IGNORE INTO categories (name, description) VALUES
('Quà lưu niệm du lịch', 'Các món quà lưu niệm mang đậm dấu ấn địa phương'),
('Đồ trang trí', 'Các sản phẩm trang trí nhà cửa, bàn làm việc'),
('Đồ thủ công mỹ nghệ', 'Sản phẩm làm thủ công độc đáo'),
('Văn phòng phẩm lưu niệm', 'Sổ, bút, bookmark và phụ kiện bàn làm việc'),
('Quà tặng doanh nghiệp', 'Hộp quà, set quà tặng đối tác'),
('Ẩm thực đặc sản', 'Trà, cà phê, đặc sản khô đóng gói quà tặng');

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
ON DUPLICATE KEY UPDATE content_key = content_key;

INSERT INTO products (name, description, price, stock, image_url, category_id) VALUES
('Tượng Rùa Vàng', 'Tượng rùa vàng phong thủy mang lại may mắn', 250000, 50, 'https://via.placeholder.com/300x300?text=Tuong+Rua', 1),
('Bình Hoa Gốm', 'Bình hoa gốm sứ Bát Tràng cao cấp', 380000, 30, 'https://via.placeholder.com/300x300?text=Binh+Hoa', 2),
('Tranh Thêu Tay', 'Tranh thêu tay truyền thống Việt Nam', 450000, 20, 'https://via.placeholder.com/300x300?text=Tranh+Theu', 3),
('Móc Khóa Địa Danh', 'Bộ móc khóa các địa danh nổi tiếng', 85000, 100, 'https://via.placeholder.com/300x300?text=Moc+Khoa', 1),
('Đèn Lồng Tre', 'Đèn lồng tre trang trí phong cách Á Đông', 120000, 40, 'https://via.placeholder.com/300x300?text=Den+Long', 2),
('Nón Lá Mini', 'Nón lá trang trí mini, họa tiết sen', 95000, 80, 'https://via.placeholder.com/300x300?text=Non+La', 1),
('Gương Cầm Tay Gốm', 'Gương nhỏ viền men rạn Bát Tràng', 145000, 45, 'https://via.placeholder.com/300x300?text=Guong+Gom', 2),
('Túi Vải In Hoa Văn', 'Túi canvas in họa tiết dân gian', 165000, 60, 'https://via.placeholder.com/300x300?text=Tui+Vai', 1),
('Hộp Trầm Hương Gỗ', 'Hộp gỗ hương trầm kèm nhang mẫu', 275000, 35, 'https://via.placeholder.com/300x300?text=Hop+Tram', 2),
('Đĩa Gốm Bát Tràng', 'Đĩa trưng bày men lam cổ điển', 198000, 40, 'https://via.placeholder.com/300x300?text=Dia+Gom', 2),
('Mô Hình Thuyền Buồm', 'Mô hình gỗ thuyền buồm trang trí', 320000, 25, 'https://via.placeholder.com/300x300?text=Thuyen', 3),
('Bộ Postcard Việt Nam', '12 tấm postcard cảnh đẹp', 120000, 120, 'https://via.placeholder.com/300x300?text=Postcard', 1),
('Hộp Quà Tết Mini', 'Set quà nhỏ: trà + kẹo + thiệp', 210000, 50, 'https://via.placeholder.com/300x300?text=Hop+Tet', 5),
('Mành Tre Cuốn', 'Mành tre che nắng cửa sổ nhỏ', 185000, 30, 'https://via.placeholder.com/300x300?text=Manh+Tre', 2),
('Đĩa Đồng Ăn Mòn', 'Đĩa đồng nghệ thuật ăn mòn', 420000, 15, 'https://via.placeholder.com/300x300?text=Dia+Dong', 3),
('Bút Ký Tên Cao Cấp', 'Bút kim loại khắc tên tùy chọn', 89000, 200, 'https://via.placeholder.com/300x300?text=But+Ky', 4),
('Sổ Tay Da PU', 'Sổ A5 da PU, ruột refill', 135000, 90, 'https://via.placeholder.com/300x300?text=So+Tay', 4),
('Sticker Địa Danh VN', 'Bộ 40 sticker trong suốt', 55000, 250, 'https://via.placeholder.com/300x300?text=Sticker', 4),
('Hộp Trà Sen', 'Trà sen cốm Hà Nội 100g', 175000, 70, 'https://via.placeholder.com/300x300?text=Tra+Sen', 6),
('Khay Mứt Gỗ', 'Khay 6 ngăn gỗ tự nhiên', 155000, 55, 'https://via.placeholder.com/300x300?text=Khay+Mut', 5),
('Đèn Bàn LED Nghệ Thuật', 'Đèn chống cận 3 cấp sáng', 245000, 40, 'https://via.placeholder.com/300x300?text=Den+Ban', 2),
('Con Lắc Newton Mini', 'Con lắc trang trí bàn làm việc', 99000, 85, 'https://via.placeholder.com/300x300?text=Con+Lac', 4),
('Khăn Lụa In Hoa', 'Khăn lụa vuông 65cm', 225000, 42, 'https://via.placeholder.com/300x300?text=Khan+Lua', 3),
('Ấm Trà Tử Sa', 'Ấm trà đất tử sa dung tích 220ml', 520000, 18, 'https://via.placeholder.com/300x300?text=Am+Tra', 2),
('Cà Phê Chồn Hộp Quà', 'Hộp quà cà phê chồn 200g', 310000, 36, 'https://via.placeholder.com/300x300?text=Ca+Phe', 6),
('Mặt Nạ Giấy Bồi', 'Mặt nạ trang trí tường cỡ A3', 78000, 65, 'https://via.placeholder.com/300x300?text=Mat+Na', 3);

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Vàng — cỡ nhỏ 12cm', 'TRV-12', 220000, 28, p.image_url, 0, 1 FROM products p WHERE p.name = 'Tượng Rùa Vàng';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Vàng — cỡ lớn 18cm', 'TRV-18', 290000, 22, p.image_url, 1, 1 FROM products p WHERE p.name = 'Tượng Rùa Vàng';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Men kem — cao 35cm', 'BHG-K35', 360000, 18, p.image_url, 0, 1 FROM products p WHERE p.name = 'Bình Hoa Gốm';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Men xanh — cao 28cm', 'BHG-X28', 320000, 12, p.image_url, 1, 1 FROM products p WHERE p.name = 'Bình Hoa Gốm';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Khổ 40×60 cm', 'TT-4060', 420000, 12, p.image_url, 0, 1 FROM products p WHERE p.name = 'Tranh Thêu Tay';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Khổ 50×70 cm', 'TT-5070', 480000, 8, p.image_url, 1, 1 FROM products p WHERE p.name = 'Tranh Thêu Tay';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Bộ Hà Nội — 5 chiếc', 'MK-HN', 75000, 55, p.image_url, 0, 1 FROM products p WHERE p.name = 'Móc Khóa Địa Danh';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Bộ TP.HCM — 5 chiếc', 'MK-SG', 80000, 45, p.image_url, 1, 1 FROM products p WHERE p.name = 'Móc Khóa Địa Danh';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Cỡ S — đỏ son', 'DLT-S-R', 110000, 22, p.image_url, 0, 1 FROM products p WHERE p.name = 'Đèn Lồng Tre';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Cỡ L — vàng ấm', 'DLT-L-Y', 135000, 18, p.image_url, 1, 1 FROM products p WHERE p.name = 'Đèn Lồng Tre';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Sen hồng — 28cm', 'NLM-28', 88000, 45, p.image_url, 0, 1 FROM products p WHERE p.name = 'Nón Lá Mini';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Trúc xanh — 32cm', 'NLM-32', 102000, 35, p.image_url, 1, 1 FROM products p WHERE p.name = 'Nón Lá Mini';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Tròn 8cm', 'GCT-08', 135000, 25, p.image_url, 0, 1 FROM products p WHERE p.name = 'Gương Cầm Tay Gốm';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Tròn 10cm', 'GCT-10', 155000, 20, p.image_url, 1, 1 FROM products p WHERE p.name = 'Gương Cầm Tay Gốm';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Hoa sen đỏ', 'TVV-SEN', 155000, 35, p.image_url, 0, 1 FROM products p WHERE p.name = 'Túi Vải In Hoa Văn';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Họa tiết cá chép', 'TVV-CA', 175000, 25, p.image_url, 1, 1 FROM products p WHERE p.name = 'Túi Vải In Hoa Văn';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Gỗ lim — 20 nén', 'HTH-L20', 255000, 20, p.image_url, 0, 1 FROM products p WHERE p.name = 'Hộp Trầm Hương Gỗ';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Gỗ gụ — 40 nén', 'HTH-G40', 295000, 15, p.image_url, 1, 1 FROM products p WHERE p.name = 'Hộp Trầm Hương Gỗ';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Đường kính 25cm', 'DGT-25', 185000, 22, p.image_url, 0, 1 FROM products p WHERE p.name = 'Đĩa Gốm Bát Tràng';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Đường kính 30cm', 'DGT-30', 210000, 18, p.image_url, 1, 1 FROM products p WHERE p.name = 'Đĩa Gốm Bát Tràng';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Cỡ nhỏ 25cm', 'THB-S', 290000, 15, p.image_url, 0, 1 FROM products p WHERE p.name = 'Mô Hình Thuyền Buồm';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Cỡ lớn 40cm', 'THB-L', 350000, 10, p.image_url, 1, 1 FROM products p WHERE p.name = 'Mô Hình Thuyền Buồm';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Bản tiếng Việt', 'PC-VN', 115000, 70, p.image_url, 0, 1 FROM products p WHERE p.name = 'Bộ Postcard Việt Nam';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Song ngữ An–Việt', 'PC-EN', 125000, 50, p.image_url, 1, 1 FROM products p WHERE p.name = 'Bộ Postcard Việt Nam';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Set đỏ', 'TET-R', 198000, 28, p.image_url, 0, 1 FROM products p WHERE p.name = 'Hộp Quà Tết Mini';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Set vàng', 'TET-Y', 222000, 22, p.image_url, 1, 1 FROM products p WHERE p.name = 'Hộp Quà Tết Mini';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Rộng 80cm', 'MT-80', 175000, 18, p.image_url, 0, 1 FROM products p WHERE p.name = 'Mành Tre Cuốn';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Rộng 100cm', 'MT-100', 195000, 12, p.image_url, 1, 1 FROM products p WHERE p.name = 'Mành Tre Cuốn';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Hoa văn rồng', 'DD-R', 400000, 9, p.image_url, 0, 1 FROM products p WHERE p.name = 'Đĩa Đồng Ăn Mòn';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Hoa văn phượng', 'DD-P', 440000, 6, p.image_url, 1, 1 FROM products p WHERE p.name = 'Đĩa Đồng Ăn Mòn';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Mạ vàng', 'BK-V', 95000, 120, p.image_url, 0, 1 FROM products p WHERE p.name = 'Bút Ký Tên Cao Cấp';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Đen mờ', 'BK-B', 83000, 80, p.image_url, 1, 1 FROM products p WHERE p.name = 'Bút Ký Tên Cao Cấp';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Nâu', 'ST-NA', 128000, 50, p.image_url, 0, 1 FROM products p WHERE p.name = 'Sổ Tay Da PU';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Đen', 'ST-D', 142000, 40, p.image_url, 1, 1 FROM products p WHERE p.name = 'Sổ Tay Da PU';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Bản trong', 'STK-IN', 52000, 150, p.image_url, 0, 1 FROM products p WHERE p.name = 'Sticker Địa Danh VN';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Bản nhám', 'STK-NH', 58000, 100, p.image_url, 1, 1 FROM products p WHERE p.name = 'Sticker Địa Danh VN';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Hộp giấy', 'TS-G', 165000, 40, p.image_url, 0, 1 FROM products p WHERE p.name = 'Hộp Trà Sen';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Hộp thiếc', 'TS-T', 185000, 30, p.image_url, 1, 1 FROM products p WHERE p.name = 'Hộp Trà Sen';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Gỗ sồi', 'KM-S', 148000, 30, p.image_url, 0, 1 FROM products p WHERE p.name = 'Khay Mứt Gỗ';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Gỗ óc chó', 'KM-O', 162000, 25, p.image_url, 1, 1 FROM products p WHERE p.name = 'Khay Mứt Gỗ';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Ánh sáng ấm', 'DB-W', 235000, 22, p.image_url, 0, 1 FROM products p WHERE p.name = 'Đèn Bàn LED Nghệ Thuật';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Ánh sáng trắng', 'DB-C', 255000, 18, p.image_url, 1, 1 FROM products p WHERE p.name = 'Đèn Bàn LED Nghệ Thuật';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, '5 quả', 'CL-5', 92000, 50, p.image_url, 0, 1 FROM products p WHERE p.name = 'Con Lắc Newton Mini';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, '7 quả', 'CL-7', 106000, 35, p.image_url, 1, 1 FROM products p WHERE p.name = 'Con Lắc Newton Mini';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Đỏ tía', 'KL-D', 210000, 22, p.image_url, 0, 1 FROM products p WHERE p.name = 'Khăn Lụa In Hoa';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Xanh ngọc', 'KL-X', 240000, 20, p.image_url, 1, 1 FROM products p WHERE p.name = 'Khăn Lụa In Hoa';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Dung tích 180ml', 'TS-180', 490000, 10, p.image_url, 0, 1 FROM products p WHERE p.name = 'Ấm Trà Tử Sa';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Dung tích 260ml', 'TS-260', 550000, 8, p.image_url, 1, 1 FROM products p WHERE p.name = 'Ấm Trà Tử Sa';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Xay nhuyễn', 'CP-X', 295000, 20, p.image_url, 0, 1 FROM products p WHERE p.name = 'Cà Phê Chồn Hộp Quà';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Nguyên hạt', 'CP-N', 325000, 16, p.image_url, 1, 1 FROM products p WHERE p.name = 'Cà Phê Chồn Hộp Quà';

INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Màu đỏ', 'MG-D', 72000, 35, p.image_url, 0, 1 FROM products p WHERE p.name = 'Mặt Nạ Giấy Bồi';
INSERT INTO product_variants (product_id, display_name, sku, price, stock, image_url, sort_order, active)
SELECT p.id, 'Màu vàng', 'MG-V', 84000, 30, p.image_url, 1, 1 FROM products p WHERE p.name = 'Mặt Nạ Giấy Bồi';

UPDATE products p
SET p.price = COALESCE((SELECT MIN(v.price) FROM product_variants v WHERE v.product_id = p.id AND v.active = 1), p.price),
    p.stock = COALESCE((SELECT SUM(v.stock) FROM product_variants v WHERE v.product_id = p.id AND v.active = 1), p.stock)
WHERE EXISTS (SELECT 1 FROM product_variants v2 WHERE v2.product_id = p.id);

INSERT INTO orders (user_id, total_amount, status, shipping_address, phone, created_at) VALUES
(2, 555000, 'CONFIRMED', '12 Nguyễn Huệ, Q.1, TP.HCM', '0901112233', DATE_SUB(NOW(), INTERVAL 5 DAY));
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, unit_price, variant_id, variant_label)
SELECT @oid, p.id, 2, v.price, v.id, v.display_name FROM products p JOIN product_variants v ON v.product_id = p.id AND v.sku = 'TRV-12' WHERE p.name = 'Tượng Rùa Vàng' LIMIT 1;
INSERT INTO order_items (order_id, product_id, quantity, unit_price, variant_id, variant_label)
SELECT @oid, p.id, 1, v.price, v.id, v.display_name FROM products p JOIN product_variants v ON v.product_id = p.id AND v.sku = 'PC-VN' WHERE p.name = 'Bộ Postcard Việt Nam' LIMIT 1;

INSERT INTO orders (user_id, total_amount, status, shipping_address, phone, created_at) VALUES
(2, 360000, 'SHIPPED', '45 Láng Hạ, Ba Đình, Hà Nội', '0912223344', DATE_SUB(NOW(), INTERVAL 12 DAY));
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, unit_price, variant_id, variant_label)
SELECT @oid, p.id, 1, v.price, v.id, v.display_name FROM products p JOIN product_variants v ON v.product_id = p.id AND v.sku = 'BHG-K35' WHERE p.name = 'Bình Hoa Gốm' LIMIT 1;

INSERT INTO orders (user_id, total_amount, status, shipping_address, phone, created_at) VALUES
(3, 75000, 'PENDING', '88 Trần Phú, Hải Phòng', '0933334455', DATE_SUB(NOW(), INTERVAL 1 DAY));
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, unit_price, variant_id, variant_label)
SELECT @oid, p.id, 1, v.price, v.id, v.display_name FROM products p JOIN product_variants v ON v.product_id = p.id AND v.sku = 'MK-HN' WHERE p.name = 'Móc Khóa Địa Danh' LIMIT 1;

INSERT INTO orders (user_id, total_amount, status, shipping_address, phone, created_at) VALUES
(4, 550000, 'CONFIRMED', '9 Điện Biên Phủ, Đà Nẵng', '0944445566', DATE_SUB(NOW(), INTERVAL 3 DAY));
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, unit_price, variant_id, variant_label)
SELECT @oid, p.id, 1, v.price, v.id, v.display_name FROM products p JOIN product_variants v ON v.product_id = p.id AND v.sku = 'TS-260' WHERE p.name = 'Ấm Trà Tử Sa' LIMIT 1;

INSERT INTO orders (user_id, total_amount, status, shipping_address, phone, created_at) VALUES
(5, 58000, 'CANCELLED', '30 Lê Lợi, Huế', '0955556677', DATE_SUB(NOW(), INTERVAL 20 DAY));
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, unit_price, variant_id, variant_label)
SELECT @oid, p.id, 1, v.price, v.id, v.display_name FROM products p JOIN product_variants v ON v.product_id = p.id AND v.sku = 'STK-NH' WHERE p.name = 'Sticker Địa Danh VN' LIMIT 1;

INSERT INTO orders (user_id, total_amount, status, shipping_address, phone, created_at) VALUES
(6, 565000, 'SHIPPED', '7 Phan Chu Trinh, Nha Trang', '0966667788', DATE_SUB(NOW(), INTERVAL 8 DAY));
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, unit_price, variant_id, variant_label)
SELECT @oid, p.id, 1, v.price, v.id, v.display_name FROM products p JOIN product_variants v ON v.product_id = p.id AND v.sku = 'KL-X' WHERE p.name = 'Khăn Lụa In Hoa' LIMIT 1;
INSERT INTO order_items (order_id, product_id, quantity, unit_price, variant_id, variant_label)
SELECT @oid, p.id, 1, v.price, v.id, v.display_name FROM products p JOIN product_variants v ON v.product_id = p.id AND v.sku = 'CP-N' WHERE p.name = 'Cà Phê Chồn Hộp Quà' LIMIT 1;

INSERT INTO orders (user_id, total_amount, status, shipping_address, phone, created_at) VALUES
(8, 255000, 'PENDING', '55 Võ Văn Tần, Cần Thơ', '0977778899', NOW());
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, unit_price, variant_id, variant_label)
SELECT @oid, p.id, 1, v.price, v.id, v.display_name FROM products p JOIN product_variants v ON v.product_id = p.id AND v.sku = 'HTH-L20' WHERE p.name = 'Hộp Trầm Hương Gỗ' LIMIT 1;
