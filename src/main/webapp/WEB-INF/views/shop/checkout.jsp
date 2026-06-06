<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Thanh toán"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>
<link href="${pageContext.request.contextPath}/css/checkout.css?v=1" rel="stylesheet">

<div class="checkout-page">
    <nav class="checkout-breadcrumb mb-3" aria-label="breadcrumb">
        <ol class="breadcrumb mb-0">
            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/cart">Giỏ hàng</a></li>
            <li class="breadcrumb-item active" aria-current="page">Thanh toán</li>
        </ol>
    </nav>

    <h4 class="section-title mb-4"><i class="fas fa-lock me-2 text-primary"></i>Thanh toán an toàn</h4>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="fas fa-exclamation-circle me-2"></i><c:out value="${error}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/checkout" id="checkoutForm" class="row g-4" novalidate>
        <div class="col-lg-8">
            <div class="checkout-card mb-4">
                <div class="checkout-card-header">
                    <span class="checkout-step">1</span>
                    <h5 class="mb-0">Thông tin người nhận</h5>
                </div>
                <div class="checkout-card-body">
                    <c:if test="${not empty sessionScope.currentUser}">
                        <div class="checkout-user-hint mb-3">
                            <i class="fas fa-user-circle me-2"></i>
                            Tài khoản: <strong><c:out value="${sessionScope.currentUser.fullName}"/></strong>
                            · <c:out value="${sessionScope.currentUser.email}"/>
                        </div>
                    </c:if>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label" for="receiverName">Họ tên người nhận <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="receiverName" name="receiverName" required
                                   minlength="2" maxlength="100" autocomplete="name"
                                   value="${not empty formReceiverName ? formReceiverName : sessionScope.currentUser.fullName}"
                                   placeholder="Nguyễn Văn A">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label" for="phone">Số điện thoại <span class="text-danger">*</span></label>
                            <input type="tel" class="form-control" id="phone" name="phone" required
                                   pattern="0[0-9]{9,10}" maxlength="11" autocomplete="tel"
                                   value="${formPhone}" placeholder="0912345678">
                            <div class="form-text">Bắt đầu bằng 0, 10–11 chữ số</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="checkout-card mb-4">
                <div class="checkout-card-header">
                    <span class="checkout-step">2</span>
                    <h5 class="mb-0">Địa chỉ giao hàng</h5>
                </div>
                <div class="checkout-card-body">
                    <div class="mb-3">
                        <label class="form-label" for="addressProvider">Nguồn dữ liệu địa chỉ</label>
                        <select class="form-select" id="addressProvider" aria-describedby="providerHelp">
                            <option value="open-api">Open API Vietnam (provinces.open-api.vn)</option>
                            <option value="vnappmob">VNAppMob (vapi.vnappmob.com)</option>
                            <c:if test="${ghnEnabled}">
                                <option value="ghn">Giao Hàng Nhanh (GHN API)</option>
                            </c:if>
                        </select>
                        <div id="providerHelp" class="form-text">Chọn API nếu một nguồn tải chậm hoặc lỗi.</div>
                    </div>
                    <div class="row g-3">
                        <div class="col-md-4">
                            <label class="form-label" for="province">Tỉnh / Thành phố <span class="text-danger">*</span></label>
                            <select class="form-select address-select" id="province" required disabled>
                                <option value="">— Đang tải —</option>
                            </select>
                            <input type="hidden" name="provinceCode" id="provinceCode" value="${formProvinceCode}">
                            <input type="hidden" name="provinceName" id="provinceName" value="${formProvinceName}">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label" for="district">Quận / Huyện <span class="text-danger">*</span></label>
                            <select class="form-select address-select" id="district" required disabled>
                                <option value="">— Chọn tỉnh trước —</option>
                            </select>
                            <input type="hidden" name="districtCode" id="districtCode" value="${formDistrictCode}">
                            <input type="hidden" name="districtName" id="districtName" value="${formDistrictName}">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label" for="ward">Phường / Xã <span class="text-danger">*</span></label>
                            <select class="form-select address-select" id="ward" required disabled>
                                <option value="">— Chọn quận trước —</option>
                            </select>
                            <input type="hidden" name="wardCode" id="wardCode" value="${formWardCode}">
                            <input type="hidden" name="wardName" id="wardName" value="${formWardName}">
                        </div>
                        <div class="col-12">
                            <label class="form-label" for="addressDetail">Địa chỉ chi tiết <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="addressDetail" name="addressDetail" required
                                   minlength="5" maxlength="500" value="${formAddressDetail}"
                                   placeholder="Số nhà, tên đường, tòa nhà...">
                        </div>
                        <div class="col-12">
                            <label class="form-label" for="customerNote">Ghi chú đơn hàng</label>
                            <textarea class="form-control" id="customerNote" name="customerNote" rows="2"
                                      maxlength="500" placeholder="Giao giờ hành chính, gọi trước khi giao...">${formCustomerNote}</textarea>
                        </div>
                    </div>
                    <div id="addressPreview" class="address-preview mt-3" style="display:none;">
                        <i class="fas fa-map-marker-alt me-2"></i>
                        <span id="addressPreviewText"></span>
                    </div>
                </div>
            </div>

            <div class="checkout-card">
                <div class="checkout-card-header">
                    <span class="checkout-step">3</span>
                    <h5 class="mb-0">Phương thức thanh toán</h5>
                </div>
                <div class="checkout-card-body">
                    <div class="payment-methods">
                        <label class="payment-method-option">
                            <input type="radio" name="paymentMethod" value="COD"
                                   ${empty formPaymentMethod or formPaymentMethod eq 'COD' ? 'checked' : ''}>
                            <span><i class="fas fa-money-bill-wave me-2"></i>Thanh toán khi nhận hàng (COD)</span>
                        </label>
                        <c:if test="${vnpayEnabled}">
                            <label class="payment-method-option">
                                <input type="radio" name="paymentMethod" value="VNPAY"
                                       ${formPaymentMethod eq 'VNPAY' ? 'checked' : ''}>
                                <span><i class="fas fa-credit-card me-2"></i>VNPay Sandbox (thẻ / QR)</span>
                            </label>
                        </c:if>
                    </div>
                    <c:if test="${vnpayFeatureOn and not vnpayEnabled}">
                        <div class="alert alert-warning mt-3 mb-0 py-2 small">
                            <i class="fas fa-exclamation-triangle me-1"></i>
                            <c:out value="${vnpayConfigWarning}"/>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="checkout-summary sticky-top">
                <h5 class="checkout-summary-title"><i class="fas fa-receipt me-2"></i>Đơn hàng</h5>
                <ul class="checkout-items list-unstyled mb-3">
                    <c:forEach var="item" items="${cartItems}">
                        <li class="checkout-item">
                            <div class="checkout-item-name">
                                <c:out value="${item.product.name}"/>
                                <c:if test="${item.variant != null}">
                                    <small class="d-block text-muted"><c:out value="${item.variant.displayName}"/></small>
                                </c:if>
                            </div>
                            <div class="checkout-item-qty">×${item.quantity}</div>
                            <div class="checkout-item-price">
                                <span class="price-value" data-price="${item.totalPrice}">${item.totalPrice}</span>đ
                            </div>
                        </li>
                    </c:forEach>
                </ul>

                <div class="coupon-box mb-3">
                    <label class="form-label small fw-semibold mb-1">Mã giảm giá</label>
                    <div class="input-group">
                        <input type="text" class="form-control text-uppercase" id="couponCodeInput"
                               placeholder="VD: WELCOME10" maxlength="32"
                               value="${quote.couponApplied ? quote.couponCode : ''}">
                        <button type="button" class="btn btn-outline-primary" id="btnApplyCoupon">Áp dụng</button>
                    </div>
                    <div id="couponMessage" class="form-text mt-1 ${quote.couponApplied ? 'text-success' : ''}">
                        <c:if test="${not empty quote.couponMessage}"><c:out value="${quote.couponMessage}"/></c:if>
                    </div>
                    <button type="button" class="btn btn-link btn-sm p-0 mt-1" id="btnRemoveCoupon"
                            style="${quote.couponApplied ? '' : 'display:none;'}">Xóa mã</button>
                </div>

                <div class="checkout-totals">
                    <div class="checkout-total-row">
                        <span>Tạm tính</span>
                        <span><span class="price-value" id="sumSubtotal" data-price="${quote.subtotal}">${quote.subtotal}</span>đ</span>
                    </div>
                    <div class="checkout-total-row text-success" id="rowDiscount" style="${quote.discountAmount > 0 ? '' : 'display:none;'}">
                        <span>Giảm giá</span>
                        <span>-<span class="price-value" id="sumDiscount" data-price="${quote.discountAmount}">${quote.discountAmount}</span>đ</span>
                    </div>
                    <div class="checkout-total-row">
                        <span>Phí vận chuyển</span>
                        <span><span class="price-value" id="sumShipping" data-price="${quote.shippingFee}">${quote.shippingFee}</span>đ</span>
                    </div>
                    <div class="checkout-total-row checkout-total-final">
                        <span>Tổng thanh toán</span>
                        <span class="text-primary fw-bold fs-5">
                            <span class="price-value" id="sumTotal" data-price="${quote.totalAmount}">${quote.totalAmount}</span>đ
                        </span>
                    </div>
                </div>

                <button type="submit" class="btn btn-souvenir w-100 mt-3 btn-lg" id="btnPlaceOrder">
                    <i class="fas fa-check-circle me-2"></i>Đặt hàng
                </button>
                <a href="${pageContext.request.contextPath}/cart" class="btn btn-link w-100 mt-2 text-center">
                    <i class="fas fa-arrow-left me-1"></i>Quay lại giỏ hàng
                </a>
            </div>
        </div>
    </form>
</div>

<script>
    window.CHECKOUT_CONFIG = {
        contextPath: '<c:out value="${pageContext.request.contextPath}"/>',
        defaultProvider: '<c:out value="${defaultAddressProvider}"/>',
        formProvinceCode: '<c:out value="${formProvinceCode}"/>',
        formDistrictCode: '<c:out value="${formDistrictCode}"/>',
        formWardCode: '<c:out value="${formWardCode}"/>'
    };
</script>
<script src="${pageContext.request.contextPath}/js/checkout.js?v=1"></script>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
