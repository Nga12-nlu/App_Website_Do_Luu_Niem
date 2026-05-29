<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Giỏ hàng"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>
<link href="${pageContext.request.contextPath}/css/checkout.css?v=1" rel="stylesheet">

<h4 class="section-title mb-4"><i class="fas fa-shopping-cart me-2 text-primary"></i>Giỏ hàng</h4>

<c:choose>
    <c:when test="${empty cartItems}">
        <div class="alert alert-info d-flex align-items-center gap-2">
            <i class="fas fa-cart-arrow-down fa-lg"></i>
            <span>Giỏ hàng trống. <a href="${pageContext.request.contextPath}/products" class="alert-link">Mua sắm ngay</a></span>
        </div>
    </c:when>
    <c:otherwise>
        <div class="cart-coupon-panel">
            <div class="row align-items-end g-2">
                <div class="col-md-8">
                    <label class="form-label fw-semibold mb-1" for="cartCouponCode">
                        <i class="fas fa-ticket-alt me-1 text-primary"></i>Mã giảm giá
                    </label>
                    <input type="text" class="form-control text-uppercase" id="cartCouponCode" maxlength="32"
                           placeholder="VD: WELCOME10, GIAM50K"
                           value="${quote.couponApplied ? quote.couponCode : ''}">
                    <div id="cartCouponMessage" class="form-text mt-1 ${quote.couponApplied ? 'text-success' : ''}">
                        <c:if test="${not empty quote.couponMessage}"><c:out value="${quote.couponMessage}"/></c:if>
                    </div>
                </div>
                <div class="col-md-4 d-flex gap-2">
                    <button type="button" class="btn btn-outline-primary flex-grow-1" id="cartBtnApplyCoupon">Áp dụng</button>
                    <button type="button" class="btn btn-outline-secondary" id="cartBtnRemoveCoupon"
                            style="${quote.couponApplied ? '' : 'display:none;'}">Xóa</button>
                </div>
            </div>
            <c:if test="${quote.couponApplied}">
                <div class="small text-muted mt-2">
                    Giảm <span class="price-value" data-price="${quote.discountAmount}">${quote.discountAmount}</span>đ
                    (áp dụng khi thanh toán)
                </div>
            </c:if>
        </div>

        <form method="post" action="${pageContext.request.contextPath}/cart">
            <input type="hidden" name="action" value="update">
            <div class="table-responsive cart-table-wrap">
                <table class="table align-middle">
                    <thead class="table-light">
                        <tr>
                            <th><i class="fas fa-cube me-1"></i>Sản phẩm</th>
                            <th><i class="fas fa-tags me-1"></i>Biến thể</th>
                            <th><i class="fas fa-money-bill-wave me-1"></i>Đơn giá</th>
                            <th><i class="fas fa-sort-numeric-up me-1"></i>Số lượng</th>
                            <th><i class="fas fa-calculator me-1"></i>Thành tiền</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="item" items="${cartItems}">
                            <tr>
                                <td>
                                    <a href="${pageContext.request.contextPath}/product?id=${item.product.id}" class="text-decoration-none text-dark fw-medium">
                                        <i class="fas fa-store text-muted me-1"></i><c:out value="${item.product.name}"/>
                                    </a>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${item.variant != null}">
                                            <span class="variant-pill"><i class="fas fa-layer-group me-1 opacity-75"></i><c:out value="${item.variant.displayName}"/></span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted small">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="price-value" data-price="${item.unitPrice}">${item.unitPrice}</span> &#8363;
                                </td>
                                <td style="max-width:110px;">
                                    <input type="hidden" name="cartProductId" value="${item.product.id}">
                                    <input type="hidden" name="cartVariantId" value="${item.variant != null ? item.variant.id : ''}">
                                    <input type="number" name="cartQuantity" value="${item.quantity}" min="1" max="${item.availableStock}" class="form-control form-control-sm">
                                </td>
                                <td>
                                    <span class="item-total price-value fw-semibold" data-price="${item.totalPrice}">${item.totalPrice}</span> &#8363;
                                </td>
                                <td>
                                    <button type="button" class="btn btn-sm btn-outline-danger" title="Xóa"
                                            onclick="document.getElementById('remove_${fn:replace(item.lineKey, '.', '_')}').submit();">
                                        <i class="fas fa-trash-alt"></i>
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
            <div class="d-flex justify-content-between align-items-center mt-4 flex-wrap gap-3">
                <a href="${pageContext.request.contextPath}/products" class="btn btn-outline-souvenir"><i class="fas fa-arrow-left me-2"></i>Tiếp tục mua</a>
                <div class="d-flex align-items-center gap-3 flex-wrap">
                    <strong class="fs-5"><i class="fas fa-receipt me-2 text-primary"></i>Tổng: <span class="price-value" data-price="${totalAmount}">${totalAmount}</span> &#8363;</strong>
                    <button type="submit" class="btn btn-souvenir"><i class="fas fa-sync-alt me-2"></i>Cập nhật giỏ</button>
                </div>
            </div>
        </form>

        <c:forEach var="item" items="${cartItems}">
            <form id="remove_${fn:replace(item.lineKey, '.', '_')}" method="post" action="${pageContext.request.contextPath}/cart" style="display:none;">
                <input type="hidden" name="action" value="remove">
                <input type="hidden" name="productId" value="${item.product.id}">
                <input type="hidden" name="variantId" value="${item.variant != null ? item.variant.id : ''}">
            </form>
        </c:forEach>

        <div class="text-end mt-3">
            <a href="${pageContext.request.contextPath}/checkout" class="btn btn-souvenir btn-lg"><i class="fas fa-lock me-2"></i>Thanh toán</a>
        </div>
    </c:otherwise>
