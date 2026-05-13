<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Quản lý sản phẩm"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<div class="admin-card">
    <div class="admin-card-header">
        <h2 class="admin-card-title">
            <i class="fas fa-box me-2"></i>Quản lý sản phẩm
        </h2>
        <a href="${pageContext.request.contextPath}/admin/products?action=create" class="admin-btn admin-btn-primary">
            <i class="fas fa-plus"></i>Thêm sản phẩm
        </a>
    </div>
</div>

<!-- Filters -->
<div class="admin-filters">
    <form method="get" action="${pageContext.request.contextPath}/admin/products" class="admin-filter-form">
        <input type="hidden" name="page" value="1">
        <div class="admin-filter-row">
            <div class="admin-filter-group">
                <label class="admin-filter-label">Tìm kiếm</label>
                <input type="text" name="search" class="form-control" 
                       value="<c:out value="${search}"/>" 
                       placeholder="Tên sản phẩm...">
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">Danh mục</label>
                <select name="categoryId" class="form-select">
                    <option value="">Tất cả danh mục</option>
                    <c:forEach var="c" items="${categories}">
                        <option value="${c.id}" ${categoryId != null && categoryId eq c.id ? 'selected' : ''}>
                            <c:out value="${c.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="admin-filter-group">
                <label class="admin-filter-label">Sắp xếp theo</label>
                <select name="sortBy" class="form-select">
                    <option value="id" ${sortBy eq 'id' ? 'selected' : ''}>ID</option>
                    <option value="name" ${sortBy eq 'name' ? 'selected' : ''}>Tên</option>
                    <option value="price" ${sortBy eq 'price' ? 'selected' : ''}>Giá</option>
                    <option value="stock" ${sortBy eq 'stock' ? 'selected' : ''}>Tồn kho</option>
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

<!-- Products Table -->
<div class="admin-table-wrapper">
    <table class="admin-table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Ảnh</th>
                <th>Tên sản phẩm</th>
                <th>Giá</th>
                <th>Tồn kho</th>
                <th>Danh mục</th>
                <th style="width: 120px;">Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${empty products}">
                    <tr>
                        <td colspan="7" class="text-center py-4 text-muted">
                            <i class="fas fa-inbox fa-2x mb-2 d-block"></i>
                            Không tìm thấy sản phẩm nào
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="p" items="${products}">
                        <tr>
                            <td>${p.id}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty p.imageUrl}">
                                        <img src="${fn:startsWith(p.imageUrl, 'data:') 
                                                   ? p.imageUrl 
                                                   : (fn:startsWith(p.imageUrl, 'http') 
                                                        ? p.imageUrl 
                                                        : pageContext.request.contextPath.concat('/').concat(p.imageUrl))}" 
                                             alt="<c:out value="${p.name}"/>" 
                                             style="width: 60px; height: 60px; object-fit: cover; border-radius: 0.25rem;">
                                    </c:when>
                                    <c:otherwise>
                                        <div style="width: 60px; height: 60px; background: #f0f0f0; border-radius: 0.25rem; display: flex; align-items: center; justify-content: center;">
                                            <i class="fas fa-image text-muted"></i>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <strong><c:out value="${p.name}"/></strong>
                                <c:if test="${not empty p.description}">
                                    <br><small class="text-muted"><c:out value="${fn:substring(p.description, 0, 50)}${fn:length(p.description) > 50 ? '...' : ''}"/></small>
                                </c:if>
                            </td>
                            <td>
                                <span class="price-value" data-price="${p.price}">${p.price}</span> &#8363;
                            </td>
                            <td>
                                <span class="admin-badge ${p.stock > 10 ? 'admin-badge-success' : p.stock > 0 ? 'admin-badge-warning' : 'admin-badge-danger'}">
                                    ${p.stock}
                                </span>
                            </td>
                            <td><c:out value="${p.category != null ? p.category.name : '-'}"/></td>
                            <td>
                                <div class="btn-group" role="group">
                                    <a href="${pageContext.request.contextPath}/admin/products?action=edit&id=${p.id}" 
                                       class="admin-btn admin-btn-outline" 
                                       title="Sửa">
                                        <i class="fas fa-edit"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/products?action=delete&id=${p.id}" 
                                       class="admin-btn admin-btn-danger" 
                                       onclick="return confirm('Xóa sản phẩm này?');" 
                                       title="Xóa">
                                        <i class="fas fa-trash"></i>
                                    </a>
                                </div>
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
                    <a class="page-link" href="?page=${currentPage - 1}&pageSize=${pageSize}&search=${search}&categoryId=${categoryId}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                        <i class="fas fa-chevron-left"></i>
                    </a>
                </li>
            </c:if>
            
            <c:forEach var="i" begin="${currentPage > 3 ? currentPage - 2 : 1}" 
                       end="${currentPage + 2 < totalPages ? currentPage + 2 : totalPages}">
                <li class="page-item ${i eq currentPage ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}&pageSize=${pageSize}&search=${search}&categoryId=${categoryId}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                        ${i}
                    </a>
                </li>
            </c:forEach>
            
            <c:if test="${currentPage < totalPages}">
                <li class="page-item">
                    <a class="page-link" href="?page=${currentPage + 1}&pageSize=${pageSize}&search=${search}&categoryId=${categoryId}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                        <i class="fas fa-chevron-right"></i>
                    </a>
                </li>
            </c:if>
        </ul>
    </nav>
    <div class="text-center mt-2 text-muted">
        Hiển thị ${(currentPage - 1) * pageSize + 1} - ${currentPage * pageSize > totalProducts ? totalProducts : currentPage * pageSize} 
        trong tổng số ${totalProducts} sản phẩm
    </div>
</c:if>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
