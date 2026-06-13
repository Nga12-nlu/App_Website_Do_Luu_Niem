# TÀI LIỆU MÔ TẢ HỆ THỐNG WEBSITE BÁN ĐỒ LƯU NIỆM (SOUVENIR SHOP)

Chào mừng bạn đến với tài liệu kỹ thuật và hướng dẫn nghiệp vụ của hệ thống **Souvenir Shop**. Đây là hệ thống website thương mại điện tử chuyên cung cấp đồ lưu niệm Việt Nam, được phát triển trên kiến trúc Java Web tiêu chuẩn, tập trung vào bảo mật cao, kiểm soát kho chặt chẽ và giao diện quản trị trực quan.

---

## 1. Yêu Cầu Nghiệp Vụ & Trạng Thái Hoàn Thành

Dưới đây là bảng đối chiếu chi tiết giữa các tính năng yêu cầu của cả 2 chủ đề nghiệp vụ và giải pháp kỹ thuật đã triển khai hoàn thiện trong dự án:

### CHỦ ĐỀ 1: SESSION TRACKING VÀ PHÂN QUYỀN NGƯỜI DÙNG

| STT | Tính năng yêu cầu | Giải pháp triển khai & Lớp mã nguồn xử lý | Trạng thái |
| :---: | :--- | :--- | :---: |
| 1 | **Session Tracking & Xác thực OTP** | Đăng ký tài khoản mới được gán mặc định trạng thái `UNVERIFIED` (chưa kích hoạt). Tạo mã OTP 6 chữ số ngẫu nhiên, lưu trữ vào bảng `otp_verifications` kèm thời gian hết hạn (10 phút) và gửi email kích hoạt tài khoản. Xử lý tại `RegisterServlet`, `VerifyOtpServlet`, `AuthService.java` và giao diện `verify-otp.jsp`. | **Hoàn thành 100%** |
| 2 | **Bảo mật mật khẩu (Băm MD5/BCrypt)** | Sử dụng thuật toán băm BCrypt cực kỳ bảo mật (thông qua thư viện `jbcrypt`) để băm mật khẩu trước khi lưu trữ vào CSDL thay thế cho MD5 thông thường. Xử lý tại `AuthService.java` và `RegisterServlet.java`. | **Hoàn thành 100%** |
| 3 | **Phân biệt trạng thái tài khoản** | Phân biệt rõ rệt giữa tài khoản chưa kích hoạt (`UNVERIFIED`) và tài khoản bị admin khóa vĩnh viễn (`BANNED`). Người dùng có email thuộc danh sách `BANNED` sẽ bị cấm đăng nhập và cấm đăng ký tài khoản mới. Xử lý tại `LoginServlet.java` và `RegisterServlet.java`. | **Hoàn thành 100%** |
| 4 | **Tự động khóa đăng nhập tạm thời** | Đếm số lần đăng nhập sai của người dùng trong ngày (cột `failed_logins`). Nếu đăng nhập sai liên tiếp quá 5 lần, tự động khóa tài khoản tạm thời trong **15 phút** bằng cách thiết lập mốc thời gian tại cột `lock_time`. Xử lý tại `AuthService.java` và `LoginServlet.java`. | **Hoàn thành 100%** |
| 5 | **Ép buộc logout khi thăng/hạ quyền** | Khi Admin thay đổi quyền hạn người dùng tại `AdminUserServlet.java`, hệ thống tự động ghi nhật ký vào bảng `user_role_updates`. Bộ lọc `AuthFilter.java` sẽ chặn và hủy toàn bộ phiên làm việc (Session Invalidate) trên mọi trình duyệt đang đăng nhập tài khoản đó ngay lập tức. | **Hoàn thành 100%** |
| 6 | **Nhật ký log thao tác của nhân viên** | Tạo bảng `system_logs` và lớp `SystemLogHelper.java` để tự động ghi nhận toàn bộ lịch sử thao tác thay đổi dữ liệu nhạy cảm (thêm/sửa sản phẩm, cập nhật trạng thái đơn, đổi quyền người dùng...) của nhân viên quản lý kèm theo địa chỉ IP và thời gian thực hiện. | **Hoàn thành 100%** |

### CHỦ ĐỀ 2: GIỎ HÀNG, ĐƠN HÀNG VÀ THỐNG KÊ

