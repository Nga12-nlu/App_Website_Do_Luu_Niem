<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Xác thực OTP"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="auth-page">
    <div class="auth-wrapper">
        <div class="auth-banner">
            <div class="icon-wrap"><i class="fas fa-key"></i></div>
            <h2>Xác thực OTP</h2>
            <p>Mã xác thực đăng ký đã được gửi đến địa chỉ email của bạn. Vui lòng nhập mã OTP để kích hoạt tài khoản.</p>
        </div>
        <div class="auth-form-section">
            <h3>Nhập mã OTP</h3>
            
            <c:if test="${param.msg eq 'unverified'}">
                <div class="alert alert-warning">Tài khoản chưa được kích hoạt. Mã OTP mới đã được gửi đến email của bạn.</div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger"><c:out value="${error}"/></div>
            </c:if>
            <c:if test="${not empty message}">
                <div class="alert alert-success"><c:out value="${message}"/></div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/verify-otp">
                <input type="hidden" name="userId" value="${param.userId != null ? param.userId : userId}"/>
                <input type="hidden" name="email" value="${param.email != null ? param.email : email}"/>
                <div class="mb-3">
                    <label class="form-label">Mã OTP (6 chữ số)</label>
                    <input type="text" name="otp" class="form-control text-center fs-4" maxlength="6" placeholder="000000" style="letter-spacing: 8px; font-weight: bold;" required>
                </div>
                <button type="submit" class="btn btn-souvenir mb-3">Xác thực tài khoản</button>
            </form>
            
            <form method="post" action="${pageContext.request.contextPath}/verify-otp" class="text-center">
                <input type="hidden" name="userId" value="${param.userId != null ? param.userId : userId}"/>
                <input type="hidden" name="email" value="${param.email != null ? param.email : email}"/>
                <input type="hidden" name="action" value="resend"/>
                <button type="submit" class="btn btn-link auth-link">Gửi lại mã OTP</button>
            </form>
            
            <div class="auth-divider">Quay lại</div>
            <a href="${pageContext.request.contextPath}/login" class="auth-link">Đăng nhập</a>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
