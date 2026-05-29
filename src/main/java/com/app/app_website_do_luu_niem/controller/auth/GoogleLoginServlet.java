package com.app.app_website_do_luu_niem.controller.auth;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.app.app_website_do_luu_niem.service.GoogleOAuthService;
import com.app.app_website_do_luu_niem.util.TokenHasher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "googleLoginServlet", urlPatterns = "/auth/google")
public class GoogleLoginServlet extends HttpServlet {

    public static final String SESSION_OAUTH_STATE = "googleOAuthState";
    public static final String SESSION_OAUTH_REDIRECT = "googleOAuthRedirect";

    private final GoogleOAuthService googleOAuth = new GoogleOAuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!AppConfig.isGoogleOAuthEnabled()) {
            resp.sendRedirect(req.getContextPath() + "/login?error=google_disabled");
            return;
        }

        String state = TokenHasher.newRawToken();
        HttpSession session = req.getSession(true);
        session.setAttribute(SESSION_OAUTH_STATE, state);

        String redirect = req.getParameter("redirect");
        if (redirect != null && !redirect.isBlank()) {
            session.setAttribute(SESSION_OAUTH_REDIRECT, redirect);
        } else {
            session.removeAttribute(SESSION_OAUTH_REDIRECT);
        }

        String callbackUri = googleOAuth.resolveRedirectUri(req);
        String authUrl = googleOAuth.buildAuthorizationUrl(state, callbackUri);
        resp.sendRedirect(authUrl);
    }
}
