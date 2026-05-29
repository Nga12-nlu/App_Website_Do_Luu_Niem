package com.app.app_website_do_luu_niem.controller.auth;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.service.AuthService;
import com.app.app_website_do_luu_niem.service.GoogleOAuthService;
import com.app.app_website_do_luu_niem.service.GoogleOAuthService.GoogleUserInfo;
import com.app.app_website_do_luu_niem.util.AuthRedirectHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "googleCallbackServlet", urlPatterns = "/auth/google/callback")
public class GoogleCallbackServlet extends HttpServlet {

    private final AuthService authService = new AuthService();
    private final GoogleOAuthService googleOAuth = new GoogleOAuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!AppConfig.isGoogleOAuthEnabled()) {
            redirectLoginError(req, resp, "google_disabled");
            return;
        }

        String error = req.getParameter("error");
        if (error != null && !error.isBlank()) {
            redirectLoginError(req, resp, "google_denied");
            return;
        }

        HttpSession session = req.getSession(false);
        String expectedState = session != null ? (String) session.getAttribute(GoogleLoginServlet.SESSION_OAUTH_STATE) : null;
        String state = req.getParameter("state");
        if (session == null || expectedState == null || !expectedState.equals(state)) {
            redirectLoginError(req, resp, "google_state");
            return;
        }
        session.removeAttribute(GoogleLoginServlet.SESSION_OAUTH_STATE);

        String code = req.getParameter("code");
        if (code == null || code.isBlank()) {
            redirectLoginError(req, resp, "google_failed");
            return;
        }

        String redirectAfter = (String) session.getAttribute(GoogleLoginServlet.SESSION_OAUTH_REDIRECT);
        session.removeAttribute(GoogleLoginServlet.SESSION_OAUTH_REDIRECT);

        try {
            String callbackUri = googleOAuth.resolveRedirectUri(req);
            GoogleOAuthService.GoogleTokenResponse tokens = googleOAuth.exchangeCode(code, callbackUri);
            if (tokens.accessToken == null || tokens.accessToken.isBlank()) {
                redirectLoginError(req, resp, "google_failed");
                return;
            }
            GoogleUserInfo profile = googleOAuth.fetchUserInfo(tokens.accessToken);
            if (!profile.emailVerified) {
                redirectLoginError(req, resp, "google_email_unverified");
                return;
            }

            Optional<String> errCode = authService.loginOrRegisterWithGoogle(profile);
            if (errCode.isPresent()) {
                redirectLoginError(req, resp, mapErrorCode(errCode.get()));
                return;
            }

            Optional<User> userOpt = authService.findActiveUserByGoogleId(profile.sub.trim());
            if (userOpt.isEmpty() || !userOpt.get().isActive()) {
                redirectLoginError(req, resp, "google_failed");
                return;
            }

            AuthRedirectHelper.completeLogin(req, resp, userOpt.get(), redirectAfter);
        } catch (Exception e) {
            req.getServletContext().log("Google OAuth callback failed: " + e.getMessage(), e);
            redirectLoginError(req, resp, "google_failed");
        }
    }

    private static String mapErrorCode(String code) {
        return switch (code) {
            case "inactive" -> "google_inactive";
            case "email_linked_other" -> "google_email_conflict";
            default -> "google_failed";
        };
    }

    private void redirectLoginError(HttpServletRequest req, HttpServletResponse resp, String code) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/login?error=" + code);
    }
}
