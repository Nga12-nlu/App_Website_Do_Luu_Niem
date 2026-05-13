<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đặt hàng thành công"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="order-success-wrapper">
    <div class="order-success-content">
        <div class="success-icon-wrapper">
            <div class="success-icon">
                <i class="fas fa-check"></i>
            </div>
        </div>
        <h1 class="success-title">Đặt hàng thành công!</h1>
        <p class="success-message">Cảm ơn bạn đã mua sắm tại Souvenir Shop. Đơn hàng của bạn đã được tiếp nhận và đang được xử lý.</p>
        
        <c:if test="${not empty order}">
            <div class="order-info-card">
                <div class="order-info-header">
                    <h5><i class="fas fa-receipt me-2"></i>Thông tin đơn hàng</h5>
                </div>
                <div class="order-info-body">
                    <div class="order-info-row">
                        <div class="order-info-label">
                            <i class="fas fa-hashtag"></i>
                            <span>Mã đơn hàng</span>
                        </div>
                        <div class="order-info-value">#${order.id}</div>
                    </div>
                    <div class="order-info-row">
                        <div class="order-info-label">
                            <i class="fas fa-money-bill-wave"></i>
                            <span>Tổng tiền</span>
                        </div>
                        <div class="order-info-value">
                            <span class="price-value" data-price="${order.totalAmount}">${order.totalAmount}</span> &#8363;
                        </div>
                    </div>
                    <div class="order-info-row">
                        <div class="order-info-label">
                            <i class="fas fa-info-circle"></i>
                            <span>Trạng thái</span>
                        </div>
                        <div class="order-info-value">
                            <span class="badge ${order.status eq 'CONFIRMED' ? 'bg-success' : order.status eq 'CANCELLED' ? 'bg-danger' : order.status eq 'SHIPPED' ? 'bg-info' : 'bg-warning'}">
                                <c:choose>
                                    <c:when test="${order.status eq 'PENDING'}">Chờ xử lý</c:when>
                                    <c:when test="${order.status eq 'CONFIRMED'}">Đã xác nhận</c:when>
                                    <c:when test="${order.status eq 'SHIPPED'}">Đã giao hàng</c:when>
                                    <c:when test="${order.status eq 'CANCELLED'}">Đã hủy</c:when>
                                    <c:otherwise>${order.status}</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>
                    <div class="order-info-row">
                        <div class="order-info-label">
                            <i class="fas fa-map-marker-alt"></i>
                            <span>Địa chỉ giao hàng</span>
                        </div>
                        <div class="order-info-value"><c:out value="${order.shippingAddress}"/></div>
                    </div>
                </div>
            </div>
        </c:if>
        
        <c:if test="${not empty order and not empty order.items}">
            <div class="order-info-card mt-4">
                <div class="order-info-header">
                    <h5><i class="fas fa-list me-2"></i>Chi tiết sản phẩm</h5>
                </div>
                <div class="order-info-body p-0">
                    <div class="table-responsive">
                        <table class="table table-borderless mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th>Sản phẩm</th>
                                    <th>Biến thể</th>
                                    <th class="text-end">Thành tiền</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="li" items="${order.items}">
                                    <tr>
                                        <td><c:out value="${li.product.name}"/> <span class="text-muted small">× ${li.quantity}</span></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty li.variantLabel}">
                                                    <span class="variant-pill"><i class="fas fa-layer-group me-1 opacity-75"></i><c:out value="${li.variantLabel}"/></span>
                                                </c:when>
                                                <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end"><span class="price-value" data-price="${li.totalPrice}">${li.totalPrice}</span> &#8363;</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </c:if>

        <div class="order-success-actions">
            <a href="${pageContext.request.contextPath}/my-orders" class="btn btn-souvenir btn-lg">
                <i class="fas fa-list me-2"></i>Xem đơn hàng của tôi
            </a>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-outline-souvenir btn-lg"><i class="fas fa-shopping-bag me-2"></i>Tiếp tục mua sắm</a>
            <a href="${pageContext.request.contextPath}/home" class="btn btn-outline-souvenir btn-lg"><i class="fas fa-home me-2"></i>Về trang chủ</a>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>

