<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${category.id > 0 ? 'Sửa danh mục' : 'Thêm danh mục'}"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<div class="admin-card">
    <div class="admin-card-header">
        <h2 class="admin-card-title">
            <i class="fas fa-${category.id > 0 ? 'edit' : 'plus'} me-2"></i>${category.id > 0 ? 'Sửa danh mục' : 'Thêm danh mục'}
        </h2>
        <a href="${pageContext.request.contextPath}/admin/categories" class="admin-btn admin-btn-outline">
            <i class="fas fa-arrow-left me-2"></i>Quay lại
        </a>
    </div>
</div>

<div class="row">
    <div class="col-md-8">
        <div class="admin-card">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">Thông tin danh mục</h5>
            </div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/admin/categories">
                    <c:if test="${category.id > 0}">
                        <input type="hidden" name="id" value="${category.id}">
                    </c:if>
                    <div class="mb-3">
                        <label class="admin-filter-label">Tên danh mục *</label>
                        <input type="text" name="name" class="form-control" 
                               value="<c:out value="${category.name}"/>" required>
                    </div>
                    <div class="mb-3">
                        <label class="admin-filter-label">Mô tả</label>
                        <textarea name="description" class="form-control" rows="4"><c:out value="${category.description}"/></textarea>
                    </div>
                    <div class="d-flex gap-2">
                        <button type="submit" class="admin-btn admin-btn-primary">
                            <i class="fas fa-save me-2"></i>Lưu
                        </button>
                        <a href="${pageContext.request.contextPath}/admin/categories" class="admin-btn admin-btn-outline">
                            <i class="fas fa-times me-2"></i>Hủy
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