| STT | Tính năng yêu cầu | Giải pháp triển khai & Lớp mã nguồn xử lý | Trạng thái |
| :---: | :--- | :--- | :---: |
| 1 | **Luồng 6 trạng thái Đơn hàng** | Mở rộng quy trình quản lý đơn hàng lên 6 trạng thái thực tế: `PENDING` (Chờ xử lý) $\rightarrow$ `PACKAGING` (Đang đóng gói) $\rightarrow$ `AWAITING_SHIPPING` (Chờ giao ĐVVC) $\rightarrow$ `SHIPPING` (Đang giao) $\rightarrow$ `SHIPPED` (Đã giao) $\rightarrow$ `CANCELLED` (Đã hủy). Xử lý tại `AdminOrderServlet.java`, `orders.jsp` và `order-detail.jsp`. | **Hoàn thành 100%** |
| 2 | **Timeline theo dõi đơn hàng trực quan** | Tích hợp thanh tiến trình nằm ngang (Horizontal Stepper Timeline) cực kỳ hiện đại trên giao diện `my-orders.jsp` để khách mua hàng theo dõi trạng thái vận đơn trực quan theo thời gian thực. | **Hoàn thành 100%** |
| 3 | **Tự động khôi phục hàng tồn khi hủy đơn** | Khi trạng thái đơn hàng được cập nhật thành `CANCELLED` (Đã hủy đơn hàng) trong `OrderDaoImpl.java` (hỗ trợ cả thanh toán COD và VNPay), hệ thống tự động khôi phục lại (cộng thêm) số lượng sản phẩm/biến thể tương ứng vào kho hàng và đồng bộ lại số lượng sản phẩm chính. | **Hoàn thành 100%** |
| 4 | **Quản lý giao dịch tồn kho & Hao hụt** | Tạo bảng `inventory_transactions` ghi nhận chi tiết lịch sử tất cả các đợt biến động kho bao gồm: `IMPORT` (nhập hàng), `EXPORT` (xuất kho bán lẻ), `DAMAGE` (hư hỏng), `LOSS` (mất mát), và `DESTROYED` (tiêu hủy hàng lỗi). Xử lý tại `AdminInventoryServlet.java` và `inventory.jsp`. | **Hoàn thành 100%** |
| 5 | **Xuất / Nhập tồn kho hàng loạt qua CSV** | Phát triển chức năng Export/Import tồn kho hàng loạt thông qua file CSV chuẩn mã hóa UTF-8 có kèm dấu BOM (tránh lỗi hiển thị tiếng Việt trên MS Excel). Hỗ trợ hai chế độ cập nhật: Cộng dồn (ADD) hoặc Ghi đè (OVERWRITE) và lưu log tự động. Xử lý tại `AdminInventoryServlet.java`. | **Hoàn thành 100%** |
| 6 | **Báo cáo hàng đọng vốn (Dead Stock)** | Thống kê danh sách sản phẩm còn tồn kho cao nhưng không phát sinh bất kỳ đơn hàng nào trong vòng `X` ngày lọc (7, 15, 30, 60, 90, 180 ngày) và ước lượng chính xác số tiền vốn đang bị chôn trong kho hàng. Xử lý tại `AdminAnalyticsServlet.java` và `analytics.jsp`. | **Hoàn thành 100%** |
| 7 | **Thống kê hư hại, tiêu hao hao hụt** | Tổng hợp tổng thiệt hại tài chính do hàng hóa hỏng hóc, mất mát, tiêu hủy và top 10 sản phẩm gây thiệt hại lớn nhất để giúp nhà quản lý kịp thời điều chỉnh kế hoạch. Xử lý tại `AdminAnalyticsServlet.java` và `analytics.jsp`. | **Hoàn thành 100%** |
| 8 | **Đánh chỉ mục (Index) tối ưu CSDL** | Đánh chỉ mục hiệu năng cao cho các cột tìm kiếm và sắp xếp thường xuyên: `products(name)`, `orders(status)`, `orders(created_at)`, `inventory_transactions(type)`, `inventory_transactions(created_at)` trong lớp `DatabaseInitializer.java`. | **Hoàn thành 100%** |

---

## 2. Kiến Trúc & Công Nghệ Sử Dụng

Dự án được xây dựng theo mô hình **MVC (Model-View-Controller)** truyền thống, phân tách rõ ràng trách nhiệm của từng lớp để dễ bảo trì và mở rộng:

