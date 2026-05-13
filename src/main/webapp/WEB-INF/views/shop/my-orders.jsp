<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.app.app_website_do_luu_niem.model.Order" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đơn hàng của tôi"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<h4 class="section-title mb-4">Đơn hàng của tôi</h4>

<c:choose>
    <c:when test="${empty orders}">
        <div class="alert alert-info text-center">
            <i class="fas fa-shopping-bag fa-3x mb-3 text-muted"></i>
            <p class="mb-0">Bạn chưa có đơn hàng nào.</p>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-souvenir mt-3">Mua sắm ngay</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="row g-4">
            <c:forEach var="order" items="${orders}">
                <div class="col-12">
                    <div class="card">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div>
                                    <h5 class="card-title mb-1">Đơn hàng #${order.id}</h5>
                                    <small class="text-muted">
                                        <i class="fas fa-calendar me-1"></i>
                                        <%
                                            Order order = (Order) pageContext.getAttribute("order");
                                            if (order != null && order.getCreatedAt() != null) {
                                                out.print(order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                                            }
                                        %>
                                    </small>
                                </div>
                                <div>
                                    <span class="badge ${order.status eq 'CONFIRMED' ? 'bg-success' : order.status eq 'CANCELLED' ? 'bg-danger' : order.status eq 'SHIPPED' ? 'bg-info' : 'bg-warning'} fs-6">
                                        <c:choose>
                                            <c:when test="${order.status eq 'PENDING'}">Chờ xử lý</c:when>
                                            <c:when test="${order.status eq 'CONFIRMED'}">Đã xác nhận</c:when>
                                            <c:when test="${order.status eq 'SHIPPED'}">Đã giao</c:when>
                                            <c:when test="${order.status eq 'CANCELLED'}">Đã hủy</c:when>
                                            <c:otherwise>${order.status}</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </div>
                            <div class="row mb-3">
                                <div class="col-md-6">
                                    <p class="mb-1"><strong>Địa chỉ giao hàng:</strong></p>
                                    <p class="text-muted mb-0"><c:out value="${order.shippingAddress}"/></p>
                                </div>
                                <div class="col-md-6">
                                    <p class="mb-1"><strong>Số điện thoại:</strong></p>
                                    <p class="text-muted mb-0"><c:out value="${order.phone}"/></p>
                                </div>
                            </div>
                            <div class="d-flex justify-content-between align-items-center">
                                <div>
                                    <strong class="fs-5">
                                        Tổng tiền: <span class="price-value" data-price="${order.totalAmount}">${order.totalAmount}</span> &#8363;
                                    </strong>
                                </div>
                                <a href="${pageContext.request.contextPath}/order-success?id=${order.id}" class="btn btn-outline-souvenir">
                                    <i class="fas fa-eye me-1"></i>Xem chi tiết
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>

