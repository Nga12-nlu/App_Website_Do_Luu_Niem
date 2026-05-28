package com.app.app_website_do_luu_niem.controller.admin;

import com.app.app_website_do_luu_niem.dao.StaticContentDao;
import com.app.app_website_do_luu_niem.dao.impl.StaticContentDaoImpl;
import com.app.app_website_do_luu_niem.model.StaticContent;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "adminStaticContentServlet", urlPatterns = "/admin/contents")
public class AdminStaticContentServlet extends HttpServlet {

    private static final int MAX_VALUE_LENGTH = 5000;
    private final StaticContentDao staticContentDao = new StaticContentDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }
        switch (action) {
            case "edit" -> showEditForm(req, resp);
            default -> showList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idRaw = req.getParameter("id");
        if (idRaw == null || idRaw.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/admin/contents?msg=error");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(idRaw);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/contents?msg=error");
            return;
        }
        Optional<StaticContent> itemOpt = staticContentDao.findById(id);
        if (itemOpt.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/admin/contents?msg=notfound");
            return;
        }

        StaticContent item = itemOpt.get();
        String value = req.getParameter("value");
        if (value == null) {
            value = "";
        }
        value = value.trim();
        if (value.length() > MAX_VALUE_LENGTH) {
            resp.sendRedirect(req.getContextPath() + "/admin/contents?action=edit&id=" + id + "&error=length");
            return;
        }

        boolean active = "on".equalsIgnoreCase(req.getParameter("active"));
        item.setValue(value);
        item.setActive(active);
        staticContentDao.update(item);
        resp.sendRedirect(req.getContextPath() + "/admin/contents?msg=updated");
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        int pageSize = parseIntOrDefault(req.getParameter("pageSize"), 15);
        String search = blankToNull(req.getParameter("search"));
        String groupName = blankToNull(req.getParameter("group"));

        List<StaticContent> items = staticContentDao.findAll(page, pageSize, search, groupName);
        int totalItems = staticContentDao.countAll(search, groupName);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));

        req.setAttribute("contents", items);
        req.setAttribute("groups", staticContentDao.findGroups());
        req.setAttribute("currentPage", page);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalItems", totalItems);
        req.setAttribute("search", search != null ? search : "");
        req.setAttribute("group", groupName != null ? groupName : "");
        req.setAttribute("statTotal", staticContentDao.countAllItems());
        req.setAttribute("statActive", staticContentDao.countActiveItems());
        req.setAttribute("statInactive", staticContentDao.countAllItems() - staticContentDao.countActiveItems());
        applyFlashMessage(req);
        req.getRequestDispatcher("/WEB-INF/views/admin/static-contents.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idRaw = req.getParameter("id");
        if (idRaw == null || idRaw.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/admin/contents");
            return;
        }
        try {
            int id = Integer.parseInt(idRaw);
            Optional<StaticContent> itemOpt = staticContentDao.findById(id);
            if (itemOpt.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/admin/contents?msg=notfound");
                return;
            }
            req.setAttribute("contentItem", itemOpt.get());
            req.setAttribute("formError", req.getParameter("error"));
            req.getRequestDispatcher("/WEB-INF/views/admin/static-content-form.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/contents");
        }
    }

    private void applyFlashMessage(HttpServletRequest req) {
        String msg = req.getParameter("msg");
        if (msg == null || msg.isBlank()) {
            return;
        }
        String text = switch (msg) {
            case "updated" -> "Đã cập nhật nội dung tĩnh.";
            case "notfound" -> "Không tìm thấy nội dung.";
            default -> "Có lỗi xảy ra. Vui lòng thử lại.";
        };
        String type = "updated".equals(msg) ? "success" : "danger";
        req.setAttribute("flashMessage", text);
        req.setAttribute("flashType", type);
    }

    private int parseIntOrDefault(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            int n = Integer.parseInt(value);
            return n > 0 ? n : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
