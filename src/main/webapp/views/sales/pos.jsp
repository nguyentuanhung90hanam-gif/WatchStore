<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<style>
    :root {
        --pos-dark: #11110f;
        --pos-burgundy: #581c20;
        --pos-gold: #c5a880;
        --pos-gold-hover: #b09168;
        --pos-border: #e2e8f0;
        --pos-bg-light: #f8fafc;
    }

    .pos-container {
        display: grid;
        grid-template-columns: 1.2fr 1fr;
        gap: 20px;
        height: calc(100vh - 120px);
        margin-top: 10px;
    }

    /* Left panel: Products Selection */
    .pos-products-panel {
        display: flex;
        flex-direction: column;
        background: #fff;
        border-radius: 12px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.06);
        padding: 20px;
        overflow: hidden;
    }

    .pos-search-area {
        display: flex;
        flex-direction: column;
        gap: 12px;
        margin-bottom: 18px;
    }

    .pos-search-row {
        display: flex;
        gap: 10px;
    }

    .pos-search-row input {
        flex: 1;
        height: 42px;
        padding: 0 14px;
        border: 1px solid var(--pos-border);
        border-radius: 6px;
        font-size: 14px;
        outline: none;
    }

    .pos-search-row input:focus {
        border-color: var(--pos-gold);
    }

    .pos-filter-row {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 10px;
    }

    .pos-filter-row select {
        height: 40px;
        padding: 0 10px;
        border: 1px solid var(--pos-border);
        border-radius: 6px;
        font-size: 13px;
        outline: none;
        background: #fff;
    }

    .pos-products-grid {
        flex: 1;
        overflow-y: auto;
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
        gap: 15px;
        padding-right: 5px;
    }

    .pos-product-card {
        border: 1px solid var(--pos-border);
        border-radius: 8px;
        padding: 12px;
        background: #fff;
        display: flex;
        flex-direction: column;
        transition: all 0.2s;
        position: relative;
    }

    .pos-product-card:hover {
        border-color: var(--pos-gold);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    }

    .pos-card-img {
        width: 100%;
        height: 120px;
        object-fit: cover;
        border-radius: 6px;
        margin-bottom: 10px;
        background: #fafafa;
    }

    .pos-card-title {
        font-size: 13px;
        font-weight: bold;
        color: #1e293b;
        margin: 0 0 4px 0;
        line-height: 1.3;
        min-height: 34px;
    }

    .pos-card-variant {
        font-size: 11px;
        color: #64748b;
        margin-bottom: 8px;
    }

    .pos-card-prices {
        display: flex;
        align-items: baseline;
        gap: 6px;
        margin-bottom: 10px;
    }

    .pos-card-price {
        font-size: 14px;
        font-weight: 700;
        color: var(--pos-burgundy);
    }

    .pos-card-oldprice {
        font-size: 11px;
        color: #94a3b8;
        text-decoration: line-through;
    }

    .pos-card-stock {
        font-size: 11px;
        color: #64748b;
        margin-bottom: 12px;
        display: flex;
        justify-content: space-between;
    }

    .pos-card-stock.out {
        color: #ef4444;
        font-weight: 600;
    }

    .btn-add-item {
        background: var(--pos-dark);
        color: #fff;
        border: none;
        border-radius: 6px;
        padding: 8px;
        font-size: 12px;
        font-weight: 600;
        cursor: pointer;
        width: 100%;
        transition: background 0.2s;
    }

    .btn-add-item:hover:not(:disabled) {
        background: var(--pos-gold);
    }

    .btn-add-item:disabled {
        background: #cbd5e1;
        cursor: not-allowed;
    }

    /* Right panel: Active Invoice */
    .pos-invoice-panel {
        display: flex;
        flex-direction: column;
        background: #fff;
        border-radius: 12px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.06);
        padding: 20px;
        overflow: hidden;
        border-top: 4px solid var(--pos-gold);
    }

    .pos-invoice-title {
        font-size: 18px;
        font-weight: 700;
        color: var(--pos-dark);
        margin-top: 0;
        margin-bottom: 15px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .pos-items-table-wrapper {
        flex: 1;
        overflow-y: auto;
        border: 1px solid var(--pos-border);
        border-radius: 8px;
        margin-bottom: 15px;
        min-height: 150px;
    }

    .pos-items-table {
        width: 100%;
        border-collapse: collapse;
    }

    .pos-items-table th {
        background: var(--pos-bg-light);
        padding: 10px;
        font-size: 12px;
        font-weight: 600;
        color: #475569;
        text-align: left;
        border-bottom: 1px solid var(--pos-border);
    }

    .pos-items-table td {
        padding: 10px;
        border-bottom: 1px solid #f1f5f9;
        font-size: 13px;
        vertical-align: middle;
    }

    .pos-item-qty-control {
        display: flex;
        align-items: center;
        gap: 5px;
    }

    .pos-item-qty-btn {
        width: 24px;
        height: 24px;
        border-radius: 4px;
        border: 1px solid #cbd5e1;
        background: #fff;
        font-size: 14px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .pos-item-qty-btn:hover {
        background: #f1f5f9;
    }

    .pos-item-qty-val {
        width: 30px;
        text-align: center;
        border: none;
        font-size: 13px;
        font-weight: 600;
    }

    .pos-item-delete {
        color: #ef4444;
        background: none;
        border: none;
        cursor: pointer;
        font-size: 14px;
    }

    .pos-item-delete:hover {
        color: #b91c1c;
    }

    /* Customer Attachment Section */
    .pos-customer-box {
        background: var(--pos-bg-light);
        border-radius: 8px;
        padding: 12px;
        margin-bottom: 15px;
    }

    .pos-customer-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
        font-size: 13px;
        font-weight: 600;
        color: #475569;
    }

    .pos-customer-search-row {
        display: flex;
        gap: 8px;
        position: relative;
    }

    .pos-customer-search-row input {
        flex: 1;
        height: 36px;
        padding: 0 10px;
        border: 1px solid var(--pos-border);
        border-radius: 6px;
        font-size: 13px;
        outline: none;
    }

    .pos-customer-search-row input:focus {
        border-color: var(--pos-gold);
    }

    .pos-customer-suggest {
        position: absolute;
        top: 38px;
        left: 0;
        right: 0;
        background: #fff;
        border: 1px solid var(--pos-border);
        border-radius: 6px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        z-index: 100;
        max-height: 150px;
        overflow-y: auto;
        display: none;
    }

    .pos-customer-suggest-item {
        padding: 8px 12px;
        font-size: 13px;
        cursor: pointer;
        border-bottom: 1px solid #f1f5f9;
    }

    .pos-customer-suggest-item:hover {
        background: var(--pos-bg-light);
    }

    .pos-customer-active-info {
        background: #fff;
        border: 1px solid var(--pos-border);
        border-radius: 6px;
        padding: 10px;
        display: none;
        position: relative;
    }

    .pos-customer-active-info .close-btn {
        position: absolute;
        top: 8px;
        right: 10px;
        background: none;
        border: none;
        cursor: pointer;
        color: #94a3b8;
        font-weight: bold;
    }

    /* Voucher Area */
    .pos-voucher-box {
        display: flex;
        gap: 8px;
        margin-bottom: 15px;
    }

    .pos-voucher-box input {
        flex: 1;
        height: 38px;
        padding: 0 10px;
        border: 1px solid var(--pos-border);
        border-radius: 6px;
        font-size: 13px;
        outline: none;
        text-transform: uppercase;
    }

    .pos-voucher-box input:focus {
        border-color: var(--pos-gold);
    }

    .btn-apply-voucher {
        height: 38px;
        padding: 0 15px;
        background: var(--pos-gold);
        color: #fff;
        border: none;
        border-radius: 6px;
        font-size: 13px;
        font-weight: 600;
        cursor: pointer;
    }

    .btn-apply-voucher:hover {
        background: var(--pos-gold-hover);
    }

    .pos-voucher-message {
        font-size: 12px;
        margin-top: -10px;
        margin-bottom: 12px;
        font-weight: 500;
    }

    /* Financial Summary */
    .pos-summary-box {
        border-top: 1px solid var(--pos-border);
        padding-top: 12px;
        margin-bottom: 18px;
    }

    .pos-summary-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 6px;
        font-size: 13px;
        color: #475569;
    }

    .pos-summary-row.total {
        font-size: 18px;
        font-weight: 700;
        color: var(--pos-burgundy);
        border-top: 1px dashed var(--pos-border);
        padding-top: 8px;
        margin-top: 8px;
    }

    /* Action Buttons */
    .pos-action-buttons {
        display: grid;
        grid-template-columns: 1fr 2fr;
        gap: 10px;
    }

    .btn-pos-cancel {
        height: 46px;
        background: #f1f5f9;
        color: #475569;
        border: 1px solid #cbd5e1;
        border-radius: 6px;
        font-weight: 600;
        cursor: pointer;
    }

    .btn-pos-cancel:hover {
        background: #e2e8f0;
    }

    .btn-pos-checkout {
        height: 46px;
        background: var(--pos-burgundy);
        color: #fff;
        border: none;
        border-radius: 6px;
        font-weight: 700;
        font-size: 14px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
    }

    .btn-pos-checkout:hover {
        background: #401316;
    }

    /* Quick Customer Add Modal */
    .pos-modal {
        display: none;
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0,0,0,0.5);
        align-items: center;
        justify-content: center;
        z-index: 1000;
    }

    .pos-modal-content {
        background: #fff;
        border-radius: 12px;
        width: 100%;
        max-width: 450px;
        padding: 24px;
        box-shadow: 0 10px 25px rgba(0,0,0,0.15);
        position: relative;
    }

    .pos-modal-title {
        font-size: 18px;
        font-weight: bold;
        margin-top: 0;
        margin-bottom: 18px;
    }

    .pos-modal-close {
        position: absolute;
        top: 15px;
        right: 15px;
        background: none;
        border: none;
        cursor: pointer;
        font-size: 20px;
        color: #94a3b8;
    }

    .pos-modal-grid {
        display: grid;
        grid-template-columns: 1fr;
        gap: 12px;
        margin-bottom: 20px;
    }

    .pos-modal-group {
        display: flex;
        flex-direction: column;
        gap: 5px;
    }

    .pos-modal-group label {
        font-size: 12px;
        font-weight: 600;
        color: #475569;
    }

    .pos-modal-group input {
        height: 38px;
        padding: 0 10px;
        border: 1px solid var(--pos-border);
        border-radius: 6px;
        outline: none;
        font-size: 13px;
    }

    .pos-modal-group input:focus {
        border-color: var(--pos-gold);
    }
