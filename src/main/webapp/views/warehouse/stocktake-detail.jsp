<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">CHI TIẾT PHIẾU KIỂM KÊ</p>
        <h2>#${stocktake.stocktakeCode}</h2>
        <p class="module-desc">Ngày tạo: ${stocktake.stocktakeDate} | Trạng thái: 
            <span class="status-badge
                ${stocktake.status == 'COMPLETED' ? 'success' :
                  stocktake.status == 'COUNTING'  ? 'warning' :
                  stocktake.status == 'CANCELLED' ? 'neutral' : 'draft'}" style="margin-left: 8px;">
                ${stocktake.status}
            </span>
        </p>
    </div>
    
    <div style="display:flex; gap:10px;">
        <c:if test="${stocktake.status == 'DRAFT'}">
            <form action="${cp}/manage/warehouse/stocktake-submit" method="POST" style="display:inline;">
                <input type="hidden" name="stocktakeId" value="${stocktake.stocktakeId}">
                <button class="button button-gold" type="submit">Gửi duyệt (Xác nhận Đếm)</button>
            </form>
            <form action="${cp}/manage/warehouse/stocktake-cancel" method="POST" style="display:inline;">
                <input type="hidden" name="stocktakeId" value="${stocktake.stocktakeId}">
                <button class="button button-outline" type="submit" style="color:red; border-color:red;" onclick="return confirm('Hủy phiếu này?');">Hủy phiếu</button>
            </form>
        </c:if>
        
        <c:if test="${stocktake.status == 'COUNTING'}">
            <form action="${cp}/manage/warehouse/stocktake-approve" method="POST" style="display:inline;">
                <input type="hidden" name="stocktakeId" value="${stocktake.stocktakeId}">
                <button class="button button-gold" type="submit" onclick="return confirm('Duyệt phiếu và điều chỉnh tồn kho theo số lượng thực tế?');">Duyệt & Cân bằng tồn kho</button>
            </form>
            <form action="${cp}/manage/warehouse/stocktake-cancel" method="POST" style="display:inline;">
                <input type="hidden" name="stocktakeId" value="${stocktake.stocktakeId}">
                <button class="button button-outline" type="submit" style="color:red; border-color:red;" onclick="return confirm('Hủy phiếu này?');">Hủy phiếu</button>
            </form>
        </c:if>

        <a class="button button-outline" href="${cp}/manage/warehouse/stocktake">Quay lại</a>
    </div>
</div>

<c:if test="${not empty sessionScope.successMsg}">
    <div class="alert alert-success">${sessionScope.successMsg}</div>
    <c:remove var="successMsg" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.errorMsg}">
    <div class="alert alert-error">${sessionScope.errorMsg}</div>
    <c:remove var="errorMsg" scope="session"/>
</c:if>

<div class="dashboard-card" style="margin-bottom: 24px; padding: 20px;">
    <h3 style="margin-bottom: 16px;">Thông tin chung</h3>
    <div style="display:grid; grid-template-columns: 1fr 1fr; gap:20px;">
        <div>
            <p><strong>Mã phiếu:</strong> ${stocktake.stocktakeCode}</p>
            <p><strong>Kho kiểm kê:</strong> ${stocktake.warehouseName}</p>
            <p><strong>Ghi chú:</strong> ${stocktake.note}</p>
        </div>
        <div>
            <p><strong>Người tạo:</strong> ${stocktake.createdByName}</p>
            <c:if test="${stocktake.status == 'COMPLETED'}">
                <p><strong>Người duyệt:</strong> ID User ${stocktake.approvedBy}</p>
                <p><strong>Ngày duyệt:</strong> ${stocktake.approvedAt}</p>
            </c:if>
        </div>
    </div>
</div>

