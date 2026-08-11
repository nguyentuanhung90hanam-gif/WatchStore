<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="module-heading">
    <div style="display:flex;gap:8px;flex-wrap:wrap;">
        <a class="button button-outline" href="${cp}/manage/warehouse/search">Tra cứu thông tin</a>
        <a class="button button-outline" href="${cp}/manage/warehouse/reports">Báo cáo kho</a>
        <a class="button button-gold" href="${cp}/manage/warehouse/inventory">Điều chỉnh tồn kho</a>
    </div>
    <div class="module-title-area">
        <p class="eyebrow dark">TRUNG TÂM KHO HÀNG</p>
        <h2>Tổng quan kho</h2>
        <p class="module-desc">Theo dõi nhanh tình trạng tồn kho và các mặt hàng cần bổ sung.</p>
    </div>
</div>

<div class="metric-grid">
    <article>
        <span>Tổng số lượng tồn</span>
        <b>${totalQuantity}</b>
        <small>Tổng QuantityOnHand của các kho</small>
    </article>
    <article>
        <span>Mặt hàng sắp hết</span>
        <b>${lowStockCount}</b>
        <small>AvailableQuantity &gt; 0 và ≤ ReorderLevel</small>
    </article>
    <article>
        <span>Mặt hàng hết hàng</span>
        <b>${outOfStockCount}</b>
        <small>AvailableQuantity ≤ 0</small>
    </article>
    <article>
        <span>Mặt hàng đang quản lý</span>
        <b>${inventoryItemCount}</b>
        <small>Số dòng tồn kho trong InventoryBalances</small>
    </article>
</div>

<div class="dashboard-card latest-table">
    <div class="card-title">
        <div>
            <b>Tình trạng tồn kho</b>
            <span>Dữ liệu lấy trực tiếp từ InventoryBalances và vw_InventoryOverview</span>
        </div>
        <a href="${cp}/manage/warehouse/inventory">Xem toàn bộ</a>
    </div>

    <div class="table-wrap">
        <table>
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
                    <td><b>Tổng số lượng tồn</b></td>
                    <td>${totalQuantity}</td>
                    <td>Số lượng đang có trong tất cả kho</td>
                    <td><a href="${cp}/manage/warehouse/inventory">Xem tồn kho</a></td>
                </tr>
                <tr>
                    <td><b>Sắp hết hàng</b></td>
                    <td>${lowStockCount}</td>
                    <td>0 &lt; AvailableQuantity ≤ ReorderLevel</td>
                    <td><a href="${cp}/manage/warehouse/alerts">Xem cảnh báo</a></td>
                </tr>
                <tr>
                    <td><b>Hết hàng</b></td>
                    <td>${outOfStockCount}</td>
                    <td>AvailableQuantity ≤ 0</td>
                    <td><a href="${cp}/manage/warehouse/alerts">Xem cảnh báo</a></td>
                </tr>
                <tr>
                    <td><b>Mặt hàng đang quản lý</b></td>
                    <td>${inventoryItemCount}</td>
                    <td>Số bản ghi tồn kho theo kho và biến thể</td>
                    <td><a href="${cp}/manage/warehouse/inventory">Xem toàn bộ</a></td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<div class="dashboard-card" style="margin-top:16px;">
    <div class="card-title">
        <div>
            <b>Quy trình kho</b>
            <span>Dashboard chỉ hiển thị thông tin; thao tác thực hiện tại menu bên trái.</span>
        </div>
    </div>
    <div style="padding:16px 0; color:#555; line-height:1.8;">
        <b>Phiếu nhập</b> → DRAFT → PENDING → COMPLETED → tăng tồn kho.
        <br>
        <b>Phiếu xuất</b> → DRAFT → PENDING → COMPLETED → giảm tồn kho khả dụng.
        <br>
        <b>Kiểm kê</b> → đối chiếu thực tế → điều chỉnh tồn khi được duyệt.
    </div>
</div>