</style>

<div class="pos-container">

    <!-- CỘT TRÁI: DANH SÁCH SẢN PHẨM & TÌM KIẾM -->
    <div class="pos-products-panel">
        <div class="pos-search-area">
            <div class="pos-search-row">
                <input type="text" id="productSearchInput" placeholder="🔍 Tìm tên sản phẩm, SKU, hoặc QUÉT MÃ VẠCH tại đây..." autofocus autocomplete="off">
            </div>
            <div class="pos-filter-row">
                <select id="brandSelect">
                    <option value="">-- Tất cả Thương hiệu --</option>
                    <c:forEach var="brand" items="${brands}">
                        <option value="${brand.id}">${brand.name}</option>
                    </c:forEach>
                </select>
                <select id="categorySelect">
                    <option value="">-- Tất cả Danh mục --</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.id}">${cat.name}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="pos-products-grid" id="productsGrid">
            <!-- Rendered dynamically by Javascript -->
        </div>
    </div>

    <!-- CỘT PHẢI: HÓA ĐƠN ĐANG TẠO -->
    <div class="pos-invoice-panel">
        <div class="pos-invoice-title">
            <span>🧾 Hóa đơn bán lẻ</span>
            <span id="posOrderCode" style="font-size:14px; font-weight:600; color:var(--pos-gold);">
                Mã: WS${8500 + ordersSize + 1}
            </span>
        </div>

        <!-- Giỏ hàng / Sản phẩm đã chọn -->
        <div class="pos-items-table-wrapper">
            <table class="pos-items-table">
                <thead>
                    <tr>
                        <th style="width: 45%;">Sản phẩm</th>
                        <th style="width: 25%; text-align: center;">Số lượng</th>
                        <th style="width: 25%; text-align: right;">Thành tiền</th>
                        <th style="width: 5%;"></th>
                    </tr>
                </thead>
                <tbody id="invoiceItemsBody">
                    <!-- Rendered dynamically by Javascript -->
                </tbody>
            </table>
        </div>

        <!-- Khách hàng -->
        <div class="pos-customer-box">
            <div class="pos-customer-header">
                <span>👤 Gắn khách hàng</span>
                <a href="javascript:void(0);" onclick="openAddCustomerModal()" style="color:var(--pos-gold); text-decoration:none; font-weight:bold;">
                    + Thêm khách hàng nhanh
                </a>
            </div>
            <div class="pos-customer-search-row" id="customerSearchRow">
                <input type="text" id="customerSearchInput" placeholder="Nhập tên hoặc số điện thoại..." autocomplete="off">
                <div class="pos-customer-suggest" id="customerSuggestBox">
                    <!-- Gợi ý khách hàng AJAX -->
                </div>
            </div>
            <div class="pos-customer-active-info" id="customerActiveInfo">
                <button type="button" class="close-btn" onclick="detachCustomer()">×</button>
                <div style="font-weight:bold; color: #1e293b;" id="activeCustName"></div>
                <div style="font-size:12px; color:#64748b; margin-top:2px;">
                    SĐT: <span id="activeCustPhone"></span> | Địa chỉ: <span id="activeCustAddress"></span>
                </div>
            </div>
        </div>

        <!-- Voucher -->
        <div class="pos-voucher-box">
            <input type="text" id="voucherCodeInput" placeholder="Nhập mã voucher (ví dụ: WELCOME10)">
            <button type="button" class="btn-apply-voucher" onclick="applyVoucher()">Áp dụng</button>
        </div>
        <div id="voucherMessage" class="pos-voucher-message"></div>

        <!-- Tóm tắt tài chính -->
        <div class="pos-summary-box">
            <div class="pos-summary-row">
                <span>Tổng tiền hàng:</span>
                <span id="sumSubtotal">0 ₫</span>
            </div>
            <div class="pos-summary-row">
                <span>Giảm giá Voucher:</span>
                <span id="sumVoucherDiscount" style="color:#ef4444;">-0 ₫</span>
            </div>
            <div class="pos-summary-row total">
                <span>Khách phải trả:</span>
                <span id="sumFinalTotal">0 ₫</span>
            </div>
        </div>

        <!-- Nút hành động -->
        <div class="pos-action-buttons">
            <button type="button" class="btn-pos-cancel" onclick="resetPOS()">Hủy</button>
            <button type="button" class="btn-pos-checkout" onclick="checkoutPOS()">
                💳 Thanh toán & In (K80)
            </button>
        </div>
    </div>

