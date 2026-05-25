<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Chi tiết danh mục"/>
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
            <i class="fas fa-tag me-2"></i>Chi tiết danh mục
        </h2>
        <div class="d-flex flex-wrap gap-2">
            <a href="${pageContext.request.contextPath}/admin/categories?action=edit&id=${category.id}"
               class="admin-btn admin-btn-primary">
                <i class="fas fa-edit"></i>Sửa
            </a>
            <a href="${pageContext.request.contextPath}/admin/products?action=create"
               class="admin-btn admin-btn-success">
                <i class="fas fa-plus"></i>Thêm sản phẩm
            </a>
            <a href="${pageContext.request.contextPath}/admin/categories" class="admin-btn admin-btn-outline">
                <i class="fas fa-arrow-left"></i>Danh sách
            </a>
        </div>
    </div>
</div>

<div class="row g-4">
    <div class="col-lg-4">
        <div class="admin-card cat-profile-card">
            <div class="card-body p-4 text-center">
                <div class="cat-icon cat-icon-lg mx-auto mb-3">
                    <i class="fas fa-tags"></i>
                </div>
                <h4 class="mb-2"><c:out value="${category.name}"/></h4>
                <p class="text-muted small mb-3">ID #${category.id}</p>
                <c:choose>
                    <c:when test="${not empty category.description}">
                        <p class="cat-detail-desc text-start"><c:out value="${category.description}"/></p>
                    </c:when>
                    <c:otherwise>
                        <p class="text-muted fst-italic">Chưa có mô tả</p>
                    </c:otherwise>
                </c:choose>
                <div class="mt-3">
                    <c:choose>
                        <c:when test="${productCount > 0}">
                            <span class="admin-badge admin-badge-success">
                                <i class="fas fa-box me-1"></i>${productCount} sản phẩm
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="admin-badge admin-badge-warning">Danh mục trống</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="cat-profile-actions p-3 border-top">
                <a href="${pageContext.request.contextPath}/admin/products?categoryId=${category.id}"
                   class="admin-btn admin-btn-outline w-100 mb-2">
                    <i class="fas fa-list me-2"></i>Xem tất cả sản phẩm
                </a>
                <c:if test="${productCount eq 0}">
                    <a href="${pageContext.request.contextPath}/admin/categories?action=delete&id=${category.id}"
                       class="admin-btn admin-btn-danger w-100"
                       data-confirm-delete>
                        <i class="fas fa-trash me-2"></i>Xóa danh mục
                    </a>
                </c:if>
            </div>
        </div>
    </div>

    <div class="col-lg-8">
        <div class="row g-3 mb-4">
            <div class="col-sm-4">
                <div class="cat-stat-card cat-stat-products">
                    <span class="cat-stat-label">Sản phẩm</span>
                    <strong class="cat-stat-value">${productCount}</strong>
                </div>
            </div>
            <div class="col-sm-8">
                <a href="${pageContext.request.contextPath}/admin/products?categoryId=${category.id}"
                   class="admin-btn admin-btn-primary h-100 d-flex align-items-center justify-content-center">
                    <i class="fas fa-external-link-alt me-2"></i>Quản lý sản phẩm thuộc danh mục
                </a>
            </div>
        </div>

        <div class="admin-card">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0"><i class="fas fa-box me-2"></i>Sản phẩm trong danh mục</h5>
            </div>
            <div class="admin-table-wrapper border-0">
                <table class="admin-table mb-0">
                    <thead>
                        <tr>
                            <th>Ảnh</th>
                            <th>Tên</th>
                            <th>Giá</th>
                            <th>Tồn kho</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty recentProducts}">
                                <tr>
                                    <td colspan="5" class="text-center py-4 text-muted">
                                        Chưa có sản phẩm trong danh mục này
                                        <div class="mt-2">
                                            <a href="${pageContext.request.contextPath}/admin/products?action=create"
                                               class="admin-btn admin-btn-sm admin-btn-primary">
                                                <i class="fas fa-plus me-1"></i>Thêm sản phẩm
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="p" items="${recentProducts}">
                                    <tr>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty p.imageUrl}">
                                                    <img src="${fn:startsWith(p.imageUrl, 'data:')
                                                               ? p.imageUrl
                                                               : (fn:startsWith(p.imageUrl, 'http')
                                                                    ? p.imageUrl
                                                                    : pageContext.request.contextPath.concat('/').concat(p.imageUrl))}"
                                                         alt="" class="cat-product-thumb">
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="cat-product-thumb cat-product-thumb-empty">
                                                        <i class="fas fa-image text-muted"></i>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><strong><c:out value="${p.name}"/></strong></td>
                                        <td>
                                            <span class="price-value" data-price="${p.price}">${p.price}</span> &#8363;
                                        </td>
                                        <td>
                                            <span class="admin-badge ${p.stock > 10 ? 'admin-badge-success' : p.stock > 0 ? 'admin-badge-warning' : 'admin-badge-danger'}">
                                                ${p.stock}
                                            </span>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/products?action=edit&id=${p.id}"
                                               class="admin-btn admin-btn-outline btn-sm">
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
            <c:if test="${productCount > 8}">
                <div class="p-3 border-top text-center">
                    <a href="${pageContext.request.contextPath}/admin/products?categoryId=${category.id}"
                       class="small">Xem thêm ${productCount - 8} sản phẩm khác →</a>
                </div>
            </c:if>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
