<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="portal-welcome">
    <div>
        <p class="eyebrow dark">${dashboardLabel}</p>
        <h2>
            Xin chào,
            ${empty sessionScope.user.fullName ? 'Quản trị viên' : sessionScope.user.fullName}
        </h2>
        <span>
            Hệ thống quản lý dữ liệu đồng hồ chính hãng WatchStore.
        </span>
    </div>

    <div class="date-chip">
        02 THÁNG 08, 2026
    </div>
</div>

<div class="metric-grid">

    <article>
        <span>${metric1Label}</span>
        <b>${metric1Value}</b>
        <em>+12%</em>
    </article>

    <article>
        <span>${metric2Label}</span>
        <b>${metric2Value}</b>
        <em>+5%</em>
    </article>

    <article>
        <span>${metric3Label}</span>
        <b>${metric3Value}</b>
        <em>+22%</em>
    </article>

    <article>
        <span>${metric4Label}</span>
        <b>${metric4Value}</b>
        <em class="danger-text">-3%</em>
    </article>

</div>

<div class="dashboard-grid">

    <article class="dashboard-card chart-card">

        <div class="card-title">
            <div>
                <b>${chartTitle}</b>
                <span>7 ngày gần nhất</span>
            </div>

            <button type="button">
                Tuần này
            </button>
        </div>

        <div class="portal-bars">
            <span style="height:35%"></span>
            <span style="height:52%"></span>
            <span class="gold" style="height:74%"></span>
            <span style="height:48%"></span>
            <span style="height:64%"></span>
            <span class="dark" style="height:86%"></span>
            <span style="height:60%"></span>
        </div>

        <div class="bar-labels">
            <span>T2</span>
            <span>T3</span>
            <span>T4</span>
            <span>T5</span>
            <span>T6</span>
            <span>T7</span>
            <span>CN</span>
        </div>

    </article>

    <article class="dashboard-card">

        <div class="card-title">

            <div>
                <b>Cần xử lý</b>
                <span>Ưu tiên trong hôm nay</span>
            </div>

            <a href="#">Xem tất cả</a>

        </div>

        <div class="task-list">

            <p>
                <i class="warning">!</i>

                <span>
                    <b>${task1}</b>
                    <small>12 phút trước</small>
                </span>

                <strong>12</strong>
            </p>

            <p>
                <i>→</i>

                <span>
                    <b>${task2}</b>
                    <small>45 phút trước</small>
                </span>

                <strong>08</strong>
            </p>

            <p>
                <i class="danger">!</i>

                <span>
                    <b>${task3}</b>
                    <small>2 giờ trước</small>
                </span>

                <strong>05</strong>
            </p>

        </div>

    </article>

</div>

<article class="dashboard-card latest-table">

    <div class="card-title">

        <div>
            <b>${tableTitle}</b>
            <span>Dữ liệu vận hành gần nhất</span>
        </div>

        <button type="button">
            Xuất báo cáo
        </button>

    </div>

    <div class="table-wrap">

        <table>

            <thead>
            <tr>
                <th>Mã</th>
                <th>Thông tin</th>
                <th>Thời gian</th>
                <th>Giá trị</th>
                <th>Trạng thái</th>
            </tr>
            </thead>

            <tbody>

            <c:forEach items="${orders}" var="order">

                <tr>

                    <td>
                        <b>#${order.code}</b>
                    </td>

                    <td>
                        ${order.customerName}
                    </td>

                    <td>
                        ${order.createdAt}
                    </td>

                    <td>
                        <fmt:formatNumber
                                value="${order.totalPrice}"
                                pattern="#,##0"
                        /> ₫
                    </td>

                    <td>

                        <span class="status-badge ${order.statusEnum.cssClass}">
                            ${order.statusEnum.label}
                        </span>

                    </td>

                </tr>

            </c:forEach>

            <c:if test="${empty orders}">

                <tr>

                    <td
                            colspan="5"
                            style="text-align:center;color:#888;padding:20px;"
                    >
                        Chưa có dữ liệu đơn hàng trong hệ thống.
                    </td>

                </tr>

            </c:if>

            </tbody>

        </table>

    </div>

</article>