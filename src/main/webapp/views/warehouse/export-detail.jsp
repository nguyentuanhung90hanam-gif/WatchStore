<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading" style="display:flex; justify-content:space-between; align-items:flex-start;">
    <div class="module-title-area">
        <p class="eyebrow dark">XUẤT KHO</p>
        <h2>Chi tiết phiếu xuất #${export.exportCode}</h2>
        <p class="module-desc">
            Trạng thái:
            <span class="status-badge
                ${export.status == 'COMPLETED' ? 'success' :
                  export.status == 'PENDING'   ? 'warning' :
                  export.status == 'CANCELLED' ? 'neutral' : 'draft'}">
                ${export.status}
            </span>
        </p>
    </div>
    <div style="display:flex; gap:8px; flex-wrap:wrap; justify-content:flex-end;">
        <a class="button button-outline" href="${cp}/manage/warehouse/exports">Quay lại</a>
        <c:if test="${export.status == 'COMPLETED'}">
            <a class="button button-gold" href="${cp}/manage/warehouse/export-pdf?id=${export.stockExportId}" target="_blank">In PDF</a>
        </c:if>
    </div>
</div>

<%-- Flash messages --%>
<c:if test="${not empty sessionScope.successMsg}">
    <div class="alert alert-success">${sessionScope.successMsg}</div>
    <c:remove var="successMsg" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.errorMsg}">
    <div class="alert alert-error">${sessionScope.errorMsg}</div>
    <c:remove var="errorMsg" scope="session"/>
</c:if>

<%-- Info card --%>
<div class="dashboard-card" style="margin-bottom:16px;">
    <h3 style="margin-bottom:12px;">Thông tin phiếu</h3>
    <table style="width:100%; border-spacing:8px;">
        <tr>
            <td style="width:160px; color:#888;">Mã phiếu</td>
            <td><b>${export.exportCode}</b></td>
            <td style="width:160px; color:#888;">Ngày lập</td>
            <td>${export.exportDate}</td>
        </tr>
        <tr>
            <td style="color:#888;">Kho xuất</td>
            <td>${export.warehouseName}</td>
            <td style="color:#888;">Người lập</td>
            <td>${export.createdByName}</td>
        </tr>
        <tr>
            <td style="color:#888;">Loại xuất</td>
            <td>${export.exportType}</td>
            <td style="color:#888;">Người nhận</td>
            <td>${export.receiverName}</td>
        </tr>
        <tr>
            <td style="color:#888;">Mã đơn hàng</td>
            <td>${export.orderId != null ? export.orderId : '—'}</td>
            <td style="color:#888;">Người duyệt</td>
            <td>${export.approvedBy != null ? export.approvedBy : '—'}<c:if test="${export.approvedAt != null}"> (${export.approvedAt})</c:if></td>
        </tr>
        <tr>
            <td style="color:#888;">Ghi chú</td>
            <td colspan="3">${export.note}</td>
        </tr>
    </table>
</div>

<%-- Workflow actions --%>
<c:if test="${export.status == 'DRAFT' || export.status == 'PENDING'}">
    <div class="dashboard-card" style="margin-bottom:16px; display:flex; gap:10px; flex-wrap:wrap;">
        <c:if test="${export.status == 'DRAFT'}">
            <form method="post" action="${cp}/manage/warehouse/export-submit" style="display:inline;">
                <input type="hidden" name="exportId" value="${export.stockExportId}">
                <button type="submit" class="button button-gold"
                    onclick="return confirm('Gửi phiếu để chờ duyệt?')">Gửi duyệt (DRAFT → PENDING)</button>
            </form>
        </c:if>
        <c:if test="${export.status == 'PENDING'}">
            <form method="post" action="${cp}/manage/warehouse/export-approve" style="display:inline;">
                <input type="hidden" name="exportId" value="${export.stockExportId}">
                <button type="submit" class="button button-gold"
                    onclick="return confirm('Xác nhận duyệt? Tồn kho sẽ bị giảm ngay lập tức.')">Duyệt &amp; Xuất kho (PENDING → COMPLETED)</button>
            </form>
        </c:if>
        <form method="post" action="${cp}/manage/warehouse/export-cancel" style="display:inline;">
            <input type="hidden" name="exportId" value="${export.stockExportId}">
            <button type="submit" class="button btn-cancel"
                onclick="return confirm('Hủy phiếu này?')">Hủy phiếu</button>
        </form>
    </div>
</c:if>

