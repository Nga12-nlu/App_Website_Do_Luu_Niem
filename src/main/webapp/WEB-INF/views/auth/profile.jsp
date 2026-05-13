<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Thông tin tài khoản"/>
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
                        <h5 class="mt-3 mb-0"><c:out value="${user.fullName}"/></h5>
                        <p class="text-muted mb-0"><c:out value="${user.email}"/></p>
                    </div>
                    <hr>
                    <ul class="nav nav-pills flex-column">
                        <li class="nav-item">
                            <a class="nav-link active" href="${pageContext.request.contextPath}/profile">
                                <i class="fas fa-user me-2"></i>Thông tin tài khoản
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/change-password">
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
                    <h5 class="mb-0"><i class="fas fa-user-edit me-2"></i>Thông tin tài khoản</h5>
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

                    <form method="post" action="${pageContext.request.contextPath}/profile">
                        <div class="mb-3">
                            <label class="form-label">Họ tên *</label>
                            <input type="text" name="fullName" class="form-control" 
                                   value="<c:out value="${user.fullName}"/>" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Email *</label>
                            <input type="email" name="email" class="form-control" 
                                   value="<c:out value="${user.email}"/>" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Vai trò</label>
                            <input type="text" class="form-control" 
                                   value="<c:out value="${user.role eq 'ADMIN' ? 'Quản trị viên' : 'Khách hàng'}"/>" 
                                   disabled>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Trạng thái</label>
                            <input type="text" class="form-control" 
                                   value="<c:out value="${user.active ? 'Hoạt động' : 'Đã khóa'}"/>" 
                                   disabled>
                        </div>
                        <div class="d-flex gap-2">
                            <button type="submit" class="btn btn-souvenir">
                                <i class="fas fa-save me-2"></i>Cập nhật thông tin
                            </button>
                            <a href="${pageContext.request.contextPath}/home" class="btn btn-outline-secondary">
                                <i class="fas fa-times me-2"></i>Hủy
                            </a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>

