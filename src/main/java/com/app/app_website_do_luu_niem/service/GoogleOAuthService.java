package com.app.app_website_do_luu_niem.service;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

/**
 * Google OAuth 2.0 (authorization code) — đăng nhập OpenID.
 */
public class GoogleOAuthService {

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public String buildAuthorizationUrl(String state, String redirectUri) {
        return AUTH_URL + "?"
                + "client_id=" + encode(AppConfig.getGoogleClientId())
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=" + encode("openid email profile")
                + "&state=" + encode(state)
                + "&access_type=online"
                + "&prompt=select_account";
    }

    public String resolveRedirectUri(HttpServletRequest req) {
        String override = AppConfig.getGoogleRedirectUriOverride();
        if (!override.isEmpty()) {
            return override;
        }
        String base = AppConfig.getPublicBaseUrl();
        String ctx = req.getContextPath();
        if (base.isEmpty()) {
            int port = req.getServerPort();
            String scheme = req.getScheme();
            String host = req.getServerName();
            boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
            base = scheme + "://" + host + (defaultPort ? "" : ":" + port) + ctx;
        } else if (!base.endsWith(ctx) && !base.contains(ctx + "/") && ctx != null && !ctx.isBlank() && !"/".equals(ctx)) {
            base = base + ctx;
        }
        return base + "/auth/google/callback";
    }

    public GoogleTokenResponse exchangeCode(String code, String redirectUri) throws Exception {
        String body = "code=" + encode(code)
                + "&client_id=" + encode(AppConfig.getGoogleClientId())
                + "&client_secret=" + encode(AppConfig.getGoogleClientSecret())
                + "&redirect_uri=" + encode(redirectUri)
                + "&grant_type=authorization_code";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Google token error: HTTP " + response.statusCode());
        }
        return GSON.fromJson(response.body(), GoogleTokenResponse.class);
    }

    public GoogleUserInfo fetchUserInfo(String accessToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERINFO_URL))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Google userinfo error: HTTP " + response.statusCode());
        }
        JsonObject json = GSON.fromJson(response.body(), JsonObject.class);
        GoogleUserInfo info = new GoogleUserInfo();
        info.sub = json.has("sub") ? json.get("sub").getAsString() : null;
        info.email = json.has("email") ? json.get("email").getAsString() : null;
        info.name = json.has("name") ? json.get("name").getAsString() : null;
        info.emailVerified = json.has("email_verified") && json.get("email_verified").getAsBoolean();
        return info;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static class GoogleTokenResponse {
        @SerializedName("access_token")
        public String accessToken;
        @SerializedName("id_token")
        public String idToken;
        @SerializedName("token_type")
        public String tokenType;
    }

    public static class GoogleUserInfo {
        public String sub;
        public String email;
        public String name;
        public boolean emailVerified;

        public String normalizedEmail() {
            if (email == null) {
                return "";
            }
            return email.trim().toLowerCase(Locale.ROOT);
        }
    }
}
