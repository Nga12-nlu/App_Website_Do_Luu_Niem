# Tài Liệu Tích Hợp Vận Chuyển Giao Hàng Nhanh (GHN) & Thanh Toán VNPay

Tài liệu này hướng dẫn chi tiết cách cấu hình, luồng hoạt động và các quy tắc nghiệp vụ liên quan đến hai tính năng cốt lõi của website Souvenir Shop: **Tính phí vận chuyển qua GHN API** và **Thanh toán trực tuyến qua cổng VNPay**.

---

## 1. Tích Hợp Vận Chuyển Giao Hàng Nhanh (GHN)

### 1.1. Luồng hoạt động (Workflow)
1. Khi khách hàng thay đổi thông tin Tỉnh/Thành, Quận/Huyện, Phường/Xã ở giao diện thanh toán:
   - JavaScript gửi một yêu cầu Ajax (GET) chứa các mã địa giới chuẩn của GHN lên API `/api/checkout/quote`.
2. Hệ thống kiểm tra xem tính năng GHN có đang bật không (`ghn.enabled=true`).
3. Nếu bật, hệ thống gửi request HTTPS (POST) chứa mã địa chỉ kho gửi, mã địa chỉ khách nhận, khối lượng, kích thước và giá trị đơn hàng sang máy chủ của GHN (`/v2/shipping-order/fee`).
4. Kết quả phí ship thực tế được trả về, cộng vào tổng tiền thanh toán và cập nhật ngay lập tức lên giao diện của khách hàng.
5. Nếu xảy ra lỗi kết nối hoặc chưa cấu hình Token, hệ thống tự động kích hoạt chế độ **Dự phòng (Fallback)** để tính phí tĩnh cố định nhằm tránh làm gián đoạn trải nghiệm mua hàng.

### 1.2. Quy tắc tính phí vận chuyển (Shipping Fee Rules)
- **Quy tắc Miễn phí (Free Ship)**: Nếu tổng tiền hàng (sau giảm giá) lớn hơn hoặc bằng ngưỡng miễn phí vận chuyển (`shipping.free.threshold=500000` - mặc định là 500.000đ), phí ship sẽ tự động trả về **0đ**.
- **Thông số đóng gói mặc định**:
  - Khối lượng: `500g` (cấu hình qua `ghn.default.weight`).
  - Kích thước hộp: `15 x 15 x 15 cm` (Dài x Rộng x Cao).
  - Loại dịch vụ: Giao hàng chuẩn / Thương mại điện tử (`service.type.id = 2`).
- **Địa chỉ kho gửi (Shop Location)**:
  - Phí giao hàng được GHN API tính toán dựa trên khoảng cách giữa địa chỉ kho của shop (`ghn.from.district.id` & `ghn.from.ward.code`) và địa chỉ nhận hàng của khách.
  - Theo mặc định mới, kho hàng được cấu hình ở **Quận Hoàn Kiếm, Hà Nội** (district `1489`, ward `1A0218`) để khi bạn test giao hàng nội tỉnh Hà Nội, mức phí trả về sẽ là phí nội tỉnh thực tế (~20.900đ) thay vì phí liên tỉnh từ TP.HCM (~80.000đ).
- **Phí bảo hiểm và giới hạn khai giá**:
  - GHN tự động cộng thêm phí bảo hiểm (khai giá) dựa trên giá trị đơn hàng. Hệ thống đã được thiết lập tự động giới hạn giá trị bảo hiểm gửi lên GHN tối đa là `20.000.000đ` (theo quy định của GHN) để tránh lỗi API.
- **Quy tắc dự phòng (Fallback Rules)**:
  - Phí cơ bản: `30.000đ` (`shipping.base.fee`).
  - Phụ phí vùng xa (Cà Mau, Bạc Liêu): `+ 15.000đ` (`shipping.remote.surcharge`).

---

## 2. Tích Hợp Thanh Toán Trực Tuyến VNPay

### 2.1. Luồng hoạt động (Workflow)
1. Khách hàng thực hiện đặt hàng và chọn phương thức thanh toán **VNPay Sandbox**.
2. Đơn hàng được tạo trong database dưới trạng thái `PENDING` (Chờ xử lý).
3. Hệ thống tạo ra một mã giao dịch duy nhất (`vnpay_txn_ref`) và gửi yêu cầu tạo URL thanh toán sang cổng VNPay Sandbox.
4. Trình duyệt của khách hàng được chuyển hướng sang giao diện thanh toán của VNPay.
5. Sau khi khách hàng thao tác thanh toán:
   - **Xử lý phản hồi (vnp_ReturnUrl)**: Khách hàng được chuyển hướng quay lại trang `/payment/vnpay/return` của shop để hiển thị thông báo kết quả giao dịch.
   - **Xử lý bất đồng bộ (vnp_IpnUrl - IPN)**: Máy chủ VNPay gọi ngầm tới API `/payment/vnpay/ipn` của shop. Hệ thống sẽ kiểm tra chữ ký bảo mật, so khớp số tiền và tự động cập nhật trạng thái đơn hàng sang `CONFIRMED` (Đã xác nhận), đồng thời trừ tồn kho sản phẩm trong database.

