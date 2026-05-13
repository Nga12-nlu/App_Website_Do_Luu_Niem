<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${product.id > 0 ? 'Sửa sản phẩm' : 'Thêm sản phẩm'}"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<div class="admin-card">
    <div class="admin-card-header">
        <h2 class="admin-card-title">
            <i class="fas fa-${product.id > 0 ? 'edit' : 'plus'} me-2"></i>${product.id > 0 ? 'Sửa sản phẩm' : 'Thêm sản phẩm'}
        </h2>
        <a href="${pageContext.request.contextPath}/admin/products" class="admin-btn admin-btn-outline">
            <i class="fas fa-arrow-left me-2"></i>Quay lại
        </a>
    </div>
</div>

<form method="post" action="${pageContext.request.contextPath}/admin/products" enctype="multipart/form-data" id="productForm">
    <c:if test="${product.id > 0}">
        <input type="hidden" name="id" value="${product.id}">
    </c:if>

    <div class="row g-4">
        <div class="col-md-8">
            <div class="admin-card">
                <div class="admin-card-header">
                    <h5 class="admin-card-title mb-0"><i class="fas fa-info-circle me-2 text-primary"></i>Thông tin sản phẩm</h5>
                </div>
                <div class="card-body">
                    <div class="mb-3">
                        <label class="admin-filter-label">Tên sản phẩm *</label>
                        <input type="text" name="name" class="form-control"
                               value="<c:out value="${product.name}"/>" required>
                    </div>
                    <div class="mb-3">
                        <label class="admin-filter-label">Mô tả</label>
                        <textarea name="description" class="form-control" rows="4"><c:out value="${product.description}"/></textarea>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="admin-filter-label">Giá hiển thị (từ) *</label>
                            <input type="number" name="price" class="form-control"
                                   value="${product.price}" min="0" step="1000" required>
                            <small class="text-muted">Được đồng bộ theo biến thể sau khi lưu (MIN giá biến thể).</small>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="admin-filter-label">Tồn kho tổng (tham khảo) *</label>
                            <input type="number" name="stock" class="form-control"
                                   value="${product.stock}" min="0" required>
                            <small class="text-muted">Tổng tồn = SUM các biến thể sau khi lưu.</small>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="admin-filter-label">Danh mục</label>
                        <select name="categoryId" class="form-select">
                            <option value="">-- Chọn danh mục --</option>
                            <c:forEach var="c" items="${categories}">
                                <option value="${c.id}" ${product.category != null and product.category.id eq c.id ? 'selected' : ''}>
                                    <c:out value="${c.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="admin-filter-label"><i class="fas fa-image me-1"></i>Ảnh sản phẩm</label>
                        <input type="file" name="image" class="form-control" accept="image/*">
                        <c:if test="${not empty product.imageUrl}">
                            <small class="text-muted mt-2 d-block">Đã có ảnh đại diện (áp dụng cho biến thể không tải ảnh riêng).</small>
                        </c:if>
                    </div>
                </div>
            </div>

            <div class="admin-card mt-4">
                <div class="admin-card-header d-flex justify-content-between align-items-center flex-wrap gap-2">
                    <h5 class="admin-card-title mb-0"><i class="fas fa-layer-group me-2 text-primary"></i>Biến thể (SKU)</h5>
                    <button type="button" class="admin-btn admin-btn-outline btn-sm" id="btnAddVariant">
                        <i class="fas fa-plus me-1"></i>Thêm dòng
                    </button>
                </div>
                <div class="card-body">
                    <p class="text-muted small mb-3">
                        Mỗi dòng là một biến thể (ví dụ màu + kích thước). Để trống toàn bộ: hệ thống tạo biến thể <strong>Mặc định</strong> theo giá/tồn ở trên.
                    </p>
                    <div id="variantRows">
                        <c:choose>
                            <c:when test="${empty variants}">
                                <div class="variant-row border rounded p-3 mb-3 bg-light">
                                    <div class="row g-2">
                                        <div class="col-md-6">
                                            <label class="small text-muted">Tên hiển thị *</label>
                                            <input type="text" name="variantDisplayName" class="form-control form-control-sm" placeholder="VD: Đỏ — Size M">
                                        </div>
                                        <div class="col-md-3">
                                            <label class="small text-muted">SKU</label>
                                            <input type="text" name="variantSku" class="form-control form-control-sm" placeholder="Mã SKU">
                                        </div>
                                        <div class="col-md-3 text-end align-self-end">
                                            <button type="button" class="btn btn-outline-danger btn-sm btn-remove-variant" title="Xóa dòng"><i class="fas fa-trash"></i></button>
                                        </div>
                                        <div class="col-md-4">
                                            <label class="small text-muted">Giá (₫) *</label>
                                            <input type="number" name="variantPrice" class="form-control form-control-sm" min="0" step="1000" value="${product.price}">
                                        </div>
                                        <div class="col-md-4">
                                            <label class="small text-muted">Tồn *</label>
                                            <input type="number" name="variantStock" class="form-control form-control-sm" min="0" value="${product.stock}">
                                        </div>
                                        <div class="col-md-4">
                                            <label class="small text-muted">Thứ tự</label>
                                            <input type="number" name="variantSortOrder" class="form-control form-control-sm" value="0">
                                        </div>
                                    </div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="v" items="${variants}" varStatus="st">
                                    <div class="variant-row border rounded p-3 mb-3 bg-light">
                                        <div class="row g-2">
                                            <div class="col-md-6">
                                                <label class="small text-muted">Tên hiển thị *</label>
                                                <input type="text" name="variantDisplayName" class="form-control form-control-sm" required
                                                       value="<c:out value="${v.displayName}"/>">
                                            </div>
                                            <div class="col-md-3">
                                                <label class="small text-muted">SKU</label>
                                                <input type="text" name="variantSku" class="form-control form-control-sm"
                                                       value="<c:out value="${v.sku != null ? v.sku : ''}"/>">
                                            </div>
                                            <div class="col-md-3 text-end align-self-end">
                                                <button type="button" class="btn btn-outline-danger btn-sm btn-remove-variant" title="Xóa dòng"><i class="fas fa-trash"></i></button>
                                            </div>
                                            <div class="col-md-4">
                                                <label class="small text-muted">Giá (₫) *</label>
                                                <input type="number" name="variantPrice" class="form-control form-control-sm" required min="0" step="1000" value="${v.price}">
                                            </div>
                                            <div class="col-md-4">
                                                <label class="small text-muted">Tồn *</label>
                                                <input type="number" name="variantStock" class="form-control form-control-sm" required min="0" value="${v.stock}">
                                            </div>
                                            <div class="col-md-4">
                                                <label class="small text-muted">Thứ tự</label>
                                                <input type="number" name="variantSortOrder" class="form-control form-control-sm" value="${v.sortOrder}">
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-4">
            <div class="admin-card">
                <div class="admin-card-header">
                    <h5 class="admin-card-title mb-0"><i class="fas fa-save me-2"></i>Thao tác</h5>
                </div>
                <div class="card-body">
                    <button type="submit" class="admin-btn admin-btn-primary w-100 mb-2">
                        <i class="fas fa-save me-2"></i>Lưu
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/products" class="admin-btn admin-btn-outline w-100">
                        <i class="fas fa-times me-2"></i>Hủy
                    </a>
                </div>
            </div>
        </div>
    </div>
