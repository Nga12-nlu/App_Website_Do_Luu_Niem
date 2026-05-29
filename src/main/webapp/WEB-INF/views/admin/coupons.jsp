<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quản lý mã giảm giá"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<c:if test="${param.saved eq '1'}">
    <div class="alert alert-success admin-flash"><i class="fas fa-check me-2"></i>Đã lưu mã giảm giá.</div>
</c:if>

<div class="admin-card">
    <div class="admin-card-header">
        <h2 class="admin-card-title"><i class="fas fa-ticket-alt me-2"></i>Mã giảm giá</h2>
        <a href="${pageContext.request.contextPath}/admin/coupons?action=create" class="admin-btn admin-btn-primary">
            <i class="fas fa-plus me-2"></i>Thêm mã
        </a>
    </div>
    <div class="card-body">
        <div class="admin-table-wrapper">
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>Mã</th>
                        <th>Mô tả</th>
                        <th>Loại</th>
                        <th>Giá trị</th>
                        <th>Đơn tối thiểu</th>
                        <th>Đã dùng</th>
                        <th>Trạng thái</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="c" items="${coupons}">
                        <tr>
                            <td><strong><c:out value="${c.code}"/></strong></td>
                            <td><c:out value="${c.description}"/></td>
                            <td><c:out value="${c.discountType eq 'PERCENT' ? 'Phần trăm' : 'Cố định'}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${c.discountType eq 'PERCENT'}"><c:out value="${c.discountValue}"/>%</c:when>
                                    <c:otherwise><span class="price-value" data-price="${c.discountValue}">${c.discountValue}</span>đ</c:otherwise>
                                </c:choose>
                            </td>
                            <td><span class="price-value" data-price="${c.minOrderAmount}">${c.minOrderAmount}</span>đ</td>
                            <td>
                                <c:out value="${c.usedCount}"/>
                                <c:if test="${c.usageLimit != null}"> / ${c.usageLimit}</c:if>
                            </td>
                            <td>
                                <span class="admin-badge ${c.active ? 'admin-badge-success' : 'admin-badge-secondary'}">
                                    ${c.active ? 'Hoạt động' : 'Tắt'}
                                </span>
                            </td>
                            <td>
                                <a href="${pageContext.request.contextPath}/admin/coupons?action=edit&id=${c.id}"
                                   class="admin-btn admin-btn-sm admin-btn-outline">
                                    <i class="fas fa-edit"></i>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty coupons}">
                        <tr><td colspan="8" class="text-center text-muted py-4">Chưa có mã giảm giá.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
        <p class="small text-muted mt-3 mb-0">
            Mã mẫu sau khi migrate: <code>WELCOME10</code>, <code>GIAM50K</code>, <code>FREESHIP</code>
        </p>
    </div>
</div>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