</div>

<!-- MODAL THÊM KHÁCH HÀNG NHANH -->
<div class="pos-modal" id="addCustomerModal">
    <div class="pos-modal-content">
        <button type="button" class="pos-modal-close" onclick="closeAddCustomerModal()">×</button>
        <h3 class="pos-modal-title">👤 Thêm khách hàng nhanh</h3>
        <div class="pos-modal-grid">
            <div class="pos-modal-group">
                <label>Họ và tên <span style="color:#ef4444;">*</span></label>
                <input type="text" id="modalCustName" placeholder="Ví dụ: Nguyễn Văn An" required>
            </div>
            <div class="pos-modal-group">
                <label>Số điện thoại <span style="color:#ef4444;">*</span></label>
                <input type="tel" id="modalCustPhone" placeholder="Ví dụ: 0901234567" required>
            </div>
            <div class="pos-modal-group">
                <label>Email <span style="color:#ef4444;">*</span></label>
                <input type="email" id="modalCustEmail" placeholder="Ví dụ: an.nguyen@gmail.com" required>
            </div>
            <div class="pos-modal-group">
                <label>Địa chỉ</label>
                <input type="text" id="modalCustAddress" placeholder="Địa chỉ thường trú">
            </div>
        </div>
        <div style="display:flex; justify-content:flex-end; gap:10px;">
            <button type="button" class="btn-pos-cancel" style="height:36px; padding:0 15px;" onclick="closeAddCustomerModal()">Hủy</button>
            <button type="button" class="btn-apply-voucher" style="height:36px; padding:0 15px;" onclick="submitQuickCustomer()">Lưu & Chọn</button>
        </div>
    </div>
