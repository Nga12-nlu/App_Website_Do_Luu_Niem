<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.app.app_website_do_luu_niem.model.Order" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Chi tiết người dùng"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<c:if test="${not empty flashMessage}">
    <div class="alert alert-${flashType} alert-dismissible fade show admin-flash" role="alert">
        <i class="fas fa-${flashType eq 'success' ? 'check-circle' : 'exclamation-triangle'} me-2"></i>
        <c:out value="${flashMessage}"/>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<div class="admin-card">
    <div class="admin-card-header">
        <h2 class="admin-card-title">
            <i class="fas fa-user me-2"></i>Chi tiết người dùng
        </h2>
        <div class="d-flex flex-wrap gap-2">
            <a href="${pageContext.request.contextPath}/admin/users?action=edit&id=${user.id}" class="admin-btn admin-btn-primary">
                <i class="fas fa-edit"></i>Sửa
            </a>
            <a href="${pageContext.request.contextPath}/admin/users" class="admin-btn admin-btn-outline">
                <i class="fas fa-arrow-left"></i>Danh sách
            </a>
        </div>
    </div>
</div>

<div class="row g-4">
    <div class="col-lg-4">
        <div class="admin-card user-profile-card">
            <div class="card-body p-4 text-center">
                <div class="user-avatar user-avatar-lg mx-auto mb-3">
                    ${fn:substring(user.fullName, 0, 1)}
                </div>
                <h4 class="mb-1"><c:out value="${user.fullName}"/></h4>
                <p class="text-muted mb-3"><c:out value="${user.email}"/></p>
                <div class="d-flex justify-content-center gap-2 flex-wrap mb-3">
                    <span class="admin-badge ${user.adminRole ? 'admin-badge-danger' : 'admin-badge-info'}">
                        ${user.adminRole ? 'Quản trị viên' : 'Khách hàng'}
                    </span>
                    <span class="admin-badge ${user.active ? 'admin-badge-success' : 'admin-badge-warning'}">
                        ${user.active ? 'Hoạt động' : 'Đã khóa'}
                    </span>
                </div>
                <p class="small text-muted mb-0">ID #${user.id}</p>
                <%
                    if (pageContext.getAttribute("user") != null) {
                        com.app.app_website_do_luu_niem.model.User u =
                                (com.app.app_website_do_luu_niem.model.User) pageContext.getAttribute("user");
                        if (u.getCreatedAt() != null) {
                            out.print("<p class=\"small text-muted\">Tham gia "
                                    + u.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                    + "</p>");
                        }
                    }
                %>
                <c:if test="${isSelf}">
                    <div class="alert alert-info small py-2 mt-3 mb-0">Đây là tài khoản của bạn.</div>
                </c:if>
                <c:if test="${isLastAdmin}">
                    <div class="alert alert-warning small py-2 mt-3 mb-0">Quản trị viên duy nhất — không thể hạ quyền hoặc khóa.</div>
                </c:if>
            </div>
            <div class="user-profile-actions p-3 border-top">
                <c:if test="${not isSelf and not isLastAdmin}">
                    <a href="${pageContext.request.contextPath}/admin/users?action=toggle-active&id=${user.id}"
                       class="admin-btn w-100 mb-2 ${user.active ? 'admin-btn-warning' : 'admin-btn-success'}"
                       onclick="return confirm('${user.active ? 'Khóa' : 'Kích hoạt'} tài khoản này?');">
                        <i class="fas fa-${user.active ? 'lock' : 'unlock'} me-2"></i>
                        ${user.active ? 'Khóa tài khoản' : 'Kích hoạt tài khoản'}
                    </a>
                </c:if>
                <c:if test="${orderCount eq 0 and not user.adminRole and not isSelf}">
                    <a href="${pageContext.request.contextPath}/admin/users?action=delete&id=${user.id}"
                       class="admin-btn admin-btn-danger w-100"
                       data-confirm-delete>
                        <i class="fas fa-trash me-2"></i>Xóa tài khoản
                    </a>
                </c:if>
            </div>
        </div>

        <div class="admin-card mt-4">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0"><i class="fas fa-key me-2"></i>Đặt lại mật khẩu</h5>
            </div>
            <div class="card-body p-4">
                <form method="post" action="${pageContext.request.contextPath}/admin/users">
                    <input type="hidden" name="action" value="reset-password">
                    <input type="hidden" name="id" value="${user.id}">
                    <div class="mb-3">
                        <label class="admin-filter-label">Mật khẩu mới</label>
                        <input type="password" name="password" class="form-control" required minlength="8"
                               placeholder="Tối thiểu 8 ký tự" autocomplete="new-password">
                    </div>
                    <button type="submit" class="admin-btn admin-btn-outline w-100"
                            onclick="return confirm('Đặt mật khẩu mới cho người dùng này?');">
                        <i class="fas fa-redo me-2"></i>Cập nhật mật khẩu
                    </button>
                </form>
            </div>
        </div>
    </div>

    <div class="col-lg-8">
        <div class="row g-3 mb-4">
            <div class="col-sm-4">
                <div class="user-stat-card user-stat-total">
                    <span class="user-stat-label">Tổng đơn hàng</span>
                    <strong class="user-stat-value">${orderCount}</strong>
                </div>
            </div>
            <div class="col-sm-8">
                <c:if test="${orderCount > 0}">
                    <a href="${pageContext.request.contextPath}/admin/orders?search=${user.email}"
                       class="admin-btn admin-btn-primary h-100 d-flex align-items-center justify-content-center">
                        <i class="fas fa-shopping-cart me-2"></i>Xem tất cả đơn của khách
                    </a>
                </c:if>
            </div>
        </div>

        <div class="admin-card">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0"><i class="fas fa-receipt me-2"></i>Đơn hàng gần đây</h5>
            </div>
            <div class="admin-table-wrapper border-0">
                <table class="admin-table mb-0">
                    <thead>
                        <tr>
                            <th>Mã đơn</th>
                            <th>Tổng tiền</th>
                            <th>Trạng thái</th>
                            <th>Ngày</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty recentOrders}">
                                <tr>
                                    <td colspan="5" class="text-center py-4 text-muted">Chưa có đơn hàng</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="o" items="${recentOrders}">
                                    <tr>
                                        <td><strong>#${o.id}</strong></td>
                                        <td>
                                            <span class="price-value" data-price="${o.totalAmount}">${o.totalAmount}</span> &#8363;
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${o.status eq 'PENDING'}">
                                                    <span class="admin-badge admin-badge-warning">Chờ xử lý</span>
                                                </c:when>
                                                <c:when test="${o.status eq 'CONFIRMED'}">
                                                    <span class="admin-badge admin-badge-success">Đã xác nhận</span>
                                                </c:when>
                                                <c:when test="${o.status eq 'PACKAGING'}">
                                                    <span class="admin-badge admin-badge-warning">Đang đóng gói</span>
                                                </c:when>
                                                <c:when test="${o.status eq 'AWAITING_SHIPPING'}">
                                                    <span class="admin-badge admin-badge-warning">Chờ giao ĐVVC</span>
                                                </c:when>
                                                <c:when test="${o.status eq 'SHIPPING'}">
                                                    <span class="admin-badge admin-badge-info">Đang giao hàng</span>
                                                </c:when>
                                                <c:when test="${o.status eq 'SHIPPED'}">
                                                    <span class="admin-badge admin-badge-success">Đã giao hàng</span>
                                                </c:when>
                                                <c:when test="${o.status eq 'CANCELLED'}">
                                                    <span class="admin-badge admin-badge-danger">Đã hủy</span>
                                                </c:when>
                                                <c:otherwise><span class="admin-badge">${o.status}</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-nowrap">
                                            <%
                                                Order ord = (Order) pageContext.getAttribute("o");
                                                if (ord != null && ord.getCreatedAt() != null) {
                                                    out.print(ord.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                                }
                                            %>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/orders?action=detail&id=${o.id}"
                                               class="admin-btn admin-btn-outline btn-sm">
                                                <i class="fas fa-eye"></i>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
