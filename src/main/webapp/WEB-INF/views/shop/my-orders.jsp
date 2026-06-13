<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.app.app_website_do_luu_niem.model.Order" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đơn hàng của tôi"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<h4 class="section-title mb-4">Đơn hàng của tôi</h4>

<c:if test="${not empty error}">
    <div class="alert alert-warning"><c:out value="${error}"/></div>
</c:if>

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
                                    <span class="badge <c:choose>
                                        <c:when test="${order.status eq 'SHIPPED'}">bg-success</c:when>
                                        <c:when test="${order.status eq 'CANCELLED'}">bg-danger</c:when>
                                        <c:when test="${order.status eq 'SHIPPING'}">bg-primary</c:when>
                                        <c:otherwise>bg-warning text-dark</c:otherwise>
                                    </c:choose> fs-6">
                                        <c:choose>
                                            <c:when test="${order.status eq 'PENDING'}">Chờ xử lý</c:when>
                                            <c:when test="${order.status eq 'PACKAGING'}">Đang đóng gói</c:when>
                                            <c:when test="${order.status eq 'AWAITING_SHIPPING'}">Chờ giao ĐVVC</c:when>
                                            <c:when test="${order.status eq 'SHIPPING'}">Đang giao hàng</c:when>
                                            <c:when test="${order.status eq 'SHIPPED'}">Đã giao hàng</c:when>
                                            <c:when test="${order.status eq 'CANCELLED'}">Đã hủy</c:when>
                                            <c:otherwise>${order.status}</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </div>

                            <!-- Timeline Tiến trình Đơn hàng -->
                            <c:if test="${order.status ne 'CANCELLED'}">
                                <c:set var="status" value="${order.status}"/>
                                <c:choose>
                                    <c:when test="${status eq 'PENDING'}">
                                        <c:set var="step" value="1"/>
                                        <c:set var="pct" value="0"/>
                                    </c:when>
                                    <c:when test="${status eq 'PACKAGING'}">
                                        <c:set var="step" value="2"/>
                                        <c:set var="pct" value="25"/>
                                    </c:when>
                                    <c:when test="${status eq 'AWAITING_SHIPPING'}">
                                        <c:set var="step" value="3"/>
                                        <c:set var="pct" value="50"/>
                                    </c:when>
                                    <c:when test="${status eq 'SHIPPING'}">
                                        <c:set var="step" value="4"/>
                                        <c:set var="pct" value="75"/>
                                    </c:when>
                                    <c:when test="${status eq 'SHIPPED'}">
                                        <c:set var="step" value="5"/>
                                        <c:set var="pct" value="100"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="step" value="1"/>
                                        <c:set var="pct" value="0"/>
                                    </c:otherwise>
                                </c:choose>
                                <div class="order-timeline-container my-4 px-2">
                                    <div class="row text-center position-relative">
                                        <!-- Line Background -->
                                        <div class="position-absolute start-0 end-0 translate-middle-y bg-secondary-subtle" style="height: 4px; top: 18px; z-index: 1;"></div>
                                        <!-- Line Active -->
                                        <div class="position-absolute start-0 translate-middle-y bg-success" style="height: 4px; top: 18px; width: ${pct}%; z-index: 1; transition: width 0.5s ease;"></div>
                                        
                                        <!-- Step 1 -->
                                        <div class="col position-relative" style="z-index: 2;">
                                            <div class="d-inline-flex align-items-center justify-content-center rounded-circle ${step >= 1 ? 'bg-success text-white' : 'bg-light text-muted border'}" style="width: 36px; height: 36px;">
                                                <i class="fas fa-file-invoice" style="font-size: 14px;"></i>
                                            </div>
                                            <div class="small mt-2 fw-semibold d-none d-sm-block">Nhận đơn</div>
                                        </div>
                                        <!-- Step 2 -->
                                        <div class="col position-relative" style="z-index: 2;">
                                            <div class="d-inline-flex align-items-center justify-content-center rounded-circle ${step >= 2 ? 'bg-success text-white' : 'bg-light text-muted border'}" style="width: 36px; height: 36px;">
                                                <i class="fas fa-box" style="font-size: 14px;"></i>
                                            </div>
                                            <div class="small mt-2 fw-semibold d-none d-sm-block">Đóng gói</div>
                                        </div>
                                        <!-- Step 3 -->
                                        <div class="col position-relative" style="z-index: 2;">
                                            <div class="d-inline-flex align-items-center justify-content-center rounded-circle ${step >= 3 ? 'bg-success text-white' : 'bg-light text-muted border'}" style="width: 36px; height: 36px;">
                                                <i class="fas fa-truck-loading" style="font-size: 14px;"></i>
                                            </div>
                                            <div class="small mt-2 fw-semibold d-none d-sm-block">Chờ ĐVVC</div>
                                        </div>
                                        <!-- Step 4 -->
                                        <div class="col position-relative" style="z-index: 2;">
                                            <div class="d-inline-flex align-items-center justify-content-center rounded-circle ${step >= 4 ? 'bg-success text-white' : 'bg-light text-muted border'}" style="width: 36px; height: 36px;">
                                                <i class="fas fa-shipping-fast" style="font-size: 14px;"></i>
                                            </div>
                                            <div class="small mt-2 fw-semibold d-none d-sm-block">Đang giao</div>
                                        </div>
                                        <!-- Step 5 -->
                                        <div class="col position-relative" style="z-index: 2;">
                                            <div class="d-inline-flex align-items-center justify-content-center rounded-circle ${step >= 5 ? 'bg-success text-white' : 'bg-light text-muted border'}" style="width: 36px; height: 36px;">
                                                <i class="fas fa-check-circle" style="font-size: 14px;"></i>
                                            </div>
                                            <div class="small mt-2 fw-semibold d-none d-sm-block">Đã giao</div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
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
                            <div class="mb-2 small text-muted">
                                <i class="fas fa-wallet me-1"></i>
                                <c:choose>
                                    <c:when test="${order.vnpay}">VNPay</c:when>
                                    <c:otherwise>COD</c:otherwise>
                                </c:choose>
                            </div>
                            <c:if test="${not empty order.couponCode}">
                                <p class="mb-2 small text-success">
                                    <i class="fas fa-ticket-alt me-1"></i>Mã: <c:out value="${order.couponCode}"/>
                                    <c:if test="${order.discountAmount != null and order.discountAmount > 0}">
                                        · Giảm <span class="price-value" data-price="${order.discountAmount}">${order.discountAmount}</span>đ
                                    </c:if>
                                </p>
                            </c:if>
                            <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                                <div>
                                    <strong class="fs-5">
                                        Tổng: <span class="price-value" data-price="${order.totalAmount}">${order.totalAmount}</span> &#8363;
                                    </strong>
                                    <c:if test="${order.shippingFee != null}">
                                        <small class="text-muted d-block">Đã gồm phí ship</small>
                                    </c:if>
                                </div>
                                <div class="d-flex gap-2 flex-wrap">
                                    <c:if test="${order.vnpay and order.status eq 'PENDING'}">
                                        <a href="${pageContext.request.contextPath}/payment/vnpay/pay?orderId=${order.id}" class="btn btn-souvenir">
                                            <i class="fas fa-credit-card me-1"></i>Thanh toán VNPay
                                        </a>
                                    </c:if>
                                    <a href="${pageContext.request.contextPath}/order-success?id=${order.id}" class="btn btn-outline-souvenir">
                                        <i class="fas fa-eye me-1"></i>Xem chi tiết
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>

