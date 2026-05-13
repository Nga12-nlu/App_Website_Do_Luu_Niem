<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Thanh toán"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<h4 class="section-title mb-4">Thanh toán</h4>

<c:if test="${not empty error}">
    <div class="alert alert-danger"><c:out value="${error}"/></div>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/checkout" class="row">
    <div class="col-md-8">
            <div class="card mb-4">
            <div class="card-body">
                <h5 class="card-title">
                    <i class="fas fa-shipping-fast me-2"></i>Thông tin giao hàng
                </h5>
                <c:if test="${not empty sessionScope.currentUser}">
                    <div class="alert alert-info">
                        <i class="fas fa-user me-2"></i>
                        <strong>Khách hàng:</strong> <c:out value="${sessionScope.currentUser.fullName}"/><br>
                        <i class="fas fa-envelope me-2"></i>
                        <strong>Email:</strong> <c:out value="${sessionScope.currentUser.email}"/>
                    </div>
                </c:if>
                <div class="mb-3">
                    <label class="form-label">Địa chỉ giao hàng *</label>
                    <textarea name="address" class="form-control" rows="3" required placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành phố"></textarea>
                </div>
                <div class="mb-3">
                    <label class="form-label">Số điện thoại *</label>
                    <input type="tel" name="phone" class="form-control" required placeholder="Số điện thoại liên hệ" pattern="[0-9]{10,11}">
                    <small class="text-muted">Nhập số điện thoại 10-11 chữ số</small>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">Đơn hàng</h5>
                <ul class="list-group list-group-flush">
                    <c:forEach var="item" items="${cartItems}">
                        <li class="list-group-item">
                            <div class="d-flex justify-content-between align-items-start gap-2">
                                <div>
                                    <i class="fas fa-box text-muted me-1"></i>
                                    <span class="fw-medium"><c:out value="${item.product.name}"/></span>
                                    <c:if test="${item.variant != null}">
                                        <div class="mt-1"><span class="variant-pill"><i class="fas fa-layer-group me-1 opacity-75"></i><c:out value="${item.variant.displayName}"/></span></div>
                                    </c:if>
                                    <div class="small text-muted">x ${item.quantity}</div>
                                </div>
                                <div class="text-end text-nowrap">
                                    <span class="price-value fw-semibold" data-price="${item.totalPrice}">${item.totalPrice}</span> <span class="text-muted">&#8363;</span>
                                </div>
                            </div>
                        </li>
                    </c:forEach>
                </ul>
                <div class="d-flex justify-content-between mt-3 fs-5">
                    <strong><i class="fas fa-coins me-2"></i>Tổng cộng:</strong>
                    <strong><span class="price-value" data-price="${totalAmount}">${totalAmount}</span> &#8363;</strong>
                </div>
                <button type="submit" class="btn btn-souvenir w-100 mt-3"><i class="fas fa-check me-2"></i>Đặt hàng</button>
            </div>
        </div>
    </div>
</form>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>

