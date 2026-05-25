package com.app.app_website_do_luu_niem.controller.admin;

import com.app.app_website_do_luu_niem.dao.CategoryDao;
import com.app.app_website_do_luu_niem.dao.ProductDao;
import com.app.app_website_do_luu_niem.dao.impl.CategoryDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.ProductDaoImpl;
import com.app.app_website_do_luu_niem.model.Category;
import com.app.app_website_do_luu_niem.model.CategoryAdminRow;
import com.app.app_website_do_luu_niem.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "adminCategoryServlet", urlPatterns = "/admin/categories")
public class AdminCategoryServlet extends HttpServlet {

    private static final int MAX_NAME_LEN = 255;
    private static final int MAX_DESC_LEN = 500;

    private final CategoryDao categoryDao = new CategoryDaoImpl();
    private final ProductDao productDao = new ProductDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }
        switch (action) {
            case "create" -> showForm(req, resp, new Category(), true);
            case "edit" -> showEditForm(req, resp);
            case "detail" -> showDetail(req, resp);
            case "delete" -> handleDelete(req, resp);
            default -> showList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleSave(req, resp);
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        int pageSize = parseIntOrDefault(req.getParameter("pageSize"), 10);
        String search = blankToNull(req.getParameter("search"));
        String productFilter = blankToNull(req.getParameter("productFilter"));
        String sortBy = blankToNull(req.getParameter("sortBy"));
        String sortOrder = blankToNull(req.getParameter("sortOrder"));
        if (sortBy == null) {
            sortBy = "name";
        }
        if (sortOrder == null) {
            sortOrder = "ASC";
        }

        List<CategoryAdminRow> categories = categoryDao.findAllAdminRows(
                page, pageSize, search, productFilter, sortBy, sortOrder);
        int totalCategories = categoryDao.countAll(search, productFilter);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCategories / pageSize));

        req.setAttribute("categories", categories);
        req.setAttribute("currentPage", page);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalCategories", totalCategories);
        req.setAttribute("search", search != null ? search : "");
        req.setAttribute("productFilter", productFilter != null ? productFilter : "");
        req.setAttribute("sortBy", sortBy);
        req.setAttribute("sortOrder", sortOrder);

        req.setAttribute("statTotal", categoryDao.countTotal());
        req.setAttribute("statWithProducts", categoryDao.countWithProducts());
        req.setAttribute("statEmpty", categoryDao.countEmpty());
        req.setAttribute("statTotalProducts", categoryDao.countTotalProducts());

        applyFlashMessage(req);
        req.getRequestDispatcher("/WEB-INF/views/admin/categories.jsp").forward(req, resp);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Category category, boolean isCreate)
            throws ServletException, IOException {
        req.setAttribute("category", category);
        req.setAttribute("isCreate", isCreate);
        req.setAttribute("formError", req.getParameter("error"));
        if (!isCreate && category.getId() > 0) {
            req.setAttribute("productCount", categoryDao.countProductsByCategoryId(category.getId()));
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/category-form.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<Category> opt = findCategoryFromParam(req, resp);
        if (opt.isEmpty()) {
            return;
        }
        showForm(req, resp, opt.get(), false);
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<Category> opt = findCategoryFromParam(req, resp);
        if (opt.isEmpty()) {
            return;
        }
        Category category = opt.get();
        long productCount = categoryDao.countProductsByCategoryId(category.getId());
        List<Product> recentProducts = productDao.findAll(1, 8, null, category.getId(), "name ASC");

        req.setAttribute("category", category);
        req.setAttribute("productCount", productCount);
        req.setAttribute("recentProducts", recentProducts);
        applyFlashMessage(req);
        req.getRequestDispatcher("/WEB-INF/views/admin/category-detail.jsp").forward(req, resp);
    }

    private void handleSave(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        Category category;
        boolean isCreate = idParam == null || idParam.isBlank();
        if (isCreate) {
            category = new Category();
        } else {
            try {
                int id = Integer.parseInt(idParam);
                category = categoryDao.findById(id).orElse(null);
                if (category == null) {
                    redirectWithMsg(req, resp, "notfound");
                    return;
                }
            } catch (NumberFormatException e) {
                redirectWithMsg(req, resp, "error");
                return;
            }
        }

        String name = trim(req.getParameter("name"));
        String description = trim(req.getParameter("description"));
        if (description.isEmpty()) {
            description = null;
        }

        if (name.isBlank()) {
            redirectFormError(req, resp, category, isCreate, "required");
            return;
        }
        if (name.length() > MAX_NAME_LEN) {
            redirectFormError(req, resp, category, isCreate, "namelen");
            return;
        }
        if (description != null && description.length() > MAX_DESC_LEN) {
            redirectFormError(req, resp, category, isCreate, "desclen");
            return;
        }
        if (categoryDao.nameExistsOtherThan(name, category.getId())) {
            redirectFormError(req, resp, category, isCreate, "name");
            return;
        }

        category.setName(name);
        category.setDescription(description);

        if (isCreate) {
            categoryDao.save(category);
            resp.sendRedirect(req.getContextPath() + "/admin/categories?action=detail&id="
                    + category.getId() + "&msg=created");
        } else {
            categoryDao.update(category);
            resp.sendRedirect(req.getContextPath() + "/admin/categories?action=detail&id="
                    + category.getId() + "&msg=updated");
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
            if (categoryDao.findById(id).isEmpty()) {
                redirectWithMsg(req, resp, "notfound");
                return;
            }
            if (categoryDao.countProductsByCategoryId(id) > 0) {
                redirectWithMsg(req, resp, "hasproducts");
                return;
            }
            categoryDao.delete(id);
            redirectWithMsg(req, resp, "deleted");
        } catch (NumberFormatException e) {
            redirectWithMsg(req, resp, "error");
        }
    }

    private Optional<Category> findCategoryFromParam(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/categories");
            return Optional.empty();
        }
        try {
            int id = Integer.parseInt(idParam);
            Optional<Category> opt = categoryDao.findById(id);
            if (opt.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/admin/categories?msg=notfound");
                return Optional.empty();
            }
            return opt;
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/categories");
            return Optional.empty();
        }
    }

    private void redirectFormError(HttpServletRequest req, HttpServletResponse resp, Category category,
                                   boolean isCreate, String error) throws IOException {
        if (isCreate) {
            resp.sendRedirect(req.getContextPath() + "/admin/categories?action=create&error=" + error);
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/categories?action=edit&id="
                    + category.getId() + "&error=" + error);
        }
    }

    private void redirectWithMsg(HttpServletRequest req, HttpServletResponse resp, String msg) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin/categories?msg=" + msg);
    }

    private void applyFlashMessage(HttpServletRequest req) {
        String msg = req.getParameter("msg");
        if (msg == null || msg.isBlank()) {
            return;
        }
        String text = switch (msg) {
            case "created" -> "Đã tạo danh mục mới thành công.";
            case "updated" -> "Đã cập nhật danh mục.";
            case "deleted" -> "Đã xóa danh mục.";
            case "hasproducts" -> "Không thể xóa danh mục đang có sản phẩm. Hãy chuyển hoặc xóa sản phẩm trước.";
            case "notfound" -> "Không tìm thấy danh mục.";
            default -> "Đã xảy ra lỗi. Vui lòng thử lại.";
        };
        String type = msg.matches("created|updated|deleted") ? "success" : "danger";
        req.setAttribute("flashMessage", text);
        req.setAttribute("flashType", type);
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
