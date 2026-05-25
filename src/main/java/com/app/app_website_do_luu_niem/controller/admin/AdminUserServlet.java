package com.app.app_website_do_luu_niem.controller.admin;

import com.app.app_website_do_luu_niem.dao.OrderDao;
import com.app.app_website_do_luu_niem.dao.UserDao;
import com.app.app_website_do_luu_niem.dao.impl.OrderDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.UserDaoImpl;
import com.app.app_website_do_luu_niem.model.Order;
import com.app.app_website_do_luu_niem.model.User;
import com.app.app_website_do_luu_niem.model.UserAdminRow;
import com.app.app_website_do_luu_niem.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@WebServlet(name = "adminUserServlet", urlPatterns = "/admin/users")
public class AdminUserServlet extends HttpServlet {

    private final UserDao userDao = new UserDaoImpl();
    private final OrderDao orderDao = new OrderDaoImpl();
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }
        switch (action) {
            case "create" -> showForm(req, resp, new User());
            case "edit" -> showEditForm(req, resp);
            case "detail" -> showDetail(req, resp);
            case "toggle-active" -> handleToggleActive(req, resp);
            case "delete" -> handleDelete(req, resp);
            default -> showList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "save";
        }
        switch (action) {
            case "reset-password" -> handleResetPassword(req, resp);
            default -> handleSave(req, resp);
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        int pageSize = parseIntOrDefault(req.getParameter("pageSize"), 10);
        String search = blankToNull(req.getParameter("search"));
        String role = blankToNull(req.getParameter("role"));
        String activeFilter = req.getParameter("active");
        String sortBy = blankToNull(req.getParameter("sortBy"));
        String sortOrder = blankToNull(req.getParameter("sortOrder"));
        if (sortBy == null) {
            sortBy = "created_at";
        }
        if (sortOrder == null) {
            sortOrder = "DESC";
        }

        Boolean activeOnly = parseActiveFilter(activeFilter);

        List<UserAdminRow> users = userDao.findAllAdminRows(page, pageSize, search, role, activeOnly, sortBy, sortOrder);
        int totalUsers = userDao.countAll(search, role, activeOnly);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalUsers / pageSize));

        req.setAttribute("users", users);
        req.setAttribute("currentPage", page);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("search", search != null ? search : "");
        req.setAttribute("role", role != null ? role : "");
        req.setAttribute("activeFilter", activeFilter != null ? activeFilter : "");
        req.setAttribute("sortBy", sortBy);
        req.setAttribute("sortOrder", sortOrder);

        req.setAttribute("statTotal", userDao.countAll(null, null, null));
        req.setAttribute("statAdmins", userDao.countAdmins());
        req.setAttribute("statCustomers", userDao.countByRole("CUSTOMER"));
        req.setAttribute("statActive", userDao.countActive());
        req.setAttribute("statInactive", userDao.countInactive());

        applyFlashMessage(req);
        req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        req.setAttribute("user", user);
        req.setAttribute("isCreate", user.getId() == 0);
        req.setAttribute("formError", req.getParameter("error"));
        req.getRequestDispatcher("/WEB-INF/views/admin/user-form.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<User> opt = findUserFromParam(req, resp);
        if (opt.isEmpty()) {
            return;
        }
        showForm(req, resp, opt.get());
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<User> opt = findUserFromParam(req, resp);
        if (opt.isEmpty()) {
            return;
        }
        User user = opt.get();
        long orderCount = userDao.countOrdersByUserId(user.getId());
        List<Order> recentOrders = orderDao.findByUserId(user.getId());
        if (recentOrders.size() > 5) {
            recentOrders = recentOrders.subList(0, 5);
        }

        User currentAdmin = getCurrentUser(req);
        boolean isSelf = currentAdmin != null && currentAdmin.getId() == user.getId();
        boolean isLastAdmin = user.isAdminRole() && userDao.countAdmins() <= 1;

        req.setAttribute("user", user);
        req.setAttribute("orderCount", orderCount);
        req.setAttribute("recentOrders", recentOrders);
        req.setAttribute("isSelf", isSelf);
        req.setAttribute("isLastAdmin", isLastAdmin);
        applyFlashMessage(req);
        req.getRequestDispatcher("/WEB-INF/views/admin/user-detail.jsp").forward(req, resp);
    }

    private void handleSave(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        User user;
        boolean isCreate = idParam == null || idParam.isBlank();
        if (isCreate) {
            user = new User();
            user.setCreatedAt(LocalDateTime.now());
            user.setActive(true);
        } else {
            int id = Integer.parseInt(idParam);
            user = userDao.findById(id).orElse(null);
            if (user == null) {
                redirectWithMsg(req, resp, "notfound");
                return;
            }
        }

        String fullName = trim(req.getParameter("fullName"));
        String email = normalizeEmail(req.getParameter("email"));
        String role = normalizeRole(req.getParameter("role"));
        String password = req.getParameter("password");
        String activeParam = req.getParameter("active");

        if (fullName.isBlank() || email.isBlank()) {
            redirectFormError(req, resp, user, isCreate, "required");
            return;
        }

        if (userDao.emailExistsOtherThan(email, user.getId())) {
            redirectFormError(req, resp, user, isCreate, "email");
            return;
        }

        User currentAdmin = getCurrentUser(req);
        boolean editingSelf = currentAdmin != null && !isCreate && currentAdmin.getId() == user.getId();

        if (isCreate) {
            if (password == null || password.isBlank()) {
                redirectFormError(req, resp, user, true, "password");
                return;
            }
            if (!authService.isPasswordStrongEnough(password)) {
                redirectFormError(req, resp, user, true, "weak");
                return;
            }
            user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        } else if (password != null && !password.isBlank()) {
            if (!authService.isPasswordStrongEnough(password)) {
                redirectFormError(req, resp, user, false, "weak");
                return;
            }
            user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        }

        user.setFullName(fullName);
        user.setEmail(email);

        if (editingSelf) {
            user.setRole("ADMIN");
            user.setActive(true);
        } else {
            if (!isCreate && user.isAdminRole() && userDao.countAdmins() <= 1 && !"ADMIN".equals(role)) {
                redirectFormError(req, resp, user, false, "lastadmin");
                return;
            }
            user.setRole(role);
            user.setActive("on".equals(activeParam) || "true".equalsIgnoreCase(activeParam));
        }

        if (isCreate) {
            userDao.save(user);
            redirectWithMsg(req, resp, "created");
        } else {
            userDao.update(user);
            redirectWithMsg(req, resp, "updated");
        }
    }

    private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String password = req.getParameter("password");
        if (idParam == null || password == null || password.isBlank()) {
            redirectWithMsg(req, resp, "error");
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            if (!authService.isPasswordStrongEnough(password)) {
                resp.sendRedirect(req.getContextPath() + "/admin/users?action=detail&id=" + id + "&msg=weak");
                return;
            }
            userDao.updatePasswordHash(id, BCrypt.hashpw(password, BCrypt.gensalt()));
            resp.sendRedirect(req.getContextPath() + "/admin/users?action=detail&id=" + id + "&msg=pwdreset");
        } catch (NumberFormatException e) {
            redirectWithMsg(req, resp, "error");
        }
    }

    private void handleToggleActive(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            redirectWithMsg(req, resp, "error");
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            Optional<User> opt = userDao.findById(id);
            if (opt.isEmpty()) {
                redirectWithMsg(req, resp, "notfound");
                return;
            }
            User user = opt.get();
            User currentAdmin = getCurrentUser(req);
            if (currentAdmin != null && currentAdmin.getId() == user.getId()) {
                redirectWithMsg(req, resp, "self");
                return;
            }
            if (user.isAdminRole() && user.isActive() && userDao.countAdmins() <= 1) {
                redirectWithMsg(req, resp, "lastadmin");
                return;
            }
            user.setActive(!user.isActive());
            userDao.update(user);
            redirectWithMsg(req, resp, user.isActive() ? "activated" : "locked");
        } catch (NumberFormatException e) {
            redirectWithMsg(req, resp, "error");
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            redirectWithMsg(req, resp, "error");
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            Optional<User> opt = userDao.findById(id);
            if (opt.isEmpty()) {
                redirectWithMsg(req, resp, "notfound");
                return;
            }
            User user = opt.get();
            User currentAdmin = getCurrentUser(req);
            if (currentAdmin != null && currentAdmin.getId() == user.getId()) {
                redirectWithMsg(req, resp, "self");
                return;
            }
            if (user.isAdminRole()) {
                redirectWithMsg(req, resp, "admindelete");
                return;
            }
            if (userDao.countOrdersByUserId(id) > 0) {
                redirectWithMsg(req, resp, "hasorders");
                return;
            }
            userDao.delete(id);
            redirectWithMsg(req, resp, "deleted");
        } catch (NumberFormatException e) {
            redirectWithMsg(req, resp, "error");
        }
    }

    private Optional<User> findUserFromParam(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return Optional.empty();
        }
        try {
            int id = Integer.parseInt(idParam);
            Optional<User> opt = userDao.findById(id);
            if (opt.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/admin/users?msg=notfound");
                return Optional.empty();
            }
            return opt;
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return Optional.empty();
        }
    }

    private void redirectFormError(HttpServletRequest req, HttpServletResponse resp, User user,
                                   boolean isCreate, String error) throws IOException {
        if (isCreate) {
            resp.sendRedirect(req.getContextPath() + "/admin/users?action=create&error=" + error);
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/users?action=edit&id=" + user.getId() + "&error=" + error);
        }
    }

    private void redirectWithMsg(HttpServletRequest req, HttpServletResponse resp, String msg) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin/users?msg=" + msg);
    }

    private void applyFlashMessage(HttpServletRequest req) {
        String msg = req.getParameter("msg");
        if (msg == null || msg.isBlank()) {
            return;
        }
        String text = switch (msg) {
            case "created" -> "Đã tạo tài khoản mới thành công.";
            case "updated" -> "Đã cập nhật thông tin người dùng.";
            case "deleted" -> "Đã xóa người dùng.";
            case "activated" -> "Đã kích hoạt tài khoản.";
            case "locked" -> "Đã khóa tài khoản.";
            case "pwdreset" -> "Đã đặt lại mật khẩu.";
            case "weak" -> "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ và số.";
            case "self" -> "Không thể thao tác trên chính tài khoản đang đăng nhập.";
            case "lastadmin" -> "Không thể hạ quyền hoặc khóa quản trị viên cuối cùng.";
            case "hasorders" -> "Không thể xóa người dùng đã có đơn hàng.";
            case "admindelete" -> "Không thể xóa tài khoản quản trị viên.";
            case "notfound" -> "Không tìm thấy người dùng.";
            default -> "Đã xảy ra lỗi. Vui lòng thử lại.";
        };
        String type = msg.matches("created|updated|deleted|activated|locked|pwdreset") ? "success"
                : "weak".equals(msg) ? "warning" : "danger";
        req.setAttribute("flashMessage", text);
        req.setAttribute("flashType", type);
    }

    private User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }

    private Boolean parseActiveFilter(String activeFilter) {
        if (activeFilter == null || activeFilter.isBlank()) {
            return null;
        }
        return switch (activeFilter) {
            case "1", "active" -> true;
            case "0", "inactive" -> false;
            default -> null;
        };
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRole(String role) {
        if (role != null && "ADMIN".equalsIgnoreCase(role.trim())) {
            return "ADMIN";
        }
        return "CUSTOMER";
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
