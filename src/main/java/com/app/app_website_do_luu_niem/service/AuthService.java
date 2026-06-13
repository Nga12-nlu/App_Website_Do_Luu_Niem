package com.app.app_website_do_luu_niem.service;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.app.app_website_do_luu_niem.dao.PasswordResetTokenDao;
import com.app.app_website_do_luu_niem.dao.UserDao;
import com.app.app_website_do_luu_niem.dao.impl.PasswordResetTokenDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.UserDaoImpl;
import com.app.app_website_do_luu_niem.model.PasswordResetToken;
import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.service.GoogleOAuthService.GoogleUserInfo;
import com.app.app_website_do_luu_niem.util.AppUrlHelper;
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

    public Optional<User> findByEmailNormalized(String email) {
        String em = normalizeEmail(email);
        if (em.isBlank()) {
            return Optional.empty();
        }
        return userDao.findByEmail(em);
    }

    public Optional<User> findById(int id) {
        return userDao.findById(id);
    }

    public static class LoginResult {
        private User user;
        private String error; // "banned", "unverified", "locked", "wrong_password", "not_found", "no_local_password", "invalid_input"
        private int remainingLockMinutes;

        public User getUser() { return user; }
        public String getError() { return error; }
        public int getRemainingLockMinutes() { return remainingLockMinutes; }
    }

    public LoginResult login(String identifier, String password) {
        LoginResult result = new LoginResult();
        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            result.error = "invalid_input";
            return result;
        }

        Optional<User> opt = userDao.findByEmailOrUsernameOrPhone(identifier);
        if (opt.isEmpty()) {
            result.error = "not_found";
            return result;
        }

        User user = opt.get();

        // Check if locked
        if (user.getLockTime() != null && user.getLockTime().isAfter(LocalDateTime.now())) {
            result.user = user;
            result.error = "locked";
            result.remainingLockMinutes = (int) java.time.Duration.between(LocalDateTime.now(), user.getLockTime()).toMinutes() + 1;
            return result;
        }

        // Check if Banned or Inactive
        if ("BANNED".equalsIgnoreCase(user.getStatus()) || !user.isActive()) {
            result.error = "banned";
            return result;
        }

        // Check if Unverified
        if ("UNVERIFIED".equalsIgnoreCase(user.getStatus())) {
            result.user = user;
            result.error = "unverified";
            // Resend OTP
            sendRegistrationOtp(user);
            return result;
        }

        if (!user.hasLocalPassword()) {
            result.error = "no_local_password";
            return result;
        }

        if (verifyPassword(password, user.getPasswordHash())) {
            // Reset failed login count
            if (user.getFailedLogins() > 0 || user.getLockTime() != null) {
                userDao.resetFailedLogins(user.getId());
                user.setFailedLogins(0);
                user.setLockTime(null);
            }
            result.user = user;
            return result; // Success (error is null)
        } else {
            // Increment failed login count
            userDao.incrementFailedLogins(user.getId());
            int currentFailed = user.getFailedLogins() + 1;
            if (currentFailed >= 5) {
                LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(15);
                userDao.lockUser(user.getId(), lockUntil);
                result.error = "locked";
                result.remainingLockMinutes = 15;
            } else {
                result.error = "wrong_password";
            }
            return result;
        }
    }

    private static boolean verifyPassword(String rawPassword, String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, passwordHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
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
            return (u.isActive() && !"BANNED".equalsIgnoreCase(u.getStatus())) ? Optional.empty() : Optional.of("inactive");
        }

        Optional<User> byEmail = userDao.findByEmail(email);
        if (byEmail.isPresent()) {
            User existing = byEmail.get();
            if (!existing.isActive() || "BANNED".equalsIgnoreCase(existing.getStatus())) {
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
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        userDao.save(user);
        return Optional.empty();
    }

    public Optional<User> findActiveUserByGoogleId(String googleId) {
        return userDao.findByGoogleId(googleId).filter(u -> u.isActive() && !"BANNED".equalsIgnoreCase(u.getStatus()));
    }

    public String register(String fullName, String email, String username, String phone, String rawPassword) {
        String em = normalizeEmail(email);
        
        // Check Banned Email
        Optional<User> existingOpt = userDao.findByEmail(em);
        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();
            if ("BANNED".equalsIgnoreCase(existing.getStatus())) {
                return "Email này đã bị khóa do vi phạm chính sách và không thể đăng ký mới.";
            }
            return "Email đã được sử dụng, vui lòng chọn email khác.";
        }

        if (username != null && !username.isBlank()) {
            if (userDao.usernameExists(username)) {
                return "Username đã được sử dụng, vui lòng chọn username khác.";
            }
        }
        if (phone != null && !phone.isBlank()) {
            if (userDao.phoneExists(phone)) {
                return "Số điện thoại đã được sử dụng, vui lòng chọn số điện thoại khác.";
            }
        }

        if (!PASSWORD_STRONG.matcher(rawPassword).matches()) {
            return "Mật khẩu phải từ 8–128 ký tự, gồm ít nhất một chữ cái và một chữ số.";
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(em);
        user.setUsername(username != null && !username.isBlank() ? username.trim() : null);
        user.setPhone(phone != null && !phone.isBlank() ? phone.trim() : null);
        user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        user.setRole("CUSTOMER");
        user.setActive(true);
        user.setStatus("UNVERIFIED"); // Require OTP
        user.setCreatedAt(LocalDateTime.now());
        
        userDao.save(user);
        
        // Generate and Send OTP
        sendRegistrationOtp(user);
        
        return null; // Success
    }

    public void sendRegistrationOtp(User user) {
        // Generate 6 digit code
        java.util.Random rand = new java.util.Random();
        String otp = String.format("%06d", rand.nextInt(1000000));
        LocalDateTime expires = LocalDateTime.now().plusMinutes(10);
        userDao.saveOtpCode(user.getId(), otp, expires);

        try {
            if (AppConfig.isMailEnabled()) {
                String html = """
                        <!DOCTYPE html>
                        <html><head><meta charset="UTF-8"></head><body style="font-family:Segoe UI,sans-serif;padding:20px;background:#f6f7fb;">
                        <div style="background:#fff;border-radius:8px;padding:24px;box-shadow:0 2px 10px rgba(0,0,0,0.05);max-width:500px;margin:auto;">
                          <h2 style="color:#2c5f2d;margin:0 0 16px;">Xác thực tài khoản của bạn</h2>
                          <p>Xin chào <strong>%s</strong>,</p>
                          <p>Cảm ơn bạn đã đăng ký tài khoản tại Souvenir Shop. Vui lòng sử dụng mã OTP dưới đây để hoàn tất việc đăng ký tài khoản (mã hết hạn sau 10 phút):</p>
                          <div style="background:#f4f9f4;border:1px dashed #2c5f2d;color:#2c5f2d;font-size:24px;font-weight:bold;text-align:center;padding:12px;margin:20px 0;letter-spacing:4px;">
                            %s
                          </div>
                          <p style="color:#666;font-size:13px;">Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>
                        </div>
                        </body></html>
                        """.formatted(user.getFullName(), otp);
                new MailService().sendHtml(user.getEmail(), user.getFullName(), "Mã xác thực đăng ký tài khoản Souvenir Shop", html);
            } else {
                System.out.println("[Tomcat Log - mail.enabled=false] Mã OTP đăng ký của " + user.getEmail() + " là: " + otp);
            }
        } catch (Exception e) {
            System.err.println("Không gửi được OTP email: " + e.getMessage());
        }
    }

    public boolean verifyRegistrationOtp(int userId, String inputOtp) {
        if (inputOtp == null || inputOtp.isBlank()) {
            return false;
        }
        Optional<String> realOtp = userDao.getOtpCode(userId);
        if (realOtp.isPresent() && realOtp.get().equals(inputOtp.trim())) {
            userDao.updateStatus(userId, "ACTIVE");
            userDao.clearOtpCode(userId);
            return true;
        }
        return false;
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
        return AppUrlHelper.absolutePath(req, "/reset-password?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8));
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

    /** VNPay sandbox thường không chấp nhận IPv6 loopback. */
    public static String clientIpForVnpay(HttpServletRequest req) {
        String ip = clientIp(req);
        if (ip == null || ip.isBlank()) {
            return "127.0.0.1";
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }
}