*   **Ngôn ngữ chính**: Java 21, HTML5, CSS3, JavaScript (Vanilla).
*   **Framework cốt lõi**: Jakarta EE Servlet/JSP (chạy trên Tomcat Container).
*   **Quản lý thư viện**: Apache Maven.
*   **Cơ sở dữ liệu**: MySQL, kết nối qua Connection Pool chuẩn (sử dụng JDBC thuần cho hiệu năng tối đa).
*   **Bảo mật & Mã hóa**: BCrypt (mật khẩu người dùng), SHA-256 (mã hóa token khôi phục mật khẩu).
*   **Thư viện bổ trợ**:
    *   `angus-mail`: Gửi thư điện tử (SMTP).
    *   `gson`: Xử lý dữ liệu JSON (đăng nhập Google OAuth).
    *   `jbcrypt`: Băm mật khẩu.

---

## 3. Chi Tiết Các Chức Năng Hệ Thống

Hệ thống được phân chia thành 2 phân hệ lớn tương ứng với hai vai trò người dùng: **Khách hàng** (Customer) và **Quản trị viên/Thủ kho** (Admin/Staff).

---

### PHÂN HỆ KHÁCH HÀNG (CUSTOMER)

#### 1. Đăng ký & Xác thực Tài khoản qua OTP
*   Cho phép đăng ký tài khoản bằng cách thu thập Họ tên, Email, Username, Số điện thoại và Mật khẩu.
*   Khi đăng ký thành công, tài khoản ở trạng thái `UNVERIFIED` (Chưa kích hoạt) và hệ thống sẽ tự động tạo **mã OTP 6 chữ số** gửi đến email người dùng (hoặc in ra console nếu cấu hình mail tắt).
*   Người dùng bắt buộc phải nhập đúng mã OTP trong vòng 10 phút để kích hoạt tài khoản thành `ACTIVE`. Nếu tài khoản chưa kích hoạt mà cố tình đăng nhập, hệ thống sẽ tự động gửi lại OTP mới và yêu cầu xác thực.

#### 2. Đăng nhập đa kênh & Bảo mật tài khoản
*   Hỗ trợ đăng nhập linh hoạt bằng 1 trong 3 thông tin: **Email**, **Username**, hoặc **Số điện thoại**.
*   **Hệ thống khóa tài khoản tự động**: Nhằm chống tấn công dò mật khẩu (Brute-force), nếu người dùng nhập sai mật khẩu quá 5 lần liên tiếp, tài khoản sẽ tự động bị khóa đăng nhập tạm thời trong **15 phút**.
*   **Xử lý tài khoản vi phạm (Banned)**: Admin có quyền khóa vĩnh viễn tài khoản. Tài khoản bị khóa sẽ bị chặn đăng nhập ngay lập tức và hệ thống sẽ cấm không cho đăng ký tài khoản mới bằng email cũ.
*   Hỗ trợ đăng nhập nhanh bằng tài khoản **Google OAuth 2.0**.

#### 3. Khôi phục mật khẩu
*   Người dùng quên mật khẩu có thể yêu cầu đặt lại qua Email.
*   Hệ thống gửi đường link chứa token dùng 1 lần (hạn dùng 1 giờ), tự động hủy token cũ chưa sử dụng để tránh chiếm đoạt tài khoản.

#### 4. Xem sản phẩm & Biến thể (Variants)
*   Hiển thị danh sách sản phẩm phân loại theo danh mục.
*   Hỗ trợ tìm kiếm theo tên và sắp xếp theo giá (từ thấp đến cao, từ cao đến thấp).
*   Mỗi sản phẩm có thể có nhiều biến thể khác nhau (ví dụ: kích thước, màu sắc, chất liệu) với giá bán và số lượng tồn kho riêng biệt.

#### 5. Giỏ hàng & Thanh toán (Checkout)
*   Thêm/Sửa/Xóa sản phẩm và chọn chính xác biến thể sản phẩm trong giỏ hàng.
*   **Mã giảm giá (Coupon)**: Áp dụng các mã giảm giá theo số tiền cố định hoặc phần trăm đơn hàng, tự động kiểm tra điều kiện áp dụng (số tiền tối thiểu, hạn dùng, lượt dùng tối đa).
*   **Phí vận chuyển động**: Tích hợp tính phí vận chuyển theo tỉnh thành/quận huyện/phường xã người nhận.
*   **Phương thức thanh toán**:
    *   Thanh toán khi nhận hàng (COD).
    *   Thanh toán trực tuyến bảo mật qua cổng **VNPay** (kèm theo ghi nhận mã giao dịch).

