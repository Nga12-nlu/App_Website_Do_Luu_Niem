<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${product.name}"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="product-detail-wrapper">
    <div class="row">
        <div class="col-lg-6 mb-4">
            <div class="product-detail-image" id="detailMainImage">
                <c:choose>
                    <c:when test="${not empty product.imageUrl}">
                        <img id="mainProductImg" src="${fn:startsWith(product.imageUrl, 'data:')
                                   ? product.imageUrl
                                   : (fn:startsWith(product.imageUrl, 'http')
                                        ? product.imageUrl
                                        : pageContext.request.contextPath.concat('/').concat(product.imageUrl))}"
                             alt="<c:out value="${product.name}"/>"
                             onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                        <div class="img-placeholder" style="display:none;"><i class="fas fa-image fa-4x text-muted"></i></div>
                    </c:when>
                    <c:otherwise>
                        <div class="img-placeholder"><i class="fas fa-image fa-4x text-muted"></i></div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="product-detail-info">
                <h1 class="product-detail-title"><c:out value="${product.name}"/></h1>

                <div class="product-detail-price-block">
                    <div class="product-detail-price d-flex align-items-baseline gap-2 flex-wrap">
                        <span class="product-detail-price-label text-muted text-uppercase small fw-semibold letter-spacing">Giá</span>
                        <span class="price-value text-success fw-bold fs-2 product-detail-price-value" id="detailPrice" data-price="${product.price != null ? product.price : 0}">${product.price != null ? product.price : 0}</span>
                        <span class="fs-5 text-muted">&#8363;</span>
                    </div>
                    <div class="product-detail-sku-row mt-2" id="detailSkuWrap" style="display:none;">
                        <span class="product-detail-sku-pill"><i class="fas fa-barcode me-1"></i><span class="font-monospace" id="detailSku"></span></span>
                    </div>
                </div>

                <div class="product-detail-description mt-3">
                    <c:out value="${product.description}"/>
                </div>

                <c:if test="${not empty variants}">
                    <section class="variant-section" aria-labelledby="variant-heading">
                        <div class="variant-section__head">
                            <div class="variant-section__title-wrap">
                                <h2 class="variant-section__title" id="variant-heading">
                                    <span class="variant-section__icon"><i class="fas fa-layer-group"></i></span>
                                    Chọn phiên bản
                                </h2>
                                <p class="variant-section__hint">Mỗi phiên bản có giá và tồn kho riêng. Chọn một dòng trước khi thêm giỏ.</p>
                            </div>
                        </div>
                        <div class="variant-picker" id="variantPicker" role="radiogroup" aria-labelledby="variant-heading">
                            <c:forEach var="v" items="${variants}" varStatus="vs">
                                <c:set var="isDefaultPick" value="${defaultVariant != null and defaultVariant.id eq v.id}"/>
                                <c:set var="soldOut" value="${v.stock le 0 or not v.active}"/>
                                <label class="variant-card ${soldOut ? 'variant-card--disabled' : ''} ${isDefaultPick ? 'variant-card--selected' : ''}">
                                    <input type="radio" name="pickedVariant" class="variant-card__input" value="${v.id}"
                                           data-price="${v.price}" data-stock="${v.stock}" data-sku="${fn:escapeXml(v.sku != null ? v.sku : '')}"
                                           data-name="${fn:escapeXml(v.displayName)}"
                                           data-img="${fn:escapeXml(v.imageUrl != null && not empty v.imageUrl ? v.imageUrl : product.imageUrl)}"
                                        ${isDefaultPick ? 'checked' : ''} ${soldOut ? 'disabled' : ''}>
                                    <span class="variant-card__face">
                                        <span class="variant-card__radio" aria-hidden="true"></span>
                                        <span class="variant-card__body">
                                            <span class="variant-card__name"><c:out value="${v.displayName}"/></span>
                                            <c:if test="${not empty v.sku}">
                                                <span class="variant-card__sku"><c:out value="${v.sku}"/></span>
                                            </c:if>
                                            <span class="variant-card__row">
                                                <span class="variant-card__price">
                                                    <span class="price-value" data-price="${v.price}">${v.price}</span>
                                                    <span class="variant-card__currency">&#8363;</span>
                                                </span>
                                                <c:choose>
                                                    <c:when test="${v.stock > 0}">
                                                        <span class="variant-card__stock variant-card__stock--ok">
                                                            <i class="fas fa-cubes me-1"></i>Còn <strong>${v.stock}</strong>
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="variant-card__stock variant-card__stock--out">
                                                            <i class="fas fa-ban me-1"></i>Hết hàng
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </span>
                                        <span class="variant-card__tick" aria-hidden="true"><i class="fas fa-check"></i></span>
                                    </span>
                                </label>
                            </c:forEach>
                        </div>
                    </section>
                </c:if>

                <div class="product-detail-meta mt-4">
                    <div class="product-detail-meta-item">
                        <i class="fas fa-box-open text-primary"></i>
                        <div>
                            <strong>Tồn kho:</strong> <span id="detailStockText">${product.stock}</span> sản phẩm
                        </div>
                    </div>
                    <c:if test="${product.category != null}">
                        <div class="product-detail-meta-item">
                            <i class="fas fa-tag text-primary"></i>
                            <div>
                                <strong>Danh mục:</strong> <a href="${pageContext.request.contextPath}/products?category=${product.category.id}" class="text-decoration-none"><c:out value="${product.category.name}"/></a>
                            </div>
                        </div>
                    </c:if>
                </div>

                <div class="product-detail-actions mt-4">
                    <c:choose>
                        <c:when test="${product.stock > 0 or not empty variants}">
                            <form method="post" action="${pageContext.request.contextPath}/cart" class="w-100" id="addCartForm">
                                <input type="hidden" name="action" value="add">
                                <input type="hidden" name="productId" value="${product.id}">
                                <c:if test="${not empty variants}">
                                    <input type="hidden" name="variantId" id="variantIdInput" value="${defaultVariant != null ? defaultVariant.id : variants[0].id}">
                                </c:if>
                                <div class="d-flex gap-3 align-items-center flex-wrap mb-3">
                                    <div class="product-detail-quantity">
                                        <label class="mb-0 fw-semibold"><i class="fas fa-sort-numeric-up me-1"></i>Số lượng</label>
                                        <input type="number" name="quantity" id="qtyInput" value="1" min="1" class="form-control mt-1" required>
                                    </div>
                                    <span class="stock-badge in-stock" id="stockBadge">
                                        <i class="fas fa-check-circle"></i><span id="stockBadgeText">Còn hàng</span>
                                    </span>
                                </div>
                                <button type="submit" class="btn btn-souvenir w-100 product-detail-add-btn" id="btnAddCart">
                                    <i class="fas fa-cart-plus me-2"></i>Thêm vào giỏ hàng
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <div class="text-center w-100">
                                <span class="stock-badge out-of-stock">
                                    <i class="fas fa-times-circle"></i>Hết hàng
                                </span>
                                <p class="text-muted mt-3">Sản phẩm này hiện không còn hàng.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</div>

