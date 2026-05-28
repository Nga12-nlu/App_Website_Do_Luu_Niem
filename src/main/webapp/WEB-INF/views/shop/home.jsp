<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Trang chủ"/>
<%@ include file="/WEB-INF/views/layout/header.jspf" %>

<div class="hero-section text-center">
    <h1><i class="fas fa-store me-2"></i>${siteContent['home.hero.title']}</h1>
    <p class="lead">${siteContent['home.hero.subtitle']}</p>
    <div class="hero-badges">
        <span class="badge bg-light text-dark"><i class="fas fa-truck me-1"></i>${siteContent['home.hero.badge1']}</span>
        <span class="badge bg-light text-dark"><i class="fas fa-shield-alt me-1"></i>${siteContent['home.hero.badge2']}</span>
        <span class="badge bg-light text-dark"><i class="fas fa-palette me-1"></i>${siteContent['home.hero.badge3']}</span>
    </div>
</div>

<c:if test="${not empty categories}">
    <h4 class="section-title"><i class="fas fa-th-large me-2"></i>Danh mục sản phẩm</h4>
    <div class="row g-3 mb-5">
        <c:forEach var="cat" items="${categories}" varStatus="st">
            <div class="col-6 col-md-4 col-lg-2">
                <a href="${pageContext.request.contextPath}/products?category=${cat.id}" class="text-decoration-none">
                    <div class="card category-card h-100">
                        <div class="card-body text-center">
                            <div class="cat-icon">
                                <c:choose>
                                    <c:when test="${st.index % 3 eq 0}"><i class="fas fa-map-marked-alt"></i></c:when>
                                    <c:when test="${st.index % 3 eq 1}"><i class="fas fa-couch"></i></c:when>
                                    <c:otherwise><i class="fas fa-paint-brush"></i></c:otherwise>
                                </c:choose>
                            </div>
                            <h6 class="card-title mb-0"><c:out value="${cat.name}"/></h6>
                        </div>
                    </div>
                </a>
            </div>
        </c:forEach>
    </div>
</c:if>

<h4 class="section-title"><i class="fas fa-star me-2 text-warning"></i>${siteContent['home.latest.title']}</h4>
<c:choose>
    <c:when test="${empty latestProducts}">
        <div class="alert alert-info">Chưa có sản phẩm nào.</div>
    </c:when>
    <c:otherwise>
        <div class="row g-4">
            <c:forEach var="p" items="${latestProducts}">
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
        <div class="mt-5 text-center">
            <a href="${pageContext.request.contextPath}/products" class="btn btn-souvenir">${siteContent['home.latest.cta']}</a>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/layout/footer.jspf" %>
