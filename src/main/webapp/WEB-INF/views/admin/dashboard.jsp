<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Bảng điều khiển"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<!-- Statistics Cards -->
<div class="row g-4 mb-4">
    <div class="col-md-6 col-lg-3">
        <div class="stat-card stat-card-primary">
            <div class="stat-card-icon">
                <i class="fas fa-box"></i>
            </div>
            <div class="stat-card-content">
                <h6 class="stat-card-label">Tổng sản phẩm</h6>
                <h3 class="stat-card-value">${totalProducts}</h3>
                <a href="${pageContext.request.contextPath}/admin/products" class="stat-card-link">
                    Xem chi tiết <i class="fas fa-arrow-right ms-1"></i>
                </a>
            </div>
        </div>
    </div>
    <div class="col-md-6 col-lg-3">
        <div class="stat-card stat-card-success">
            <div class="stat-card-icon">
                <i class="fas fa-shopping-cart"></i>
            </div>
            <div class="stat-card-content">
                <h6 class="stat-card-label">Tổng đơn hàng</h6>
                <h3 class="stat-card-value">${totalOrders}</h3>
                <a href="${pageContext.request.contextPath}/admin/orders" class="stat-card-link">
                    Xem chi tiết <i class="fas fa-arrow-right ms-1"></i>
                </a>
            </div>
        </div>
    </div>
    <div class="col-md-6 col-lg-3">
        <div class="stat-card stat-card-info">
            <div class="stat-card-icon">
                <i class="fas fa-money-bill-wave"></i>
            </div>
            <div class="stat-card-content">
                <h6 class="stat-card-label">Tổng doanh thu</h6>
                <h3 class="stat-card-value">
                    <span class="price-value" data-price="${totalRevenue}">${totalRevenue}</span> &#8363;
                </h3>
                <span class="stat-card-link">Đã xác nhận & đã giao</span>
            </div>
        </div>
    </div>
    <div class="col-md-6 col-lg-3">
        <div class="stat-card stat-card-warning">
            <div class="stat-card-icon">
                <i class="fas fa-clock"></i>
            </div>
            <div class="stat-card-content">
                <h6 class="stat-card-label">Đơn chờ xử lý</h6>
                <h3 class="stat-card-value">${pendingOrders}</h3>
                <a href="${pageContext.request.contextPath}/admin/orders?status=PENDING" class="stat-card-link">
                    Xử lý ngay <i class="fas fa-arrow-right ms-1"></i>
                </a>
            </div>
        </div>
    </div>
</div>

<!-- Order Status Overview -->
<div class="row g-4">
    <div class="col-md-8">
        <div class="admin-card">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-chart-bar me-2"></i>Thống kê đơn hàng
                </h5>
            </div>
            <div class="card-body">
                <div class="row g-3">
                    <div class="col-6 col-md-3">
                        <div class="status-stat">
                            <div class="status-stat-badge bg-warning">
                                <i class="fas fa-clock"></i>
                            </div>
                            <div class="status-stat-info">
                                <h4>${pendingOrders}</h4>
                                <p>Chờ xử lý</p>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="status-stat">
                            <div class="status-stat-badge bg-success">
                                <i class="fas fa-check-circle"></i>
                            </div>
                            <div class="status-stat-info">
                                <h4>${confirmedOrders}</h4>
                                <p>Đã xác nhận</p>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="status-stat">
                            <div class="status-stat-badge bg-info">
                                <i class="fas fa-shipping-fast"></i>
                            </div>
                            <div class="status-stat-info">
                                <h4>${shippedOrders}</h4>
                                <p>Đã giao hàng</p>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="status-stat">
                            <div class="status-stat-badge bg-danger">
                                <i class="fas fa-times-circle"></i>
                            </div>
                            <div class="status-stat-info">
                                <h4>${cancelledOrders}</h4>
                                <p>Đã hủy</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="admin-card">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-cog me-2"></i>Quản lý nhanh
                </h5>
            </div>
            <div class="card-body">
                <div class="d-grid gap-2">
                    <a href="${pageContext.request.contextPath}/admin/products?action=create" class="admin-btn admin-btn-primary">
                        <i class="fas fa-plus me-2"></i>Thêm sản phẩm
                    </a>
                    <a href="${pageContext.request.contextPath}/admin/categories?action=create" class="admin-btn admin-btn-outline">
                        <i class="fas fa-tag me-2"></i>Thêm danh mục
                    </a>
                    <a href="${pageContext.request.contextPath}/admin/categories" class="admin-btn admin-btn-outline">
                        <i class="fas fa-list me-2"></i>Quản lý danh mục
                    </a>
                    <a href="${pageContext.request.contextPath}/admin/users" class="admin-btn admin-btn-outline">
                        <i class="fas fa-users me-2"></i>Quản lý người dùng
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
