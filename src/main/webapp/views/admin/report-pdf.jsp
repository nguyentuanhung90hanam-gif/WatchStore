<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Báo cáo quản trị - WatchStore</title>
    <style>
        @page {
            size: A4 portrait;
            margin: 15mm;
        }
        body {
            font-family: "Segoe UI", Arial, sans-serif;
            color: #222;
            background: #fff;
            margin: 0;
            padding: 20px;
            font-size: 13px;
            line-height: 1.4;
        }
        .header-section {
            border-bottom: 2px solid #b8860b;
            padding-bottom: 15px;
            margin-bottom: 20px;
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
        }
        .company-brand {
            font-size: 20px;
            font-weight: 800;
            letter-spacing: 1px;
            color: #111;
        }
        .company-sub {
            font-size: 11px;
            color: #666;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .report-title {
            text-align: right;
        }
        .report-title h1 {
            margin: 0;
            font-size: 18px;
            color: #b8860b;
            text-transform: uppercase;
        }
        .report-meta {
            font-size: 11px;
            color: #555;
            margin-top: 4px;
        }
        .metric-summary {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 12px;
            margin-bottom: 25px;
        }
        .metric-box {
            border: 1px solid #ddd;
            border-radius: 6px;
            padding: 10px 12px;
            background: #fafafa;
        }
        .metric-box span {
            font-size: 11px;
            color: #666;
            display: block;
            margin-bottom: 4px;
        }
        .metric-box b {
            font-size: 15px;
            color: #111;
        }
        .section-title {
            font-size: 14px;
            font-weight: 700;
            color: #111;
            margin-top: 20px;
            margin-bottom: 10px;
            padding-left: 8px;
            border-left: 4px solid #b8860b;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
            font-size: 12px;
        }
        th, td {
            border: 1px solid #e0e0e0;
            padding: 8px 10px;
            text-align: left;
        }
        th {
            background-color: #f4f4f4;
            color: #333;
            font-weight: 700;
            text-transform: uppercase;
            font-size: 10.5px;
        }
        tr:nth-child(even) {
            background-color: #fcfcfc;
        }
        .text-right { text-align: right; }
        .text-center { text-align: center; }
        .footer-sign {
            margin-top: 40px;
            display: flex;
            justify-content: space-between;
            text-align: center;
        }
        .sign-box {
            width: 200px;
        }
        .sign-box p {
            margin: 4px 0;
        }
        @media print {
            .no-print { display: none !important; }
            body { padding: 0; }
        }
    </style>
</head>
<body>

    <div class="no-print" style="margin-bottom:20px;text-align:right;">
        <button onclick="window.print()" style="padding:10px 20px;background:#b8860b;color:#fff;border:none;border-radius:4px;font-weight:bold;cursor:pointer;">
            🖨 In / Lưu PDF
        </button>
        <button onclick="window.close()" style="padding:10px 16px;background:#666;color:#fff;border:none;border-radius:4px;cursor:pointer;margin-left:8px;">
            Đóng
        </button>
    </div>

    <div class="header-section">
        <div>
            <div class="company-brand">WATCHSTORE VIỆT NAM</div>
            <div class="company-sub">Hệ thống đồng hồ chính hãng cao cấp</div>
        </div>
        <div class="report-title">
            <h1>BÁO CÁO TỔNG HỢP ĐIỀU HÀNH</h1>
            <div class="report-meta">Ngày xuất báo cáo: <b>${exportDate}</b></div>
        </div>
    </div>

    <div class="metric-summary">
        <div class="metric-box">
            <span>Tổng doanh thu</span>
            <b><fmt:formatNumber value="${totalRevenue}" pattern="#,##0"/> ₫</b>
        </div>
        <div class="metric-box">
            <span>Tổng đơn hàng</span>
            <b>${totalOrdersCount} đơn</b>
        </div>
        <div class="metric-box">
            <span>Sản phẩm quản lý</span>
            <b>${totalProductsCount} mặt hàng</b>
        </div>
    </div>

    <%-- 1. Sản phẩm bán chạy --%>
    <div class="section-title">1. BÁO CÁO SẢN PHẨM BÁN CHẠY NHẤT</div>
    <table>
        <thead>
        <tr>
            <th style="width:40px;">STT</th>
            <th>Mã sản phẩm</th>
            <th>Tên sản phẩm</th>
            <th>SKU</th>
            <th class="text-right">Số lượng bán</th>
            <th class="text-right">Doanh thu phát sinh</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${topSellingProducts}" var="p" varStatus="st">
            <tr>
                <td class="text-center">${st.index + 1}</td>
                <td>${p.productCode}</td>
                <td><b>${p.productName}</b> <c:if test="${not empty p.variantName}">(${p.variantName})</c:if></td>
                <td>${p.sku}</td>
                <td class="text-right"><b>${p.quantitySold}</b></td>
                <td class="text-right"><b><fmt:formatNumber value="${p.revenue}" pattern="#,##0"/> ₫</b></td>
            </tr>
        </c:forEach>
        <c:if test="${empty topSellingProducts}">
            <tr>
                <td colspan="6" class="text-center" style="color:#888;">Chưa phát sinh doanh số bán hàng trong hệ thống.</td>
            </tr>
        </c:if>
        </tbody>
    </table>

    <%-- 2. Khách hàng tiêu biểu --%>
    <div class="section-title">2. BÁO CÁO KHÁCH HÀNG TIÊU BIỂU</div>
    <table>
        <thead>
        <tr>
            <th style="width:40px;">STT</th>
            <th>Họ và tên</th>
            <th>Email</th>
            <th>Số điện thoại</th>
            <th class="text-right">Số đơn đặt</th>
            <th class="text-right">Tổng chi tiêu</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${topCustomers}" var="c" varStatus="st">
            <tr>
                <td class="text-center">${st.index + 1}</td>
                <td><b>${c.fullName}</b></td>
                <td>${c.email}</td>
                <td>${not empty c.phone ? c.phone : '—'}</td>
                <td class="text-right">${c.ordersCount}</td>
                <td class="text-right"><b><fmt:formatNumber value="${c.spentAmount}" pattern="#,##0"/> ₫</b></td>
            </tr>
        </c:forEach>
        <c:if test="${empty topCustomers}">
            <tr>
                <td colspan="6" class="text-center" style="color:#888;">Chưa có dữ liệu chi tiêu khách hàng.</td>
            </tr>
        </c:if>
        </tbody>
    </table>

    <div class="footer-sign">
        <div class="sign-box">
            <p><b>Người lập báo cáo</b></p>
            <p style="font-size:11px;color:#777;">(Ký và ghi rõ họ tên)</p>
        </div>
        <div class="sign-box">
            <p><b>Phê duyệt Quản trị viên</b></p>
            <p style="font-size:11px;color:#777;">(Ký và đóng dấu)</p>
        </div>
    </div>

    <script>
        window.onload = function() {
            setTimeout(function() {
                window.print();
            }, 300);
        };
    </script>
</body>
</html>
