<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>



    <!-- Load Chart.js from CDN -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: 'Segoe UI', Arial, sans-serif;
            background: #f5f6fa;
            color: #333;
        }

        .container {
            padding: 30px;
        }

        .header {
            margin-bottom: 25px;
        }

        .header h1 {
            margin: 0;
            font-size: 28px;
            font-weight: 700;
        }

        .header p {
            margin-top: 8px;
            color: #777;
        }

        /* ================= FILTER ================= */
        .filter-box {
            background: white;
            padding: 20px;
            border-radius: 12px;
            margin-bottom: 20px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, .08);
        }

        .filter-form {
            display: flex;
            gap: 15px;
            flex-wrap: wrap;
            align-items: end;
        }

        .form-group {
            display: flex;
            flex-direction: column;
            gap: 7px;
        }

        .form-group label {
            font-size: 13px;
            font-weight: 600;
            color: #666;
        }

        .filter-form input,
        .filter-form select {
            height: 42px;
            padding: 0 12px;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 14px;
            outline: none;
            min-width: 160px;
        }

        .btn {
            height: 42px;
            padding: 0 20px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
            font-weight: 600;
        }

        .btn-search {
            background: #2563eb;
            color: white;
        }
        
        .btn-search:hover {
            background: #1d4ed8;
        }

        .btn-reset {
            background: #e5e7eb;
            color: #333;
        }
        
        .btn-reset:hover {
            background: #d1d5db;
        }

        /* ================= CARDS ================= */
        .cards {
            display: grid;
            grid-template-columns: repeat(5, 1fr);
            gap: 15px;
            margin-bottom: 20px;
        }

        .card {
            background: white;
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, .05);
        }

        .card-title {
            color: #777;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 8px;
        }

        .card-value {
            font-size: 24px;
            font-weight: bold;
        }

        /* ================= REVENUE HIGHLIGHT CARD ================= */
        .revenue-card {
            background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
            color: white;
            padding: 22px 30px;
            border-radius: 12px;
            box-shadow: 0 4px 15px rgba(37, 99, 235, 0.2);
            margin-bottom: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .revenue-card-left .title {
            font-size: 13px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            opacity: 0.9;
        }

        .revenue-card-left .value {
            font-size: 32px;
            font-weight: 800;
            margin-top: 5px;
        }

        .revenue-card-right {
            font-size: 36px;
            opacity: 0.85;
        }

        /* ================= CHARTS ================= */
        .chart-box {
            background: white;
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, .08);
            margin-bottom: 20px;
        }

        .chart-box h2 {
            margin-top: 0;
            font-size: 18px;
            margin-bottom: 15px;
            color: #1e293b;
        }

        /* ================= REPORT ================= */
        .report-box {
            background: white;
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, .08);
            margin-bottom: 20px;
            overflow-x: auto;
        }

        .report-box-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }

        .report-box h2 {
            margin: 0;
            font-size: 20px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            min-width: 700px;
        }

        th,
        td {
            padding: 14px 12px;
            border-bottom: 1px solid #eee;
            text-align: left;
        }

        th {
            background: #f8fafc;
            color: #475569;
            font-size: 13px;
            font-weight: 600;
            text-transform: uppercase;
        }

        td {
            font-size: 14px;
        }

        .money {
            font-weight: bold;
        }

        .empty {
            text-align: center;
            padding: 40px;
            color: #777;
        }

        /* ================= RESPONSIVE & PRINT ================= */
        @media (max-width: 1000px) {
            .cards {
                grid-template-columns: repeat(3, 1fr);
            }
        }

        @media (max-width: 700px) {
            .container {
                padding: 15px;
            }

            .cards {
                grid-template-columns: repeat(2, 1fr);
            }

            .filter-form {
                display: block;
            }

            .form-group {
                margin-bottom: 10px;
            }

            .filter-form input,
            .filter-form select,
            .btn {
                width: 100%;
            }
            
            .report-box-header {
                flex-direction: column;
                align-items: flex-start;
                gap: 10px;
            }
        }

        @media print {
            body {
                background: white;
            }
            .filter-box, 
            .report-box-header .export-buttons,
            .btn,
            a.btn-reset {
                display: none !important;
            }
            .container {
                padding: 0;
            }
            .card, .report-box, .chart-box {
                box-shadow: none;
                border: 1px solid #ddd;
            }
        }
    </style>

