package com.app.app_website_do_luu_niem.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class CharacterEncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String path = ((HttpServletRequest) request).getRequestURI();
        if (!path.matches(".*\\.(css|js|png|jpg|jpeg|gif|ico|woff|woff2|svg)$")) {
            response.setContentType("text/html; charset=UTF-8");
        }
        chain.doFilter(request, response);
    }
}