</form>

<template id="variantRowTpl">
    <div class="variant-row border rounded p-3 mb-3 bg-light">
        <div class="row g-2">
            <div class="col-md-6">
                <label class="small text-muted">Tên hiển thị *</label>
                <input type="text" name="variantDisplayName" class="form-control form-control-sm" placeholder="VD: Xanh — L">
            </div>
            <div class="col-md-3">
                <label class="small text-muted">SKU</label>
                <input type="text" name="variantSku" class="form-control form-control-sm">
            </div>
            <div class="col-md-3 text-end align-self-end">
                <button type="button" class="btn btn-outline-danger btn-sm btn-remove-variant"><i class="fas fa-trash"></i></button>
            </div>
            <div class="col-md-4">
                <label class="small text-muted">Giá (₫) *</label>
                <input type="number" name="variantPrice" class="form-control form-control-sm" min="0" step="1000" value="0">
            </div>
            <div class="col-md-4">
                <label class="small text-muted">Tồn *</label>
                <input type="number" name="variantStock" class="form-control form-control-sm" min="0" value="0">
            </div>
            <div class="col-md-4">
                <label class="small text-muted">Thứ tự</label>
                <input type="number" name="variantSortOrder" class="form-control form-control-sm" value="0">
            </div>
        </div>
    </div>
</template>

<script>
(function () {
    var container = document.getElementById('variantRows');
    var tpl = document.getElementById('variantRowTpl');
    document.getElementById('btnAddVariant').addEventListener('click', function () {
        var node = tpl.content.cloneNode(true);
        container.appendChild(node);
    });
    container.addEventListener('click', function (e) {
        if (e.target.closest('.btn-remove-variant')) {
            var row = e.target.closest('.variant-row');
            if (container.querySelectorAll('.variant-row').length > 1) {
                row.remove();
            }
        }
    });
})();
</script>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
