<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đăng ký tài khoản"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="auth-page">
    <div class="auth-wrapper">
        <div class="auth-banner">
            <div class="icon-wrap"><i class="fas fa-user-plus"></i></div>
            <h2>Tham gia Souvenir Shop</h2>
            <p>Tạo tài khoản để mua sắm nhanh chóng, theo dõi đơn hàng và nhận nhiều ưu đãi hấp dẫn từ chúng tôi.</p>
        </div>
        <div class="auth-form-section">
            <h3>Đăng ký</h3>
            <c:if test="${not empty error}">
                <div class="alert alert-danger"><c:out value="${error}"/></div>
            </c:if>
            <c:if test="${googleEnabled}">
                <a href="${pageContext.request.contextPath}/auth/google" class="btn btn-google mb-3">
                    <i class="fab fa-google"></i> Đăng ký nhanh với Google
                </a>
                <div class="auth-divider">hoặc đăng ký bằng email</div>
            </c:if>
            <form method="post" action="${pageContext.request.contextPath}/register">
                <div class="mb-3">
                    <label class="form-label">Họ tên</label>
                    <input type="text" name="fullName" class="form-control" placeholder="Nhập họ tên đầy đủ" required>
                </div>
                <div class="mb-3">
                    <label class="form-label">Email</label>
                    <input type="email" name="email" class="form-control" placeholder="Nhập email" required>
                </div>
                <div class="mb-3">
                    <label class="form-label">Mật khẩu</label>
                    <input type="password" name="password" class="form-control" placeholder="8+ ký tự, có chữ và số" minlength="8" maxlength="128" required>
                </div>
                <div class="mb-3">
                    <label class="form-label">Nhập lại mật khẩu</label>
                    <input type="password" name="confirmPassword" class="form-control" placeholder="Xác nhận mật khẩu" minlength="8" maxlength="128" required>
                </div>
                <div class="form-text mb-3">Mật khẩu từ 8–128 ký tự, gồm ít nhất một chữ cái và một chữ số.</div>
                <button type="submit" class="btn btn-souvenir mb-3">Đăng ký</button>
            </form>
            <div class="auth-divider">Đã có tài khoản?</div>
            <a href="${pageContext.request.contextPath}/login" class="auth-link">Đăng nhập</a>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
