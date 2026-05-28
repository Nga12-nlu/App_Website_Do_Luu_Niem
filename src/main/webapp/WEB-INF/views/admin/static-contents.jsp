<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Nội dung tĩnh website"/>
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
            <i class="fas fa-file-lines me-2"></i>Quản lý nội dung tĩnh
        </h2>
    </div>
</div>

<div class="row g-3 mb-4">
    <div class="col-6 col-md-4">
        <div class="cat-stat-card cat-stat-total">
            <span class="cat-stat-label">Tổng block</span>
            <strong class="cat-stat-value">${statTotal}</strong>
        </div>
    </div>
    <div class="col-6 col-md-4">
        <div class="cat-stat-card cat-stat-active">
            <span class="cat-stat-label">Đang hiển thị</span>
            <strong class="cat-stat-value">${statActive}</strong>
        </div>
    </div>
    <div class="col-6 col-md-4">
        <div class="cat-stat-card cat-stat-empty">
            <span class="cat-stat-label">Đang tắt</span>
            <strong class="cat-stat-value">${statInactive}</strong>
        </div>
    </div>
</div>

<div class="admin-filters">
    <form method="get" action="${pageContext.request.contextPath}/admin/contents" class="admin-filter-form">
        <input type="hidden" name="page" value="1">
        <div class="admin-filter-row">
            <div class="admin-filter-group admin-filter-group-wide">
                <label class="admin-filter-label">Tìm kiếm</label>
                <input type="text" name="search" class="form-control"
                       value="<c:out value="${search}"/>"
                       placeholder="Khóa, nhãn, nội dung...">
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">Nhóm</label>
                <select name="group" class="form-select">
                    <option value="">Tất cả</option>
                    <c:forEach var="g" items="${groups}">
                        <option value="${g}" ${group eq g ? 'selected' : ''}><c:out value="${g}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">/ trang</label>
                <select name="pageSize" class="form-select" onchange="this.form.submit()">
                    <option value="15" ${pageSize eq 15 ? 'selected' : ''}>15</option>
                    <option value="30" ${pageSize eq 30 ? 'selected' : ''}>30</option>
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
    <table class="admin-table admin-table-categories">
        <thead>
        <tr>
            <th>Nhóm</th>
            <th>Nhãn hiển thị</th>
            <th>Khóa</th>
            <th>Giá trị</th>
            <th class="content-status-col">Trạng thái</th>
            <th style="width: 100px;">Thao tác</th>
        </tr>
        </thead>
        <tbody>
        <c:choose>
            <c:when test="${empty contents}">
                <tr>
                    <td colspan="6" class="text-center py-5 text-muted">
                        <i class="fas fa-inbox fa-2x mb-2 d-block"></i>
                        Không có nội dung nào
                    </td>
                </tr>
            </c:when>
            <c:otherwise>
                <c:forEach var="it" items="${contents}">
                    <tr>
                        <td><span class="admin-badge admin-badge-info"><c:out value="${it.groupName}"/></span></td>
                        <td><strong><c:out value="${it.label}"/></strong></td>
                        <td><code><c:out value="${it.contentKey}"/></code></td>
                        <td>
                            <c:choose>
                                <c:when test="${fn:length(it.value) > 80}">
                                    <c:out value="${fn:substring(it.value, 0, 80)}"/>...
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${it.value}"/>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td class="content-status-col">
                            <span class="admin-badge admin-badge-nowrap ${it.active ? 'admin-badge-success' : 'admin-badge-warning'}">
                                ${it.active ? 'Hiển thị' : 'Đang tắt'}
                            </span>
                        </td>
                        <td>
                            <a href="${pageContext.request.contextPath}/admin/contents?action=edit&id=${it.id}"
                               class="admin-btn admin-btn-outline" title="Chỉnh sửa">
                                <i class="fas fa-edit"></i>
                            </a>
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
                    <a class="page-link" href="?page=${currentPage - 1}&pageSize=${pageSize}&search=${search}&group=${group}">
                        <i class="fas fa-chevron-left"></i>
                    </a>
                </li>
            </c:if>
            <c:forEach var="i" begin="${currentPage > 3 ? currentPage - 2 : 1}"
                       end="${currentPage + 2 < totalPages ? currentPage + 2 : totalPages}">
                <li class="page-item ${i eq currentPage ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}&pageSize=${pageSize}&search=${search}&group=${group}">${i}</a>
                </li>
            </c:forEach>
            <c:if test="${currentPage < totalPages}">
                <li class="page-item">
                    <a class="page-link" href="?page=${currentPage + 1}&pageSize=${pageSize}&search=${search}&group=${group}">
                        <i class="fas fa-chevron-right"></i>
                    </a>
                </li>
            </c:if>
        </ul>
    </nav>
</c:if>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
