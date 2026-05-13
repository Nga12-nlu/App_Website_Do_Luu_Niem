package com.app.app_website_do_luu_niem.controller.shop;

import com.app.app_website_do_luu_niem.dao.ProductDao;
import com.app.app_website_do_luu_niem.dao.ProductVariantDao;
import com.app.app_website_do_luu_niem.dao.impl.ProductDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.ProductVariantDaoImpl;
import com.app.app_website_do_luu_niem.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "productDetailServlet", urlPatterns = "/product")
public class ProductDetailServlet extends HttpServlet {

    private final ProductDao productDao = new ProductDaoImpl();
    private final ProductVariantDao variantDao = new ProductVariantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        Optional<Product> opt = productDao.findById(id);
        if (opt.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        Product product = opt.get();
        req.setAttribute("product", product);
        var variants = variantDao.findByProductId(id);
        req.setAttribute("variants", variants);
        variants.stream()
                .filter(v -> v.isActive() && v.getStock() > 0)
                .findFirst()
                .ifPresent(v -> req.setAttribute("defaultVariant", v));

        if (product.getCategory() != null) {
            var relatedProducts = productDao.findAll(1, 4, null, product.getCategory().getId(), null);
            relatedProducts.removeIf(p -> p.getId() == product.getId());
            req.setAttribute("relatedProducts", relatedProducts);
        }

        req.getRequestDispatcher("/WEB-INF/views/shop/product-detail.jsp").forward(req, resp);
    }
}
