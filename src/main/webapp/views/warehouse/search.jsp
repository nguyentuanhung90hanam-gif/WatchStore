<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">TRA CỨU</p>
        <h2>Tra cứu thông tin kho</h2>
        <p class="module-desc">Tìm nhanh biến thể, tồn kho, phiếu nhập, phiếu xuất và phiếu kiểm kê theo mã, SKU, tên hoặc kho.</p>
    </div>
</div>

<form method="get" action="${cp}/manage/warehouse/search" class="dashboard-card" style="display:flex;gap:10px;align-items:end;flex-wrap:wrap;margin-bottom:16px;">
    <label style="flex:1;min-width:280px;">Từ khóa
        <input name="keyword" value="${keyword}" placeholder="SKU, tên sản phẩm, mã phiếu, nhà cung cấp..." autofocus>
    </label>
    <button class="button button-gold" type="submit">Tra cứu</button>
</form>

<c:if test="${not empty keyword}">
<div class="dashboard-card" style="margin-bottom:16px;">
    <h3>Biến thể (${variants.size()})</h3>
    <div class="table-wrap"><table><thead><tr><th>SKU</th><th>Sản phẩm</th><th>Biến thể</th><th>Trạng thái</th></tr></thead><tbody>
    <c:forEach items="${variants}" var="v"><tr><td><b>${v.sku}</b></td><td>${v.productName}</td><td>${v.variantName}</td><td>${v.status}</td></tr></c:forEach>
    <c:if test="${empty variants}"><tr><td colspan="4">Không có kết quả.</td></tr></c:if>
    </tbody></table></div>
</div>

<div class="dashboard-card" style="margin-bottom:16px;">
    <h3>Tồn kho (${inventoryItems.size()})</h3>
    <div class="table-wrap"><table><thead><tr><th>Kho</th><th>SKU</th><th>Sản phẩm</th><th>Tồn</th><th>Khả dụng</th><th>Trạng thái</th></tr></thead><tbody>
    <c:forEach items="${inventoryItems}" var="i"><tr><td>${i.warehouseName}</td><td><b>${i.sku}</b></td><td>${i.productName}<br><small>${i.variantName}</small></td><td>${i.quantityOnHand}</td><td>${i.availableQuantity}</td><td>${i.stockStatus}</td></tr></c:forEach>
    <c:if test="${empty inventoryItems}"><tr><td colspan="6">Không có kết quả.</td></tr></c:if>
    </tbody></table></div>
</div>

<div class="dashboard-card" style="margin-bottom:16px;">
    <h3>Phiếu nhập (${receipts.size()})</h3>
    <div class="table-wrap"><table><thead><tr><th>Mã</th><th>Kho</th><th>Nhà cung cấp</th><th>Ngày</th><th>Trạng thái</th></tr></thead><tbody>
    <c:forEach items="${receipts}" var="r"><tr><td><a href="${cp}/manage/warehouse/receipt-detail?id=${r.stockReceiptId}">${r.receiptCode}</a></td><td>${r.warehouseName}</td><td>${r.supplierName}</td><td>${r.receiptDate}</td><td>${r.status}</td></tr></c:forEach>
    <c:if test="${empty receipts}"><tr><td colspan="5">Không có kết quả.</td></tr></c:if>
    </tbody></table></div>
</div>

<div class="dashboard-card" style="margin-bottom:16px;">
    <h3>Phiếu xuất (${exports.size()})</h3>
    <div class="table-wrap"><table><thead><tr><th>Mã</th><th>Kho</th><th>Loại</th><th>Ngày</th><th>Trạng thái</th></tr></thead><tbody>
    <c:forEach items="${exports}" var="e"><tr><td><a href="${cp}/manage/warehouse/export-detail?id=${e.stockExportId}">${e.exportCode}</a></td><td>${e.warehouseName}</td><td>${e.exportType}</td><td>${e.exportDate}</td><td>${e.status}</td></tr></c:forEach>
    <c:if test="${empty exports}"><tr><td colspan="5">Không có kết quả.</td></tr></c:if>
    </tbody></table></div>
</div>

<div class="dashboard-card" style="margin-bottom:16px;">
    <h3>Lịch sử biến động (${transactions.size()})</h3>
    <div class="table-wrap"><table><thead><tr><th>Thời gian</th><th>Kho</th><th>SKU</th><th>Loại</th><th>Thay đổi</th><th>Tham chiếu</th></tr></thead><tbody>
    <c:forEach items="${transactions}" var="t"><tr><td>${t.createdAt}</td><td>${t.warehouseName}</td><td><b>${t.sku}</b></td><td>${t.transactionType}</td><td>${t.quantityChange}</td><td>${t.referenceType} ${t.referenceId}</td></tr></c:forEach>
    <c:if test="${empty transactions}"><tr><td colspan="6">Không có kết quả.</td></tr></c:if>
    </tbody></table></div>
</div>

<div class="dashboard-card">
    <h3>Phiếu kiểm kê (${stocktakes.size()})</h3>
    <div class="table-wrap"><table><thead><tr><th>Mã</th><th>Kho</th><th>Ngày</th><th>Trạng thái</th></tr></thead><tbody>
    <c:forEach items="${stocktakes}" var="s"><tr><td><a href="${cp}/manage/warehouse/stocktake-detail?id=${s.stocktakeId}">${s.stocktakeCode}</a></td><td>${s.warehouseName}</td><td>${s.stocktakeDate}</td><td>${s.status}</td></tr></c:forEach>
    <c:if test="${empty stocktakes}"><tr><td colspan="4">Không có kết quả.</td></tr></c:if>
    </tbody></table></div>
</div>
</c:if>
