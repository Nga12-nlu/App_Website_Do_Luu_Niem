<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.app.app_website_do_luu_niem.model.InventoryTransaction" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quản lý kho hàng & Hao hụt"/>
<%@ include file="/WEB-INF/views/admin/layout/admin-header.jspf" %>

<div class="admin-card">
    <div class="admin-card-header">
        <h2 class="admin-card-title">
            <i class="fas fa-warehouse me-2"></i>Quản lý kho hàng & Giao dịch tồn kho
        </h2>
        <div>
            <a href="${pageContext.request.contextPath}/admin/inventory?action=export" class="admin-btn admin-btn-outline">
                <i class="fas fa-file-export me-2"></i>Xuất Excel (CSV)
            </a>
        </div>
    </div>
</div>

<c:if test="${not empty successMsg}">
    <div class="alert alert-success mt-3"><c:out value="${successMsg}"/></div>
</c:if>
<c:if test="${not empty errorMsg}">
    <div class="alert alert-danger mt-3"><c:out value="${errorMsg}"/></div>
</c:if>

<c:if test="${not empty importErrors}">
    <div class="alert alert-warning mt-3">
        <h5><i class="fas fa-exclamation-triangle me-2"></i>Chi tiết lỗi khi Import:</h5>
        <ul class="mb-0" style="max-height: 200px; overflow-y: auto;">
            <c:forEach var="err" items="${importErrors}">
                <li><c:out value="${err}"/></li>
            </c:forEach>
        </ul>
    </div>
</c:if>

<div class="row g-4 mt-1">
    <!-- Manual Adjust -->
    <div class="col-md-6">
        <div class="admin-card h-100">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-tools me-2"></i>Điều chỉnh kho thủ công
                </h5>
            </div>
            <div class="card-body p-3">
                <form method="post" action="${pageContext.request.contextPath}/admin/inventory" id="adjustForm">
                    <input type="hidden" name="action" value="adjust">
                    
                    <div class="mb-3">
                        <label class="admin-filter-label">Chọn sản phẩm</label>
                        <select name="productId" id="adjustProduct" class="form-select" required onchange="updateVariantOptions()">
                            <option value="">-- Chọn sản phẩm --</option>
                        </select>
                    </div>

                    <div class="mb-3" id="variantWrapper" style="display: none;">
                        <label class="admin-filter-label">Chọn biến thể</label>
                        <select name="variantId" id="adjustVariant" class="form-select">
                            <!-- Populated dynamically -->
                        </select>
                    </div>

                    <div class="row">
                        <div class="col-6 mb-3">
                            <label class="admin-filter-label">Loại điều chỉnh</label>
                            <select name="type" class="form-select" required>
                                <option value="IMPORT">IMPORT (Nhập thêm)</option>
                                <option value="EXPORT">EXPORT (Xuất kho bán/khác)</option>
                                <option value="DAMAGE">DAMAGE (Hư hỏng)</option>
                                <option value="LOSS">LOSS (Thất thoát)</option>
                                <option value="DESTROYED">DESTROYED (Tiêu hủy)</option>
                            </select>
                        </div>
                        <div class="col-6 mb-3">
                            <label class="admin-filter-label">Số lượng</label>
                            <input type="number" name="quantity" class="form-control" min="1" required placeholder="Nhập số lượng">
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="admin-filter-label">Ghi chú / Lý do điều chỉnh</label>
                        <textarea name="note" class="form-control" rows="2" placeholder="Nhập lý do hư hỏng, nguồn gốc nhập kho..."></textarea>
                    </div>

                    <button type="submit" class="admin-btn admin-btn-primary w-100">
                        <i class="fas fa-check me-2"></i>Xác nhận điều chỉnh
                    </button>
                </form>
            </div>
        </div>
    </div>

    <!-- CSV Import -->
    <div class="col-md-6">
        <div class="admin-card h-100">
            <div class="admin-card-header">
                <h5 class="admin-card-title mb-0">
                    <i class="fas fa-file-csv me-2"></i>Nhập kho hàng loạt (CSV)
                </h5>
            </div>
            <div class="card-body p-3">
                <form method="post" action="${pageContext.request.contextPath}/admin/inventory" enctype="multipart/form-data">
                    <input type="hidden" name="action" value="import">
                    
                    <div class="mb-3">
                        <label class="admin-filter-label">1. Chọn file CSV tồn kho</label>
                        <input type="file" name="file" class="form-control" accept=".csv" required>
                        <div class="form-text text-muted">
                            Mẹo: Hãy bấm nút <strong>"Xuất Excel (CSV)"</strong> ở góc trên bên phải để lấy file mẫu chuẩn UTF-8, chỉnh sửa cột số lượng rồi tải lên tại đây.
                        </div>
                    </div>

                    <div class="mb-4">
                        <label class="admin-filter-label">2. Chế độ cập nhật</label>
                        <div class="form-check mb-2">
                            <input class="form-check-input" type="radio" name="importMode" id="modeAdd" value="ADD" checked>
                            <label class="form-check-label" for="modeAdd">
                                <strong>Cộng dồn (ADD)</strong>: Số lượng trong file sẽ được cộng thêm vào tồn kho hiện tại.
                            </label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="importMode" id="modeOverwrite" value="OVERWRITE">
                            <label class="form-check-label" for="modeOverwrite">
                                <strong>Ghi đè (OVERWRITE)</strong>: Điều chỉnh tồn kho hiện tại khớp chính xác với số lượng trong file.
                            </label>
                        </div>
                    </div>

                    <button type="submit" class="admin-btn admin-btn-primary w-100">
                        <i class="fas fa-upload me-2"></i>Bắt đầu Import File
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- Log Transactions -->
<div class="admin-card mt-4">
    <div class="admin-card-header">
        <h5 class="admin-card-title mb-0">
            <i class="fas fa-history me-2"></i>Lịch sử giao dịch kho hàng
        </h5>
    </div>
