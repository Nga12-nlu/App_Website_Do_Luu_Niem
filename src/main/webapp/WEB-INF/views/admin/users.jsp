<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.app.app_website_do_luu_niem.model.UserAdminRow" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Quản lý người dùng"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<c:if test="${not empty flashMessage}">
    <div class="alert alert-${flashType} alert-dismissible fade show admin-flash" role="alert">
        <i class="fas fa-${flashType eq 'success' ? 'check-circle' : 'exclamation-triangle'} me-2"></i>
        <c:out value="${flashMessage}"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<div class="admin-card">
    <div class="admin-card-header">
        <h2 class="admin-card-title">
            <i class="fas fa-users me-2"></i>Quản lý người dùng
        </h2>
        <a href="${pageContext.request.contextPath}/admin/users?action=create" class="admin-btn admin-btn-primary">
            <i class="fas fa-user-plus"></i>Thêm người dùng
        </a>
    </div>
</div>

<div class="row g-3 mb-4">
    <div class="col-6 col-md-3">
        <div class="user-stat-card user-stat-total">
            <span class="user-stat-label">Tổng tài khoản</span>
            <strong class="user-stat-value">${statTotal}</strong>
        </div>
    </div>
    <div class="col-6 col-md-3">
        <div class="user-stat-card user-stat-admin">
            <span class="user-stat-label">Quản trị viên</span>
            <strong class="user-stat-value">${statAdmins}</strong>
        </div>
    </div>
    <div class="col-6 col-md-3">
        <div class="user-stat-card user-stat-customer">
            <span class="user-stat-label">Khách hàng</span>
            <strong class="user-stat-value">${statCustomers}</strong>
        </div>
    </div>
    <div class="col-6 col-md-3">
        <div class="user-stat-card user-stat-active">
            <span class="user-stat-label">Đang hoạt động</span>
            <strong class="user-stat-value">${statActive}</strong>
            <small class="text-muted">${statInactive} đã khóa</small>
        </div>
    </div>
</div>

<div class="admin-filters">
    <form method="get" action="${pageContext.request.contextPath}/admin/users" class="admin-filter-form">
        <input type="hidden" name="page" value="1">
        <div class="admin-filter-row">
            <div class="admin-filter-group admin-filter-group-wide">
                <label class="admin-filter-label">Tìm kiếm</label>
                <input type="text" name="search" class="form-control"
                       value="<c:out value="${search}"/>"
                       placeholder="Họ tên, email...">
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">Vai trò</label>
                <select name="role" class="form-select">
                    <option value="">Tất cả</option>
                    <option value="ADMIN" ${role eq 'ADMIN' ? 'selected' : ''}>Quản trị viên</option>
                    <option value="CUSTOMER" ${role eq 'CUSTOMER' ? 'selected' : ''}>Khách hàng</option>
                </select>
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">Trạng thái</label>
                <select name="active" class="form-select">
                    <option value="">Tất cả</option>
                    <option value="active" ${activeFilter eq 'active' ? 'selected' : ''}>Hoạt động</option>
                    <option value="inactive" ${activeFilter eq 'inactive' ? 'selected' : ''}>Đã khóa</option>
                </select>
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">Sắp xếp</label>
                <select name="sortBy" class="form-select">
                    <option value="created_at" ${sortBy eq 'created_at' ? 'selected' : ''}>Ngày tạo</option>
                    <option value="name" ${sortBy eq 'name' ? 'selected' : ''}>Họ tên</option>
                    <option value="email" ${sortBy eq 'email' ? 'selected' : ''}>Email</option>
                    <option value="role" ${sortBy eq 'role' ? 'selected' : ''}>Vai trò</option>
                    <option value="orders" ${sortBy eq 'orders' ? 'selected' : ''}>Số đơn</option>
                </select>
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">Thứ tự</label>
                <select name="sortOrder" class="form-select">
                    <option value="DESC" ${sortOrder eq 'DESC' ? 'selected' : ''}>Giảm dần</option>
                    <option value="ASC" ${sortOrder eq 'ASC' ? 'selected' : ''}>Tăng dần</option>
                </select>
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">/ trang</label>
                <select name="pageSize" class="form-select" onchange="this.form.submit()">
                    <option value="10" ${pageSize eq 10 ? 'selected' : ''}>10</option>
                    <option value="25" ${pageSize eq 25 ? 'selected' : ''}>25</option>
                    <option value="50" ${pageSize eq 50 ? 'selected' : ''}>50</option>
                </select>
            </div>
            <div class="admin-filter-group">
                <button type="submit" class="admin-btn admin-btn-primary w-100">
                    <i class="fas fa-search"></i>Lọc
                </button>
            </div>
        </div>
    </form>
</div>

