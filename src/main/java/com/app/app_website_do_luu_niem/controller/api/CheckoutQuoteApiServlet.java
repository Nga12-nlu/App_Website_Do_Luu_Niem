package com.app.app_website_do_luu_niem.controller.api;

import com.app.app_website_do_luu_niem.model.CartItem;
import com.app.app_website_do_luu_niem.model.CheckoutQuote;
import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.service.CheckoutService;
import com.app.app_website_do_luu_niem.service.CouponService;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "checkoutQuoteApiServlet", urlPatterns = "/api/checkout/quote")
public class CheckoutQuoteApiServlet extends HttpServlet {

    private static final Gson GSON = new Gson();
    private final CheckoutService checkoutService = new CheckoutService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null) {
            writeError(resp, 401, "Vui lòng đăng nhập");
            return;
        }
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            writeError(resp, 400, "Giỏ hàng trống");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            writeError(resp, 401, "Vui lòng đăng nhập");
            return;
        }
        Integer userId = user.getId();
        String provinceCode = req.getParameter("provinceCode");
        String couponCode = (String) session.getAttribute(CouponService.SESSION_APPLIED_COUPON);

        CheckoutQuote quote = checkoutService.buildQuote(cart, couponCode, userId, provinceCode);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("quote", quoteMap(quote));
        resp.getWriter().write(GSON.toJson(body));
    }

    private Map<String, Object> quoteMap(CheckoutQuote q) {
        Map<String, Object> m = new HashMap<>();
        m.put("subtotal", q.getSubtotal());
        m.put("discountAmount", q.getDiscountAmount());
        m.put("shippingFee", q.getShippingFee());
        m.put("totalAmount", q.getTotalAmount());
        m.put("couponCode", q.getCouponCode());
        m.put("couponApplied", q.isCouponApplied());
        m.put("couponMessage", q.getCouponMessage());
        return m;
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        resp.getWriter().write(GSON.toJson(body));
    }
}
