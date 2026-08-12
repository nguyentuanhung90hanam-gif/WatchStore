<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="warehouse-dashboard">

    <!-- ==================== HEADER ==================== -->
    <div class="warehouse-dashboard-header">

        <div class="warehouse-dashboard-title">
            <p class="warehouse-eyebrow">TRUNG TÂM KHO HÀNG</p>

            <h2>Tổng quan kho</h2>

            <p class="warehouse-dashboard-desc">
                Theo dõi nhanh tình trạng tồn kho và các mặt hàng cần bổ sung.
            </p>
        </div>

    </div>


    <!-- ==================== METRICS ==================== -->
    <div class="warehouse-metric-grid">

        <article class="warehouse-metric-card">
            <span>Tổng số lượng tồn</span>

            <b>${totalQuantity}</b>

            <small>
                Tổng QuantityOnHand của các kho
            </small>
        </article>


        <article class="warehouse-metric-card">
            <span>Mặt hàng sắp hết</span>

            <b>${lowStockCount}</b>

            <small>
                AvailableQuantity &gt; 0 và ≤ ReorderLevel
            </small>
        </article>


        <article class="warehouse-metric-card">
            <span>Mặt hàng hết hàng</span>

            <b>${outOfStockCount}</b>

            <small>
                AvailableQuantity ≤ 0
            </small>
        </article>


        <article class="warehouse-metric-card">
            <span>Mặt hàng đang quản lý</span>

            <b>${inventoryItemCount}</b>

            <small>
                Số dòng tồn kho trong InventoryBalances
            </small>
        </article>

    </div>


    <!-- ==================== INVENTORY STATUS ==================== -->
    <div class="dashboard-card warehouse-dashboard-card latest-table">

        <div class="card-title warehouse-card-title">

            <div>
                <b>Tình trạng tồn kho</b>

                <span>
                    Dữ liệu lấy trực tiếp từ InventoryBalances và vw_InventoryOverview
                </span>
            </div>

            <a href="${cp}/manage/warehouse/inventory">
                Xem toàn bộ
            </a>

        </div>


        <div class="table-wrap warehouse-dashboard-table-wrap">

            <table class="warehouse-dashboard-table">

                <thead>
                    <tr>
                        <th>Chỉ số</th>
                        <th>Giá trị</th>
                        <th>Ý nghĩa</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>


                <tbody>

                    <tr>
                        <td>
                            <b>Tổng số lượng tồn</b>
                        </td>

                        <td>
                            ${totalQuantity}
                        </td>

                        <td>
                            Số lượng đang có trong tất cả kho
                        </td>

                        <td>
                            <a href="${cp}/manage/warehouse/inventory">
                                Xem tồn kho
                            </a>
                        </td>
                    </tr>


                    <tr>
                        <td>
                            <b>Sắp hết hàng</b>
                        </td>

                        <td>
                            ${lowStockCount}
                        </td>

                        <td>
                            0 &lt; AvailableQuantity ≤ ReorderLevel
                        </td>

                        <td>
                            <a href="${cp}/manage/warehouse/alerts">
                                Xem cảnh báo
                            </a>
                        </td>
                    </tr>


                    <tr>
                        <td>
                            <b>Hết hàng</b>
                        </td>

                        <td>
                            ${outOfStockCount}
                        </td>

                        <td>
                            AvailableQuantity ≤ 0
                        </td>

                        <td>
                            <a href="${cp}/manage/warehouse/alerts">
                                Xem cảnh báo
                            </a>
                        </td>
                    </tr>


                    <tr>
                        <td>
                            <b>Mặt hàng đang quản lý</b>
                        </td>

                        <td>
                            ${inventoryItemCount}
                        </td>

                        <td>
                            Số bản ghi tồn kho theo kho và biến thể
                        </td>

                        <td>
                            <a href="${cp}/manage/warehouse/inventory">
                                Xem toàn bộ
                            </a>
                        </td>
                    </tr>

                </tbody>

            </table>

        </div>

    </div>


    <!-- ==================== WAREHOUSE PROCESS ==================== -->
    <div class="dashboard-card warehouse-dashboard-card warehouse-process-card">

        <div class="card-title warehouse-card-title">

            <div>
                <b>Quy trình kho</b>

                <span>
                    Dashboard chỉ hiển thị thông tin; thao tác thực hiện tại menu bên trái.
                </span>
            </div>

        </div>


        <div class="warehouse-process-content">

            <div>
                <b>Phiếu nhập</b>
                <span>→ DRAFT → PENDING → COMPLETED → tăng tồn kho.</span>
            </div>

            <div>
                <b>Phiếu xuất</b>
                <span>→ DRAFT → PENDING → COMPLETED → giảm tồn kho khả dụng.</span>
            </div>

            <div>
                <b>Kiểm kê</b>
                <span>→ đối chiếu thực tế → điều chỉnh tồn khi được duyệt.</span>
            </div>

        </div>

    </div>

</div>


<style>

/* =========================================================
   WAREHOUSE DASHBOARD
   ========================================================= */

.warehouse-dashboard {
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    color: #222;
    font-family: inherit;
}


/* =========================================================
   HEADER
   ========================================================= */

.warehouse-dashboard-header {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    width: 100%;
    margin-bottom: 24px;
    box-sizing: border-box;
}

.warehouse-dashboard-title {
    min-width: 0;
}

.warehouse-eyebrow {
    margin: 0 0 8px;
    color: #b58a25;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 2.4px;
    line-height: 1.4;
    text-transform: uppercase;
}

.warehouse-dashboard-title h2 {
    margin: 0;
    color: #171717;
    font-family: Georgia, "Times New Roman", serif;
    font-size: 31px;
    font-weight: 500;
    line-height: 1.2;
}