</div>

<div class="admin-table-wrapper">
    <table class="admin-table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Sản phẩm / Biến thể</th>
                <th>Loại GD</th>
                <th>Số lượng</th>
                <th>Ghi chú</th>
                <th>Người thực hiện</th>
                <th>Thời gian</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${empty transactions}">
                    <tr>
                        <td colspan="7" class="text-center py-4 text-muted">
                            <i class="fas fa-history fa-2x mb-2 d-block"></i>
                            Chưa có giao dịch kho nào được ghi nhận.
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="t" items="${transactions}">
                        <tr>
                            <td>#${t.id}</td>
                            <td>
                                <strong><c:out value="${t.productName}"/></strong>
                                <c:if test="${not empty t.variantLabel}">
                                    <br><span class="admin-badge admin-badge-info"><c:out value="${t.variantLabel}"/></span>
                                </c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${t.type eq 'IMPORT'}">
                                        <span class="admin-badge admin-badge-success">IMPORT</span>
                                    </c:when>
                                    <c:when test="${t.type eq 'EXPORT'}">
                                        <span class="admin-badge admin-badge-info">EXPORT</span>
                                    </c:when>
                                    <c:when test="${t.type eq 'DAMAGE'}">
                                        <span class="admin-badge admin-badge-warning">DAMAGE</span>
                                    </c:when>
                                    <c:when test="${t.type eq 'LOSS'}">
                                        <span class="admin-badge admin-badge-danger">LOSS</span>
                                    </c:when>
                                    <c:when test="${t.type eq 'DESTROYED'}">
                                        <span class="admin-badge admin-badge-danger">DESTROYED</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="admin-badge">${t.type}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <strong>
                                    <c:choose>
                                        <c:when test="${t.type eq 'IMPORT'}">+</c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                    ${t.quantity}
                                </strong>
                            </td>
                            <td><c:out value="${t.note != null ? t.note : '—'}"/></td>
                            <td><c:out value="${t.userFullName}"/></td>
                            <td>
                                <%
                                    InventoryTransaction t = (InventoryTransaction) pageContext.getAttribute("t");
                                    if (t != null && t.getCreatedAt() != null) {
                                        out.print(t.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                                    }
                                %>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>

<!-- Pagination -->
<c:if test="${totalPages > 1}">
    <nav class="admin-pagination">
        <ul class="pagination mb-0">
            <c:if test="${currentPage > 1}">
                <li class="page-item">
                    <a class="page-link" href="?page=${currentPage - 1}&pageSize=${pageSize}">
                        <i class="fas fa-chevron-left"></i>
                    </a>
                </li>
            </c:if>
            <c:forEach var="i" begin="1" end="${totalPages}">
                <li class="page-item ${i eq currentPage ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}&pageSize=${pageSize}">${i}</a>
                </li>
            </c:forEach>
            <c:if test="${currentPage < totalPages}">
                <li class="page-item">
                    <a class="page-link" href="?page=${currentPage + 1}&pageSize=${pageSize}">
                        <i class="fas fa-chevron-right"></i>
                    </a>
                </li>
            </c:if>
        </ul>
    </nav>
</c:if>

<script>
    // Product data parsed from server
    const rawData = [
        <c:forEach var="opt" items="${productOptions}" varStatus="status">
        {
            productId: ${opt.productId},
            productName: `<c:out value="${opt.productName}"/>`,
            variantId: ${opt.variantId != null ? opt.variantId : 'null'},
            variantName: `<c:out value="${opt.variantName != null ? opt.variantName : ''}"/>`,
            stock: ${opt.stock}
        }<c:if test="${not status.last}">,</c:if>
        </c:forEach>
    ];

    // Group items by product id
    const productsMap = {};
    rawData.forEach(item => {
        if (!productsMap[item.productId]) {
            productsMap[item.productId] = {
                id: item.productId,
                name: item.productName,
                variants: []
            };
        }
        if (item.variantId !== null) {
            productsMap[item.productId].variants.push({
                id: item.variantId,
                name: item.variantName,
                stock: item.stock
            });
        } else {
            productsMap[item.productId].stockWithoutVariant = item.stock;
        }
    });

    // Populate products dropdown on load
    const productSelect = document.getElementById('adjustProduct');
    Object.values(productsMap).forEach(p => {
        const option = document.createElement('option');
        option.value = p.id;
        option.textContent = p.name;
        productSelect.appendChild(option);
    });

    function updateVariantOptions() {
        const productId = productSelect.value;
        const variantSelect = document.getElementById('adjustVariant');
        const variantWrapper = document.getElementById('variantWrapper');
        
        variantSelect.innerHTML = '';
        
        if (!productId || !productsMap[productId]) {
            variantWrapper.style.display = 'none';
            return;
        }

        const p = productsMap[productId];
        if (p.variants && p.variants.length > 0) {
            variantWrapper.style.display = 'block';
            p.variants.forEach(v => {
                const opt = document.createElement('option');
                opt.value = v.id;
                opt.textContent = v.name + " (Tồn kho: " + v.stock + ")";
                variantSelect.appendChild(opt);
            });
        } else {
            variantWrapper.style.display = 'none';
            // Add a hidden input or just let variantId be null
            const opt = document.createElement('option');
            opt.value = 'null';
            opt.textContent = 'Mặc định';
            variantSelect.appendChild(opt);
        }
    }
</script>

<%@ include file="/WEB-INF/views/admin/layout/admin-footer.jspf" %>
