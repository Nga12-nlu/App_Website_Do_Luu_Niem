<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${isCreate ? 'Thêm người dùng' : 'Sửa người dùng'}"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<c:if test="${not empty formError}">
    <div class="alert alert-danger admin-flash">
        <i class="fas fa-exclamation-circle me-2"></i>
        <c:choose>
            <c:when test="${formError eq 'required'}">Vui lòng nhập đầy đủ họ tên và email.</c:when>
            <c:when test="${formError eq 'email'}">Email đã được sử dụng bởi tài khoản khác.</c:when>
            <c:when test="${formError eq 'password'}">Vui lòng nhập mật khẩu cho tài khoản mới.</c:when>
            <c:when test="${formError eq 'weak'}">Mật khẩu phải có ít nhất 8 ký tự, gồm chữ và số.</c:when>
            <c:when test="${formError eq 'lastadmin'}">Không thể hạ quyền quản trị viên cuối cùng.</c:when>
            <c:otherwise>Có lỗi xảy ra. Vui lòng kiểm tra lại.</c:otherwise>
        </c:choose>
    </div>
</c:if>

<div class="admin-card">
    <div class="admin-card-header">
        <h2 class="admin-card-title">
            <i class="fas fa-${isCreate ? 'user-plus' : 'user-edit'} me-2"></i>${pageTitle}
        </h2>
        <a href="${pageContext.request.contextPath}/admin/users" class="admin-btn admin-btn-outline">
            <i class="fas fa-arrow-left me-2"></i>Quay lại
        </a>
    </div>
</div>

<div class="row g-4">
    <div class="col-lg-8">
        <div class="admin-card">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">Thông tin tài khoản</h5>
            </div>
            <div class="card-body p-4">
                <form method="post" action="${pageContext.request.contextPath}/admin/users" class="user-form" id="userForm">
                    <c:if test="${not isCreate}">
                        <input type="hidden" name="id" value="${user.id}">
                    </c:if>

                    <div class="mb-3">
                        <label class="admin-filter-label">Họ và tên <span class="text-danger">*</span></label>
                        <input type="text" name="fullName" class="form-control" required maxlength="255"
                               value="<c:out value="${user.fullName}"/>"
                               placeholder="Nguyễn Văn A">
                    </div>

                    <div class="mb-3">
                        <label class="admin-filter-label">Email <span class="text-danger">*</span></label>
                        <input type="email" name="email" class="form-control" required maxlength="255"
                               value="<c:out value="${user.email}"/>"
                               placeholder="email@example.com" autocomplete="off">
                    </div>

                    <div class="mb-3">
                        <label class="admin-filter-label">
                            Mật khẩu
                            <c:if test="${isCreate}"><span class="text-danger">*</span></c:if>
                            <c:if test="${not isCreate}"><span class="text-muted small">(để trống nếu không đổi)</span></c:if>
                        </label>
                        <div class="input-group">
                            <input type="password" name="password" id="userPassword" class="form-control"
                                   minlength="8" ${isCreate ? 'required' : ''}
                                   placeholder="Tối thiểu 8 ký tự, có chữ và số"
                                   autocomplete="new-password">
                            <button type="button" class="btn btn-outline-secondary" id="togglePwd" tabindex="-1">
                                <i class="fas fa-eye"></i>
                            </button>
                        </div>
                        <div class="form-text">Gồm ít nhất một chữ cái và một chữ số.</div>
                    </div>

                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="admin-filter-label">Vai trò</label>
                            <select name="role" class="form-select" id="userRole"
                                    ${sessionScope.currentUser.id eq user.id ? 'disabled' : ''}>
                                <option value="CUSTOMER" ${user.role eq 'CUSTOMER' ? 'selected' : ''}>Khách hàng</option>
                                <option value="ADMIN" ${user.role eq 'ADMIN' ? 'selected' : ''}>Quản trị viên</option>
                            </select>
                            <c:if test="${sessionScope.currentUser.id eq user.id}">
                                <input type="hidden" name="role" value="ADMIN">
                                <div class="form-text">Không thể đổi vai trò của chính bạn.</div>
                            </c:if>
                        </div>
                        <div class="col-md-6">
                            <label class="admin-filter-label">Trạng thái</label>
                            <div class="form-check form-switch mt-2">
                                <input class="form-check-input" type="checkbox" name="active" id="userActive" value="on"
                                       ${user.active ? 'checked' : ''}
                                       ${sessionScope.currentUser.id eq user.id ? 'disabled' : ''}>
                                <label class="form-check-label" for="userActive">Tài khoản hoạt động</label>
                            </div>
                            <c:if test="${sessionScope.currentUser.id eq user.id}">
                                <input type="hidden" name="active" value="on">
                            </c:if>
                        </div>
                    </div>

                    <hr class="my-4">

                    <div class="d-flex flex-wrap gap-2">
                        <button type="submit" class="admin-btn admin-btn-primary">
                            <i class="fas fa-save me-2"></i>${isCreate ? 'Tạo tài khoản' : 'Lưu thay đổi'}
                        </button>
                        <a href="${pageContext.request.contextPath}/admin/users" class="admin-btn admin-btn-outline">
                            Hủy
                        </a>
                        <c:if test="${not isCreate}">
                            <a href="${pageContext.request.contextPath}/admin/users?action=detail&id=${user.id}"
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
        <div class="admin-card user-form-hint">
            <div class="card-body p-4">
                <h6 class="mb-3"><i class="fas fa-info-circle text-primary me-2"></i>Gợi ý</h6>
                <ul class="small text-muted mb-0 ps-3">
                    <li class="mb-2">Khách hàng có thể đăng nhập và đặt hàng trên cửa hàng.</li>
                    <li class="mb-2">Quản trị viên truy cập khu vực <code>/admin</code>.</li>
                    <li class="mb-2">Khóa tài khoản để chặn đăng nhập mà không xóa dữ liệu.</li>
                    <li>Chỉ xóa được khách hàng chưa có đơn hàng.</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<script>
document.getElementById('togglePwd')?.addEventListener('click', function () {
    var inp = document.getElementById('userPassword');
    if (!inp) return;
    var isPwd = inp.type === 'password';
    inp.type = isPwd ? 'text' : 'password';
    this.querySelector('i').className = isPwd ? 'fas fa-eye-slash' : 'fas fa-eye';
});
</script>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
