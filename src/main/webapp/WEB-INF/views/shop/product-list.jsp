<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Sản phẩm"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<h4 class="section-title mb-4"><i class="fas fa-th-large me-2 text-primary"></i>Danh sách sản phẩm</h4>

<form method="get" action="${pageContext.request.contextPath}/products" class="row g-3 mb-4">
    <div class="col-md-4">
        <input type="text" name="q" class="form-control" placeholder="Tìm kiếm..." value="${keyword}">
    </div>
    <div class="col-md-3">
        <select name="category" class="form-select">
            <option value="">Tất cả danh mục</option>
            <c:forEach var="c" items="${categories}">
                <option value="${c.id}" ${currentCategory eq c.id ? 'selected' : ''}><c:out value="${c.name}"/></option>
            </c:forEach>
        </select>
    </div>
    <div class="col-md-3">
        <select name="sort" class="form-select">
            <option value="">Mới nhất</option>
            <option value="price_asc" ${sort eq 'price_asc' ? 'selected' : ''}>Giá thấp đến cao</option>
            <option value="price_desc" ${sort eq 'price_desc' ? 'selected' : ''}>Giá cao đến thấp</option>
        </select>
    </div>
    <div class="col-md-2">
        <button type="submit" class="btn btn-souvenir w-100"><i class="fas fa-search me-2"></i>Tìm kiếm</button>
    </div>
</form>

<c:choose>
    <c:when test="${empty products}">
        <div class="alert alert-info">Không tìm thấy sản phẩm nào.</div>
    </c:when>
    <c:otherwise>
        <div class="row g-4">
            <c:forEach var="p" items="${products}">
                <div class="col-6 col-md-4 col-lg-3">
                    <div class="card product-card h-100">
                        <a href="${pageContext.request.contextPath}/product?id=${p.id}" class="text-decoration-none text-dark">
                            <div class="card-img-wrapper">
                                <c:choose>
                                    <c:when test="${not empty p.imageUrl}">
                                        <img src="${fn:startsWith(p.imageUrl, 'data:') 
                                                   ? p.imageUrl 
                                                   : (fn:startsWith(p.imageUrl, 'http') 
                                                        ? p.imageUrl 
                                                        : pageContext.request.contextPath.concat('/').concat(p.imageUrl))}" 
                                             class="card-img-top" alt="<c:out value="${p.name}"/>"
                                             onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                                        <div class="img-placeholder" style="display:none;">&#127921;</div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="img-placeholder">&#127921;</div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="card-body">
                                <h6 class="card-title"><c:out value="${p.name}"/></h6>
                                <p class="price mb-1">
                                    <span class="price-value" data-price="${p.price != null ? p.price : 0}">${p.price != null ? p.price : 0}</span>
                                    <small>&#8363;</small>
                                </p>
                                <c:if test="${p.stock <= 0}">
                                    <span class="badge bg-secondary">Hết hàng</span>
                                </c:if>
                            </div>
                        </a>
                    </div>
                </div>
            </c:forEach>
        </div>

        <c:if test="${totalPages > 1}">
            <nav class="mt-4">
                <ul class="pagination justify-content-center">
                    <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                        <a class="page-link" href="?page=${currentPage-1}&q=${keyword}&category=${currentCategory}&sort=${sort}">Trước</a>
                    </li>
                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <li class="page-item ${i eq currentPage ? 'active' : ''}">
                            <a class="page-link" href="?page=${i}&q=${keyword}&category=${currentCategory}&sort=${sort}">${i}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                        <a class="page-link" href="?page=${currentPage+1}&q=${keyword}&category=${currentCategory}&sort=${sort}">Sau</a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>

