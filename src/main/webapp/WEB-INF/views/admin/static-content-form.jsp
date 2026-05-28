<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Sửa nội dung tĩnh"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<c:if test="${not empty formError}">
    <div class="alert alert-danger admin-flash">
        <i class="fas fa-exclamation-circle me-2"></i>
        <c:choose>
            <c:when test="${formError eq 'length'}">Nội dung tối đa 5000 ký tự.</c:when>
            <c:otherwise>Dữ liệu không hợp lệ.</c:otherwise>
        </c:choose>
    </div>
</c:if>

<div class="admin-card">
    <div class="admin-card-header">
        <h2 class="admin-card-title">
            <i class="fas fa-pen-to-square me-2"></i>Sửa nội dung tĩnh
        </h2>
        <a href="${pageContext.request.contextPath}/admin/contents" class="admin-btn admin-btn-outline">
            <i class="fas fa-arrow-left me-2"></i>Quay lại
        </a>
    </div>
</div>

<div class="row g-4">
    <div class="col-lg-8">
        <div class="admin-card">
            <div class="card-body p-4">
                <form method="post" action="${pageContext.request.contextPath}/admin/contents">
                    <input type="hidden" name="id" value="${contentItem.id}">

                    <div class="mb-3">
                        <label class="admin-filter-label">Nhóm</label>
                        <input type="text" class="form-control" value="<c:out value="${contentItem.groupName}"/>" disabled>
                    </div>
                    <div class="mb-3">
                        <label class="admin-filter-label">Nhãn</label>
                        <input type="text" class="form-control" value="<c:out value="${contentItem.label}"/>" disabled>
                    </div>
                    <div class="mb-3">
                        <label class="admin-filter-label">Khóa hệ thống</label>
                        <input type="text" class="form-control" value="<c:out value="${contentItem.contentKey}"/>" disabled>
                    </div>
                    <div class="mb-3">
                        <label class="admin-filter-label">Nội dung</label>
                        <c:choose>
                            <c:when test="${contentItem.textarea}">
                                <textarea name="value" rows="6" class="form-control" maxlength="5000"><c:out value="${contentItem.value}"/></textarea>
                            </c:when>
                            <c:otherwise>
                                <input type="text" name="value" class="form-control" maxlength="5000"
                                       value="<c:out value="${contentItem.value}"/>">
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="mb-4">
                        <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="active" id="activeSwitch"
                                   ${contentItem.active ? 'checked' : ''}>
                            <label class="form-check-label" for="activeSwitch">Hiển thị trên website</label>
                        </div>
                    </div>
                    <div class="d-flex gap-2">
                        <button type="submit" class="admin-btn admin-btn-primary">
                            <i class="fas fa-save me-2"></i>Lưu thay đổi
                        </button>
                        <a href="${pageContext.request.contextPath}/admin/contents" class="admin-btn admin-btn-outline">Hủy</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="admin-card cat-form-hint">
            <div class="card-body p-4">
                <h6 class="mb-3"><i class="fas fa-circle-info text-primary me-2"></i>Lưu ý</h6>
                <ul class="small text-muted mb-0 ps-3">
                    <li class="mb-2">Tắt nội dung sẽ dùng giá trị mặc định của hệ thống.</li>
                    <li class="mb-2">Không sửa khóa hệ thống để tránh sai vị trí hiển thị.</li>
                    <li>Sau khi lưu, tải lại trang client để kiểm tra cập nhật.</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
