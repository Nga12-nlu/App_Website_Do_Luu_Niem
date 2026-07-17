<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Chi tiết đơn hàng #${order.id}"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<div class="admin-card">
    <div class="admin-card-header">
        <h2 class="admin-card-title">
            <i class="fas fa-receipt me-2"></i>Đơn hàng #${order.id}
        </h2>
        <a href="${pageContext.request.contextPath}/admin/orders" class="admin-btn admin-btn-outline">
            <i class="fas fa-arrow-left me-2"></i>Quay lại
        </a>
    </div>
</div>

<div class="row g-4">
    <div class="col-md-8">
        <div class="admin-card">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-truck me-2"></i>Thông tin giao hàng
                </h5>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <strong>Khách hàng:</strong><br>
                        <c:out value="${order.user.fullName}"/>
                    </div>
                    <div class="col-md-6 mb-3">
                        <strong>Email:</strong><br>
                        <c:out value="${order.user.email}"/>
                    </div>
                    <c:if test="${not empty order.receiverName}">
                        <div class="col-md-6 mb-3">
                            <strong>Người nhận:</strong><br>
                            <c:out value="${order.receiverName}"/>
                        </div>
                    </c:if>
                    <div class="col-12 mb-3">
                        <strong>Địa chỉ:</strong><br>
                        <c:out value="${order.shippingAddress}"/>
                    </div>
                    <c:if test="${not empty order.customerNote}">
                        <div class="col-12 mb-3">
                            <strong>Ghi chú:</strong><br>
                            <c:out value="${order.customerNote}"/>
                        </div>
                    </c:if>
                    <div class="col-md-6 mb-3">
                        <strong>Điện thoại:</strong><br>
                        <c:out value="${order.phone}"/>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="admin-card">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-box me-2"></i>Sản phẩm
                </h5>
            </div>
            <div class="card-body">
                <div class="admin-table-wrapper">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th><i class="fas fa-cube me-1"></i>Sản phẩm</th>
                                <th><i class="fas fa-layer-group me-1"></i>Biến thể</th>
                                <th>Đơn giá</th>
                                <th>SL</th>
                                <th>Thành tiền</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${order.items}">
                                <tr>
                                    <td><c:out value="${item.product.name}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty item.variantLabel}">
                                                <span class="admin-badge admin-badge-info"><c:out value="${item.variantLabel}"/></span>
                                            </c:when>
                                            <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><span class="price-value" data-price="${item.unitPrice}">${item.unitPrice}</span> &#8363;</td>
                                    <td>${item.quantity}</td>
                                    <td><strong><span class="price-value" data-price="${item.totalPrice}">${item.totalPrice}</span> &#8363;</strong></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    
    <div class="col-md-4">
        <div class="admin-card">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-info-circle me-2"></i>Trạng thái đơn hàng
                </h5>
            </div>
            <div class="card-body">
                <div class="mb-3">
                    <strong>Thanh toán:</strong>
                    <div class="mt-1">
                        <c:choose>
                            <c:when test="${order.vnpay}">VNPay</c:when>
                            <c:otherwise>COD</c:otherwise>
                        </c:choose>
                        <c:if test="${not empty order.vnpayTxnRef}">
                            <div class="small text-muted mt-1">Mã GD: <c:out value="${order.vnpayTxnRef}"/></div>
                        </c:if>
                        <c:if test="${not empty order.vnpayTransactionNo}">
                            <div class="small text-muted">VNPay #<c:out value="${order.vnpayTransactionNo}"/></div>
                        </c:if>
                    </div>
                </div>
                <div class="mb-3">
                    <strong>Trạng thái hiện tại:</strong>
                    <div class="mt-2">
                        <c:choose>
                            <c:when test="${order.status eq 'PENDING'}">
                                <span class="admin-badge admin-badge-warning">Chờ xử lý</span>
                            </c:when>
                            <c:when test="${order.status eq 'CONFIRMED'}">
                                <span class="admin-badge admin-badge-success">Đã xác nhận</span>
                            </c:when>
                            <c:when test="${order.status eq 'PACKAGING'}">
                                <span class="admin-badge admin-badge-warning">Đang đóng gói</span>
                            </c:when>
                            <c:when test="${order.status eq 'AWAITING_SHIPPING'}">
                                <span class="admin-badge admin-badge-warning">Chờ giao ĐVVC</span>
                            </c:when>
                            <c:when test="${order.status eq 'SHIPPING'}">
                                <span class="admin-badge admin-badge-info">Đang giao hàng</span>
                            </c:when>
                            <c:when test="${order.status eq 'SHIPPED'}">
                                <span class="admin-badge admin-badge-success">Đã giao hàng</span>
                            </c:when>
                            <c:when test="${order.status eq 'CANCELLED'}">
                                <span class="admin-badge admin-badge-danger">Đã hủy</span>
                            </c:when>
                            <c:otherwise>
                                <span class="admin-badge">${order.status}</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                
                <form method="post" action="${pageContext.request.contextPath}/admin/orders" id="statusForm">
                    <input type="hidden" name="action" value="update-status">
                    <input type="hidden" name="id" value="${order.id}">
                    <div class="mb-3">
                        <label class="admin-filter-label">Cập nhật trạng thái</label>
                        <select name="status" class="form-select">
                            <option value="PENDING" ${order.status eq 'PENDING' ? 'selected' : ''}>Chờ xử lý</option>
                            <option value="CONFIRMED" ${order.status eq 'CONFIRMED' ? 'selected' : ''}>Đã xác nhận</option>
                            <option value="PACKAGING" ${order.status eq 'PACKAGING' ? 'selected' : ''}>Đang đóng gói</option>
                            <option value="AWAITING_SHIPPING" ${order.status eq 'AWAITING_SHIPPING' ? 'selected' : ''}>Chờ giao ĐVVC</option>
                            <option value="SHIPPING" ${order.status eq 'SHIPPING' ? 'selected' : ''}>Đang giao hàng</option>
                            <option value="SHIPPED" ${order.status eq 'SHIPPED' ? 'selected' : ''}>Đã giao hàng</option>
                            <option value="CANCELLED" ${order.status eq 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                        </select>
                    </div>
                    <button type="submit" class="admin-btn admin-btn-primary w-100">
                        <i class="fas fa-save me-2"></i>Cập nhật
                    </button>
                </form>
                
                <hr>
                
                <c:if test="${order.subtotal != null}">
                    <div class="d-flex justify-content-between mb-2">
                        <span>Tạm tính</span>
                        <span><span class="price-value" data-price="${order.subtotal}">${order.subtotal}</span>đ</span>
                    </div>
                </c:if>
                <c:if test="${order.discountAmount != null and order.discountAmount > 0}">
                    <div class="d-flex justify-content-between mb-2 text-success">
                        <span>Giảm giá <c:if test="${not empty order.couponCode}">(<c:out value="${order.couponCode}"/>)</c:if></span>
                        <span>-<span class="price-value" data-price="${order.discountAmount}">${order.discountAmount}</span>đ</span>
                    </div>
                </c:if>
                <c:if test="${order.shippingFee != null}">
                    <div class="d-flex justify-content-between mb-2">
                        <span>Phí ship</span>
                        <span><span class="price-value" data-price="${order.shippingFee}">${order.shippingFee}</span>đ</span>
                    </div>
                </c:if>
                <div class="mt-3">
                    <strong>Tổng thanh toán:</strong>
                    <h4 class="text-primary mb-0">
                        <span class="price-value" data-price="${order.totalAmount}">${order.totalAmount}</span> &#8363;
                    </h4>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
