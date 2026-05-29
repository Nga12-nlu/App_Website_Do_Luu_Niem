package com.app.app_website_do_luu_niem.service;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.app.app_website_do_luu_niem.dao.PasswordResetTokenDao;
import com.app.app_website_do_luu_niem.dao.UserDao;
import com.app.app_website_do_luu_niem.dao.impl.PasswordResetTokenDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.UserDaoImpl;
import com.app.app_website_do_luu_niem.model.PasswordResetToken;
import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.service.GoogleOAuthService.GoogleUserInfo;
import com.app.app_website_do_luu_niem.util.TokenHasher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class AuthService {

    public static final String SESSION_PWD_RESET_CSRF = "pwdResetCsrf";

    private static final Pattern PASSWORD_STRONG = Pattern.compile("^(?=.*\\p{L})(?=.*\\d).{8,128}$");

    private final UserDao userDao = new UserDaoImpl();
    private final PasswordResetTokenDao tokenDao = new PasswordResetTokenDaoImpl();

    public boolean isPasswordStrongEnough(String raw) {
        return raw != null && PASSWORD_STRONG.matcher(raw).matches();
    }

    public Optional<User> login(String email, String password) {
        return userDao.findByEmail(normalizeEmail(email))
                .filter(User::isActive)
                .filter(User::hasLocalPassword)
                .filter(u -> BCrypt.checkpw(password, u.getPasswordHash()));
    }

    /**
     * Đăng nhập hoặc tạo tài khoản từ thông tin Google.
     *
     * @return empty nếu thất bại; nếu không rỗng là mã lỗi: inactive, email_linked_other, invalid
     */
    public Optional<String> loginOrRegisterWithGoogle(GoogleUserInfo profile) {
        if (profile == null || profile.sub == null || profile.sub.isBlank()) {
            return Optional.of("invalid");
        }
        String googleId = profile.sub.trim();
        String email = profile.normalizedEmail();
        if (email.isBlank()) {
            return Optional.of("invalid");
        }

        Optional<User> byGoogle = userDao.findByGoogleId(googleId);
        if (byGoogle.isPresent()) {
            User u = byGoogle.get();
            return u.isActive() ? Optional.empty() : Optional.of("inactive");
        }

        Optional<User> byEmail = userDao.findByEmail(email);
        if (byEmail.isPresent()) {
            User existing = byEmail.get();
            if (!existing.isActive()) {
                return Optional.of("inactive");
            }
            if (existing.getGoogleId() != null && !existing.getGoogleId().isBlank()
                    && !existing.getGoogleId().equals(googleId)) {
                return Optional.of("email_linked_other");
            }
            if (existing.getGoogleId() == null || existing.getGoogleId().isBlank()) {
                userDao.linkGoogleAccount(existing.getId(), googleId);
                existing.setGoogleId(googleId);
            }
            return Optional.empty();
        }

        String displayName = profile.name != null && !profile.name.isBlank() ? profile.name.trim() : email;
        User user = new User();
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setFullName(displayName);
        user.setPasswordHash(null);
        user.setRole("CUSTOMER");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userDao.save(user);
        return Optional.empty();
    }

    public Optional<User> findActiveUserByGoogleId(String googleId) {
        return userDao.findByGoogleId(googleId).filter(User::isActive);
    }

    public boolean register(String fullName, String email, String rawPassword) {
        String em = normalizeEmail(email);
        if (userDao.findByEmail(em).isPresent()) {
            return false;
        }
        if (!PASSWORD_STRONG.matcher(rawPassword).matches()) {
            return false;
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(em);
        user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        user.setRole("CUSTOMER");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userDao.save(user);
        return true;
    }

    /**
     * Tạo token và gửi email (hoặc ghi log nếu {@code mail.enabled=false}).
     * Không tiết lộ email có tồn tại hay không — servlet luôn hiển thị cùng thông báo.
     */
    public void requestPasswordReset(String email, String clientIp, HttpServletRequest req, ServletContext ctx) {
        tokenDao.deleteExpired();
        Optional<User> opt = userDao.findByEmail(normalizeEmail(email));
        if (opt.isEmpty() || !opt.get().isActive()) {
            return;
        }
        User user = opt.get();
        if (tokenDao.countCreatedSince(user.getId(), LocalDateTime.now().minusHours(1)) >= 5) {
            ctx.log("Password reset rate limited for user id=" + user.getId());
            return;
        }

        tokenDao.invalidateUnusedForUser(user.getId());
        String rawToken = TokenHasher.newRawToken();
        String tokenHash = TokenHasher.sha256Hex(rawToken);
        LocalDateTime expires = LocalDateTime.now().plusHours(1);
        long tokenRowId = tokenDao.insert(user.getId(), tokenHash, expires, truncateIp(clientIp));

        String resetLink = buildResetUrl(req, rawToken);
        try {
            if (AppConfig.isMailEnabled()) {
                new MailService().sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
            } else {
                ctx.log("[mail.enabled=false] Link đặt lại mật khẩu cho " + user.getEmail() + ": " + resetLink);
            }
        } catch (Exception e) {
            if (tokenRowId > 0) {
                tokenDao.deleteById(tokenRowId);
            }
            ctx.log("Không gửi được email đặt lại mật khẩu (token đã hủy): " + e.getMessage(), e);
        }
    }

    /**
     * @return empty nếu thành công; nếu không rỗng là mã lỗi cho servlet.
     */
    public Optional<String> completePasswordReset(String rawToken, String newPassword, String confirmPassword,
                                                   String sessionCsrf, String formCsrf) {
        if (formCsrf == null || sessionCsrf == null || !sessionCsrf.equals(formCsrf)) {
            return Optional.of("csrf");
        }
        if (rawToken == null || !rawToken.matches("[0-9a-f]{64}")) {
            return Optional.of("token");
        }
        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            return Optional.of("mismatch");
        }
        if (!PASSWORD_STRONG.matcher(newPassword).matches()) {
            return Optional.of("weak");
        }

        String hash = TokenHasher.sha256Hex(rawToken);
        Optional<PasswordResetToken> row = tokenDao.findValidByTokenHash(hash);
        if (row.isEmpty()) {
            return Optional.of("token");
        }

        PasswordResetToken t = row.get();
        String bcrypt = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        userDao.updatePasswordHash(t.getUserId(), bcrypt);
        tokenDao.markUsed(t.getId());
        return Optional.empty();
    }

    public Optional<PasswordResetToken> peekValidToken(String rawToken) {
        if (rawToken == null || !rawToken.matches("[0-9a-f]{64}")) {
            return Optional.empty();
        }
        return tokenDao.findValidByTokenHash(TokenHasher.sha256Hex(rawToken));
    }

    private String buildResetUrl(HttpServletRequest req, String rawToken) {
        String base = AppConfig.getPublicBaseUrl();
        String ctxPath = req.getContextPath();
        if (base.isEmpty()) {
            int port = req.getServerPort();
            String scheme = req.getScheme();
            String host = req.getServerName();
            boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
            base = scheme + "://" + host + (defaultPort ? "" : ":" + port) + ctxPath;
        } else if (shouldAppendContextPath(base, ctxPath)) {
            // Tránh lỗi 404 khi cấu hình base URL không chứa đúng context path local hiện tại.
            base = base + ctxPath;
        }
        return base + "/reset-password?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
    }

    private boolean shouldAppendContextPath(String baseUrl, String contextPath) {
        if (contextPath == null || contextPath.isBlank() || "/".equals(contextPath)) {
            return false;
        }
        return !(baseUrl.endsWith(contextPath) || baseUrl.contains(contextPath + "/"));
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String truncateIp(String ip) {
        if (ip == null) {
            return null;
        }
        return ip.length() > 45 ? ip.substring(0, 45) : ip;
    }

    public static String clientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return truncateIp(xf.split(",")[0].trim());
        }
        return truncateIp(req.getRemoteAddr());
    }
}
