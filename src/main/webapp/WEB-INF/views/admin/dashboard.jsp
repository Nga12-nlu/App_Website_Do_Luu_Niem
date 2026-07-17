<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.app.app_website_do_luu_niem.model.Order" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Bảng điều khiển"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<div class="dash-welcome mb-4">
    <div class="dash-welcome-inner">
        <div class="dash-welcome-text">
            <p class="dash-welcome-greeting mb-1">Xin chào, <strong><c:out value="${sessionScope.currentUser.fullName}"/></strong></p>
            <h2 class="dash-welcome-title mb-2">Tổng quan cửa hàng</h2>
            <p class="dash-welcome-date mb-0">
                <i class="far fa-calendar-alt me-2"></i><c:out value="${todayFormatted}"/>
            </p>
        </div>
        <div class="dash-welcome-actions d-none d-md-flex gap-2">
            <a href="${pageContext.request.contextPath}/admin/orders?status=PENDING" class="admin-btn admin-btn-light">
                <i class="fas fa-clock me-2"></i>${pendingOrders} đơn chờ
            </a>
            <a href="${pageContext.request.contextPath}/admin/products?action=create" class="admin-btn admin-btn-primary">
                <i class="fas fa-plus me-2"></i>Thêm sản phẩm
            </a>
        </div>
    </div>
</div>

<div class="row g-3 g-xl-4 mb-4">
    <div class="col-sm-6 col-xl-3">
        <div class="stat-card stat-card-primary">
            <div class="stat-card-body">
                <div class="stat-card-icon"><i class="fas fa-box"></i></div>
                <div class="stat-card-content">
                    <span class="stat-card-label">Tổng sản phẩm</span>
                    <h3 class="stat-card-value">${totalProducts}</h3>
                    <span class="stat-card-meta">${totalCategories} danh mục</span>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/admin/products" class="stat-card-footer">
                Quản lý sản phẩm <i class="fas fa-arrow-right ms-1"></i>
            </a>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="stat-card stat-card-success">
            <div class="stat-card-body">
                <div class="stat-card-icon"><i class="fas fa-shopping-bag"></i></div>
                <div class="stat-card-content">
                    <span class="stat-card-label">Tổng đơn hàng</span>
                    <h3 class="stat-card-value">${totalOrders}</h3>
                    <span class="stat-card-meta">${ordersThisMonth} đơn tháng này</span>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/admin/orders" class="stat-card-footer">
                Xem tất cả đơn <i class="fas fa-arrow-right ms-1"></i>
            </a>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="stat-card stat-card-info">
            <div class="stat-card-body">
                <div class="stat-card-icon"><i class="fas fa-chart-line"></i></div>
                <div class="stat-card-content">
                    <span class="stat-card-label">Tổng doanh thu</span>
                    <h3 class="stat-card-value">
                        <span class="price-value" data-price="${totalRevenue}">${totalRevenue}</span><small>đ</small>
                    </h3>
                    <span class="stat-card-meta">Đã xác nhận &amp; đã giao</span>
                </div>
            </div>
            <span class="stat-card-footer stat-card-footer-static">
                TB đơn: <span class="price-value" data-price="${averageOrderValue}">${averageOrderValue}</span>đ
            </span>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="stat-card stat-card-warning">
            <div class="stat-card-body">
                <div class="stat-card-icon"><i class="fas fa-exclamation-circle"></i></div>
                <div class="stat-card-content">
                    <span class="stat-card-label">Cần chú ý</span>
                    <h3 class="stat-card-value">${pendingOrders + lowStockCount}</h3>
                    <span class="stat-card-meta">${pendingOrders} đơn chờ · ${lowStockCount} SP sắp hết</span>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/admin/orders?status=PENDING" class="stat-card-footer">
                Xử lý ngay <i class="fas fa-arrow-right ms-1"></i>
            </a>
        </div>
    </div>
</div>