<div class="dashboard-card" style="padding: 20px;">
    <h3 style="margin-bottom: 16px;">Danh sách sản phẩm kiểm đếm</h3>
    <table style="width: 100%; text-align: left; border-collapse: collapse;">
        <thead>
            <tr style="border-bottom: 2px solid #eee;">
                <th style="padding: 10px;">SKU</th>
                <th style="padding: 10px;">Sản phẩm</th>
                <th style="padding: 10px; text-align:right;">Tồn hệ thống</th>
                <th style="padding: 10px; text-align:right;">Tồn đếm thực tế</th>
                <th style="padding: 10px; text-align:right;">Chênh lệch</th>
                <c:if test="${stocktake.status == 'DRAFT'}">
                    <th style="padding: 10px; text-align:center;">Thao tác</th>
                </c:if>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${stocktake.items}" var="item">
                <tr style="border-bottom: 1px solid #eee;">
                    <td style="padding: 10px;">${item.sku}</td>
                    <td style="padding: 10px;"><b>${item.productName}</b><br><small>${item.variantName}</small></td>
                    <td style="padding: 10px; text-align:right;">${item.systemQuantity}</td>
                    
                    <c:if test="${stocktake.status == 'DRAFT'}">
                        <td style="padding: 10px; text-align:right;">
                            <form action="${cp}/manage/warehouse/stocktake-update-item" method="POST" style="display:flex; justify-content:flex-end; gap:5px;">
                                <input type="hidden" name="stocktakeId" value="${stocktake.stocktakeId}">
                                <input type="hidden" name="itemId" value="${item.stocktakeItemId}">
                                <input type="number" name="actualQuantity" value="${item.actualQuantity}" class="input-field" style="width: 80px; padding: 4px; text-align:right;" min="0">
                                <button type="submit" class="button button-outline" style="padding: 4px 8px;">Lưu</button>
                            </form>
                        </td>
                        <td style="padding: 10px; text-align:right; font-weight:bold; color: ${item.differenceQuantity < 0 ? 'red' : (item.differenceQuantity > 0 ? 'green' : 'black')}">
                            ${item.differenceQuantity > 0 ? '+' : ''}${item.differenceQuantity}
                        </td>
                        <td style="padding: 10px; text-align:center;">
                            <form action="${cp}/manage/warehouse/stocktake-delete-item" method="POST">
                                <input type="hidden" name="stocktakeId" value="${stocktake.stocktakeId}">
                                <input type="hidden" name="itemId" value="${item.stocktakeItemId}">
                                <button type="submit" class="button button-outline" style="color:red; border:none; padding:4px;" onclick="return confirm('Xóa dòng này?');">Xóa</button>
                            </form>
                        </td>
                    </c:if>
                    <c:if test="${stocktake.status != 'DRAFT'}">
                        <td style="padding: 10px; text-align:right;"><b>${item.actualQuantity}</b></td>
                        <td style="padding: 10px; text-align:right; font-weight:bold; color: ${item.differenceQuantity < 0 ? 'red' : (item.differenceQuantity > 0 ? 'green' : 'black')}">
                            ${item.differenceQuantity > 0 ? '+' : ''}${item.differenceQuantity}
                        </td>
                    </c:if>
                </tr>
            </c:forEach>
        </tbody>
    </table>

    <c:if test="${stocktake.status == 'DRAFT'}">
        <div style="margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee;">
            <h4>Thêm sản phẩm khác</h4>
            <form action="${cp}/manage/warehouse/stocktake-add-item" method="POST" style="display:flex; gap:10px; margin-top: 10px;">
                <input type="hidden" name="stocktakeId" value="${stocktake.stocktakeId}">
                <select name="variantId" class="input-field" required style="width: 40%;">
                    <option value="">-- Chọn sản phẩm --</option>
                    <c:forEach items="${variants}" var="v">
                        <option value="${v.variantId}">${v.sku} - ${v.productName} (${v.variantName})</option>
                    </c:forEach>
                </select>
                <input type="number" name="actualQuantity" class="input-field" placeholder="Số lượng thực tế" required min="0" style="width: 20%;">
                <button type="submit" class="button button-gold">Thêm dòng</button>
            </form>
        </div>
    </c:if>
</div>

<style>
.alert { padding:12px 16px; border-radius:6px; margin-bottom:16px; }
.alert-success { background:#d4edda; color:#155724; border:1px solid #c3e6cb; }
.alert-error   { background:#f8d7da; color:#721c24; border:1px solid #f5c6cb; }
.status-badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }
.status-badge.draft { background:#e8f4fd; color:#2980b9; }
.status-badge.warning { background:#fef3c7; color:#b45309; }
.status-badge.success { background:#d1fae5; color:#065f46; }
.status-badge.neutral { background:#f3f4f6; color:#4b5563; }
</style>
