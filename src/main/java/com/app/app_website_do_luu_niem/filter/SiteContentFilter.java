package com.app.app_website_do_luu_niem.filter;

import com.app.app_website_do_luu_niem.service.StaticContentService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;
import java.util.Map;

@WebFilter(urlPatterns = "/*")
public class SiteContentFilter implements Filter {

    private final StaticContentService staticContentService = new StaticContentService();

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Map<String, String> contentMap = staticContentService.resolveContentMap();
        request.setAttribute("siteContent", contentMap);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no-op
    }
}
