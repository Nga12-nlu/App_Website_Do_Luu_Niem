package com.app.app_website_do_luu_niem.service;

import com.app.app_website_do_luu_niem.config.AppConfig;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Gửi email HTML qua SMTP (cấu hình trong {@code db.properties}).
 */
public class MailService {

    public void sendHtml(String toAddress, String toPersonalName, String subject, String htmlBody) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", AppConfig.getMailHost());
        props.put("mail.smtp.port", String.valueOf(AppConfig.getMailPort()));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", AppConfig.getMailStartTls() ? "true" : "false");
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(AppConfig.getMailUsername(), AppConfig.getMailPassword());
            }
        });

        MimeMessage message = new MimeMessage(session);
        String fromAddr = AppConfig.getMailFromAddress();
        if (fromAddr.isEmpty()) {
            fromAddr = AppConfig.getMailUsername();
        }
        message.setFrom(new InternetAddress(fromAddr, AppConfig.getMailFromName(), StandardCharsets.UTF_8.name()));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress, toPersonalName != null ? toPersonalName : "", StandardCharsets.UTF_8.name()));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setContent(htmlBody, "text/html; charset=UTF-8");

        Transport.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String recipientName, String resetLink) throws Exception {
        String safeName = recipientName != null && !recipientName.isBlank() ? recipientName : "bạn";
        String subject = "Đặt lại mật khẩu — Souvenir Shop";
        String html = """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8"></head><body style="font-family:Segoe UI,Roboto,sans-serif;background:#f6f7fb;padding:24px;">
                <table width="100%%" cellpadding="0" cellspacing="0"><tr><td align="center">
                <table width="560" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:12px;padding:32px;box-shadow:0 4px 24px rgba(0,0,0,.06);">
                <tr><td>
                  <h1 style="color:#1e4620;font-size:22px;margin:0 0 12px;">Đặt lại mật khẩu</h1>
                  <p style="color:#2d3436;line-height:1.6;margin:0 0 20px;">Xin chào <strong>%s</strong>,</p>
                  <p style="color:#2d3436;line-height:1.6;margin:0 0 24px;">Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản Souvenir Shop của bạn. Nhấn nút bên dưới để chọn mật khẩu mới (liên kết hết hạn sau 60 phút, chỉ dùng một lần).</p>
                  <p style="text-align:center;margin:28px 0;">
                    <a href="%s" style="background:#2c5f2d;color:#fff;text-decoration:none;padding:14px 28px;border-radius:8px;font-weight:600;display:inline-block;">Đặt lại mật khẩu</a>
                  </p>
                  <p style="color:#636e72;font-size:13px;line-height:1.5;margin:0 0 12px;">Nếu nút không hoạt động, sao chép liên kết sau vào trình duyệt:</p>
                  <p style="word-break:break-all;font-size:12px;color:#2d3436;background:#faf8f5;padding:12px;border-radius:8px;">%s</p>
                  <p style="color:#636e72;font-size:13px;line-height:1.5;margin-top:24px;">Nếu bạn không yêu cầu thao tác này, hãy bỏ qua email — mật khẩu của bạn không thay đổi.</p>
                  <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
                  <p style="color:#b2bec3;font-size:12px;margin:0;">Souvenir Shop · Email tự động, vui lòng không trả lời.</p>
                </td></tr></table></td></tr></table></body></html>
                """.formatted(escapeHtml(safeName), resetLink, resetLink);
        sendHtml(toEmail, recipientName, subject, html);
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