---

## 3. Hướng Dẫn Cấu Hình Trong File `db.properties`

Tất cả các khóa cấu hình được quản lý trong file [db.properties](file:///c:/Users/toilamanh/Desktop/AppDoLuuNiem/App_Website_Do_Luu_Niem/src/main/resources/db.properties):

```properties
# ==========================================
# CẤU HÌNH GIAO HÀNG NHANH (GHN)
# ==========================================
# Bật/tắt tính năng tính phí ship qua GHN
ghn.enabled=true

# Đường dẫn API (Sử dụng dev-online-gateway cho môi trường thử nghiệm Sandbox)
ghn.api.url=https://dev-online-gateway.ghn.vn/shiip/public-api/v2/

# API Token bảo mật lấy từ trang quản trị cá nhân của GHN
ghn.api.token=a335f65a-623a-11f1-a973-aee5264794df

# Mã cửa hàng của bạn lấy từ trang GHN
ghn.shop.id=200593

# Địa chỉ kho gửi hàng mặc định (Mã Quận/Huyện và Phường/Xã của Shop - mặc định Quận Hoàn Kiếm, Hà Nội)
ghn.from.district.id=1489
ghn.from.ward.code=1A0218

# Trọng lượng mặc định của một gói hàng (gram)
ghn.default.weight=500

# Loại dịch vụ giao hàng (2: Chuẩn / Thương mại điện tử)
ghn.service.type.id=2

# Bắt buộc chọn nguồn dữ liệu địa chỉ là GHN để đồng bộ mã ID
address.api.provider=ghn

# ==========================================
# CẤU HÌNH VNPAY PAYMENT
# ==========================================
# Bật/tắt phương thức thanh toán VNPay
vnpay.enabled=true

# Mã định danh website của bạn tại VNPay (TMN Code)
vnpay.tmn.code=TMJ7UWMF

# Chuỗi bí mật dùng để tạo và xác thực chữ ký bảo mật (Hash Secret)
vnpay.hash.secret=PCYPY7L159DSIEKYFRXWCAL6M503VYUH

# Cổng thanh toán Sandbox của VNPay
vnpay.pay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.version=2.1.0

# Các URL Callback (Để trống = Hệ thống tự động suy ra từ tên miền chạy ứng dụng)
vnpay.return.url=
vnpay.ipn.url=

# ==========================================
# PHÍ VẬN CHUYỂN DỰ PHÒNG & MIỄN PHÍ SHIP
# ==========================================
shipping.base.fee=30000          # Phí giao hàng cơ bản mặc định
shipping.free.threshold=500000    # Đơn hàng từ 500k trở lên được miễn phí ship hoàn toàn
shipping.remote.surcharge=15000   # Phụ phí vùng xa
shipping.remote.provinces=96,97   # Danh sách mã tỉnh vùng xa (ví dụ Cà Mau, Bạc Liêu)
```

### 3.1. Hướng dẫn lấy mã Quận/Huyện (`ghn.from.district.id`) và Phường/Xã (`ghn.from.ward.code`) của kho hàng
Khi bạn chuyển sang sử dụng địa chỉ kho thật của bạn, bạn cần điền chính xác hai mã ID địa giới này. Có 3 cách đơn giản để tra cứu:

*   **Cách 1: Lấy trực tiếp từ trang Thanh toán (Khuyên dùng)**
    1. Bật ứng dụng, truy cập trang thanh toán (Checkout) trên trình duyệt.
    2. Trong phần chọn địa chỉ nhận hàng, chọn đúng địa chỉ Tỉnh/Thành phố, Quận/Huyện, Phường/Xã nơi kho hàng/shop của bạn đặt trụ sở.
    3. Nhấp chuột phải vào dropdown Quận/Huyện -> Chọn **Inspect** (Kiểm tra phần tử). Bạn sẽ thấy mã `value` của thẻ `<option>` đang được chọn (Ví dụ Quận Hoàn Kiếm là `1489`). Copy mã này điền vào `ghn.from.district.id`.
    4. Tương tự, nhấp chuột phải vào dropdown Phường/Xã -> Chọn **Inspect** để lấy mã `value` của option tương ứng (Ví dụ Phường Tràng Tiền là `1A0218`). Copy mã này điền vào `ghn.from.ward.code`.
*   **Cách 2: Tra cứu trên trang quản trị GHN**
    * Đăng nhập tài khoản GHN của bạn tại [5sao.ghn.dev](https://5sao.ghn.dev/).
    * Vào phần cấu hình địa chỉ kho/cửa hàng, các thông tin mã Quận/Huyện và Phường/Xã sẽ được hiển thị chi tiết tại danh sách kho hàng.
*   **Cách 3: Sử dụng API của GHN**
    * Bạn có thể gửi yêu cầu HTTP Post đến URL `https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/district` (với body chứa `province_id`) để lấy danh sách mã Huyện.
    * Và đến `https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/ward` (với body chứa `district_id`) để lấy danh sách mã Xã.

---

## 4. Các File Mã Nguồn Liên Quan Trong Dự Án

### 4.1. Phần Giao Hàng Nhanh (GHN)
- [GhnProvider.java](file:///c:/Users/toilamanh/Desktop/AppDoLuuNiem/App_Website_Do_Luu_Niem/src/main/java/com/app/app_website_do_luu_niem/service/address/GhnProvider.java): Gọi API lấy danh mục Tỉnh/Huyện/Xã từ GHN. Đã được sửa đổi để tự động loại bỏ hậu tố `/v2/` khi truy vấn dữ liệu địa danh để tránh lỗi 404.
- [GhnShippingService.java](file:///c:/Users/toilamanh/Desktop/AppDoLuuNiem/App_Website_Do_Luu_Niem/src/main/java/com/app/app_website_do_luu_niem/service/GhnShippingService.java): Gọi API tính toán phí giao hàng `/v2/shipping-order/fee` từ máy chủ GHN.
- [CheckoutService.java](file:///c:/Users/toilamanh/Desktop/AppDoLuuNiem/App_Website_Do_Luu_Niem/src/main/java/com/app/app_website_do_luu_niem/service/CheckoutService.java): Thực hiện logic kiểm tra ngưỡng miễn phí ship và điều phối gọi `GhnShippingService` hoặc kích hoạt Fallback tính phí tĩnh.

### 4.2. Phần Thanh Toán VNPay
- [VNPayService.java](file:///c:/Users/toilamanh/Desktop/AppDoLuuNiem/App_Website_Do_Luu_Niem/src/main/java/com/app/app_website_do_luu_niem/service/VNPayService.java): Đọc cấu hình VNPay, tạo mã giao dịch tạm thời và xây dựng URL thanh toán có chữ ký bảo mật bảo vệ chống thay đổi số tiền đơn hàng.
- [VNPayPayServlet.java](file:///c:/Users/toilamanh/Desktop/AppDoLuuNiem/App_Website_Do_Luu_Niem/src/main/java/com/app/app_website_do_luu_niem/controller/shop/VNPayPayServlet.java): Xử lý yêu cầu thanh toán/thanh toán lại cho một đơn hàng đang ở trạng thái `PENDING`.
- [VNPayReturnServlet.java](file:///c:/Users/toilamanh/Desktop/AppDoLuuNiem/App_Website_Do_Luu_Niem/src/main/java/com/app/app_website_do_luu_niem/controller/shop/VNPayReturnServlet.java): Xử lý chuyển hướng của người dùng sau khi giao dịch trên VNPay hoàn tất và hiển thị trang kết quả đặt hàng thành công.
- [VNPayIpnServlet.java](file:///c:/Users/toilamanh/Desktop/AppDoLuuNiem/App_Website_Do_Luu_Niem/src/main/java/com/app/app_website_do_luu_niem/controller/shop/VNPayIpnServlet.java): Tiếp nhận phản hồi ngầm (IPN) từ máy chủ VNPay để xác thực giao dịch chính xác tuyệt đối, cập nhật trạng thái đơn hàng và tiến hành trừ kho sản phẩm.
- [VNPayUtil.java](file:///c:/Users/toilamanh/Desktop/AppDoLuuNiem/App_Website_Do_Luu_Niem/src/main/java/com/app/app_website_do_luu_niem/util/VNPayUtil.java): Lớp tiện ích mã hóa và tạo chữ ký HMAC-SHA512.

### 4.3. Biểu mẫu Thanh toán (Checkout)
- [checkout.jsp](file:///c:/Users/toilamanh/Desktop/AppDoLuuNiem/App_Website_Do_Luu_Niem/src/main/webapp/WEB-INF/views/shop/checkout.jsp): Giao diện thanh toán chính. Đã được cấu hình ẩn nguồn chọn địa chỉ khác và chỉ để mặc định sử dụng GHN API.
- [checkout.js](file:///c:/Users/toilamanh/Desktop/AppDoLuuNiem/App_Website_Do_Luu_Niem/src/main/webapp/js/checkout.js): JavaScript quản lý Ajax gọi địa danh, thay đổi tổng tiền và tính toán báo giá.

---

## 5. Hướng Dẫn Thử Nghiệm Sandbox

1. **Địa chỉ giao hàng**: Sử dụng các địa danh của Giao Hàng Nhanh hiển thị trên dropdown để tính phí giao hàng động.
2. **Thanh toán VNPay**:
   - Khi thanh toán bằng VNPay Sandbox, chọn ngân hàng **NCB**.
   - **Số thẻ**: `970419852619143218`
   - **Tên chủ thẻ**: `NGUYEN VAN A`
   - **Ngày phát hành**: `07/15`
   - **Mật khẩu OTP**: `123456`
