<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Quản lý danh mục"/>
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
            <i class="fas fa-tags me-2"></i>Quản lý danh mục
        </h2>
        <a href="${pageContext.request.contextPath}/admin/categories?action=create" class="admin-btn admin-btn-primary">
            <i class="fas fa-plus"></i>Thêm danh mục
        </a>
    </div>
</div>

<div class="row g-3 mb-4">
    <div class="col-6 col-md-3">
        <div class="cat-stat-card cat-stat-total">
            <span class="cat-stat-label">Tổng danh mục</span>
            <strong class="cat-stat-value">${statTotal}</strong>
        </div>
    </div>
    <div class="col-6 col-md-3">
        <div class="cat-stat-card cat-stat-active">
            <span class="cat-stat-label">Có sản phẩm</span>
            <strong class="cat-stat-value">${statWithProducts}</strong>
        </div>
    </div>
    <div class="col-6 col-md-3">
        <div class="cat-stat-card cat-stat-empty">
            <span class="cat-stat-label">Danh mục trống</span>
            <strong class="cat-stat-value">${statEmpty}</strong>
        </div>
    </div>
    <div class="col-6 col-md-3">
        <div class="cat-stat-card cat-stat-products">
            <span class="cat-stat-label">Tổng sản phẩm</span>
            <strong class="cat-stat-value">${statTotalProducts}</strong>
        </div>
    </div>
</div>

<div class="admin-filters">
    <form method="get" action="${pageContext.request.contextPath}/admin/categories" class="admin-filter-form">
        <input type="hidden" name="page" value="1">
        <div class="admin-filter-row">
            <div class="admin-filter-group admin-filter-group-wide">
                <label class="admin-filter-label">Tìm kiếm</label>
                <input type="text" name="search" class="form-control"
                       value="<c:out value="${search}"/>"
                       placeholder="Tên, mô tả danh mục...">
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">Sản phẩm</label>
                <select name="productFilter" class="form-select">
                    <option value="">Tất cả</option>
                    <option value="with" ${productFilter eq 'with' ? 'selected' : ''}>Có sản phẩm</option>
                    <option value="empty" ${productFilter eq 'empty' ? 'selected' : ''}>Danh mục trống</option>
                </select>
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">Sắp xếp</label>
                <select name="sortBy" class="form-select">
                    <option value="name" ${sortBy eq 'name' ? 'selected' : ''}>Tên</option>
                    <option value="products" ${sortBy eq 'products' ? 'selected' : ''}>Số SP</option>
                    <option value="id" ${sortBy eq 'id' ? 'selected' : ''}>ID</option>
                </select>
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">Thứ tự</label>
                <select name="sortOrder" class="form-select">
                    <option value="ASC" ${sortOrder eq 'ASC' ? 'selected' : ''}>Tăng dần</option>
                    <option value="DESC" ${sortOrder eq 'DESC' ? 'selected' : ''}>Giảm dần</option>
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
    <table class="admin-table admin-table-categories">
        <thead>
            <tr>
                <th>Danh mục</th>
                <th>Mô tả</th>
                <th>Sản phẩm</th>
                <th style="width: 160px;">Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${empty categories}">
                    <tr>
                        <td colspan="4" class="text-center py-5 text-muted">
                            <i class="fas fa-folder-open fa-2x mb-2 d-block"></i>
                            Không tìm thấy danh mục phù hợp
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="c" items="${categories}">
                        <tr class="${c.productCount eq 0 ? 'cat-row-empty' : ''}">
                            <td>
                                <div class="cat-cell">
                                    <div class="cat-icon" aria-hidden="true">
                                        <i class="fas fa-tag"></i>
                                    </div>
                                    <div>
                                        <a href="${pageContext.request.contextPath}/admin/categories?action=detail&id=${c.id}"
                                           class="cat-name-link">
                                            <strong><c:out value="${c.name}"/></strong>
                                        </a>
                                        <span class="text-muted small d-block">#${c.id}</span>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty c.description}">
                                        <span class="cat-desc-preview" title="<c:out value="${c.description}"/>">
                                            <c:choose>
                                                <c:when test="${fn:length(c.description) > 60}">
                                                    <c:out value="${fn:substring(c.description, 0, 60)}"/>...
                                                </c:when>
                                                <c:otherwise><c:out value="${c.description}"/></c:otherwise>
                                            </c:choose>
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted">—</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${c.productCount > 0}">
                                        <a href="${pageContext.request.contextPath}/admin/products?categoryId=${c.id}"
                                           class="cat-product-badge" title="Xem sản phẩm">
                                            ${c.productCount} SP
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="admin-badge admin-badge-warning">Trống</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div class="cat-actions">
                                    <a href="${pageContext.request.contextPath}/admin/categories?action=detail&id=${c.id}"
                                       class="admin-btn admin-btn-outline" title="Chi tiết">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/categories?action=edit&id=${c.id}"
                                       class="admin-btn admin-btn-outline" title="Sửa">
                                        <i class="fas fa-edit"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/products?categoryId=${c.id}"
                                       class="admin-btn admin-btn-success" title="Xem sản phẩm">
                                        <i class="fas fa-box"></i>
                                    </a>
                                    <c:if test="${c.productCount eq 0}">
                                        <a href="${pageContext.request.contextPath}/admin/categories?action=delete&id=${c.id}"
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
                    <a class="page-link" href="?page=${currentPage - 1}&pageSize=${pageSize}&search=${search}&productFilter=${productFilter}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                        <i class="fas fa-chevron-left"></i>
                    </a>
                </li>
            </c:if>
            <c:forEach var="i" begin="${currentPage > 3 ? currentPage - 2 : 1}"
                       end="${currentPage + 2 < totalPages ? currentPage + 2 : totalPages}">
                <li class="page-item ${i eq currentPage ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}&pageSize=${pageSize}&search=${search}&productFilter=${productFilter}&sortBy=${sortBy}&sortOrder=${sortOrder}">${i}</a>
                </li>
            </c:forEach>
            <c:if test="${currentPage < totalPages}">
                <li class="page-item">
                    <a class="page-link" href="?page=${currentPage + 1}&pageSize=${pageSize}&search=${search}&productFilter=${productFilter}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                        <i class="fas fa-chevron-right"></i>
                    </a>
                </li>
            </c:if>
        </ul>
    </nav>
    <div class="text-center mt-2 text-muted small">
        Trang ${currentPage}/${totalPages} — ${totalCategories} danh mục
    </div>
</c:if>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