<div class="row g-3 mb-4">
    <div class="col-6 col-md-3">
        <div class="mini-metric">
            <i class="fas fa-users text-primary"></i>
            <div>
                <span class="mini-metric-value">${totalCustomers}</span>
                <span class="mini-metric-label">Khách hàng</span>
            </div>
        </div>
    </div>
    <div class="col-6 col-md-3">
        <div class="mini-metric">
            <i class="fas fa-calendar-check text-success"></i>
            <div>
                <span class="mini-metric-value">${ordersThisMonth}</span>
                <span class="mini-metric-label">Đơn tháng này</span>
            </div>
        </div>
    </div>
    <div class="col-6 col-md-3">
        <div class="mini-metric">
            <i class="fas fa-coins text-info"></i>
            <div>
                <span class="mini-metric-value price-value" data-price="${revenueThisMonth}">${revenueThisMonth}</span>
                <span class="mini-metric-label">Doanh thu tháng</span>
            </div>
        </div>
    </div>
    <div class="col-6 col-md-3">
        <div class="mini-metric">
            <i class="fas fa-warehouse text-danger"></i>
            <div>
                <span class="mini-metric-value">${lowStockCount}</span>
                <span class="mini-metric-label">SP tồn ≤ ${lowStockThreshold}</span>
            </div>
        </div>
    </div>
</div>