#### 6. Theo dõi đơn hàng trực quan (Timeline Tracker)
*   Khách hàng vào mục **Đơn hàng của tôi** để xem danh sách đơn hàng đã đặt.
*   Mỗi đơn hàng hiển thị một **Thanh tiến trình (Horizontal Stepper)** gồm 5 bước thể hiện trực quan quá trình xử lý đơn hàng thực tế:
    `Nhận đơn (PENDING)` $\rightarrow$ `Đóng gói (PACKAGING)` $\rightarrow$ `Chờ ĐVVC (AWAITING_SHIPPING)` $\rightarrow$ `Đang giao (SHIPPING)` $\rightarrow$ `Đã giao (SHIPPED)` (hoặc hiển thị nhãn Đã hủy nếu đơn hàng bị `CANCELLED`).

---

### PHÂN HỆ QUẢN TRỊ (ADMIN/STAFF PANEL)

#### 1. Bảng điều khiển (Dashboard)
*   Thống kê các số liệu kinh doanh cốt lõi: Tổng doanh thu, tổng sản phẩm, tổng đơn hàng, số lượng khách hàng hoạt động.
*   Biểu đồ khu vực trực quan: Thống kê doanh thu theo 7 ngày gần nhất.
*   Biểu đồ tròn: Thống kê cơ cấu trạng thái đơn hàng hiện tại.
*   **Cảnh báo tồn kho**: Danh sách các sản phẩm có số lượng tồn kho dưới ngưỡng an toàn (mặc định dưới 15 sản phẩm) để kịp thời nhập hàng.

#### 2. Quản lý sản phẩm & Biến thể
*   Thêm mới, chỉnh sửa thông tin sản phẩm (Tên, danh mục, mô tả, ảnh đại diện).
*   Thêm mới và chỉnh sửa danh sách các biến thể của sản phẩm (Tên biến thể, SKU, giá bán lẻ, số lượng tồn kho, ảnh riêng).
*   Tự động đồng bộ ngược: Số lượng tồn kho và giá bán hiển thị của sản phẩm chính luôn tự động bằng tổng số lượng tồn kho và giá nhỏ nhất của các biến thể con đang kích hoạt.

#### 3. Quản lý đơn hàng nâng cao & Khôi phục kho
*   Xem danh sách toàn bộ đơn hàng (hỗ trợ phân trang, tìm kiếm theo mã/tên/email và lọc theo trạng thái đơn hàng).
*   Cập nhật trạng thái đơn hàng qua 6 bước nghiệp vụ thực tế.
*   **Nghiệp vụ khôi phục kho khi hủy đơn**: Nếu đơn hàng bị chuyển sang trạng thái `CANCELLED` (Đã hủy), hệ thống sẽ tự động hoàn trả (cộng lại) toàn bộ số lượng sản phẩm/biến thể của đơn hàng đó vào kho để tái bán, đồng thời thực hiện cập nhật lại số lượng của sản phẩm chính.

#### 4. Quản lý Kho hàng & Hao hụt (Mới)
*   **Lịch sử giao dịch kho**: Ghi nhận toàn bộ các đợt biến động kho bao gồm nhập kho bán lẻ (`IMPORT`), xuất kho bán hàng (`EXPORT`), hư hỏng hàng hóa (`DAMAGE`), mất mát thất thoát (`LOSS`), và tiêu hủy sản phẩm lỗi (`DESTROYED`).
*   **Điều chỉnh kho thủ công**: Thủ kho có thể chọn sản phẩm và biến thể, chọn loại điều chỉnh và số lượng để tăng/giảm tồn kho trực tiếp kèm ghi chú/lý do. Hệ thống tự động ngăn chặn nếu thao tác làm tồn kho âm.
*   **Xuất Excel (CSV)**: Xuất danh sách toàn bộ sản phẩm, biến thể, mã SKU và tồn kho hiện tại ra file CSV chuẩn UTF-8 có BOM để quản lý ngoại tuyến.
*   **Nhập kho hàng loạt (CSV)**: Tải lên file CSV đã chỉnh sửa số lượng với hai chế độ:
    *   *Cộng dồn (ADD)*: Cộng thêm số lượng trong file vào kho hiện tại.
    *   *Ghi đè (OVERWRITE)*: Điều chỉnh tồn kho khớp chính xác với số lượng trong file CSV.

