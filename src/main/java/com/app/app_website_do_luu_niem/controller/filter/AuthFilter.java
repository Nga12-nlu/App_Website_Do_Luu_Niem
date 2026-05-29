package com.app.app_website_do_luu_niem.controller.filter;

import com.app.app_website_do_luu_niem.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebFilter(urlPatterns = {
        "/cart", "/checkout", "/my-orders", "/order-success", "/profile", "/change-password",
        "/payment/vnpay/*", "/api/checkout/*", "/api/coupon/*", "/admin/*"
})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;
        HttpSession session = httpReq.getSession(false);

        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        String path = httpReq.getRequestURI().substring(httpReq.getContextPath().length());

        if (currentUser == null) {
            String redirect = httpReq.getRequestURI();
            if (httpReq.getQueryString() != null) {
                redirect += "?" + httpReq.getQueryString();
            }
            httpRes.sendRedirect(httpReq.getContextPath() + "/login?redirect=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8));
            return;
        }

        if (path.startsWith("/admin") && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            httpRes.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no-op
    }
}