<div class="row g-4 mb-4">
    <div class="col-lg-8">
        <div class="admin-card admin-card-chart h-100">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-chart-area me-2 text-primary"></i>Doanh thu 7 ngày qua
                </h5>
                <span class="dash-chart-hint">Đơn đã xác nhận &amp; đã giao</span>
            </div>
            <div class="admin-card-body chart-wrap">
                <canvas id="revenueChart" height="120"></canvas>
            </div>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="admin-card admin-card-chart h-100">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-chart-pie me-2 text-primary"></i>Trạng thái đơn
                </h5>
            </div>
            <div class="admin-card-body d-flex flex-column align-items-center">
                <div class="chart-donut-wrap">
                    <canvas id="orderStatusChart"></canvas>
                </div>
                <div class="order-legend mt-3 w-100">
                    <div class="order-legend-item">
                        <span class="legend-dot bg-warning"></span>
                        <span>Chờ xử lý</span>
                        <strong>${pendingOrders}</strong>
                    </div>
                    <div class="order-legend-item">
                        <span class="legend-dot bg-success"></span>
                        <span>Đã xác nhận</span>
                        <strong>${confirmedOrders}</strong>
                    </div>
                    <div class="order-legend-item">
                        <span class="legend-dot bg-info"></span>
                        <span>Đã giao</span>
                        <strong>${shippedOrders}</strong>
                    </div>
                    <div class="order-legend-item">
                        <span class="legend-dot bg-danger"></span>
                        <span>Đã hủy</span>
                        <strong>${cancelledOrders}</strong>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="row g-4">
    <div class="col-xl-8">
        <div class="admin-card admin-card-flush">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-receipt me-2 text-primary"></i>Đơn hàng gần đây
                </h5>
                <a href="${pageContext.request.contextPath}/admin/orders" class="admin-btn admin-btn-outline admin-btn-sm">
                    Xem tất cả
                </a>
            </div>
            <div class="admin-table-responsive">
                <table class="admin-table admin-table-compact mb-0">
                    <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Khách hàng</th>
                            <th>Tổng tiền</th>
                            <th>Trạng thái</th>
                            <th>Thời gian</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty recentOrders}">
                                <tr>
                                    <td colspan="6" class="text-center py-5 text-muted">
                                        <i class="fas fa-inbox fa-2x mb-2 d-block opacity-50"></i>
                                        Chưa có đơn hàng nào
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="o" items="${recentOrders}">
                                    <tr>
                                        <td><strong class="text-primary">#${o.id}</strong></td>
                                        <td>
                                            <div class="dash-customer-name"><c:out value="${o.user.fullName}"/></div>
                                            <small class="text-muted"><c:out value="${o.user.email}"/></small>
                                        </td>
                                        <td>
                                            <strong><span class="price-value" data-price="${o.totalAmount}">${o.totalAmount}</span>đ</strong>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${o.status eq 'PENDING'}">
                                                    <span class="admin-badge admin-badge-warning">Chờ xử lý</span>
                                                </c:when>
                                                <c:when test="${o.status eq 'CONFIRMED'}">
                                                    <span class="admin-badge admin-badge-success">Đã xác nhận</span>
                                                </c:when>
                                                <c:when test="${o.status eq 'PACKAGING'}">
                                                    <span class="admin-badge admin-badge-warning">Đang đóng gói</span>
                                                </c:when>
                                                <c:when test="${o.status eq 'AWAITING_SHIPPING'}">
                                                    <span class="admin-badge admin-badge-warning">Chờ giao ĐVVC</span>
                                                </c:when>
                                                <c:when test="${o.status eq 'SHIPPING'}">
                                                    <span class="admin-badge admin-badge-info">Đang giao hàng</span>
                                                </c:when>
                                                <c:when test="${o.status eq 'SHIPPED'}">
                                                    <span class="admin-badge admin-badge-success">Đã giao hàng</span>
                                                </c:when>
                                                <c:when test="${o.status eq 'CANCELLED'}">
                                                    <span class="admin-badge admin-badge-danger">Đã hủy</span>
                                                </c:when>
                                                <c:otherwise><span class="admin-badge">${o.status}</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-nowrap text-muted small">
                                            <%
                                                Order o = (Order) pageContext.getAttribute("o");
                                                if (o != null && o.getCreatedAt() != null) {
                                                    out.print(o.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                                                }
                                            %>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/orders?action=detail&id=${o.id}"
                                               class="admin-btn admin-btn-outline admin-btn-sm" title="Chi tiết">
                                                <i class="fas fa-eye"></i>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="col-xl-4">
        <div class="admin-card admin-card-flush mb-4">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-bolt me-2 text-warning"></i>Thao tác nhanh
                </h5>
            </div>
            <div class="admin-card-body pt-0">
                <div class="quick-action-grid">
                    <a href="${pageContext.request.contextPath}/admin/products?action=create" class="quick-action-item quick-action-primary">
                        <i class="fas fa-plus-circle"></i>
                        <span>Thêm sản phẩm</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/admin/categories?action=create" class="quick-action-item">
                        <i class="fas fa-tag"></i>
                        <span>Thêm danh mục</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/admin/orders" class="quick-action-item">
                        <i class="fas fa-list-alt"></i>
                        <span>Đơn hàng</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/admin/users" class="quick-action-item">
                        <i class="fas fa-user-cog"></i>
                        <span>Người dùng</span>
                    </a>
                </div>
            </div>
        </div>

        <div class="admin-card admin-card-flush">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-box-open me-2 text-danger"></i>Sắp hết hàng
                </h5>
                <a href="${pageContext.request.contextPath}/admin/products" class="admin-btn admin-btn-outline admin-btn-sm">Tất cả</a>
            </div>
            <div class="admin-card-body pt-0">
                <c:choose>
                    <c:when test="${empty lowStockProducts}">
                        <p class="text-muted text-center py-4 mb-0">
                            <i class="fas fa-check-circle text-success me-1"></i> Tồn kho ổn định
                        </p>
                    </c:when>
                    <c:otherwise>
                        <ul class="low-stock-list list-unstyled mb-0">
                            <c:forEach var="p" items="${lowStockProducts}">
                                <li class="low-stock-item">
                                    <div class="low-stock-info">
                                        <a href="${pageContext.request.contextPath}/admin/products?action=edit&id=${p.id}" class="low-stock-name">
                                            <c:out value="${p.name}"/>
                                        </a>
                                        <small class="text-muted">Còn <strong class="${p.stock le 5 ? 'text-danger' : 'text-warning'}">${p.stock}</strong> sp</small>
                                    </div>
                                    <div class="low-stock-bar-wrap">
                                        <c:set var="barPct" value="${p.stock * 100 / lowStockThreshold}"/>
                                        <div class="low-stock-bar" style="width: ${barPct > 100 ? 100 : barPct}%"></div>
                                    </div>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<script type="application/json" id="revenueChartData">
[
<c:forEach var="pt" items="${revenueChart}" varStatus="st">
{"label":"${pt.label}","revenue":${pt.revenue},"orders":${pt.orderCount}}<c:if test="${!st.last}">,</c:if>
</c:forEach>
]
</script>
<script>
    window.dashboardOrderStats = {
        pending: ${pendingOrders},
        confirmed: ${confirmedOrders},
        shipped: ${shippedOrders},
        cancelled: ${cancelledOrders}
    };
</script>

<c:set var="dashboardPage" value="true" scope="request"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
