package com.app.app_website_do_luu_niem.controller.admin;

import com.app.app_website_do_luu_niem.dao.CategoryDao;
import com.app.app_website_do_luu_niem.dao.ProductDao;
import com.app.app_website_do_luu_niem.dao.ProductVariantDao;
import com.app.app_website_do_luu_niem.dao.impl.CategoryDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.ProductDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.ProductVariantDaoImpl;
import com.app.app_website_do_luu_niem.model.Category;
import com.app.app_website_do_luu_niem.model.Product;
import com.app.app_website_do_luu_niem.model.ProductVariant;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import com.app.app_website_do_luu_niem.util.SystemLogHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "adminProductServlet", urlPatterns = "/admin/products")
@MultipartConfig
public class AdminProductServlet extends HttpServlet {

    private final ProductDao productDao = new ProductDaoImpl();
    private final CategoryDao categoryDao = new CategoryDaoImpl();
    private final ProductVariantDao variantDao = new ProductVariantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }
        switch (action) {
            case "create" -> showForm(req, resp, new Product());
            case "edit" -> showEditForm(req, resp);
            case "delete" -> handleDelete(req, resp);
            default -> showList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        Product product;
        if (idParam == null || idParam.isBlank()) {
            product = new Product();
            product.setCreatedAt(LocalDateTime.now());
        } else {
            int id = Integer.parseInt(idParam);
            Optional<Product> opt = productDao.findById(id);
            product = opt.orElseGet(Product::new);
        }

        product.setName(req.getParameter("name"));
        product.setDescription(req.getParameter("description"));
        try {
            product.setPrice(new BigDecimal(req.getParameter("price")));
        } catch (Exception e) {
            product.setPrice(BigDecimal.ZERO);
        }
        try {
            product.setStock(Integer.parseInt(req.getParameter("stock")));
        } catch (Exception e) {
            product.setStock(0);
        }

        String categoryIdParam = req.getParameter("categoryId");
        if (categoryIdParam != null && !categoryIdParam.isBlank()) {
            try {
                int categoryId = Integer.parseInt(categoryIdParam);
                Category c = new Category();
                c.setId(categoryId);
                product.setCategory(c);
            } catch (NumberFormatException ignored) {
            }
        }

        Part imagePart = req.getPart("image");
        if (imagePart != null && imagePart.getSize() > 0) {
            try (InputStream is = imagePart.getInputStream();
                 ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] data = new byte[8192];
                int nRead;
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                byte[] imageBytes = buffer.toByteArray();
                String base64 = Base64.getEncoder().encodeToString(imageBytes);
                String contentType = imagePart.getContentType();
                if (contentType == null || contentType.isBlank()) {
                    contentType = "image/*";
                }
                String dataUrl = "data:" + contentType + ";base64," + base64;
                product.setImageUrl(dataUrl);
            }
        }

        if (product.getId() == 0) {
            productDao.save(product);
            SystemLogHelper.log(req, "CREATE_PRODUCT", "PRODUCT", "Thêm mới sản phẩm: " + product.getName() + " (id=" + product.getId() + ")");
        } else {
            productDao.update(product);
            SystemLogHelper.log(req, "UPDATE_PRODUCT", "PRODUCT", "Cập nhật sản phẩm id=" + product.getId() + ", tên=" + product.getName());
        }

        List<ProductVariant> variants = parseVariantsFromRequest(req);
        if (variants.isEmpty()) {
            ProductVariant def = new ProductVariant();
            def.setDisplayName("Mặc định");
            def.setSku(null);
            def.setPrice(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
            def.setStock(product.getStock());
            def.setImageUrl(product.getImageUrl());
            def.setSortOrder(0);
            def.setActive(true);
            variants.add(def);
        } else {
            String baseImg = product.getImageUrl();
            for (ProductVariant v : variants) {
                if (v.getImageUrl() == null || v.getImageUrl().isBlank()) {
                    v.setImageUrl(baseImg);
                }
            }
        }

        variantDao.replaceAllForProduct(product.getId(), variants);

        resp.sendRedirect(req.getContextPath() + "/admin/products");
    }

    private List<ProductVariant> parseVariantsFromRequest(HttpServletRequest req) {
        String[] names = req.getParameterValues("variantDisplayName");
        if (names == null) {
            return List.of();
        }
        String[] skus = req.getParameterValues("variantSku");
        String[] prices = req.getParameterValues("variantPrice");
        String[] stocks = req.getParameterValues("variantStock");
        String[] sorts = req.getParameterValues("variantSortOrder");
        List<ProductVariant> list = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            String name = names[i] != null ? names[i].trim() : "";
            if (name.isEmpty()) {
                continue;
            }
            ProductVariant v = new ProductVariant();
            v.setDisplayName(name);
            if (skus != null && i < skus.length && skus[i] != null && !skus[i].isBlank()) {
                v.setSku(skus[i].trim());
            }
            BigDecimal price = BigDecimal.ZERO;
            if (prices != null && i < prices.length && prices[i] != null && !prices[i].isBlank()) {
                try {
                    price = new BigDecimal(prices[i].trim());
                } catch (Exception ignored) {
                }
            }
            v.setPrice(price);
            int stock = 0;
            if (stocks != null && i < stocks.length && stocks[i] != null && !stocks[i].isBlank()) {
                try {
                    stock = Integer.parseInt(stocks[i].trim());
                } catch (NumberFormatException ignored) {
                }
            }
            v.setStock(stock);
            int sort = i;
            if (sorts != null && i < sorts.length && sorts[i] != null && !sorts[i].isBlank()) {
                try {
                    sort = Integer.parseInt(sorts[i].trim());
                } catch (NumberFormatException ignored) {
                }
            }
            v.setSortOrder(sort);
            v.setActive(true);
            list.add(v);
        }
        return list;
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int page = parseIntOrDefault(req.getParameter("page"), 1);
        int pageSize = parseIntOrDefault(req.getParameter("pageSize"), 10);
        String search = req.getParameter("search");
        String categoryIdParam = req.getParameter("categoryId");
        String sortBy = req.getParameter("sortBy");
        String sortOrder = req.getParameter("sortOrder");

        if (search != null && search.isBlank()) {
            search = null;
        }

        Integer categoryId = null;
        if (categoryIdParam != null && !categoryIdParam.isBlank()) {
            try {
                categoryId = Integer.parseInt(categoryIdParam);
            } catch (NumberFormatException ignored) {
            }
        }

        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "id";
        }
        if (sortOrder == null || sortOrder.isBlank()) {
            sortOrder = "DESC";
        }

        List<Product> products = productDao.findAll(page, pageSize, search, categoryId, sortBy + " " + sortOrder);
        int totalProducts = productDao.countAll(search, categoryId);
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);

        List<Category> categories = categoryDao.findAll();

        req.setAttribute("products", products);
        req.setAttribute("categories", categories);
        req.setAttribute("currentPage", page);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalProducts", totalProducts);
        req.setAttribute("search", search != null ? search : "");
        req.setAttribute("categoryId", categoryId);
        req.setAttribute("sortBy", sortBy);
        req.setAttribute("sortOrder", sortOrder);
        req.getRequestDispatcher("/WEB-INF/views/admin/products.jsp").forward(req, resp);
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

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Product product)
            throws ServletException, IOException {
        req.setAttribute("product", product);
        req.setAttribute("categories", categoryDao.findAll());
        if (product.getId() > 0) {
            req.setAttribute("variants", variantDao.findByProductId(product.getId()));
        } else {
            req.setAttribute("variants", List.of());
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/product-form.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/products");
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            Optional<Product> opt = productDao.findById(id);
            if (opt.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/admin/products");
                return;
            }
            showForm(req, resp, opt.get());
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/products");
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Optional<Product> opt = productDao.findById(id);
                if (opt.isPresent()) {
                    productDao.delete(id);
                    SystemLogHelper.log(req, "DELETE_PRODUCT", "PRODUCT", "Xóa sản phẩm id=" + id + ", tên=" + opt.get().getName());
                }
            } catch (NumberFormatException ignored) {
            }
        }
        resp.sendRedirect(req.getContextPath() + "/admin/products");
    }
}