.warehouse-dashboard-desc {
    margin: 8px 0 0;
    color: #777;
    font-size: 13px;
    line-height: 1.6;
}


/* =========================================================
   METRIC GRID
   ========================================================= */

.warehouse-metric-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 14px;
    width: 100%;
    margin-bottom: 16px;
    box-sizing: border-box;
}

.warehouse-metric-card {
    min-width: 0;
    min-height: 138px;
    padding: 19px 20px;
    border: 1px solid #ddd9d0;
    background: #fff;
    box-sizing: border-box;
}

.warehouse-metric-card > span {
    display: block;
    margin-bottom: 14px;
    color: #777;
    font-size: 12px;
    line-height: 1.4;
}

.warehouse-metric-card > b {
    display: block;
    margin-bottom: 9px;
    color: #181818;
    font-family: Georgia, "Times New Roman", serif;
    font-size: 30px;
    font-weight: 500;
    line-height: 1;
}

.warehouse-metric-card > small {
    display: block;
    color: #777;
    font-size: 11px;
    line-height: 1.5;
}


/* =========================================================
   DASHBOARD CARD
   ========================================================= */

.warehouse-dashboard-card {
    width: 100%;
    margin: 0 0 16px;
    box-sizing: border-box;
}

.warehouse-process-card {
    margin-bottom: 0;
}


/* =========================================================
   CARD TITLE
   ========================================================= */

.warehouse-card-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
}

.warehouse-card-title > div {
    min-width: 0;
}

.warehouse-card-title b {
    display: block;
    margin: 0;
    color: #242424;
    font-size: 17px;
    font-weight: 700;
    line-height: 1.4;
}

.warehouse-card-title span {
    display: block;
    margin-top: 5px;
    color: #888;
    font-size: 11px;
    font-weight: 400;
    line-height: 1.5;
}

.warehouse-card-title > a {
    flex-shrink: 0;
    color: #9a741f;
    font-size: 11px;
    font-weight: 600;
    text-decoration: none;
    white-space: nowrap;
}

.warehouse-card-title > a:hover {
    color: #725416;
    text-decoration: underline;
}


/* =========================================================
   TABLE
   ========================================================= */

.warehouse-dashboard-table-wrap {
    width: 100%;
    overflow-x: auto;
    overflow-y: hidden;
}

.warehouse-dashboard-table {
    width: 100%;
    min-width: 720px;
    border-collapse: collapse;
    table-layout: fixed;
}

.warehouse-dashboard-table thead th {
    height: 42px;
    padding: 0 14px;
    border-bottom: 1px solid #dedbd4;
    background: #f8f7f4;
    color: #666;
    font-size: 10px;
    font-weight: 700;
    letter-spacing: .8px;
    line-height: 1.3;
    text-align: left;
    text-transform: uppercase;
    vertical-align: middle;
}

.warehouse-dashboard-table tbody td {
    padding: 14px;
    border-bottom: 1px solid #ece9e3;
    color: #444;
    font-size: 12px;
    line-height: 1.5;
    vertical-align: middle;
}

.warehouse-dashboard-table tbody tr:last-child td {
    border-bottom: none;
}

.warehouse-dashboard-table tbody tr {
    transition: background-color .12s ease;
}

.warehouse-dashboard-table tbody tr:hover {
    background: #fcfbf8;
}

.warehouse-dashboard-table th:nth-child(1) {
    width: 27%;
}

.warehouse-dashboard-table th:nth-child(2) {
    width: 12%;
}

.warehouse-dashboard-table th:nth-child(3) {
    width: 39%;
}

.warehouse-dashboard-table th:nth-child(4) {
    width: 22%;
}

.warehouse-dashboard-table td:nth-child(2) {
    color: #222;
    font-weight: 600;
}

.warehouse-dashboard-table td:nth-child(4) a {
    color: #99721d;
    font-size: 11px;
    font-weight: 600;
    text-decoration: none;
    white-space: nowrap;
}

.warehouse-dashboard-table td:nth-child(4) a:hover {
    color: #6f5114;
    text-decoration: underline;
}


/* =========================================================
   PROCESS
   ========================================================= */

.warehouse-process-content {
    display: flex;
    flex-direction: column;
    gap: 0;
    padding-top: 14px;
    color: #555;
    font-size: 13px;
    line-height: 1.8;
}

.warehouse-process-content > div {
    padding: 8px 0;
    border-bottom: 1px solid #f0eee9;
}

.warehouse-process-content > div:last-child {
    border-bottom: none;
}

.warehouse-process-content b {
    display: inline;
    margin-right: 4px;
    color: #333;
    font-weight: 700;
}

.warehouse-process-content span {
    color: #555;
}


/* =========================================================
   RESPONSIVE
   ========================================================= */

@media (max-width: 1200px) {

    .warehouse-metric-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}


@media (max-width: 800px) {

    .warehouse-dashboard-header {
        align-items: flex-start;
    }

    .warehouse-dashboard-title h2 {
        font-size: 28px;
    }

    .warehouse-card-title {
        align-items: flex-start;
        flex-direction: column;
        gap: 10px;
    }

    .warehouse-card-title > a {
        align-self: flex-start;
    }
}


@media (max-width: 600px) {

    .warehouse-metric-grid {
        grid-template-columns: 1fr;
    }

    .warehouse-metric-card {
        min-height: auto;
    }

    .warehouse-dashboard-title h2 {
        font-size: 25px;
    }

    .warehouse-dashboard-desc {
        font-size: 12px;
    }

    .warehouse-process-content {
        font-size: 12px;
    }
}

</style>