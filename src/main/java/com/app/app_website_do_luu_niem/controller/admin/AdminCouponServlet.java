package com.app.app_website_do_luu_niem.controller.admin;

import com.app.app_website_do_luu_niem.dao.CouponDao;
import com.app.app_website_do_luu_niem.dao.impl.CouponDaoImpl;
import com.app.app_website_do_luu_niem.model.Coupon;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@WebServlet(name = "adminCouponServlet", urlPatterns = "/admin/coupons")
public class AdminCouponServlet extends HttpServlet {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{3,32}$");
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final CouponDao couponDao = new CouponDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }
        switch (action) {
            case "create" -> showForm(req, resp, new Coupon(), true);
            case "edit" -> showEdit(req, resp);
            default -> showList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleSave(req, resp);
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Coupon> coupons = couponDao.findAll();
        req.setAttribute("coupons", coupons);
        req.getRequestDispatcher("/WEB-INF/views/admin/coupons.jsp").forward(req, resp);
    }

    private void showEdit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = parseInt(req.getParameter("id"), 0);
        Optional<Coupon> opt = couponDao.findById(id);
        if (opt.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/admin/coupons");
            return;
        }
        showForm(req, resp, opt.get(), false);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Coupon coupon, boolean isNew)
            throws ServletException, IOException {
        req.setAttribute("coupon", coupon);
        req.setAttribute("isNew", isNew);
        req.getRequestDispatcher("/WEB-INF/views/admin/coupon-form.jsp").forward(req, resp);
    }

    private void handleSave(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        boolean isNew = idParam == null || idParam.isBlank();
        Coupon coupon;
        if (isNew) {
            coupon = new Coupon();
        } else {
            Optional<Coupon> existing = couponDao.findById(Integer.parseInt(idParam));
            if (existing.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/admin/coupons");
                return;
            }
            coupon = existing.get();
        }

        String code = trim(req.getParameter("code"));
        if (code == null || !CODE_PATTERN.matcher(code).matches()) {
            redirectWithError(req, resp, isNew ? null : coupon.getId(), "Mã không hợp lệ (3–32 ký tự, chữ/số/gạch).");
            return;
        }
        coupon.setCode(code.toUpperCase(Locale.ROOT));
        coupon.setDescription(trim(req.getParameter("description")));

        String type = trim(req.getParameter("discountType"));
        if (!"PERCENT".equalsIgnoreCase(type) && !"FIXED".equalsIgnoreCase(type)) {
            redirectWithError(req, resp, isNew ? null : coupon.getId(), "Loại giảm giá không hợp lệ.");
            return;
        }
        coupon.setDiscountType(type.toUpperCase(Locale.ROOT));

        try {
            coupon.setDiscountValue(new BigDecimal(trim(req.getParameter("discountValue"))));
            String minStr = trim(req.getParameter("minOrderAmount"));
            coupon.setMinOrderAmount(minStr != null && !minStr.isBlank()
                    ? new BigDecimal(minStr) : BigDecimal.ZERO);
            String maxStr = trim(req.getParameter("maxDiscount"));
            coupon.setMaxDiscount(maxStr != null && !maxStr.isBlank() ? new BigDecimal(maxStr) : null);
            String limitStr = trim(req.getParameter("usageLimit"));
            coupon.setUsageLimit(limitStr != null && !limitStr.isBlank() ? Integer.parseInt(limitStr) : null);
            coupon.setPerUserLimit(Math.max(1, parseInt(req.getParameter("perUserLimit"), 1)));
        } catch (NumberFormatException e) {
            redirectWithError(req, resp, isNew ? null : coupon.getId(), "Giá trị số không hợp lệ.");
            return;
        }

        coupon.setStartsAt(parseDateTime(req.getParameter("startsAt")));
        coupon.setExpiresAt(parseDateTime(req.getParameter("expiresAt")));
        coupon.setActive("on".equals(req.getParameter("active")) || "true".equals(req.getParameter("active")));

        if (isNew) {
            coupon.setUsedCount(0);
            couponDao.save(coupon);
        } else {
            couponDao.update(coupon);
        }
        resp.sendRedirect(req.getContextPath() + "/admin/coupons?saved=1");
    }

    private void redirectWithError(HttpServletRequest req, HttpServletResponse resp, Integer id, String msg)
            throws IOException {
        String base = req.getContextPath() + "/admin/coupons?action=" + (id == null ? "create" : "edit&id=" + id);
        resp.sendRedirect(base + "&error=" + java.net.URLEncoder.encode(msg, java.nio.charset.StandardCharsets.UTF_8));
    }

    private static LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(raw, DT);
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private static String trim(String s) {
        return s != null ? s.trim() : null;
    }
}
