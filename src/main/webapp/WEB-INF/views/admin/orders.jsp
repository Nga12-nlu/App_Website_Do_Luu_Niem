<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.app.app_website_do_luu_niem.model.Order" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quản lý đơn hàng"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<div class="admin-card">
    <div class="admin-card-header">
        <h2 class="admin-card-title">
            <i class="fas fa-shopping-cart me-2"></i>Quản lý đơn hàng
        </h2>
    </div>
</div>

<!-- Filters -->
<div class="admin-filters">
    <form method="get" action="${pageContext.request.contextPath}/admin/orders" class="admin-filter-form">
        <input type="hidden" name="page" value="1">
        <div class="admin-filter-row">
            <div class="admin-filter-group">
                <label class="admin-filter-label">Tìm kiếm</label>
                <input type="text" name="search" class="form-control" 
                       value="<c:out value="${search}"/>" 
                       placeholder="ID, tên khách hàng, email...">
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">Trạng thái</label>
                <select name="status" class="form-select">
                    <option value="">Tất cả trạng thái</option>
                    <option value="PENDING" ${status eq 'PENDING' ? 'selected' : ''}>Chờ xử lý</option>
                    <option value="CONFIRMED" ${status eq 'CONFIRMED' ? 'selected' : ''}>Đã xác nhận</option>
                    <option value="SHIPPED" ${status eq 'SHIPPED' ? 'selected' : ''}>Đã giao hàng</option>
                    <option value="CANCELLED" ${status eq 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                </select>
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">Số lượng/trang</label>
                <select name="pageSize" class="form-select" onchange="this.form.submit()">
                    <option value="10" ${pageSize eq 10 ? 'selected' : ''}>10</option>
                    <option value="25" ${pageSize eq 25 ? 'selected' : ''}>25</option>
                    <option value="50" ${pageSize eq 50 ? 'selected' : ''}>50</option>
                    <option value="100" ${pageSize eq 100 ? 'selected' : ''}>100</option>
                </select>
            </div>
            <div class="admin-filter-group">
                <button type="submit" class="admin-btn admin-btn-primary w-100">
                    <i class="fas fa-search"></i>Tìm kiếm
                </button>
            </div>
        </div>
    </form>
</div>

<!-- Orders Table -->
<div class="admin-table-wrapper">
    <table class="admin-table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Khách hàng</th>
                <th>Email</th>
                <th>Tổng tiền</th>
                <th>Trạng thái</th>
                <th>Ngày đặt</th>
                <th style="width: 120px;">Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${empty orders}">
                    <tr>
                        <td colspan="7" class="text-center py-4 text-muted">
                            <i class="fas fa-inbox fa-2x mb-2 d-block"></i>
                            Không tìm thấy đơn hàng nào
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="o" items="${orders}">
                        <tr>
                            <td><strong>#${o.id}</strong></td>
                            <td><c:out value="${o.user.fullName}"/></td>
                            <td><c:out value="${o.user.email}"/></td>
                            <td>
                                <strong>
                                    <span class="price-value" data-price="${o.totalAmount}">${o.totalAmount}</span> &#8363;
                                </strong>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${o.status eq 'PENDING'}">
                                        <span class="admin-badge admin-badge-warning">Chờ xử lý</span>
                                    </c:when>
                                    <c:when test="${o.status eq 'CONFIRMED'}">
                                        <span class="admin-badge admin-badge-success">Đã xác nhận</span>
                                    </c:when>
                                    <c:when test="${o.status eq 'SHIPPED'}">
                                        <span class="admin-badge admin-badge-info">Đã giao hàng</span>
                                    </c:when>
                                    <c:when test="${o.status eq 'CANCELLED'}">
                                        <span class="admin-badge admin-badge-danger">Đã hủy</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="admin-badge">${o.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <%
                                    Order o = (Order) pageContext.getAttribute("o");
                                    if (o != null && o.getCreatedAt() != null) {
                                        out.print(o.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                                    }
                                %>
                            </td>
                            <td>
                                <a href="${pageContext.request.contextPath}/admin/orders?action=detail&id=${o.id}" 
                                   class="admin-btn admin-btn-outline" 
                                   title="Chi tiết">
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

<!-- Pagination -->
<c:if test="${totalPages > 1}">
    <nav class="admin-pagination">
        <ul class="pagination mb-0">
            <c:if test="${currentPage > 1}">
                <li class="page-item">
                    <a class="page-link" href="?page=${currentPage - 1}&pageSize=${pageSize}&status=${status}&search=${search}">
                        <i class="fas fa-chevron-left"></i>
                    </a>
                </li>
            </c:if>
            
            <c:forEach var="i" begin="${currentPage > 3 ? currentPage - 2 : 1}" 
                       end="${currentPage + 2 < totalPages ? currentPage + 2 : totalPages}">
                <li class="page-item ${i eq currentPage ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}&pageSize=${pageSize}&status=${status}&search=${search}">
                        ${i}
                    </a>
                </li>
            </c:forEach>
            
            <c:if test="${currentPage < totalPages}">
                <li class="page-item">
                    <a class="page-link" href="?page=${currentPage + 1}&pageSize=${pageSize}&status=${status}&search=${search}">
                        <i class="fas fa-chevron-right"></i>
                    </a>
                </li>
            </c:if>
        </ul>
    </nav>
    <div class="text-center mt-2 text-muted">
        Hiển thị ${(currentPage - 1) * pageSize + 1} - ${currentPage * pageSize > totalOrders ? totalOrders : currentPage * pageSize} 
        trong tổng số ${totalOrders} đơn hàng
    </div>
</c:if>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
