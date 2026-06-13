package com.app.app_website_do_luu_niem.util;

import com.app.app_website_do_luu_niem.dao.SystemLogDao;
import com.app.app_website_do_luu_niem.dao.impl.SystemLogDaoImpl;
import com.app.app_website_do_luu_niem.model.SystemLog;
import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;

public class SystemLogHelper {

    private static final SystemLogDao logDao = new SystemLogDaoImpl();

    public static void log(HttpServletRequest req, String action, String target, String details) {
        HttpSession session = req.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            return;
        }
        SystemLog log = new SystemLog();
        log.setUserId(currentUser.getId());
        log.setAction(action);
        log.setTarget(target);
        log.setDetails(details);
        log.setIpAddress(AuthService.clientIp(req));
        log.setCreatedAt(LocalDateTime.now());
        try {
            logDao.save(log);
        } catch (Exception e) {
            req.getServletContext().log("Không thể ghi log hệ thống: " + e.getMessage(), e);
        }
    }
}