<c:if test="${not empty relatedProducts}">
    <div class="related-products-section">
        <h4 class="section-title"><i class="fas fa-th-large me-2"></i>Sản phẩm liên quan</h4>
        <div class="row g-4">
            <c:forEach var="p" items="${relatedProducts}">
                <div class="col-6 col-md-4 col-lg-3">
                    <div class="card product-card h-100">
                        <a href="${pageContext.request.contextPath}/product?id=${p.id}" class="text-decoration-none text-dark">
                            <div class="card-img-wrapper">
                                <c:choose>
                                    <c:when test="${not empty p.imageUrl}">
                                        <img src="${fn:startsWith(p.imageUrl, 'data:')
                                                   ? p.imageUrl
                                                   : (fn:startsWith(p.imageUrl, 'http')
                                                        ? p.imageUrl
                                                        : pageContext.request.contextPath.concat('/').concat(p.imageUrl))}"
                                             class="card-img-top" alt="<c:out value="${p.name}"/>"
                                             onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                                        <div class="img-placeholder" style="display:none;"><i class="fas fa-image"></i></div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="img-placeholder"><i class="fas fa-image"></i></div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="card-body">
                                <h6 class="card-title"><c:out value="${p.name}"/></h6>
                                <p class="price mb-1">
                                    <span class="price-value" data-price="${p.price != null ? p.price : 0}">${p.price != null ? p.price : 0}</span>
                                    <small>&#8363;</small>
                                </p>
                                <c:if test="${p.stock <= 0}">
                                    <span class="badge bg-secondary">Hết hàng</span>
                                </c:if>
                            </div>
                        </a>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</c:if>

