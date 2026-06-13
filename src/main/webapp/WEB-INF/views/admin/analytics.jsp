<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Thống kê chi tiết"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<div class="admin-card mb-4">
    <div class="admin-card-header">
        <h2 class="admin-card-title">
            <i class="fas fa-chart-pie me-2"></i>Thống kê chi tiết tồn kho &amp; Hao hụt
        </h2>
    </div>
</div>

<!-- Metrics Cards -->
<div class="row g-4 mb-4">
    <div class="col-md-6">
        <div class="stat-card stat-card-warning" style="border-left: 4px solid var(--admin-warning);">
            <div class="stat-card-body p-4">
                <div class="stat-card-icon text-warning" style="background: rgba(245, 158, 11, 0.1);"><i class="fas fa-coins"></i></div>
                <div class="stat-card-content ms-3">
                    <span class="stat-card-label text-muted">Vốn đọng trong kho (Dead Stock)</span>
                    <h3 class="stat-card-value text-warning mt-1 mb-2">
                        <span class="price-value" data-price="${totalCapitalTiedUp}">${totalCapitalTiedUp}</span>đ
                    </h3>
                    <span class="stat-card-meta text-muted">Tổng giá trị sản phẩm không có đơn hàng trong <strong>${deadStockDays} ngày</strong> qua</span>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="stat-card stat-card-danger" style="border-left: 4px solid var(--admin-danger);">
            <div class="stat-card-body p-4">
                <div class="stat-card-icon text-danger" style="background: rgba(239, 68, 68, 0.1);"><i class="fas fa-exclamation-triangle"></i></div>
                <div class="stat-card-content ms-3">
                    <span class="stat-card-label text-muted">Tổng thiệt hại hao hụt</span>
                    <h3 class="stat-card-value text-danger mt-1 mb-2">
                        <span class="price-value" data-price="${totalLossValue}">${totalLossValue}</span>đ
                    </h3>
                    <span class="stat-card-meta text-muted">Do hư hỏng (DAMAGE), thất thoát (LOSS) &amp; tiêu hủy (DESTROYED)</span>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Dead Stock Segment -->
<div class="admin-card">
    <div class="admin-card-header d-flex justify-content-between align-items-center flex-wrap gap-3">
        <h5 class="admin-card-title mb-0">
            <i class="fas fa-archive me-2 text-warning"></i>Báo cáo hàng tồn đọng (Dead Stock)
        </h5>
        
        <form method="get" action="${pageContext.request.contextPath}/admin/analytics" class="d-flex align-items-center gap-2">
            <label class="admin-filter-label text-nowrap mb-0">Thời gian không phát sinh đơn:</label>
            <select name="deadStockDays" class="form-select form-select-sm w-auto" onchange="this.form.submit()">
                <option value="7" ${deadStockDays eq 7 ? 'selected' : ''}>7 ngày qua</option>
                <option value="15" ${deadStockDays eq 15 ? 'selected' : ''}>15 ngày qua</option>
                <option value="30" ${deadStockDays eq 30 ? 'selected' : ''}>30 ngày qua</option>
                <option value="60" ${deadStockDays eq 60 ? 'selected' : ''}>60 ngày qua</option>
                <option value="90" ${deadStockDays eq 90 ? 'selected' : ''}>90 ngày qua</option>
                <option value="180" ${deadStockDays eq 180 ? 'selected' : ''}>180 ngày qua</option>
            </select>
        </form>
    </div>
</div>

<div class="admin-table-wrapper mb-5">
    <table class="admin-table">
        <thead>
            <tr>
                <th>Mã</th>
                <th>Sản phẩm / Biến thể</th>
                <th>Mã SKU</th>
                <th>Tồn kho</th>
                <th>Giá niêm yết</th>
                <th>Vốn ứ đọng</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${empty deadStockList}">
                    <tr>
                        <td colspan="6" class="text-center py-4 text-muted">
                            <i class="fas fa-check-circle fa-2x mb-2 d-block text-success"></i>
                            Tất cả sản phẩm đều đang bán tốt trong vòng ${deadStockDays} ngày qua!
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="ds" items="${deadStockList}">
                        <tr>
                            <td>#${ds.productId}</td>
                            <td>
                                <strong><c:out value="${ds.productName}"/></strong>
                                <c:if test="${not empty ds.variantName}">
                                    <br><span class="admin-badge admin-badge-info"><c:out value="${ds.variantName}"/></span>
                                </c:if>
                            </td>
                            <td><c:out value="${ds.sku != null ? ds.sku : '—'}"/></td>
                            <td><strong>${ds.stockQty}</strong></td>
                            <td><span class="price-value" data-price="${ds.price}">${ds.price}</span>đ</td>
                            <td>
                                <strong class="text-warning">
                                    <span class="price-value" data-price="${ds.capitalTiedUp}">${ds.capitalTiedUp}</span>đ
                                </strong>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>

<!-- Losses Segment -->
<div class="row g-4">
    <!-- Loss Breakdown -->
    <div class="col-lg-5">
        <div class="admin-card">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-chart-pie me-2 text-danger"></i>Cơ cấu thiệt hại hao hụt
                </h5>
            </div>
            <div class="admin-table-wrapper">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>Loại hao hụt</th>
                            <th>Số lượng hỏng</th>
                            <th>Giá trị thiệt hại</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty lossBreakdown}">
                                <tr>
                                    <td colspan="3" class="text-center py-4 text-muted">
                                        Chưa ghi nhận thiệt hại hao hụt nào.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="lb" items="${lossBreakdown}">
                                    <tr>
                                        <td>
                                            <c:choose>
                                                <c:when test="${lb.type eq 'DAMAGE'}">
                                                    <span class="admin-badge admin-badge-warning">DAMAGE (Hư hỏng)</span>
                                                </c:when>
                                                <c:when test="${lb.type eq 'LOSS'}">
                                                    <span class="admin-badge admin-badge-danger">LOSS (Thất thoát)</span>
                                                </c:when>
                                                <c:when test="${lb.type eq 'DESTROYED'}">
                                                    <span class="admin-badge admin-badge-danger">DESTROYED (Tiêu hủy)</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="admin-badge">${lb.type}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><strong>${lb.totalQty}</strong></td>
                                        <td>
                                            <strong class="text-danger">
                                                <span class="price-value" data-price="${lb.totalVal}">${lb.totalVal}</span>đ
                                            </strong>
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

    <!-- Top Loss Items -->
    <div class="col-lg-7">
        <div class="admin-card">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-exclamation-circle me-2 text-danger"></i>Top 10 sản phẩm hao hại nhiều nhất
                </h5>
            </div>
            <div class="admin-table-wrapper">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>Sản phẩm / Biến thể</th>
                            <th>Tổng số lượng hỏng</th>
                            <th>Ước lượng thiệt hại</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty topLossItems}">
                                <tr>
                                    <td colspan="3" class="text-center py-4 text-muted">
                                        Chưa có sản phẩm nào bị hao hụt.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="li" items="${topLossItems}">
                                    <tr>
                                        <td>
                                            <strong><c:out value="${li.productName}"/></strong>
                                            <c:if test="${not empty li.variantName}">
                                                <br><span class="admin-badge admin-badge-info"><c:out value="${li.variantName}"/></span>
                                            </c:if>
                                        </td>
                                        <td><strong>${li.totalQty}</strong></td>
                                        <td>
                                            <strong class="text-danger">
                                                <span class="price-value" data-price="${li.totalVal}">${li.totalVal}</span>đ
                                            </strong>
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
</div>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
