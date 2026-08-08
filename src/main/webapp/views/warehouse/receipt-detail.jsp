<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading" style="display:flex; justify-content:space-between; align-items:flex-start;">
    <div class="module-title-area">
        <p class="eyebrow dark">NHẬP KHO</p>
        <h2>Chi tiết phiếu nhập #${receipt.receiptCode}</h2>
        <p class="module-desc">
            Trạng thái:
            <span class="status-badge
                ${receipt.status == 'COMPLETED' ? 'success' :
                  receipt.status == 'PENDING'   ? 'warning' :
                  receipt.status == 'CANCELLED' ? 'neutral' : 'draft'}">
                ${receipt.status}
            </span>
        </p>
    </div>
    <div style="display:flex; gap:8px; flex-wrap:wrap; justify-content:flex-end;">
        <a class="button button-outline" href="${cp}/manage/warehouse/receipts">Quay lại</a>
        <c:if test="${receipt.status == 'COMPLETED'}">
            <a class="button button-gold" href="${cp}/manage/warehouse/receipt-pdf?id=${receipt.stockReceiptId}" target="_blank">In PDF</a>
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
            <td><b>${receipt.receiptCode}</b></td>
            <td style="width:160px; color:#888;">Ngày lập</td>
            <td>${receipt.receiptDate}</td>
        </tr>
        <tr>
            <td style="color:#888;">Kho nhập</td>
            <td>${receipt.warehouseName}</td>
            <td style="color:#888;">Người lập</td>
            <td>${receipt.createdByName}</td>
        </tr>
        <tr>
            <td style="color:#888;">Nhà cung cấp</td>
            <td>${receipt.supplierName}<c:if test="${not empty receipt.supplierPhone}"> – ${receipt.supplierPhone}</c:if></td>
            <td style="color:#888;">Người duyệt</td>
            <td>${receipt.approvedBy != null ? receipt.approvedBy : '—'}<c:if test="${receipt.approvedAt != null}"> (${receipt.approvedAt})</c:if></td>
        </tr>
        <tr>
            <td style="color:#888;">Tổng tiền</td>
            <td><b><fmt:formatNumber value="${receipt.totalCost}" pattern="#,##0"/>₫</b></td>
            <td style="color:#888;">Ghi chú</td>
            <td>${receipt.note}</td>
        </tr>
    </table>
</div>

<%-- Workflow actions --%>
<c:if test="${receipt.status == 'DRAFT' || receipt.status == 'PENDING'}">
    <div class="dashboard-card" style="margin-bottom:16px; display:flex; gap:10px; flex-wrap:wrap;">
        <c:if test="${receipt.status == 'DRAFT'}">
            <form method="post" action="${cp}/manage/warehouse/receipt-submit" style="display:inline;">
                <input type="hidden" name="receiptId" value="${receipt.stockReceiptId}">
                <button type="submit" class="button button-gold"
                    onclick="return confirm('Gửi phiếu để chờ duyệt?')">Gửi duyệt (DRAFT → PENDING)</button>
            </form>
        </c:if>
        <c:if test="${receipt.status == 'PENDING'}">
            <form method="post" action="${cp}/manage/warehouse/receipt-approve" style="display:inline;">
                <input type="hidden" name="receiptId" value="${receipt.stockReceiptId}">
                <button type="submit" class="button button-gold"
                    onclick="return confirm('Xác nhận duyệt phiếu? Tồn kho sẽ được cập nhật ngay.')">Duyệt &amp; Nhập kho (PENDING → COMPLETED)</button>
            </form>
        </c:if>
        <form method="post" action="${cp}/manage/warehouse/receipt-cancel" style="display:inline;">
            <input type="hidden" name="receiptId" value="${receipt.stockReceiptId}">
            <button type="submit" class="button btn-cancel"
                onclick="return confirm('Hủy phiếu này?')">Hủy phiếu</button>
        </form>
    </div>
</c:if>