<script>
(function () {
    var form = document.getElementById('addCartForm');
    if (!form) return;
    var picker = document.getElementById('variantPicker');
    var priceEl = document.getElementById('detailPrice');
    var stockText = document.getElementById('detailStockText');
    var qtyInput = document.getElementById('qtyInput');
    var variantInput = document.getElementById('variantIdInput');
    var mainImg = document.getElementById('mainProductImg');
    var skuWrap = document.getElementById('detailSkuWrap');
    var skuEl = document.getElementById('detailSku');
    var stockBadge = document.getElementById('stockBadge');
    var stockBadgeText = document.getElementById('stockBadgeText');
    var btnAdd = document.getElementById('btnAddCart');
    var priceBlock = document.querySelector('.product-detail-price-value');

    function formatPrice(n) {
        return new Intl.NumberFormat('vi-VN').format(n);
    }

    function applyVariant(radio) {
        if (!radio) return;
        var price = parseFloat(radio.getAttribute('data-price')) || 0;
        var stock = parseInt(radio.getAttribute('data-stock'), 10) || 0;
        var sku = radio.getAttribute('data-sku') || '';
        var img = radio.getAttribute('data-img') || '';
        priceEl.setAttribute('data-price', price);
        priceEl.textContent = formatPrice(price);
        if (priceBlock) {
            priceBlock.classList.remove('price-pop');
            void priceBlock.offsetWidth;
            priceBlock.classList.add('price-pop');
        }
        stockText.textContent = stock;
        if (variantInput) variantInput.value = radio.value;
        if (qtyInput) {
            qtyInput.max = stock;
            if (parseInt(qtyInput.value, 10) > stock) qtyInput.value = Math.max(1, stock);
            if (stock < 1) qtyInput.value = 0;
        }
        if (sku) {
            skuEl.textContent = sku;
            skuWrap.style.display = 'block';
        } else {
            skuWrap.style.display = 'none';
        }
        if (mainImg && img) {
            mainImg.style.opacity = '0.92';
            mainImg.src = img;
            mainImg.onload = function () { mainImg.style.opacity = '1'; };
        }
        document.querySelectorAll('.variant-card').forEach(function (el) {
            el.classList.remove('variant-card--selected');
        });
        var card = radio.closest('.variant-card');
        if (card) card.classList.add('variant-card--selected');
        if (stock > 0) {
            stockBadge.className = 'stock-badge in-stock';
            stockBadgeText.textContent = 'Còn hàng';
            btnAdd.disabled = false;
        } else {
            stockBadge.className = 'stock-badge out-of-stock';
            stockBadgeText.textContent = 'Hết hàng';
            btnAdd.disabled = true;
        }
    }

    if (picker) {
        picker.addEventListener('change', function (e) {
            if (e.target && e.target.name === 'pickedVariant') {
                applyVariant(e.target);
            }
        });
        var first = picker.querySelector('input[name="pickedVariant"]:checked')
            || picker.querySelector('input[name="pickedVariant"]:enabled');
        if (first) applyVariant(first);
    } else if (qtyInput) {
        qtyInput.max = ${product.stock};
    }
})();
</script>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
