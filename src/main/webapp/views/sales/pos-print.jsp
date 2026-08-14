<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Hóa đơn #ORD-${order.id}</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            font-size: 13px;
            line-height: 1.4;
            color: #000;
            margin: 0;
            padding: 20px;
            background: #fff;
        }
        .receipt-container {
            max-width: 400px;
            margin: auto;
            border: 1px dashed #ccc;
            padding: 15px;
        }
        .store-header {
            text-align: center;
            margin-bottom: 15px;
        }
        .store-header h2 {
            margin: 0 0 5px 0;
            font-size: 20px;
            font-weight: bold;
            letter-spacing: 1px;
        }
        .store-header p {
            margin: 0 0 3px 0;
            color: #444;
            font-size: 12px;
        }
        .divider {
            border-top: 1px dashed #000;
            margin: 10px 0;
        }
        .title {
            text-align: center;
            font-weight: bold;
            font-size: 15px;
            margin: 10px 0;
            text-transform: uppercase;
        }
        .info-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 4px;
        }
        .info-row span:first-child {
            color: #555;
        }
        .info-row span:last-child {
            font-weight: 500;
            text-align: right;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 15px 0;
        }
        table th {
            text-align: left;
            border-bottom: 1px solid #000;
            padding: 5px 0;
            font-size: 12px;
        }
        table td {
            padding: 6px 0;
            border-bottom: 1px dashed #eee;
            vertical-align: top;
        }
        .summary-box {
            margin-top: 10px;
        }
        .summary-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 4px;
            font-size: 13px;
        }
        .summary-row.total {
            font-size: 16px;
            font-weight: bold;
            border-top: 1px solid #000;
            padding-top: 6px;
            margin-top: 6px;
        }
        .footer-note {
            text-align: center;
            margin-top: 25px;
            font-size: 11px;
            color: #555;
        }
        @media print {
            body {
                padding: 0;
            }
            .receipt-container {
                border: none;
                padding: 0;
                max-width: 100%;
            }
        }
    </style>
</head>
<body>

<div class="receipt-container">
    <div class="store-header">
        <h2>WATCHSTORE</h2>
        <p>Đồng hồ nam chính hãng cao cấp</p>
        <p>Showroom: Mỹ Đình, Nam Từ Liêm, Hà Nội</p>
        <p>Hotline: 1900 6868</p>
    </div>

    <div class="divider"></div>

    <div class="title">Hóa đơn bán lẻ</div>

    <div class="info-row">
        <span>Số hóa đơn:</span>
        <span>${order.code}</span>
    </div>
    <div class="info-row">
        <span>Ngày bán:</span>
        <span><fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/></span>
    </div>
    <div class="info-row">
        <span>Nhân viên:</span>
        <span>${empty sessionScope.user ? 'Nhân viên bán hàng' : sessionScope.user.fullName}</span>
    </div>

    <div class="divider"></div>

    <div style="font-weight:bold; margin-bottom: 5px;">Khách hàng:</div>
    <div class="info-row">
        <span>Họ và tên:</span>
        <span>${order.customerName}</span>
    </div>
    <c:if test="${not empty order.phone}">
        <div class="info-row">
            <span>Số điện thoại:</span>
            <span>${order.phone}</span>
        </div>
    </c:if>
    <c:if test="${not empty order.shippingAddress}">
        <div class="info-row">
            <span>Địa chỉ:</span>
            <span>${order.shippingAddress}</span>
        </div>
    </c:if>

    <table>
        <thead>
            <tr>
                <th style="width: 50%;">Sản phẩm</th>
                <th style="text-align: center; width: 15%;">SL</th>
                <th style="text-align: right; width: 35%;">Thành tiền</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="item" items="${orderItems}">
                <tr>
                    <td>
                        <div>${item.productName}</div>
                        <div style="font-size: 11px; color:#555;">${item.variantName}</div>
                    </td>
                    <td style="text-align: center;">${item.quantity}</td>
                    <td style="text-align: right;">
                        <fmt:formatNumber value="${item.lineTotal}" pattern="#,##0"/> ₫
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>

    <div class="divider"></div>

    <div class="summary-box">
        <div class="summary-row">
            <span>Tổng tiền hàng:</span>
            <span>
                <fmt:formatNumber value="${order.totalPrice + order.discountAmount}" pattern="#,##0"/> ₫
            </span>
        </div>
        <c:if test="${order.discountAmount > 0}">
            <div class="summary-row" style="color: #c2410c;">
                <span>Giảm giá Voucher:</span>
                <span>
                    -<fmt:formatNumber value="${order.discountAmount}" pattern="#,##0"/> ₫
                </span>
            </div>
        </c:if>
        <div class="summary-row total">
            <span>Thành tiền:</span>
            <span>
                <fmt:formatNumber value="${order.totalPrice}" pattern="#,##0"/> ₫
            </span>
        </div>
    </div>

    <div class="footer-note">
        <p>Cảm ơn quý khách đã mua sắm tại WatchStore!</p>
        <p>Hóa đơn có giá trị bảo hành chính hãng trong thời gian bảo hành quy định.</p>
        <p>Vui lòng giữ lại hóa đơn này.</p>
    </div>
</div>

<script>
    window.addEventListener("DOMContentLoaded", function() {
        setTimeout(function() {
            window.print();
        }, 300);
    });
</script>

</body>
</html>
