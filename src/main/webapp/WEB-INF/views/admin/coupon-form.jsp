<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.app.app_website_do_luu_niem.model.Coupon" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${isNew ? 'Thêm mã giảm giá' : 'Sửa mã giảm giá'}"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<c:if test="${not empty param.error}">
    <div class="alert alert-danger"><c:out value="${param.error}"/></div>
</c:if>

<div class="admin-card mb-4">
    <div class="admin-card-header">
        <h2 class="admin-card-title"><i class="fas fa-ticket-alt me-2"></i>${pageTitle}</h2>
        <a href="${pageContext.request.contextPath}/admin/coupons" class="admin-btn admin-btn-outline">
            <i class="fas fa-arrow-left me-2"></i>Quay lại
        </a>
    </div>
</div>

<div class="admin-card">
    <div class="card-body p-4">
        <form method="post" action="${pageContext.request.contextPath}/admin/coupons">
            <c:if test="${not isNew}">
                <input type="hidden" name="id" value="${coupon.id}">
            </c:if>
            <div class="row g-3">
                <div class="col-md-4">
                    <label class="form-label">Mã *</label>
                    <input type="text" name="code" class="form-control text-uppercase" required maxlength="32"
                           value="${coupon.code}" ${not isNew ? 'readonly' : ''}>
                </div>
                <div class="col-md-8">
                    <label class="form-label">Mô tả</label>
                    <input type="text" name="description" class="form-control" maxlength="255"
                           value="${coupon.description}">
                </div>
                <div class="col-md-4">
                    <label class="form-label">Loại giảm *</label>
                    <select name="discountType" class="form-select" required>
                        <option value="PERCENT" ${isNew or coupon.discountType eq 'PERCENT' ? 'selected' : ''}>Phần trăm (%)</option>
                        <option value="FIXED" ${not isNew and coupon.discountType eq 'FIXED' ? 'selected' : ''}>Số tiền cố định (đ)</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label">Giá trị *</label>
                    <input type="number" name="discountValue" class="form-control" required min="0" step="0.01"
                           value="${coupon.discountValue}">
                </div>
                <div class="col-md-4">
                    <label class="form-label">Giảm tối đa (%, chỉ PERCENT)</label>
                    <input type="number" name="maxDiscount" class="form-control" min="0" step="1000"
                           value="${coupon.maxDiscount}">
                </div>
                <div class="col-md-4">
                    <label class="form-label">Đơn tối thiểu (đ)</label>
                    <input type="number" name="minOrderAmount" class="form-control" min="0" step="1000"
                           value="${coupon.minOrderAmount != null ? coupon.minOrderAmount : 0}">
                </div>
                <div class="col-md-4">
                    <label class="form-label">Giới hạn lượt (để trống = không giới hạn)</label>
                    <input type="number" name="usageLimit" class="form-control" min="1"
                           value="${coupon.usageLimit}">
                </div>
                <div class="col-md-4">
                    <label class="form-label">Lượt / 1 khách</label>
                    <input type="number" name="perUserLimit" class="form-control" min="1" required
                           value="${coupon.perUserLimit > 0 ? coupon.perUserLimit : 1}">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Bắt đầu</label>
                    <input type="datetime-local" name="startsAt" class="form-control"
                           value="<%
                               Coupon cp = (Coupon) request.getAttribute("coupon");
                               if (cp != null && cp.getStartsAt() != null) {
                                   out.print(cp.getStartsAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
                               }
                           %>">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Hết hạn</label>
                    <input type="datetime-local" name="expiresAt" class="form-control"
                           value="<%
                               if (cp != null && cp.getExpiresAt() != null) {
                                   out.print(cp.getExpiresAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
                               }
                           %>">
                </div>
                <div class="col-12">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="active" id="active" value="on"
                               ${coupon.active or isNew ? 'checked' : ''}>
                        <label class="form-check-label" for="active">Đang hoạt động</label>
                    </div>
                    <c:if test="${not isNew}">
                        <p class="small text-muted mt-2">Đã sử dụng: ${coupon.usedCount} lượt</p>
                    </c:if>
                </div>
            </div>
            <button type="submit" class="admin-btn admin-btn-primary mt-4">
                <i class="fas fa-save me-2"></i>Lưu
            </button>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
