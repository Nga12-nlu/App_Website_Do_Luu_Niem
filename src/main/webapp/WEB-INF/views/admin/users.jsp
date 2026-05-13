<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.app.app_website_do_luu_niem.model.User" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quản lý người dùng"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<div class="admin-card">
    <div class="admin-card-header">
        <h2 class="admin-card-title">
            <i class="fas fa-users me-2"></i>Quản lý người dùng
        </h2>
    </div>
</div>

<div class="admin-table-wrapper">
    <table class="admin-table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Họ tên</th>
                <th>Email</th>
                <th>Vai trò</th>
                <th>Trạng thái</th>
                <th>Ngày tạo</th>
                <th style="width: 120px;">Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${empty users}">
                    <tr>
                        <td colspan="7" class="text-center py-4 text-muted">
                            <i class="fas fa-inbox fa-2x mb-2 d-block"></i>
                            Chưa có người dùng nào
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="u" items="${users}">
                        <tr>
                            <td>${u.id}</td>
                            <td><strong><c:out value="${u.fullName}"/></strong></td>
                            <td><c:out value="${u.email}"/></td>
                            <td>
                                <span class="admin-badge ${u.role eq 'ADMIN' ? 'admin-badge-danger' : 'admin-badge-info'}">
                                    ${u.role eq 'ADMIN' ? 'Quản trị viên' : 'Khách hàng'}
                                </span>
                            </td>
                            <td>
                                <span class="admin-badge ${u.active ? 'admin-badge-success' : 'admin-badge-warning'}">
                                    ${u.active ? 'Hoạt động' : 'Đã khóa'}
                                </span>
                            </td>
                            <td>
                                <%
                                    User u = (User) pageContext.getAttribute("u");
                                    if (u != null && u.getCreatedAt() != null) {
                                        out.print(u.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                    }
                                %>
                            </td>
                            <td>
                                <a href="${pageContext.request.contextPath}/admin/users?action=toggle-active&id=${u.id}" 
                                   class="admin-btn ${u.active ? 'admin-btn-outline' : 'admin-btn-primary'}"
                                   onclick="return confirm('${u.active ? 'Khóa' : 'Kích hoạt'} tài khoản này?');" 
                                   title="${u.active ? 'Khóa' : 'Kích hoạt'}">
                                    <i class="fas ${u.active ? 'fa-lock' : 'fa-unlock'}"></i>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
