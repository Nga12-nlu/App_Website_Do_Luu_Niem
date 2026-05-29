<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đăng nhập"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="auth-page">
    <div class="auth-wrapper">
        <div class="auth-banner">
            <div class="icon-wrap"><i class="fas fa-sign-in-alt"></i></div>
            <h2>Souvenir Shop</h2>
            <p>Khám phá những món quà lưu niệm độc đáo, mang đậm dấu ấn văn hóa Việt Nam. Đăng nhập để trải nghiệm mua sắm thuận tiện.</p>
        </div>
        <div class="auth-form-section">
            <h3>Đăng nhập</h3>
            <c:if test="${not empty error}">
                <div class="alert alert-danger"><c:out value="${error}"/></div>
            </c:if>
            <c:if test="${not empty message}">
                <div class="alert alert-success"><c:out value="${message}"/></div>
            </c:if>
            <c:if test="${not empty googleError}">
                <div class="alert alert-danger"><c:out value="${googleError}"/></div>
            </c:if>
            <c:if test="${googleEnabled}">
                <c:url var="googleLoginUrl" value="/auth/google">
                    <c:if test="${not empty param.redirect}">
                        <c:param name="redirect" value="${param.redirect}"/>
                    </c:if>
                </c:url>
                <a href="${googleLoginUrl}" class="btn btn-google mb-3">
                    <i class="fab fa-google"></i> Đăng nhập với Google
                </a>
                <div class="auth-divider">hoặc đăng nhập bằng email</div>
            </c:if>
            <form method="post" action="${pageContext.request.contextPath}/login">
                <c:if test="${not empty param.redirect}">
                    <input type="hidden" name="redirect" value="${param.redirect}"/>
                </c:if>
                <div class="mb-3">
                    <label class="form-label">Email</label>
                    <input type="email" name="email" class="form-control" value="${email}" placeholder="Nhập email của bạn" required>
                </div>
                <div class="mb-3">
                    <label class="form-label">Mật khẩu</label>
                    <input type="password" name="password" class="form-control" placeholder="Nhập mật khẩu" required>
                </div>
                <div class="mb-3">
                    <a href="${pageContext.request.contextPath}/forgot-password" class="auth-link">Quên mật khẩu?</a>
                </div>
                <button type="submit" class="btn btn-souvenir mb-3">Đăng nhập</button>
            </form>
            <div class="auth-divider">Chưa có tài khoản?</div>
            <a href="${pageContext.request.contextPath}/register" class="auth-link">Đăng ký ngay</a>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