</c:choose>

<script>
(function() {
    var base = '${pageContext.request.contextPath}';
    var btnApply = document.getElementById('cartBtnApplyCoupon');
    var btnRemove = document.getElementById('cartBtnRemoveCoupon');
    if (btnApply) {
        btnApply.addEventListener('click', function() {
            var code = document.getElementById('cartCouponCode').value.trim();
            var body = new URLSearchParams();
            body.set('code', code);
            fetch(base + '/api/coupon/apply', {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body.toString()
            }).then(function(r) { return r.json().then(function(d) { return { ok: r.ok, data: d }; }); })
              .then(function(res) {
                var msg = document.getElementById('cartCouponMessage');
                if (!res.ok) {
                    msg.textContent = res.data.message || 'Mã không hợp lệ';
                    msg.className = 'form-text mt-1 text-danger';
                    return;
                }
                msg.textContent = res.data.message || 'Đã áp dụng mã';
                msg.className = 'form-text mt-1 text-success';
                if (btnRemove) btnRemove.style.display = '';
                location.reload();
              });
        });
    }
    if (btnRemove) {
        btnRemove.addEventListener('click', function() {
            fetch(base + '/api/coupon/remove', {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: ''
            }).then(function() { location.reload(); });
        });
    }
})();
document.addEventListener('DOMContentLoaded', function() {
    var quantityInputs = document.querySelectorAll('input[name="cartQuantity"]');
    quantityInputs.forEach(function(input) {
        input.addEventListener('change', function() {
            var row = this.closest('tr');
            var price = parseFloat(row.querySelector('td:nth-child(3) .price-value').getAttribute('data-price')) || 0;
            var qty = parseInt(this.value, 10) || 0;
            var total = price * qty;
            var totalCell = row.querySelector('.item-total');
            totalCell.textContent = new Intl.NumberFormat('vi-VN').format(total);
            totalCell.setAttribute('data-price', total);
            var grand = 0;
            document.querySelectorAll('.item-total').forEach(function(el) {
                grand += parseFloat(el.getAttribute('data-price')) || 0;
            });
            var totalEl = document.querySelector('strong .price-value');
            if (totalEl) {
                totalEl.textContent = new Intl.NumberFormat('vi-VN').format(grand);
                totalEl.setAttribute('data-price', grand);
            }
        });
    });
});
</script>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
