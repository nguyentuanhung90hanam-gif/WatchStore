<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>In phiếu nhập — ${receipt.receiptCode}</title>
    <style>
        /* CSS đơn giản chỉ dùng khi in, không cần framework */
        body        { font-family: Arial, sans-serif; color: #000; padding: 30px; }
        h1          { font-size: 22px; margin-bottom: 4px; }
        .subtitle   { color: #666; font-size: 14px; margin-bottom: 24px; }
        table       { width: 100%; border-collapse: collapse; margin-top: 16px; }
        th, td      { border: 1px solid #ccc; padding: 8px 12px; text-align: left; font-size: 14px; }
        th          { background: #f5f5f5; }
        .row        { display: flex; gap: 40px; margin-bottom: 12px; }
        .row label  { font-weight: bold; width: 150px; display: inline-block; }
        .signature  { margin-top: 60px; display: flex; justify-content: space-around; }
        .sign-box   { text-align: center; }
        .sign-name  { font-weight: bold; margin-bottom: 50px; }
        @media print {
            .no-print { display: none; }
        }
    </style>
</head>
<body>

    <%-- Nút in — khi in thật sẽ ẩn đi --%>
    <div class="no-print" style="margin-bottom:20px;">
        <button onclick="window.print()" style="padding:8px 20px;font-size:14px;cursor:pointer;">🖨️ In phiếu</button>
        <button onclick="window.close()" style="padding:8px 20px;font-size:14px;margin-left:10px;cursor:pointer;">✖ Đóng</button>
    </div>

    <h1>PHIẾU NHẬP KHO</h1>
    <p class="subtitle">WatchStore — Quản lý kho hàng</p>

    <%-- Thông tin phiếu --%>
    <div class="row">
        <span><label>Mã phiếu:</label> ${receipt.receiptCode}</span>
        <span><label>Ngày nhập:</label> ${receipt.receiptDate}</span>
    </div>
    <div class="row">
        <span><label>Nhà cung cấp:</label> ${receipt.supplierName}</span>
        <span><label>Số điện thoại:</label> ${receipt.supplierPhone}</span>
    </div>
    <div class="row">
        <span><label>Người tạo:</label> ${receipt.createdByName}</span>
        <span><label>Trạng thái:</label> ${receipt.statusLabel}</span>
    </div>
    <div class="row">
        <span><label>Tổng tiền:</label>
            <fmt:formatNumber value="${receipt.totalCost}" type="number" maxFractionDigits="0"/> VNĐ
        </span>
    </div>
    <div class="row">
        <span><label>Ghi chú:</label> ${receipt.note}</span>
    </div>

    <%-- Bảng hàng hóa — placeholder vì chưa có chi tiết phiếu --%>
    <table>
        <thead>
            <tr>
                <th>STT</th>
                <th>SKU</th>
                <th>Tên sản phẩm</th>
                <th>Số lượng</th>
                <th>Đơn giá</th>
                <th>Thành tiền</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td colspan="6" style="text-align:center;color:#999;">
                    (Chi tiết từng mặt hàng sẽ hiển thị khi tích hợp bảng StockReceiptItems)
                </td>
            </tr>
        </tbody>
        <tfoot>
            <tr>
                <td colspan="5" style="text-align:right;font-weight:bold;">TỔNG CỘNG:</td>
                <td><b><fmt:formatNumber value="${receipt.totalCost}" type="number" maxFractionDigits="0"/> đ</b></td>
            </tr>
        </tfoot>
    </table>

    <%-- Chữ ký --%>
    <div class="signature">
        <div class="sign-box">
            <p class="sign-name">Người giao hàng</p>
            <p>(Ký, ghi rõ họ tên)</p>
        </div>
        <div class="sign-box">
            <p class="sign-name">Nhân viên kho</p>
            <p>(Ký, ghi rõ họ tên)</p>
        </div>
        <div class="sign-box">
            <p class="sign-name">Quản lý kho</p>
            <p>(Ký, ghi rõ họ tên)</p>
        </div>
    </div>

</body>
</html>