<div class="admin-table-wrapper">
    <table class="admin-table admin-table-users">
        <thead>
            <tr>
                <th>Người dùng</th>
                <th>Vai trò</th>
                <th>Trạng thái</th>
                <th>Đơn hàng</th>
                <th>Ngày tạo</th>
                <th style="width: 160px;">Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${empty users}">
                    <tr>
                        <td colspan="6" class="text-center py-5 text-muted">
                            <i class="fas fa-user-slash fa-2x mb-2 d-block"></i>
                            Không tìm thấy người dùng phù hợp
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="u" items="${users}">
                        <tr class="${not u.active ? 'user-row-inactive' : ''}">
                            <td>
                                <div class="user-cell">
                                    <div class="user-avatar" aria-hidden="true">
                                        ${fn:substring(u.fullName, 0, 1)}
                                    </div>
                                    <div>
                                        <a href="${pageContext.request.contextPath}/admin/users?action=detail&id=${u.id}"
                                           class="user-name-link">
                                            <strong><c:out value="${u.fullName}"/></strong>
                                        </a>
                                        <div class="user-email-sub"><c:out value="${u.email}"/></div>
                                        <span class="text-muted small">#${u.id}</span>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <span class="admin-badge ${u.admin ? 'admin-badge-danger' : 'admin-badge-info'}">
                                    ${u.admin ? 'Quản trị viên' : 'Khách hàng'}
                                </span>
                            </td>
                            <td>
                                <span class="admin-badge ${u.active ? 'admin-badge-success' : 'admin-badge-warning'}">
                                    <i class="fas fa-${u.active ? 'circle-check' : 'ban'} me-1"></i>
                                    ${u.active ? 'Hoạt động' : 'Đã khóa'}
                                </span>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${u.orderCount > 0}">
                                        <a href="${pageContext.request.contextPath}/admin/orders?search=${u.email}"
                                           class="user-order-badge" title="Xem đơn hàng">
                                            ${u.orderCount} đơn
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted">—</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-nowrap">
                                <%
                                    UserAdminRow row = (UserAdminRow) pageContext.getAttribute("u");
                                    if (row != null && row.getCreatedAt() != null) {
                                        out.print(row.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                    } else {
                                        out.print("—");
                                    }
                                %>
                            </td>
                            <td>
                                <div class="user-actions">
                                    <a href="${pageContext.request.contextPath}/admin/users?action=detail&id=${u.id}"
                                       class="admin-btn admin-btn-outline" title="Chi tiết">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/users?action=edit&id=${u.id}"
                                       class="admin-btn admin-btn-outline" title="Sửa">
                                        <i class="fas fa-edit"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/users?action=toggle-active&id=${u.id}"
                                       class="admin-btn ${u.active ? 'admin-btn-warning' : 'admin-btn-success'}"
                                       onclick="return confirm('${u.active ? 'Khóa' : 'Kích hoạt'} tài khoản &quot;<c:out value="${u.fullName}"/>&quot;?');"
                                       title="${u.active ? 'Khóa' : 'Kích hoạt'}">
                                        <i class="fas fa-${u.active ? 'lock' : 'unlock'}"></i>
                                    </a>
                                    <c:if test="${u.orderCount eq 0 and not u.admin}">
                                        <a href="${pageContext.request.contextPath}/admin/users?action=delete&id=${u.id}"
                                           class="admin-btn admin-btn-danger"
                                           data-confirm-delete
                                           title="Xóa">
                                            <i class="fas fa-trash"></i>
                                        </a>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>

<c:if test="${totalPages > 1}">
    <nav class="admin-pagination">
        <ul class="pagination mb-0">
            <c:if test="${currentPage > 1}">
                <li class="page-item">
                    <a class="page-link" href="?page=${currentPage - 1}&pageSize=${pageSize}&search=${search}&role=${role}&active=${activeFilter}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                        <i class="fas fa-chevron-left"></i>
                    </a>
                </li>
            </c:if>
            <c:forEach var="i" begin="${currentPage > 3 ? currentPage - 2 : 1}"
                       end="${currentPage + 2 < totalPages ? currentPage + 2 : totalPages}">
                <li class="page-item ${i eq currentPage ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}&pageSize=${pageSize}&search=${search}&role=${role}&active=${activeFilter}&sortBy=${sortBy}&sortOrder=${sortOrder}">${i}</a>
                </li>
            </c:forEach>
            <c:if test="${currentPage < totalPages}">
                <li class="page-item">
                    <a class="page-link" href="?page=${currentPage + 1}&pageSize=${pageSize}&search=${search}&role=${role}&active=${activeFilter}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                        <i class="fas fa-chevron-right"></i>
                    </a>
                </li>
            </c:if>
        </ul>
    </nav>
    <div class="text-center mt-2 text-muted small">
        Trang ${currentPage}/${totalPages} — ${totalUsers} người dùng
    </div>
</c:if>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