<%-- Item list --%>
<div class="dashboard-card">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
        <h3>Danh sách sản phẩm xuất</h3>
        <c:if test="${export.status == 'DRAFT'}">
            <button class="button button-outline" onclick="toggleAddForm()">+ Thêm sản phẩm</button>
        </c:if>
    </div>

    <%-- Add-item form (DRAFT only) --%>
    <c:if test="${export.status == 'DRAFT'}">
        <div id="add-item-form" style="display:none; background:#f9f9f9; padding:16px; border-radius:8px; margin-bottom:16px;">
            <form method="post" action="${cp}/manage/warehouse/export-add-item">
                <input type="hidden" name="exportId" value="${export.stockExportId}">
                <div style="display:flex; gap:12px; flex-wrap:wrap; align-items:flex-end;">
                    <label style="flex:2; min-width:200px;">Biến thể
                        <select name="variantId" required>
                            <option value="">-- Chọn --</option>
                            <c:forEach items="${variants}" var="v">
                                <option value="${v.variantId}">${v.productName} – ${v.variantName} (${v.sku})</option>
                            </c:forEach>
                        </select>
                    </label>
                    <label style="width:110px;">Số lượng
                        <input type="number" name="quantity" min="1" required value="1">
                    </label>
                    <button type="submit" class="button button-gold" style="height:38px;">Thêm</button>
                </div>
            </form>
        </div>
    </c:if>

    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>STT</th>
                    <th>Sản phẩm</th>
                    <th>SKU</th>
                    <th>Số lượng</th>
                    <c:if test="${export.status == 'DRAFT'}"><th>Thao tác</th></c:if>
                </tr>
            </thead>
            <tbody>
                <c:set var="totalQty" value="0"/>
                <c:forEach items="${export.items}" var="item" varStatus="loop">
                    <tr>
                        <td>${loop.index + 1}</td>
                        <td><b>${item.productName}</b><br><small>${item.variantName}</small></td>
                        <td>${item.sku}</td>
                        <td>
                            <c:if test="${export.status == 'DRAFT'}">
                                <form method="post" action="${cp}/manage/warehouse/export-update-item"
                                      style="display:flex; gap:6px; align-items:center;">
                                    <input type="hidden" name="itemId" value="${item.stockExportItemId}">
                                    <input type="hidden" name="exportId" value="${export.stockExportId}">
                                    <input type="number" name="quantity" value="${item.quantity}" min="1"
                                           style="width:70px;" required>
                                    <button type="submit" class="button button-outline" style="padding:4px 8px; font-size:11px;">Lưu</button>
                                </form>
                            </c:if>
                            <c:if test="${export.status != 'DRAFT'}">${item.quantity}</c:if>
                        </td>
                        <c:if test="${export.status == 'DRAFT'}">
                            <td>
                                <form method="post" action="${cp}/manage/warehouse/export-delete-item"
                                      style="display:inline;"
                                      onsubmit="return confirm('Xóa sản phẩm này?')">
                                    <input type="hidden" name="itemId" value="${item.stockExportItemId}">
                                    <input type="hidden" name="exportId" value="${export.stockExportId}">
                                    <button type="submit" class="btn-remove">Xóa</button>
                                </form>
                            </td>
                        </c:if>
                    </tr>
                    <c:set var="totalQty" value="${totalQty + item.quantity}"/>
                </c:forEach>
                <c:if test="${empty export.items}">
                    <tr><td colspan="5" style="text-align:center; color:#888;">Chưa có sản phẩm nào</td></tr>
                </c:if>
                <tr style="font-weight:bold; background:#f5f5f5;">
                    <td colspan="3" style="text-align:right;">Tổng cộng:</td>
                    <td>${totalQty}</td>
                    <c:if test="${export.status == 'DRAFT'}"><td></td></c:if>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<style>
.btn-remove { background:#e74c3c; color:#fff; border:none; border-radius:4px; padding:4px 10px; cursor:pointer; }
.btn-cancel { background:#95a5a6; color:#fff; border:none; border-radius:6px; padding:8px 18px; cursor:pointer; }
.alert { padding:12px 16px; border-radius:6px; margin-bottom:16px; }
.alert-success { background:#d4edda; color:#155724; border:1px solid #c3e6cb; }
.alert-error   { background:#f8d7da; color:#721c24; border:1px solid #f5c6cb; }
.status-badge.draft { background:#e8f4fd; color:#2980b9; }
</style>

<script>
function toggleAddForm() {
    var f = document.getElementById('add-item-form');
    f.style.display = f.style.display === 'none' ? 'block' : 'none';
}
</script>