<%-- Item list --%>
<div class="dashboard-card">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
        <h3>Danh sách sản phẩm</h3>
        <c:if test="${receipt.status == 'DRAFT'}">
            <button class="button button-outline" onclick="toggleAddForm()">+ Thêm sản phẩm</button>
        </c:if>
    </div>

    <%-- Add-item form (DRAFT only) --%>
    <c:if test="${receipt.status == 'DRAFT'}">
        <div id="add-item-form" style="display:none; background:#f9f9f9; padding:16px; border-radius:8px; margin-bottom:16px;">
            <form method="post" action="${cp}/manage/warehouse/receipt-add-item">
                <input type="hidden" name="receiptId" value="${receipt.stockReceiptId}">
                <div style="display:flex; gap:12px; flex-wrap:wrap; align-items:flex-end;">
                    <label style="flex:2; min-width:200px;">Biến thể
                        <select name="variantId" required>
                            <option value="">-- Chọn --</option>
                            <c:forEach items="${variants}" var="v">
                                <option value="${v.variantId}">${v.productName} – ${v.variantName} (${v.sku})</option>
                            </c:forEach>
                        </select>
                    </label>
                    <label style="width:100px;">Số lượng
                        <input type="number" name="quantity" min="1" required value="1">
                    </label>
                    <label style="width:140px;">Đơn giá (₫)
                        <input type="number" name="unitCost" min="0" step="1000" required value="0">
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
                    <th>Đơn giá</th>
                    <th>Thành tiền</th>
                    <c:if test="${receipt.status == 'DRAFT'}"><th>Thao tác</th></c:if>
                </tr>
            </thead>
            <tbody>
                <c:set var="totalQty" value="0"/>
                <c:set var="totalAmount" value="0"/>
                <c:forEach items="${receipt.items}" var="item" varStatus="loop">
                    <tr>
                        <td>${loop.index + 1}</td>
                        <td><b>${item.productName}</b><br><small>${item.variantName}</small></td>
                        <td>${item.sku}</td>
                        <td>
                            <c:if test="${receipt.status == 'DRAFT'}">
                                <form method="post" action="${cp}/manage/warehouse/receipt-update-item"
                                      style="display:flex; gap:6px; align-items:center;">
                                    <input type="hidden" name="itemId" value="${item.stockReceiptItemId}">
                                    <input type="hidden" name="receiptId" value="${receipt.stockReceiptId}">
                                    <input type="hidden" name="unitCost" value="${item.unitCost}">
                                    <input type="number" name="quantity" value="${item.quantity}" min="1"
                                           style="width:70px;" required>
                                    <button type="submit" class="button button-outline" style="padding:4px 8px; font-size:11px;">Lưu</button>
                                </form>
                            </c:if>
                            <c:if test="${receipt.status != 'DRAFT'}">${item.quantity}</c:if>
                        </td>
                        <td><fmt:formatNumber value="${item.unitCost}" pattern="#,##0"/>₫</td>
                        <td><fmt:formatNumber value="${item.lineTotal}" pattern="#,##0"/>₫</td>
                        <c:if test="${receipt.status == 'DRAFT'}">
                            <td>
                                <form method="post" action="${cp}/manage/warehouse/receipt-delete-item"
                                      style="display:inline;"
                                      onsubmit="return confirm('Xóa sản phẩm này?')">
                                    <input type="hidden" name="itemId" value="${item.stockReceiptItemId}">
                                    <input type="hidden" name="receiptId" value="${receipt.stockReceiptId}">
                                    <button type="submit" class="btn-remove">Xóa</button>
                                </form>
                            </td>
                        </c:if>
                    </tr>
                    <c:set var="totalQty" value="${totalQty + item.quantity}"/>
                    <c:set var="totalAmount" value="${totalAmount + item.lineTotal}"/>
                </c:forEach>
                <c:if test="${empty receipt.items}">
                    <tr><td colspan="7" style="text-align:center; color:#888;">Chưa có sản phẩm nào</td></tr>
                </c:if>
                <tr style="font-weight:bold; background:#f5f5f5;">
                    <td colspan="${receipt.status == 'DRAFT' ? 3 : 3}" style="text-align:right;">Tổng cộng:</td>
                    <td>${totalQty}</td>
                    <td></td>
                    <td><fmt:formatNumber value="${totalAmount}" pattern="#,##0"/>₫</td>
                    <c:if test="${receipt.status == 'DRAFT'}"><td></td></c:if>
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
