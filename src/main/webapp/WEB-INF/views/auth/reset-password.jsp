<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đặt lại mật khẩu"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="auth-page">
    <div class="auth-wrapper">
        <div class="auth-banner">
            <div class="icon-wrap"><i class="fas fa-shield-alt"></i></div>
            <h2>Đặt mật khẩu mới</h2>
            <p>Liên kết từ email chỉ dùng một lần và hết hạn sau 60 phút. Mật khẩu mới phải đủ mạnh theo chính sách bảo mật.</p>
        </div>
        <div class="auth-form-section">
            <h3>Đặt lại mật khẩu</h3>
            <c:if test="${not empty error}">
                <div class="alert alert-danger"><c:out value="${error}"/></div>
            </c:if>

            <c:choose>
                <c:when test="${not empty resetToken and not empty csrfToken}">
                    <form method="post" action="${pageContext.request.contextPath}/reset-password" autocomplete="off">
                        <input type="hidden" name="token" value="<c:out value='${resetToken}'/>">
                        <input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>">
                        <div class="mb-3">
                            <label class="form-label">Mật khẩu mới</label>
                            <input type="password" name="newPassword" class="form-control" minlength="8" maxlength="128" required autofocus
                                   placeholder="8+ ký tự, có chữ cái và số">
                            <div class="form-text">Từ 8–128 ký tự, gồm ít nhất một chữ cái và một chữ số.</div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Xác nhận mật khẩu</label>
                            <input type="password" name="confirmPassword" class="form-control" minlength="8" maxlength="128" required>
                        </div>
                        <button type="submit" class="btn btn-souvenir mb-3 w-100">
                            <i class="fas fa-check me-2"></i>Cập nhật mật khẩu
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <p class="text-muted">Bạn cần mở đúng liên kết trong email hoặc yêu cầu gửi lại.</p>
                    <a href="${pageContext.request.contextPath}/forgot-password" class="btn btn-outline-souvenir w-100 mb-2">
                        <i class="fas fa-redo me-2"></i>Gửi lại email
                    </a>
                </c:otherwise>
            </c:choose>

            <div class="auth-divider">Hoàn tất rồi?</div>
            <a href="${pageContext.request.contextPath}/login" class="auth-link"><i class="fas fa-sign-in-alt me-1"></i>Đăng nhập</a>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
