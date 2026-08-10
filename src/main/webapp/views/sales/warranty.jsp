<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Bảo hành – WatchStore</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f6fa; color: #333; }
        .container { max-width: 1200px; margin: auto; padding: 28px 24px; }
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
        .page-header h1 { font-size: 26px; font-weight: 700; }
        .page-header p { color: #777; font-size: 14px; margin-top: 4px; }
        .btn {
            display: inline-flex; align-items: center; gap: 6px;
            padding: 10px 20px; border: none; border-radius: 8px;
            font-size: 14px; font-weight: 600; cursor: pointer; text-decoration: none;
        }
        .btn-primary { background: #2563eb; color: white; }
        .btn-primary:hover { background: #1d4ed8; }
        .btn-sm { padding: 5px 12px; font-size: 12px; border-radius: 5px; }
        .btn-outline { background: white; border: 1px solid #ddd; color: #333; }
        .btn-outline:hover { background: #f0f0f0; }

        /* Stats */
        .stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
        .stat-card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
        .stat-card .label { font-size: 13px; color: #888; margin-bottom: 8px; }
        .stat-card .value { font-size: 30px; font-weight: 700; color: #1e293b; }
        .stat-card.active .value { color: #16a34a; }
        .stat-card.expired .value { color: #dc2626; }
        .stat-card.pending .value { color: #d97706; }

        /* Filter */
        .filter-bar { background: white; padding: 16px 20px; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,.06); margin-bottom: 20px; display: flex; gap: 12px; flex-wrap: wrap; align-items: flex-end; }
        .filter-bar input, .filter-bar select { padding: 9px 12px; border: 1px solid #ddd; border-radius: 7px; font-size: 14px; outline: none; min-width: 180px; }
        .filter-bar input:focus, .filter-bar select:focus { border-color: #2563eb; }

        /* Table */
        .card { background: white; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,.06); overflow: hidden; }
        table { width: 100%; border-collapse: collapse; font-size: 14px; }
        thead tr { background: #f8fafc; }
        th { padding: 14px 16px; text-align: left; font-size: 12px; font-weight: 600; color: #888; text-transform: uppercase; letter-spacing: .5px; border-bottom: 1px solid #f0f0f0; }
        td { padding: 14px 16px; border-bottom: 1px solid #f9f9f9; }
        tr:last-child td { border-bottom: none; }
        tr:hover td { background: #fafafa; }
        .empty { text-align: center; padding: 40px; color: #aaa; }
        .empty-icon { font-size: 48px; margin-bottom: 12px; }

        /* Status badges */
        .badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-active   { background: #d1fae5; color: #065f46; }
        .badge-expired  { background: #fee2e2; color: #991b1b; }
        .badge-pending  { background: #fef3c7; color: #92400e; }
        .badge-repair   { background: #dbeafe; color: #1e40af; }
        .badge-cancelled{ background: #f1f5f9; color: #64748b; }

        /* Flash */
        .flash-success { background: #d1fae5; border: 1px solid #6ee7b7; color: #065f46; padding: 12px 18px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; display: flex; align-items: center; gap: 8px; }
        .flash-error   { background: #fee2e2; border: 1px solid #fca5a5; color: #991b1b; padding: 12px 18px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; }

        /* Update status form */
        .status-form { display: flex; gap: 6px; align-items: center; }
        .status-form select { padding: 5px 8px; border: 1px solid #ddd; border-radius: 5px; font-size: 12px; }
        .status-form button { padding: 5px 12px; background: #2563eb; color: white; border: none; border-radius: 5px; font-size: 12px; cursor: pointer; }

        @media (max-width: 768px) {
            .stats-row { grid-template-columns: 1fr 1fr; }
        }
    </style>
</head>
<body>
<div class="container">

    <div class="page-header">
        <div>
            <h1>🛡 Quản lý Bảo hành</h1>
            <p>Theo dõi phiếu bảo hành sản phẩm đồng hồ.</p>
        </div>
        <a href="${pageContext.request.contextPath}/manage/sales/warranty-add" class="btn btn-primary">
            ➕ Thêm phiếu bảo hành
        </a>
    </div>

    <%-- Flash message --%>
    <c:if test="${not empty sessionScope.flash}">
        <div class="flash-success">✅ ${sessionScope.flash}</div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <%-- Thống kê nhanh --%>
    <div class="stats-row">
        <div class="stat-card">
            <div class="label">Tổng phiếu</div>
            <div class="value">${warranties.size()}</div>
        </div>
        <div class="stat-card active">
            <div class="label">🟢 Đang bảo hành</div>
            <div class="value">
                <c:set var="activeCount" value="0"/>
                <c:forEach var="w" items="${warranties}">
                    <c:if test="${w.status == 'ACTIVE'}"><c:set var="activeCount" value="${activeCount + 1}"/></c:if>
                </c:forEach>
                ${activeCount}
            </div>
        </div>
        <div class="stat-card expired">
            <div class="label">🔴 Hết hạn</div>
            <div class="value">
                <c:set var="expiredCount" value="0"/>
                <c:forEach var="w" items="${warranties}">
                    <c:if test="${w.status == 'EXPIRED'}"><c:set var="expiredCount" value="${expiredCount + 1}"/></c:if>
                </c:forEach>
                ${expiredCount}
            </div>
        </div>
        <div class="stat-card pending">
            <div class="label">🔧 Đang sửa chữa</div>
            <div class="value">
                <c:set var="repairCount" value="0"/>
                <c:forEach var="w" items="${warranties}">
                    <c:if test="${w.status == 'REPAIR'}"><c:set var="repairCount" value="${repairCount + 1}"/></c:if>
                </c:forEach>
                ${repairCount}
            </div>
        </div>
    </div>

    <%-- Filter --%>
    <form method="get" action="${pageContext.request.contextPath}/manage/sales/warranty">
        <div class="filter-bar">
            <input type="text" name="keyword" value="${param.keyword}" placeholder="🔍 Tìm mã đơn, tên sản phẩm, serial..."/>
            <select name="status">
                <option value="">-- Tất cả trạng thái --</option>
                <option value="ACTIVE"     ${param.status == 'ACTIVE'     ? 'selected' : ''}>Đang bảo hành</option>
                <option value="EXPIRED"    ${param.status == 'EXPIRED'    ? 'selected' : ''}>Hết hạn</option>
                <option value="REPAIR"     ${param.status == 'REPAIR'     ? 'selected' : ''}>Đang sửa chữa</option>
                <option value="COMPLETED"  ${param.status == 'COMPLETED'  ? 'selected' : ''}>Đã hoàn trả</option>
                <option value="CANCELLED"  ${param.status == 'CANCELLED'  ? 'selected' : ''}>Đã hủy</option>
            </select>
            <button type="submit" class="btn btn-primary">Lọc</button>
            <a href="${pageContext.request.contextPath}/manage/sales/warranty" class="btn btn-outline">Đặt lại</a>
        </div>
    </form>

    <%-- Table --%>
    <div class="card">
        <table>
            <thead>
                <tr>
                    <th>#</th>
                    <th>Mã đơn hàng</th>
                    <th>Sản phẩm</th>
                    <th>Số Serial</th>
                    <th>Thời hạn BH</th>
                    <th>Ngày bắt đầu</th>
                    <th>Ngày hết hạn</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty warranties}">
                        <c:forEach var="w" items="${warranties}" varStatus="loop">
                            <tr>
                                <td style="color:#aaa;">${loop.count}</td>
                                <td>
                                    <span style="font-weight:600;color:#2563eb;">${w.orderCode != null ? w.orderCode : 'N/A'}</span>
                                </td>
                                <td style="font-weight:500;">${w.productName}</td>
                                <td style="font-family:monospace;color:#555;">${w.serial != null ? w.serial : '—'}</td>
                                <td style="text-align:center;font-weight:600;">${w.months} tháng</td>
                                <td>
                                    <fmt:formatDate value="${w.startDate}" pattern="dd/MM/yyyy"/>
                                </td>
                                <td>
                                    <fmt:formatDate value="${w.endDate}" pattern="dd/MM/yyyy"/>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${w.status == 'ACTIVE'}"><span class="badge badge-active">Đang BH</span></c:when>
                                        <c:when test="${w.status == 'EXPIRED'}"><span class="badge badge-expired">Hết hạn</span></c:when>
                                        <c:when test="${w.status == 'REPAIR'}"><span class="badge badge-repair">Đang sửa</span></c:when>
                                        <c:when test="${w.status == 'COMPLETED'}"><span class="badge badge-active">Hoàn trả</span></c:when>
                                        <c:otherwise><span class="badge badge-cancelled">${w.status}</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <form method="post" action="${pageContext.request.contextPath}/manage/sales/warranty" class="status-form">
                                        <input type="hidden" name="id" value="${w.id}"/>
                                        <select name="status">
                                            <option value="ACTIVE"    ${w.status == 'ACTIVE'    ? 'selected' : ''}>Đang BH</option>
                                            <option value="REPAIR"    ${w.status == 'REPAIR'    ? 'selected' : ''}>Sửa chữa</option>
                                            <option value="COMPLETED" ${w.status == 'COMPLETED' ? 'selected' : ''}>Hoàn trả</option>
                                            <option value="EXPIRED"   ${w.status == 'EXPIRED'   ? 'selected' : ''}>Hết hạn</option>
                                            <option value="CANCELLED" ${w.status == 'CANCELLED' ? 'selected' : ''}>Hủy</option>
                                        </select>
                                        <button type="submit">Lưu</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="9" class="empty">
                                <div class="empty-icon">🛡</div>
                                <p>Chưa có phiếu bảo hành nào.</p>
                                <br/>
                                <a href="${pageContext.request.contextPath}/manage/sales/warranty-add" class="btn btn-primary btn-sm">Thêm phiếu đầu tiên</a>
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

</div>
</body>
</html>
