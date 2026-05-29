package com.app.app_website_do_luu_niem.controller.shop;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.app.app_website_do_luu_niem.dao.OrderDao;
import com.app.app_website_do_luu_niem.dao.impl.OrderDaoImpl;
import com.app.app_website_do_luu_niem.model.CartItem;
import com.app.app_website_do_luu_niem.model.CheckoutQuote;
import com.app.app_website_do_luu_niem.model.Coupon;
import com.app.app_website_do_luu_niem.model.Order;
import com.app.app_website_do_luu_niem.model.OrderItem;
import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.service.CheckoutService;
import com.app.app_website_do_luu_niem.service.CouponService;
import com.app.app_website_do_luu_niem.service.VNPayService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@WebServlet(name = "checkoutServlet", urlPatterns = "/checkout")
public class CheckoutServlet extends HttpServlet {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{9,10}$");

    private final OrderDao orderDao = new OrderDaoImpl();
    private final VNPayService vnpayService = new VNPayService();
    private final CheckoutService checkoutService = new CheckoutService();
    private final CouponService couponService = new CouponService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }
        List<CartItem> cart = getCart(session);
        if (cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        checkoutService.refreshCartFromDatabase(cart);
        String couponCode = (String) session.getAttribute(CouponService.SESSION_APPLIED_COUPON);
        CheckoutQuote quote = checkoutService.buildQuote(cart, couponCode, user.getId(), null);

        req.setAttribute("cartItems", cart);
        req.setAttribute("quote", quote);
        req.setAttribute("defaultAddressProvider", AppConfig.getAddressApiProvider());
        setVnpayCheckoutAttributes(req);
        req.getRequestDispatcher("/WEB-INF/views/shop/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }
        List<CartItem> cart = getCart(session);
        if (cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        String receiverName = trim(req.getParameter("receiverName"));
        String phone = trim(req.getParameter("phone"));
        String provinceCode = trim(req.getParameter("provinceCode"));
        String provinceName = trim(req.getParameter("provinceName"));
        String districtCode = trim(req.getParameter("districtCode"));
        String districtName = trim(req.getParameter("districtName"));
        String wardCode = trim(req.getParameter("wardCode"));
        String wardName = trim(req.getParameter("wardName"));
        String addressDetail = trim(req.getParameter("addressDetail"));
        String customerNote = trim(req.getParameter("customerNote"));
        String paymentMethod = trim(req.getParameter("paymentMethod"));
        if (paymentMethod == null || paymentMethod.isBlank()) {
            paymentMethod = "COD";
        }
        boolean useVnpay = "VNPAY".equalsIgnoreCase(paymentMethod);

        List<String> errors = validateCheckoutForm(receiverName, phone, provinceCode, provinceName,
                districtCode, districtName, wardCode, wardName, addressDetail, customerNote);
        if (!errors.isEmpty()) {
            forwardCheckoutError(req, resp, cart, currentUser, String.join(" ", errors),
                    receiverName, phone, provinceCode, provinceName, districtCode, districtName,
                    wardCode, wardName, addressDetail, customerNote, paymentMethod);
            return;
        }

        if (useVnpay && !vnpayService.isConfigured()) {
            forwardCheckoutError(req, resp, cart, currentUser,
                    "Thanh toán VNPay chưa được cấu hình. Vui lòng chọn COD hoặc điền vnpay.tmn.code và vnpay.hash.secret.",
                    receiverName, phone, provinceCode, provinceName, districtCode, districtName,
                    wardCode, wardName, addressDetail, customerNote, paymentMethod);
            return;
        }

        checkoutService.refreshCartFromDatabase(cart);
        String sessionCoupon = (String) session.getAttribute(CouponService.SESSION_APPLIED_COUPON);
        CheckoutQuote quote = checkoutService.buildQuote(cart, sessionCoupon, currentUser.getId(), provinceCode);
        if (sessionCoupon != null && !sessionCoupon.isBlank() && !quote.isCouponApplied()) {
            session.removeAttribute(CouponService.SESSION_APPLIED_COUPON);
            forwardCheckoutError(req, resp, cart, currentUser,
                    quote.getCouponMessage() != null ? quote.getCouponMessage() : "Mã giảm giá không còn hợp lệ.",
                    receiverName, phone, provinceCode, provinceName, districtCode, districtName,
                    wardCode, wardName, addressDetail, customerNote, paymentMethod);
            return;
        }

        String fullAddress = CheckoutService.buildFullAddress(addressDetail, wardName, districtName, provinceName);

        Order order = new Order();
        order.setUser(currentUser);
        order.setSubtotal(quote.getSubtotal());
        order.setDiscountAmount(quote.getDiscountAmount());
        order.setShippingFee(quote.getShippingFee());
        order.setTotalAmount(quote.getTotalAmount());
        order.setStatus("PENDING");
        order.setPaymentMethod(useVnpay ? "VNPAY" : "COD");
        order.setReceiverName(receiverName);
        order.setCustomerNote(blankToNull(customerNote));
        order.setProvinceCode(provinceCode);
        order.setProvinceName(provinceName);
        order.setDistrictCode(districtCode);
        order.setDistrictName(districtName);
        order.setWardCode(wardCode);
        order.setWardName(wardName);
        order.setAddressDetail(addressDetail);
        order.setShippingAddress(fullAddress);
        order.setPhone(phone);
        order.setCreatedAt(LocalDateTime.now());

        if (quote.isCouponApplied() && quote.getCouponCode() != null) {
            Optional<Coupon> couponOpt = checkoutService.resolveCouponForOrder(
                    quote.getCouponCode(), quote.getSubtotal(), currentUser.getId());
            if (couponOpt.isEmpty()) {
                forwardCheckoutError(req, resp, cart, currentUser, "Mã giảm giá không còn hợp lệ.",
                        receiverName, phone, provinceCode, provinceName, districtCode, districtName,
                        wardCode, wardName, addressDetail, customerNote, paymentMethod);
                return;
            }
            Coupon coupon = couponOpt.get();
            order.setCouponId(coupon.getId());
            order.setCouponCode(coupon.getCode());
        }

        List<OrderItem> items = new ArrayList<>();
        for (CartItem ci : cart) {
            OrderItem oi = new OrderItem();
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());
            if (ci.getVariant() != null) {
                oi.setVariantId(ci.getVariant().getId());
                oi.setVariantLabel(ci.getVariant().getDisplayName());
            }
            items.add(oi);
        }
        order.setItems(items);

        try {
            orderDao.saveWithItems(order, !useVnpay);
            if (!useVnpay && order.getCouponId() != null) {
                couponService.recordOrderUsage(order.getCouponId(), currentUser.getId(), order.getId());
            }
        } catch (RuntimeException ex) {
            forwardCheckoutError(req, resp, cart, currentUser, "Không thể hoàn tất đơn hàng: " + ex.getMessage(),
                    receiverName, phone, provinceCode, provinceName, districtCode, districtName,
                    wardCode, wardName, addressDetail, customerNote, paymentMethod);
            return;
        }

        session.removeAttribute(CouponService.SESSION_APPLIED_COUPON);
        session.removeAttribute("cart");

        if (useVnpay) {
            String txnRef = vnpayService.newTxnRef(order.getId());
            orderDao.updateVnpayTxnRef(order.getId(), txnRef);
            order.setVnpayTxnRef(txnRef);
            session.setAttribute("vnpayPendingOrderId", order.getId());
            try {
                String payUrl = vnpayService.buildPaymentUrl(order, txnRef, req);
                resp.sendRedirect(payUrl);
            } catch (Exception e) {
                req.getServletContext().log("VNPay redirect failed: " + e.getMessage(), e);
                resp.sendRedirect(req.getContextPath() + "/order-success?id=" + order.getId()
                        + "&payment=vnpay_error");
            }
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/order-success?id=" + order.getId());
    }

    private List<String> validateCheckoutForm(String receiverName, String phone,
                                              String provinceCode, String provinceName,
                                              String districtCode, String districtName,
                                              String wardCode, String wardName,
                                              String addressDetail, String customerNote) {
        List<String> errors = new ArrayList<>();
        if (receiverName == null || receiverName.length() < 2 || receiverName.length() > 100) {
            errors.add("Họ tên người nhận phải từ 2–100 ký tự.");
        }
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            errors.add("Số điện thoại phải bắt đầu bằng 0 và có 10–11 chữ số.");
        }
        if (isBlank(provinceCode) || isBlank(provinceName)) {
            errors.add("Vui lòng chọn Tỉnh/Thành phố.");
        }
        if (isBlank(districtCode) || isBlank(districtName)) {
            errors.add("Vui lòng chọn Quận/Huyện.");
        }
        if (isBlank(wardCode) || isBlank(wardName)) {
            errors.add("Vui lòng chọn Phường/Xã.");
        }
        if (addressDetail == null || addressDetail.length() < 5 || addressDetail.length() > 500) {
            errors.add("Địa chỉ chi tiết phải từ 5–500 ký tự.");
        }
        if (customerNote != null && customerNote.length() > 500) {
            errors.add("Ghi chú tối đa 500 ký tự.");
        }
        return errors;
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCart(HttpSession session) {
        Object obj = session.getAttribute("cart");
        if (obj instanceof List<?> list) {
            return (List<CartItem>) list;
        }
        return List.of();
    }

    private void forwardCheckoutError(HttpServletRequest req, HttpServletResponse resp,
                                      List<CartItem> cart, User user, String error,
                                      String receiverName, String phone,
                                      String provinceCode, String provinceName,
                                      String districtCode, String districtName,
                                      String wardCode, String wardName,
                                      String addressDetail, String customerNote,
                                      String paymentMethod) throws ServletException, IOException {
        req.setAttribute("error", error);
        req.setAttribute("cartItems", cart);
        String couponCode = (String) req.getSession().getAttribute(CouponService.SESSION_APPLIED_COUPON);
        CheckoutQuote quote = checkoutService.buildQuote(cart, couponCode, user.getId(), provinceCode);
        req.setAttribute("quote", quote);
        req.setAttribute("formReceiverName", receiverName);
        req.setAttribute("formPhone", phone);
        req.setAttribute("formProvinceCode", provinceCode);
        req.setAttribute("formProvinceName", provinceName);
        req.setAttribute("formDistrictCode", districtCode);
        req.setAttribute("formDistrictName", districtName);
        req.setAttribute("formWardCode", wardCode);
        req.setAttribute("formWardName", wardName);
        req.setAttribute("formAddressDetail", addressDetail);
        req.setAttribute("formCustomerNote", customerNote);
        req.setAttribute("formPaymentMethod", paymentMethod);
        req.setAttribute("defaultAddressProvider", AppConfig.getAddressApiProvider());
        setVnpayCheckoutAttributes(req);
        req.getRequestDispatcher("/WEB-INF/views/shop/checkout.jsp").forward(req, resp);
    }

    private void setVnpayCheckoutAttributes(HttpServletRequest req) {
        req.setAttribute("vnpayEnabled", AppConfig.isVnpayEnabled());
        req.setAttribute("vnpayFeatureOn", AppConfig.isVnpayFeatureOn());
        req.setAttribute("vnpaySandbox", AppConfig.isVnpaySandbox());
        if (AppConfig.isVnpayFeatureOn() && !AppConfig.isVnpayEnabled()) {
            req.setAttribute("vnpayConfigWarning",
                    "VNPay sandbox đã bật — hãy điền vnpay.tmn.code và vnpay.hash.secret trong db.properties.");
        }
    }

    private static String trim(String s) {
        return s != null ? s.trim() : null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String blankToNull(String s) {
        return isBlank(s) ? null : s;
    }
}