</div>

<!-- JAVASCRIPT POS -->
<script>
    const contextPath = '${pageContext.request.contextPath}';
    let posCart = []; // [{variantId, productName, variantName, sku, price, oldPrice, stock, quantity}]
    let selectedCustomer = null; // {id, fullName, phone, address}
    let appliedVoucher = null; // {voucherId, code, discountAmount}

    // Load initial products list
    document.addEventListener("DOMContentLoaded", function() {
        searchProducts();

        // Register input search listeners
        document.getElementById("productSearchInput").addEventListener("input", debounce(searchProducts, 300));
        document.getElementById("brandSelect").addEventListener("change", searchProducts);
        document.getElementById("categorySelect").addEventListener("change", searchProducts);

        // Barcode reader simulation (detects Enter on product search box)
        document.getElementById("productSearchInput").addEventListener("keypress", function(e) {
            if (e.key === "Enter") {
                e.preventDefault();
                let keyword = this.value.trim();
                if (keyword.length > 0) {
                    // Try to add directly if it is a exact barcode or SKU match
                    fetchProductByBarcodeOrSKU(keyword);
                }
            }
        });

        // Customer auto-suggestions
        document.getElementById("customerSearchInput").addEventListener("input", debounce(searchCustomers, 250));
        
        // Focus productSearchInput by default
        document.getElementById("productSearchInput").focus();
    });

    function debounce(func, wait) {
        let timeout;
        return function(...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    }

    // 1. AJAX search products
    function searchProducts() {
        let keyword = document.getElementById("productSearchInput").value.trim();
        let brandId = document.getElementById("brandSelect").value;
        let catId = document.getElementById("categorySelect").value;

        let url = contextPath + '/manage/sales/pos/search-products?keyword=' + encodeURIComponent(keyword) + '&brandId=' + brandId + '&categoryId=' + catId;
        
        fetch(url)
            .then(res => res.json())
            .then(data => {
                renderProductsGrid(data);
            })
            .catch(err => console.error("Lỗi tải sản phẩm:", err));
    }

    // Fetch product when Enter pressed (Scan barcode)
    function fetchProductByBarcodeOrSKU(code) {
        let url = contextPath + '/manage/sales/pos/search-products?keyword=' + encodeURIComponent(code);
        fetch(url)
            .then(res => res.json())
            .then(data => {
                if (data.length > 0) {
                    // If exactly 1 match or one of them has exact SKU/barcode, add it!
                    let matched = data.find(p => p.barcode === code || p.sku.toLowerCase() === code.toLowerCase());
                    if (!matched) matched = data[0];
                    addToCart(matched);
                    // Clear search box
                    document.getElementById("productSearchInput").value = "";
                } else {
                    alert('Không tìm thấy sản phẩm có mã vạch hoặc SKU: "' + code + '"');
                }
            });
    }

    // Render product items to the grid
    function renderProductsGrid(products) {
        let grid = document.getElementById("productsGrid");
        grid.innerHTML = "";

        if (products.length === 0) {
            grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: #888; padding: 40px;">Không tìm thấy sản phẩm phù hợp.</div>';
            return;
        }

        products.forEach(p => {
            let discountPercent = 0;
            if (p.oldPrice > p.price) {
                discountPercent = Math.round(((p.oldPrice - p.price) / p.oldPrice) * 100);
            }

            let cardHtml = '<div class="pos-product-card">';
            if (discountPercent > 0) {
                cardHtml += '<span style="position:absolute; top:8px; left:8px; background:#ef4444; color:#fff; font-size:10px; font-weight:bold; padding:2px 6px; border-radius:4px; z-index:5;">-' + discountPercent + '%</span>';
            }
            cardHtml += '<div style="text-align:center;">' +
                        '<img class="pos-card-img" src="' + contextPath + '/assets/images/' + (p.imageUrl ? p.imageUrl : 'default.jpg') + '" onerror="this.src=\'' + contextPath + '/assets/images/seiko-5.jpg\'" alt="' + p.productName + '">' +
                        '</div>' +
                        '<div class="pos-card-title">' + p.productName + '</div>' +
                        '<div class="pos-card-variant">' + p.variantName + '</div>' +
                        '<div class="pos-card-prices">' +
                        '<span class="pos-card-price">' + formatMoney(p.price) + '</span>';
            if (p.oldPrice > p.price) {
                cardHtml += ' <span class="pos-card-oldprice">' + formatMoney(p.oldPrice) + '</span>';
            }
            cardHtml += '</div>' +
                        '<div class="pos-card-stock ' + (p.stock <= 0 ? 'out' : '') + '">' +
                        '<span>Kho: <b>' + p.stock + '</b></span>' +
                        '<span style="font-size:10px; color:#94a3b8;">' + p.sku + '</span>' +
                        '</div>' +
                        '<button class="btn-add-item" ' + (p.stock <= 0 ? 'disabled' : '') + ' onclick=\'addToCart(' + JSON.stringify(p).replace(/'/g, "&apos;") + ')\'>' +
                        (p.stock <= 0 ? 'Hết hàng' : 'Thêm sản phẩm') +
                        '</button>' +
                        '</div>';
            grid.insertAdjacentHTML("beforeend", cardHtml);
        });
    }

    // 2. Add to active invoice cart
    function addToCart(product) {
        let existing = posCart.find(item => item.variantId === product.variantId);
        if (existing) {
            if (existing.quantity >= product.stock) {
                alert('Không thể thêm! Số lượng trong hóa đơn đã đạt mức tối đa tồn kho (' + product.stock + ').');
                return;
            }
            existing.quantity++;
        } else {
            posCart.push({
                variantId: product.variantId,
                productName: product.productName,
                variantName: product.variantName,
                sku: product.sku,
                price: product.price,
                oldPrice: product.oldPrice,
                stock: product.stock,
                quantity: 1
            });
        }
        renderCart();
        revalidateVoucher();
    }

    // Render active cart items
    function renderCart() {
        let tbody = document.getElementById("invoiceItemsBody");
        tbody.innerHTML = "";

        if (posCart.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: #94a3b8; padding: 30px;">Hóa đơn chưa có sản phẩm.</td></tr>';
            updateCalculations();
            return;
        }

        posCart.forEach((item, index) => {
            let rowHtml = '<tr>' +
                '<td>' +
                '<div style="font-weight:bold; line-height:1.2;">' + item.productName + '</div>' +
                '<div style="font-size:11px; color:#64748b; margin-top:2px;">' + item.variantName + '</div>' +
                '</td>' +
                '<td style="text-align: center;">' +
                '<div class="pos-item-qty-control">' +
                '<button type="button" class="pos-item-qty-btn" onclick="updateQty(' + index + ', -1)">-</button>' +
                '<input type="text" class="pos-item-qty-val" value="' + item.quantity + '" readonly>' +
                '<button type="button" class="pos-item-qty-btn" onclick="updateQty(' + index + ', 1)">+</button>' +
                '</div>' +
                '</td>' +
                '<td style="text-align: right; font-weight:600; color: #1e293b;">' +
                formatMoney(item.price * item.quantity) +
                '</td>' +
                '<td style="text-align: center;">' +
                '<button type="button" class="pos-item-delete" onclick="deleteItem(' + index + ')">×</button>' +
                '</td>' +
                '</tr>';
            tbody.insertAdjacentHTML("beforeend", rowHtml);
        });

        updateCalculations();
    }

    function updateQty(index, change) {
        let item = posCart[index];
        let newQty = item.quantity + change;
        if (newQty < 1) return;
        if (newQty > item.stock) {
            alert('Sản phẩm này chỉ còn ' + item.stock + ' chiếc trong kho!');
            return;
        }
        item.quantity = newQty;
        renderCart();
        revalidateVoucher();
    }

    function deleteItem(index) {
        posCart.splice(index, 1);
        renderCart();
        revalidateVoucher();
    }

    // 3. Financial calculations
    function updateCalculations() {
        let subtotal = 0;
        posCart.forEach(item => {
            subtotal += item.price * item.quantity;
        });

        let voucherDiscount = 0;
        if (appliedVoucher) {
            voucherDiscount = appliedVoucher.discountAmount;
            // Cap at subtotal
            if (voucherDiscount > subtotal) {
                voucherDiscount = subtotal;
            }
        }

        let finalTotal = subtotal - voucherDiscount;
        if (finalTotal < 0) finalTotal = 0;

        document.getElementById("sumSubtotal").textContent = formatMoney(subtotal);
        document.getElementById("sumVoucherDiscount").textContent = '-' + formatMoney(voucherDiscount);
        document.getElementById("sumFinalTotal").textContent = formatMoney(finalTotal);
    }

    // 4. Voucher Management
    function applyVoucher() {
        let code = document.getElementById("voucherCodeInput").value.trim();
        let msgEl = document.getElementById("voucherMessage");

        if (posCart.length === 0) {
            alert("Vui lòng thêm sản phẩm vào hóa đơn trước khi áp dụng voucher!");
            return;
        }

        if (code.length === 0) {
            appliedVoucher = null;
            msgEl.textContent = "";
            renderCart();
            return;
        }

        let subtotal = 0;
        posCart.forEach(item => {
            subtotal += item.price * item.quantity;
        });

        let url = contextPath + '/manage/sales/pos/check-voucher';
        
        fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: 'code=' + encodeURIComponent(code) + '&subtotal=' + subtotal
        })
        .then(res => res.json())
        .then(res => {
            if (res.status === "valid") {
                appliedVoucher = {
                    voucherId: res.voucherId,
                    code: res.code,
                    discountAmount: res.discountAmount
                };
                msgEl.style.color = "#16a34a";
                msgEl.textContent = res.message;
            } else {
                appliedVoucher = null;
                msgEl.style.color = "#ef4444";
                msgEl.textContent = res.message;
            }
            updateCalculations();
        })
        .catch(err => {
            console.error("Lỗi check voucher:", err);
            msgEl.style.color = "#ef4444";
            msgEl.textContent = "Không thể áp dụng voucher (Lỗi kết nối server).";
        });
    }

    function revalidateVoucher() {
        if (appliedVoucher) {
            applyVoucher();
        }
    }

    // 5. Customer search suggestions
    function searchCustomers() {
        let keyword = document.getElementById("customerSearchInput").value.trim();
        let box = document.getElementById("customerSuggestBox");

        if (keyword.length < 2) {
            box.style.display = "none";
            return;
        }

        let url = contextPath + '/manage/sales/pos/search-customers?keyword=' + encodeURIComponent(keyword);
        fetch(url)
            .then(res => res.json())
            .then(data => {
                box.innerHTML = "";
                if (data.length > 0) {
                    box.style.display = "block";
                    data.forEach(c => {
                        let div = document.createElement("div");
                        div.className = "pos-customer-suggest-item";
                        div.innerHTML = '<b>' + c.fullName + '</b> - ' + c.phone;
                        div.onclick = function() {
                            attachCustomer(c);
                        };
                        box.appendChild(div);
                    });
                } else {
                    box.style.display = "none";
                }
            });
    }

    function attachCustomer(c) {
        selectedCustomer = c;
        document.getElementById("customerSearchRow").style.display = "none";
        document.getElementById("customerSuggestBox").style.display = "none";
        document.getElementById("customerSearchInput").value = "";

        document.getElementById("activeCustName").textContent = c.fullName;
        document.getElementById("activeCustPhone").textContent = c.phone;
        document.getElementById("activeCustAddress").textContent = c.address;
        document.getElementById("customerActiveInfo").style.display = "block";
    }

    function detachCustomer() {
        selectedCustomer = null;
        document.getElementById("customerActiveInfo").style.display = "none";
        document.getElementById("customerSearchRow").style.display = "flex";
        document.getElementById("customerSearchInput").focus();
    }

    // Quick customer Modal actions
    function openAddCustomerModal() {
        document.getElementById("addCustomerModal").style.display = "flex";
        document.getElementById("modalCustName").focus();
    }

    function closeAddCustomerModal() {
        document.getElementById("addCustomerModal").style.display = "none";
        // Clear fields
        document.getElementById("modalCustName").value = "";
        document.getElementById("modalCustPhone").value = "";
        document.getElementById("modalCustEmail").value = "";
        document.getElementById("modalCustAddress").value = "";
    }

    function submitQuickCustomer() {
        let name = document.getElementById("modalCustName").value.trim();
        let phone = document.getElementById("modalCustPhone").value.trim();
        let email = document.getElementById("modalCustEmail").value.trim();
        let address = document.getElementById("modalCustAddress").value.trim();

        if (name === "" || phone === "" || email === "") {
            alert("Vui lòng nhập đầy đủ các trường bắt buộc (*).");
            return;
        }

        let url = contextPath + '/manage/sales/pos/add-customer';
        let params = 'fullName=' + encodeURIComponent(name) + '&phone=' + encodeURIComponent(phone) + '&email=' + encodeURIComponent(email) + '&address=' + encodeURIComponent(address);

        fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params
        })
        .then(res => res.json())
        .then(res => {
            if (res.status === "success") {
                attachCustomer(res);
                closeAddCustomerModal();
            } else {
                alert("Lỗi: " + res.message);
            }
        })
        .catch(err => {
            alert("Lỗi kết nối server khi tạo khách hàng.");
        });
    }

    // 6. POS Reset / Cancel Invoice
    function resetPOS() {
        if (confirm("Bạn có chắc chắn muốn hủy và xóa sạch giỏ hàng hiện tại?")) {
            posCart = [];
            selectedCustomer = null;
            appliedVoucher = null;
            document.getElementById("voucherCodeInput").value = "";
            document.getElementById("voucherMessage").textContent = "";
            detachCustomer();
            renderCart();
            searchProducts(); // refresh product stock displays
            document.getElementById("productSearchInput").value = "";
            document.getElementById("productSearchInput").focus();
        }
    }

    // 7. Checkout Transaction
    function checkoutPOS() {
        if (posCart.length === 0) {
            alert("Hóa đơn chưa có sản phẩm! Vui lòng chọn ít nhất một sản phẩm.");
            return;
        }

        let itemsPayload = posCart.map(item => {
            return {
                variantId: item.variantId,
                quantity: item.quantity
            };
        });

        let custId = selectedCustomer ? selectedCustomer.id : 4; // Default to Guest Customer ID 4
        let vId = appliedVoucher ? appliedVoucher.voucherId : 0;
        let vDiscount = appliedVoucher ? appliedVoucher.discountAmount : 0;

        let custName = selectedCustomer ? selectedCustomer.fullName : "Khách mua tại quầy";
        let custPhone = selectedCustomer ? selectedCustomer.phone : "";
        let custAddress = selectedCustomer ? selectedCustomer.address : "Mua tại quầy";

        let params = new URLSearchParams();
        params.append("customerId", custId);
        params.append("voucherId", vId);
        params.append("discountAmount", vDiscount);
        params.append("customerName", custName);
        params.append("phone", custPhone);
        params.append("address", custAddress);
        params.append("items", JSON.stringify(itemsPayload));

        let url = contextPath + '/manage/sales/pos/checkout';

        fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params.toString()
        })
        .then(res => res.json())
        .then(res => {
            if (res.status === "success") {
                // Success: open K80 printing popup
                alert('Thanh toán thành công! Tạo hóa đơn ' + res.orderCode + '. Tiến hành in...');
                
                // Open printing popup
                let printUrl = contextPath + '/manage/sales/pos/print?id=' + res.orderId;
                window.open(printUrl, "_blank", "width=450,height=650");

                // Reset POS
                posCart = [];
                selectedCustomer = null;
                appliedVoucher = null;
                document.getElementById("voucherCodeInput").value = "";
                document.getElementById("voucherMessage").textContent = "";
                detachCustomer();
                renderCart();
                searchProducts(); // refresh product stock displays
            } else {
                alert("Lỗi thanh toán: " + res.message);
            }
        })
        .catch(err => {
            alert("Lỗi kết nối server khi thanh toán: " + err);
        });
    }

    // Helper functions
    function formatMoney(amount) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' })
            .format(amount)
            .replace("₫", "₫");
    }
</script>