#### 5. Thống Kê Chi Tiết & Báo Cáo Tài Chính (Mới)
*   **Báo cáo hàng đọng vốn (Dead Stock)**: Liệt kê danh sách các sản phẩm và biến thể còn hàng nhưng **không phát sinh bất kỳ đơn hàng nào** trong vòng `X` ngày qua (cho phép lọc nhanh: 7, 15, 30, 60, 90, 180 ngày). Tính toán chính xác tổng giá trị vốn bị chôn trong các sản phẩm đọng này.
*   **Báo cáo tiêu hủy, hao hụt**: Tổng hợp tổng số lượng và ước lượng tổng giá trị thiệt hại tài chính phân loại theo từng loại hư hỏng (`DAMAGE`, `LOSS`, `DESTROYED`).
*   **Top 10 mặt hàng thất thoát**: Liệt kê 10 sản phẩm gây thiệt hại tài chính lớn nhất do hỏng hóc/mất mát.

#### 6. Quản lý người dùng & Ép buộc đăng xuất (Force Logout)
*   Quản lý danh sách người dùng, thăng/hạ quyền hạn (ADMIN, CUSTOMER...) hoặc khóa tài khoản.
*   **Đồng bộ Logout tức thì**: Khi Admin thay đổi quyền hạn của bất kỳ tài khoản nào, hệ thống ghi nhận mốc thời gian vào bảng `user_role_updates`. Lần tải trang tiếp theo của tài khoản đó trên mọi trình duyệt đang đăng nhập sẽ lập tức bị hủy phiên (Session Invalidate), chuyển hướng về trang đăng nhập kèm thông báo bắt buộc đăng nhập lại để nhận quyền mới.

#### 7. Nhật ký thao tác (System Logs)
*   Tự động ghi lại lịch sử các hành động sửa đổi dữ liệu quan trọng của nhân viên (ví dụ: thay đổi quyền người dùng, thay đổi giá trị cấu hình, cập nhật trạng thái đơn hàng, điều chỉnh kho thủ công...) kèm theo thời gian và địa chỉ IP thực hiện.

#### 8. Quản lý nội dung tĩnh website
*   Cho phép Admin thay đổi nội dung hiển thị trên trang chủ (tiêu đề banner hero, mô tả, các nhãn badge dịch vụ) và thông tin liên hệ chân trang (địa chỉ, số điện thoại, email liên hệ, dòng bản quyền) trực tiếp từ giao diện quản trị mà không cần sửa code giao diện.

---

## 4. Thiết Lập & Khởi Chạy Hệ Thống

### Bước 1: Chuẩn bị Cơ sở dữ liệu
1.  Tạo một cơ sở dữ liệu MySQL trống (ví dụ tên: `souvenir_db`).
2.  Mở tệp tin `src/main/resources/db.properties` và cấu hình các thông số kết nối:
    ```properties
    db.url=jdbc:mysql://localhost:3306/souvenir_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
    db.username=tên_đăng_nhập_mysql
    db.password=mật_khẩu_mysql
    
    # Cấu hình gửi mail OTP
    mail.enabled=false
    ```

### Bước 2: Tự động khởi tạo cấu trúc bảng (Migration)
Hệ thống sử dụng lớp tự động khởi chạy database `DatabaseInitializer` khi ứng dụng deploy. Cấu trúc bảng SQL sẽ tự động được tạo lập, nạp dữ liệu mẫu và đánh chỉ mục tối ưu ngay trong lần đầu khởi chạy trên Tomcat mà không cần chạy file SQL thủ công.

### Bước 3: Biên dịch dự án
Mở terminal tại thư mục gốc của dự án và chạy lệnh:
```powershell
$env:JAVA_HOME="C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.2\jbr"
.\mvnw clean package
```

### Bước 4: Khởi chạy
Deploy tệp `war` được tạo ra trong thư mục `target` vào máy chủ **Apache Tomcat 10** (hoặc sử dụng plugin SmartTomcat trên IntelliJ IDEA). Truy cập địa chỉ `http://localhost:8080/App_Website_Do_Luu_Niem/` để trải nghiệm hệ thống.
