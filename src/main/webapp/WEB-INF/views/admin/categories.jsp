<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quản lý danh mục"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

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

<div class="admin-table-wrapper">
    <table class="admin-table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Tên danh mục</th>
                <th>Mô tả</th>
                <th style="width: 150px;">Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${empty categories}">
                    <tr>
                        <td colspan="4" class="text-center py-4 text-muted">
                            <i class="fas fa-inbox fa-2x mb-2 d-block"></i>
                            Chưa có danh mục nào
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="c" items="${categories}">
                        <tr>
                            <td>${c.id}</td>
                            <td><strong><c:out value="${c.name}"/></strong></td>
                            <td><c:out value="${c.description}"/></td>
                            <td>
                                <div class="btn-group" role="group">
                                    <a href="${pageContext.request.contextPath}/admin/categories?action=edit&id=${c.id}" 
                                       class="admin-btn admin-btn-outline" 
                                       title="Sửa">
                                        <i class="fas fa-edit"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/categories?action=delete&id=${c.id}" 
                                       class="admin-btn admin-btn-danger" 
                                       onclick="return confirm('Xóa danh mục này? Tất cả sản phẩm trong danh mục sẽ bị ảnh hưởng.');" 
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

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
