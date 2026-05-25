<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${isCreate ? 'Thêm danh mục' : 'Sửa danh mục'}"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<c:if test="${not empty formError}">
    <div class="alert alert-danger admin-flash">
        <i class="fas fa-exclamation-circle me-2"></i>
        <c:choose>
            <c:when test="${formError eq 'required'}">Vui lòng nhập tên danh mục.</c:when>
            <c:when test="${formError eq 'name'}">Tên danh mục đã tồn tại.</c:when>
            <c:when test="${formError eq 'namelen'}">Tên danh mục tối đa 255 ký tự.</c:when>
            <c:when test="${formError eq 'desclen'}">Mô tả tối đa 500 ký tự.</c:when>
            <c:otherwise>Có lỗi xảy ra. Vui lòng kiểm tra lại.</c:otherwise>
        </c:choose>
    </div>
</c:if>

<div class="admin-card">
    <div class="admin-card-header">
        <h2 class="admin-card-title">
            <i class="fas fa-${isCreate ? 'plus' : 'edit'} me-2"></i>${pageTitle}
        </h2>
        <a href="${pageContext.request.contextPath}/admin/categories" class="admin-btn admin-btn-outline">
            <i class="fas fa-arrow-left me-2"></i>Quay lại
        </a>
    </div>
</div>

<div class="row g-4">
    <div class="col-lg-8">
        <div class="admin-card">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">Thông tin danh mục</h5>
            </div>
            <div class="card-body p-4">
                <form method="post" action="${pageContext.request.contextPath}/admin/categories" id="categoryForm">
                    <c:if test="${not isCreate}">
                        <input type="hidden" name="id" value="${category.id}">
                    </c:if>

                    <div class="mb-3">
                        <label class="admin-filter-label">Tên danh mục <span class="text-danger">*</span></label>
                        <input type="text" name="name" class="form-control" required maxlength="255"
                               value="<c:out value="${category.name}"/>"
                               placeholder="Ví dụ: Quà lưu niệm du lịch">
                    </div>

                    <div class="mb-3">
                        <label class="admin-filter-label">Mô tả</label>
                        <textarea name="description" class="form-control" rows="5" maxlength="500"
                                  placeholder="Mô tả ngắn về danh mục (tùy chọn)"><c:out value="${category.description}"/></textarea>
                        <div class="form-text">Tối đa 500 ký tự.</div>
                    </div>

                    <c:if test="${not isCreate and productCount > 0}">
                        <div class="alert alert-info py-2 small">
                            <i class="fas fa-box me-2"></i>
                            Danh mục này đang có <strong>${productCount}</strong> sản phẩm.
                            <a href="${pageContext.request.contextPath}/admin/products?categoryId=${category.id}">Xem sản phẩm</a>
                        </div>
                    </c:if>

                    <hr class="my-4">

                    <div class="d-flex flex-wrap gap-2">
                        <button type="submit" class="admin-btn admin-btn-primary">
                            <i class="fas fa-save me-2"></i>${isCreate ? 'Tạo danh mục' : 'Lưu thay đổi'}
                        </button>
                        <a href="${pageContext.request.contextPath}/admin/categories" class="admin-btn admin-btn-outline">Hủy</a>
                        <c:if test="${not isCreate}">
                            <a href="${pageContext.request.contextPath}/admin/categories?action=detail&id=${category.id}"
                               class="admin-btn admin-btn-outline ms-auto">
                                <i class="fas fa-eye me-1"></i>Xem chi tiết
                            </a>
                        </c:if>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div class="col-lg-4">
        <div class="admin-card cat-form-hint">
            <div class="card-body p-4">
                <h6 class="mb-3"><i class="fas fa-info-circle text-primary me-2"></i>Gợi ý</h6>
                <ul class="small text-muted mb-0 ps-3">
                    <li class="mb-2">Tên danh mục phải là duy nhất trong hệ thống.</li>
                    <li class="mb-2">Sản phẩm được gán danh mục khi thêm/sửa sản phẩm.</li>
                    <li class="mb-2">Chỉ xóa được danh mục <strong>chưa có sản phẩm</strong>.</li>
                    <li>Khách hàng lọc sản phẩm theo danh mục trên cửa hàng.</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
