<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đổi mật khẩu"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="profile-page">
    <div class="row">
        <div class="col-lg-3 mb-4">
            <div class="card">
                <div class="card-body">
                    <div class="text-center mb-3">
                        <div class="profile-avatar">
                            <i class="fas fa-user"></i>
                        </div>
                        <h5 class="mt-3 mb-0"><c:out value="${sessionScope.currentUser.fullName}"/></h5>
                        <p class="text-muted mb-0"><c:out value="${sessionScope.currentUser.email}"/></p>
                    </div>
                    <hr>
                    <ul class="nav nav-pills flex-column">
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/profile">
                                <i class="fas fa-user me-2"></i>Thông tin tài khoản
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link active" href="${pageContext.request.contextPath}/change-password">
                                <i class="fas fa-key me-2"></i>Đổi mật khẩu
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/my-orders">
                                <i class="fas fa-shopping-bag me-2"></i>Đơn hàng của tôi
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
        <div class="col-lg-9">
            <div class="card">
                <div class="card-header">
                    <h5 class="mb-0"><i class="fas fa-key me-2"></i>Đổi mật khẩu</h5>
                </div>
                <div class="card-body">
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger">
                            <i class="fas fa-exclamation-circle me-2"></i><c:out value="${error}"/>
                        </div>
                    </c:if>
                    <c:if test="${not empty success}">
                        <div class="alert alert-success">
                            <i class="fas fa-check-circle me-2"></i><c:out value="${success}"/>
                        </div>
                    </c:if>

                    <c:if test="${oauthOnly}">
                        <div class="alert alert-info">
                            Tài khoản đăng nhập bằng Google chưa có mật khẩu. Đặt mật khẩu để có thể đăng nhập bằng email.
                        </div>
                    </c:if>
                    <form method="post" action="${pageContext.request.contextPath}/change-password" id="changePasswordForm">
                        <c:if test="${not oauthOnly}">
                            <div class="mb-3">
                                <label class="form-label">Mật khẩu hiện tại *</label>
                                <input type="password" name="currentPassword" class="form-control"
                                       placeholder="Nhập mật khẩu hiện tại" required>
                            </div>
                        </c:if>
                        <c:if test="${oauthOnly}">
                            <input type="hidden" name="currentPassword" value="">
                        </c:if>
                        <div class="mb-3">
                            <label class="form-label">Mật khẩu mới *</label>
                            <input type="password" name="newPassword" class="form-control"
                                   placeholder="8+ ký tự, có chữ cái và số" required minlength="8" maxlength="128">
                            <small class="text-muted">Từ 8–128 ký tự, gồm ít nhất một chữ cái và một chữ số.</small>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Xác nhận mật khẩu mới *</label>
                            <input type="password" name="confirmPassword" class="form-control"
                                   placeholder="Nhập lại mật khẩu mới" required minlength="8" maxlength="128">
                        </div>
                        <div class="d-flex gap-2">
                            <button type="submit" class="btn btn-souvenir">
                                <i class="fas fa-save me-2"></i>Đổi mật khẩu
                            </button>
                            <a href="${pageContext.request.contextPath}/profile" class="btn btn-outline-secondary">
                                <i class="fas fa-times me-2"></i>Hủy
                            </a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
document.getElementById('changePasswordForm').addEventListener('submit', function(e) {
    const newPassword = document.querySelector('input[name="newPassword"]').value;
    const confirmPassword = document.querySelector('input[name="confirmPassword"]').value;
    
    if (newPassword !== confirmPassword) {
        e.preventDefault();
        alert('Mật khẩu mới và xác nhận không khớp!');
        return false;
    }
    
    const pwdOk = /^(?=.*\p{L})(?=.*\d).{8,128}$/u.test(newPassword);
    if (!pwdOk) {
        e.preventDefault();
        alert('Mật khẩu mới phải từ 8–128 ký tự, gồm ít nhất một chữ cái và một chữ số.');
        return false;
    }
});
</script>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>