<div class="container">

    <div class="header">
        <h1>📊 Báo cáo bán hàng</h1>
        <p>Thống kê số lượng đơn hàng, doanh số và chi tiết doanh thu thực tế.</p>
    </div>

    <!-- ================= FILTER ================= -->
    <div class="filter-box">
        <form method="get" action="${pageContext.request.contextPath}/manage/sales/report" class="filter-form">
            
            <div class="form-group">
                <label>Từ ngày</label>
                <input type="date" name="fromDate" value="${fromDate}">
            </div>

            <div class="form-group">
                <label>Đến ngày</label>
                <input type="date" name="toDate" value="${toDate}">
            </div>

            <div class="form-group">
                <label>Trạng thái đơn</label>
                <select name="status">
                    <option value="">Tất cả</option>
                    <option value="PENDING"   ${status == 'PENDING'   ? 'selected' : ''}>Chờ xử lý</option>
                    <option value="SHIPPING"  ${status == 'SHIPPING'  ? 'selected' : ''}>Đang giao</option>
                    <option value="COMPLETED" ${status == 'COMPLETED' ? 'selected' : ''}>Hoàn thành</option>
                    <option value="CANCELLED" ${status == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                </select>
            </div>

            <button type="submit" class="btn btn-search">Xem báo cáo</button>
            <a href="${pageContext.request.contextPath}/manage/sales/report" class="btn btn-reset">Đặt lại</a>
        </form>
    </div>

    <!-- ================= SUMMARY CARDS ================= -->
    <div class="cards">
        <div class="card">
            <div class="card-title">Tổng đơn hàng</div>
            <div class="card-value">${totalOrders}</div>
        </div>

        <div class="card">
            <div class="card-title" style="color:#d97706;">⌛ Chờ xử lý</div>
            <div class="card-value">${pendingOrders}</div>
        </div>

        <div class="card">
            <div class="card-title" style="color:#2563eb;">🚚 Đang giao</div>
            <div class="card-value">${shippingOrders}</div>
        </div>

        <div class="card">
            <div class="card-title" style="color:#16a34a;">🟢 Hoàn thành</div>
            <div class="card-value">${completedOrders}</div>
        </div>

        <div class="card">
            <div class="card-title" style="color:#dc2626;">🔴 Đã hủy</div>
            <div class="card-value">${cancelledOrders}</div>
        </div>
    </div>

    <!-- ================= REVENUE HIGHLIGHT CARD ================= -->
    <div class="revenue-card">
        <div class="revenue-card-left">
            <div class="title">Doanh thu thực tế (Đơn Hoàn thành)</div>
            <div class="value">${totalRevenue} ₫</div>
        </div>
        <div class="revenue-card-right">
            💰
        </div>
    </div>

    <!-- ================= REVENUE TREND CHART ================= -->
    <div class="chart-box">
        <h2>📈 Biểu đồ xu hướng doanh thu theo ngày</h2>
        <div style="height: 320px; width: 100%; position: relative;">
            <canvas id="revenueChart"></canvas>
        </div>
    </div>

    <!-- ================= REPORT TABLE ================= -->
    <div class="report-box">
        <div class="report-box-header">
            <h2>Chi tiết báo cáo</h2>
            <c:if test="${sessionScope.user.role == 'ADMIN' || (not empty sessionScope.userPermissions && (sessionScope.userPermissions.contains('REPORT_EXPORT') || sessionScope.userPermissions.contains('SALES_REPORT')))}">
                <div class="export-buttons" style="display:flex; gap:10px;">
                    <button type="button" onclick="exportToExcel()" class="btn" style="background:#16a34a; color:white; font-size:13px; height:34px; padding:0 14px;">
                        📥 Xuất Excel
                    </button>
                    <button type="button" onclick="exportToPDF()" class="btn" style="background:#dc2626; color:white; font-size:13px; height:34px; padding:0 14px;">
                        📄 Xuất PDF
                    </button>
                </div>
            </c:if>
        </div>

        <table>
            <thead>
                <tr>
                    <th>Mã đơn</th>
                    <th>Khách hàng</th>
                    <th>Ngày đặt</th>
                    <th>Trạng thái</th>
                    <th>Tổng tiền</th>
                </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${not empty reportOrders}">
                    <c:forEach var="order" items="${reportOrders}">
                        <tr>
                            <td>#${order.id}</td>
                            <td style="font-weight:500;">${order.customerName}</td>
                            <td>
                                <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${order.statusCode == 'COMPLETED' or order.statusCode == 'Hoàn thành'}">
                                        <span style="color:#16a34a;font-weight:600;background:#d1fae5;padding:4px 10px;border-radius:12px;font-size:12px;">Hoàn thành</span>
                                    </c:when>
                                    <c:when test="${order.statusCode == 'SHIPPING' or order.statusCode == 'DELIVERED' or order.statusCode == 'Đang giao'}">
                                        <span style="color:#2563eb;font-weight:600;background:#dbeafe;padding:4px 10px;border-radius:12px;font-size:12px;">Đang giao</span>
                                    </c:when>
                                    <c:when test="${order.statusCode == 'PENDING' or order.statusCode == 'CONFIRMED' or order.statusCode == 'PACKING' or order.statusCode == 'Đang xử lý'}">
                                        <span style="color:#d97706;font-weight:600;background:#fef3c7;padding:4px 10px;border-radius:12px;font-size:12px;">Đang xử lý</span>
                                    </c:when>
                                    <c:when test="${order.statusCode == 'CANCELLED' or order.statusCode == 'Đã hủy'}">
                                        <span style="color:#dc2626;font-weight:600;background:#fee2e2;padding:4px 10px;border-radius:12px;font-size:12px;">Đã hủy</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="background:#f1f5f9;padding:4px 10px;border-radius:12px;font-size:12px;">${order.statusCode}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="money" style="color:#0f172a;">
                                <fmt:formatNumber value="${order.total}" pattern="#,###"/> ₫
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <tr>
                        <td colspan="5" class="empty">Không có dữ liệu báo cáo.</td>
                    </tr>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>

</div>

<!-- Render Javascript Report Array -->
<script>
    const reportData = [
        <c:forEach var="order" items="${reportOrders}" varStatus="status">
            {
                id: "${order.id}",
                customer: "${order.customerName}",
                date: "<fmt:formatDate value="${order.createdAt}" pattern="yyyy-MM-dd"/>",
                status: "${order.statusCode}",
                total: ${order.total != null ? order.total.doubleValue() : 0.0}
            }${not status.last ? ',' : ''}
        </c:forEach>
    ];

    document.addEventListener("DOMContentLoaded", function() {
        // Group completed orders by date
        const revenueByDate = {};
        reportData.forEach(item => {
            if (item.status === 'COMPLETED' || item.status === 'Hoàn thành') {
                const dateStr = item.date;
                if (dateStr && dateStr.trim() !== '') {
                    revenueByDate[dateStr] = (revenueByDate[dateStr] || 0) + item.total;
                }
            }
        });

        // Sort dates
        const sortedDates = Object.keys(revenueByDate).sort();
        const chartLabels = sortedDates.map(d => {
            const parts = d.split('-');
            return parts[2] + '/' + parts[1]; // dd/MM
        });
        const chartData = sortedDates.map(d => revenueByDate[d]);

        // Render Chart.js
        const ctx = document.getElementById('revenueChart').getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: chartLabels.length > 0 ? chartLabels : ['Chưa có dữ liệu'],
                datasets: [{
                    label: 'Doanh thu thực tế (đ)',
                    data: chartData.length > 0 ? chartData : [0],
                    borderColor: '#2563eb',
                    backgroundColor: 'rgba(37, 99, 235, 0.1)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.3,
                    pointBackgroundColor: '#1d4ed8',
                    pointRadius: 5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return value.toLocaleString('vi-VN') + ' ₫';
                            }
                        }
                    }
                }
            }
        });
    });

    function exportToExcel() {
        if (reportData.length === 0) {
            alert("Không có dữ liệu để xuất!");
            return;
        }
        // Generate UTF-8 CSV with BOM for Vietnamese characters
        let csvContent = "\uFEFF";
        csvContent += "Mã đơn,Khách hàng,Ngày đặt,Trạng thái,Tổng tiền (đ)\n";
        
        reportData.forEach(function(item) {
            let statusText = item.status;
            if (item.status === 'COMPLETED') statusText = 'Hoàn thành';
            else if (item.status === 'SHIPPING') statusText = 'Đang giao';
            else if (item.status === 'PENDING') statusText = 'Đang xử lý';
            else if (item.status === 'CANCELLED') statusText = 'Đã hủy';
            
            let row = '#' + item.id + ',"' + item.customer + '",' + item.date + ',"' + statusText + '",' + item.total;
            csvContent += row + "\n";
        });
        
        let blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        let url = URL.createObjectURL(blob);
        let link = document.createElement("a");
        link.setAttribute("href", url);
        link.setAttribute("download", "Bao_Cao_Ban_Hang_" + new Date().toISOString().split('T')[0] + ".csv");
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

    function exportToPDF() {
        window.print();
    }
</script>
