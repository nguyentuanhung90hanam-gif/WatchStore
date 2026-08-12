<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thêm phiếu bảo hành – WatchStore</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f6fa; color: #333; }
        .container { max-width: 750px; margin: auto; padding: 30px 24px; }
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
        .page-header h1 { font-size: 26px; font-weight: 700; }
        .back-link { color: #2563eb; text-decoration: none; font-size: 14px; font-weight: 500; }
        .card { background: white; border-radius: 14px; padding: 32px; box-shadow: 0 2px 12px rgba(0,0,0,.07); }
        .card h2 { font-size: 18px; margin-bottom: 24px; color: #1e293b; border-bottom: 2px solid #f0f4ff; padding-bottom: 12px; }
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .form-group { margin-bottom: 4px; }
        .form-group.full { grid-column: 1 / -1; }
        label { display: block; margin-bottom: 7px; font-weight: 600; font-size: 13px; color: #555; }
        .req { color: #e53e3e; }
        input, select, textarea {
            width: 100%; padding: 10px 13px; border: 1.5px solid #e2e8f0;
            border-radius: 8px; font-size: 14px; outline: none; transition: border-color .2s;
            font-family: inherit;
        }
        input:focus, select:focus, textarea:focus { border-color: #2563eb; box-shadow: 0 0 0 3px rgba(37,99,235,.08); }
        textarea { height: 90px; resize: vertical; }
        .hint { font-size: 12px; color: #94a3b8; margin-top: 5px; }
        .actions { display: flex; gap: 12px; margin-top: 28px; }
        .btn { display: inline-flex; align-items: center; gap: 6px; padding: 11px 26px; border: none; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; text-decoration: none; }
        .btn-save   { background: #2563eb; color: white; }
        .btn-save:hover { background: #1d4ed8; }
        .btn-cancel { background: #f1f5f9; color: #475569; }
        .btn-cancel:hover { background: #e2e8f0; }
        .flash-error { background: #fef2f2; border: 1px solid #fca5a5; color: #b91c1c; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; }
        @media (max-width: 600px) { .form-grid { grid-template-columns: 1fr; } .actions { flex-direction: column; } }
    </style>
</head>
<body>
<div class="container">

    <div class="page-header">
        <div>
            <h1>🛡 Thêm phiếu bảo hành</h1>
        </div>
        <a href="${pageContext.request.contextPath}/manage/sales/warranty" class="back-link">← Quay lại danh sách</a>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div class="flash-error">⚠ ${sessionScope.flash}</div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <div class="card">
        <h2>📋 Thông tin phiếu bảo hành</h2>

        <form method="post" action="${pageContext.request.contextPath}/manage/sales/warranty-add">
            <div class="form-grid">

                <div class="form-group">
                    <label for="orderId">Đơn hàng liên kết <span class="req">*</span></label>
                    <select id="orderId" name="orderId" required>
                        <option value="">-- Chọn đơn hàng --</option>
                        <c:forEach var="order" items="${orders}">
                            <option value="${order.id}">${order.code} – ${order.customerName}</option>
                        </c:forEach>
                    </select>
                    <div class="hint">Chọn đơn hàng mà phiếu bảo hành này thuộc về.</div>
                </div>

                <div class="form-group">
                    <label for="productName">Tên sản phẩm <span class="req">*</span></label>
                    <input type="text" id="productName" name="productName" placeholder="Ví dụ: Rolex Datejust 41" required/>
                </div>

                <div class="form-group">
                    <label for="serial">Số Serial</label>
                    <input type="text" id="serial" name="serial" placeholder="Ví dụ: RLX-2024-001234"/>
                    <div class="hint">Số serial trên vỏ/đáy đồng hồ.</div>
                </div>

                <div class="form-group">
                    <label for="months">Thời hạn bảo hành (tháng) <span class="req">*</span></label>
                    <select id="months" name="months" required>
                        <option value="6">6 tháng</option>
                        <option value="12" selected>12 tháng</option>
                        <option value="18">18 tháng</option>
                        <option value="24">24 tháng</option>
                        <option value="36">36 tháng (3 năm)</option>
                        <option value="60">60 tháng (5 năm)</option>
                    </select>
                </div>

                <div class="form-group full">
                    <label for="note">Ghi chú</label>
                    <textarea id="note" name="note" placeholder="Ghi chú thêm về tình trạng sản phẩm khi bắt đầu bảo hành..."></textarea>
                </div>

            </div>

            <div class="actions">
                <button type="submit" class="btn btn-save">💾 Tạo phiếu bảo hành</button>
                <a href="${pageContext.request.contextPath}/manage/sales/warranty" class="btn btn-cancel">Hủy</a>
            </div>
        </form>
    </div>

</div>
</body>
</html>
