package com.app.app_website_do_luu_niem.controller.shop;

import com.app.app_website_do_luu_niem.dao.ProductDao;
import com.app.app_website_do_luu_niem.dao.ProductVariantDao;
import com.app.app_website_do_luu_niem.dao.impl.ProductDaoImpl;
import com.app.app_website_do_luu_niem.dao.impl.ProductVariantDaoImpl;
import com.app.app_website_do_luu_niem.model.CartItem;
import com.app.app_website_do_luu_niem.model.Product;
import com.app.app_website_do_luu_niem.model.ProductVariant;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "cartServlet", urlPatterns = "/cart")
public class CartServlet extends HttpServlet {

    private final ProductDao productDao = new ProductDaoImpl();
    private final ProductVariantDao variantDao = new ProductVariantDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(true);
        List<CartItem> cart = getCart(session);
        req.setAttribute("cartItems", cart);
        req.setAttribute("totalAmount", calculateTotal(cart));
        req.getRequestDispatcher("/WEB-INF/views/shop/cart.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) {
            action = "add";
        }
        HttpSession session = req.getSession(true);
        List<CartItem> cart = getCart(session);

        switch (action) {
            case "add" -> handleAdd(req, cart);
            case "update" -> handleUpdate(req, cart);
            case "remove" -> handleRemove(req, cart);
            default -> {
            }
        }

        session.setAttribute("cart", cart);
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCart(HttpSession session) {
        Object obj = session.getAttribute("cart");
        if (obj instanceof List) {
            return (List<CartItem>) obj;
        }
        List<CartItem> cart = new ArrayList<>();
        session.setAttribute("cart", cart);
        return cart;
    }

    private void handleAdd(HttpServletRequest req, List<CartItem> cart) {
        String idParam = req.getParameter("productId");
        String qtyParam = req.getParameter("quantity");
        int productId;
        int quantity = 1;
        try {
            productId = Integer.parseInt(idParam);
            if (qtyParam != null) {
                quantity = Integer.parseInt(qtyParam);
            }
        } catch (NumberFormatException e) {
            return;
        }
        if (quantity < 1) {
            return;
        }

        Optional<Product> opt = productDao.findById(productId);
        if (opt.isEmpty()) {
            return;
        }
        Product product = opt.get();
        List<ProductVariant> variants = variantDao.findByProductId(productId);
        ProductVariant chosen = null;
        if (!variants.isEmpty()) {
            String vParam = req.getParameter("variantId");
            if (vParam == null || vParam.isBlank()) {
                return;
            }
            int variantId;
            try {
                variantId = Integer.parseInt(vParam);
            } catch (NumberFormatException e) {
                return;
            }
            Optional<ProductVariant> vOpt = variantDao.findById(variantId);
            if (vOpt.isEmpty() || vOpt.get().getProductId() != productId || !vOpt.get().isActive()) {
                return;
            }
            chosen = vOpt.get();
            if (chosen.getStock() < quantity) {
                return;
            }
        }

        for (CartItem item : cart) {
            if (item.getProduct().getId() == productId && sameVariant(item, chosen)) {
                int maxAdd = chosen != null ? chosen.getStock() : product.getStock();
                int newQty = item.getQuantity() + quantity;
                if (newQty > maxAdd) {
                    newQty = maxAdd;
                }
                item.setQuantity(newQty);
                if (chosen != null) {
                    item.setVariant(chosen);
                }
                return;
            }
        }
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setVariant(chosen);
        item.setQuantity(quantity);
        cart.add(item);
    }

    private boolean sameVariant(CartItem item, ProductVariant chosen) {
        if (chosen == null) {
            return item.getVariant() == null;
        }
        return item.getVariant() != null && item.getVariant().getId() == chosen.getId();
    }

    private void handleUpdate(HttpServletRequest req, List<CartItem> cart) {
        String[] pids = req.getParameterValues("cartProductId");
        String[] vids = req.getParameterValues("cartVariantId");
        String[] quantities = req.getParameterValues("cartQuantity");
        if (pids == null || quantities == null || pids.length != quantities.length) {
            return;
        }
        if (vids == null || vids.length != pids.length) {
            return;
        }
        for (int i = 0; i < pids.length; i++) {
            try {
                int productId = Integer.parseInt(pids[i]);
                Integer variantId = null;
                if (vids[i] != null && !vids[i].isBlank()) {
                    variantId = Integer.parseInt(vids[i]);
                }
                int qty = Integer.parseInt(quantities[i]);
                for (CartItem item : cart) {
                    if (item.getProduct().getId() == productId && variantIdMatches(item, variantId)) {
                        int maxStock = item.getAvailableStock();
                        if (qty <= 0) {
                            item.setQuantity(0);
                        } else {
                            item.setQuantity(Math.min(qty, maxStock));
                        }
                        break;
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
        Iterator<CartItem> it = cart.iterator();
        while (it.hasNext()) {
            if (it.next().getQuantity() <= 0) {
                it.remove();
            }
        }
    }

    private boolean variantIdMatches(CartItem item, Integer variantId) {
        if (variantId == null) {
            return item.getVariant() == null;
        }
        return item.getVariant() != null && item.getVariant().getId() == variantId;
    }

    private void handleRemove(HttpServletRequest req, List<CartItem> cart) {
        String idParam = req.getParameter("productId");
        String vParam = req.getParameter("variantId");
        try {
            int id = Integer.parseInt(idParam);
            final Integer vid = (vParam != null && !vParam.isBlank())
                    ? Integer.parseInt(vParam)
                    : null;
            cart.removeIf(item -> item.getProduct().getId() == id && variantIdMatches(item, vid));
        } catch (NumberFormatException ignored) {
        }
    }

    private BigDecimal calculateTotal(List<CartItem> cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }
}
