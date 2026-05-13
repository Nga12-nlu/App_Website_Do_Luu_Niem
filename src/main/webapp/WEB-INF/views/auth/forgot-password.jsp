<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quên mật khẩu"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="auth-page">
    <div class="auth-wrapper">
        <div class="auth-banner">
            <div class="icon-wrap"><i class="fas fa-envelope-open-text"></i></div>
            <h2>Khôi phục qua email</h2>
            <p>Nhập email đã đăng ký. Hệ thống gửi liên kết một lần, có thời hạn, để bạn đặt mật khẩu mới an toàn.</p>
            <ul class="text-start small ps-3 mt-3 mb-0 opacity-90">
                <li>Không tiết lộ tài khoản có tồn tại hay không.</li>
                <li>Giới hạn số lần gửi mỗi giờ để chống lạm dụng.</li>
                <li>Kiểm tra cả thư mục spam.</li>
            </ul>
        </div>
        <div class="auth-form-section">
            <h3>Quên mật khẩu</h3>
            <c:if test="${not empty error}">
                <div class="alert alert-danger"><c:out value="${error}"/></div>
            </c:if>
            <c:if test="${not empty message}">
                <div class="alert alert-success"><c:out value="${message}"/></div>
            </c:if>
            <c:if test="${empty message}">
                <form method="post" action="${pageContext.request.contextPath}/forgot-password" autocomplete="on">
                    <div class="mb-3">
                        <label class="form-label"><i class="fas fa-at me-1 text-muted"></i>Email đăng ký</label>
                        <input type="email" name="email" class="form-control" placeholder="name@example.com" required autofocus>
                    </div>
                    <button type="submit" class="btn btn-souvenir mb-3 w-100">
                        <i class="fas fa-paper-plane me-2"></i>Gửi liên kết đặt lại mật khẩu
                    </button>
                </form>
            </c:if>
            <div class="auth-divider">Đã nhớ mật khẩu?</div>
            <a href="${pageContext.request.contextPath}/login" class="auth-link"><i class="fas fa-arrow-left me-1"></i>Quay lại đăng nhập</a>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
