<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tạo đơn hàng mới</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f8fafc; color: #1e293b; }
        .app-container { max-width: 1300px; margin: auto; padding: 20px; }
        .app-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        .app-header h1 { margin: 0; font-size: 24px; color: #0f172a; font-weight: 700; }
        .back-link { color: #2563eb; text-decoration: none; font-size: 14px; font-weight: 600; }
        .back-link:hover { text-decoration: underline; }
        
        .grid-layout { display: grid; grid-template-columns: 1.2fr 1fr; gap: 20px; }
        
        /* Left Column: Product Search */
        .panel { background: white; border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); padding: 20px; display: flex; flex-direction: column; height: calc(100vh - 120px); }
        .panel h2 { margin-top: 0; font-size: 18px; color: #0f172a; border-bottom: 1px solid #e2e8f0; padding-bottom: 10px; margin-bottom: 15px; }
        
        .search-row { display: flex; gap: 10px; margin-bottom: 15px; }
        .search-row input { flex: 1; height: 40px; padding: 0 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 14px; outline: none; }
        .search-row input:focus { border-color: #2563eb; }
        .btn-search { background: #2563eb; color: white; border: none; padding: 0 16px; border-radius: 6px; cursor: pointer; font-size: 14px; font-weight: 600; }
        
        .product-list-container { flex: 1; overflow-y: auto; padding-right: 5px; }
        .product-item { display: flex; align-items: center; justify-content: space-between; padding: 12px; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 10px; transition: border-color 0.2s; }
        .product-item:hover { border-color: #cbd5e1; }
        .prod-info { flex: 1; padding-right: 12px; }
        .prod-title { font-weight: 600; font-size: 14px; color: #0f172a; margin-bottom: 4px; }
        .prod-meta { font-size: 12px; color: #64748b; margin-bottom: 4px; }
        .prod-price { font-weight: 700; color: #b91c1c; font-size: 13px; }
        .prod-stock { font-size: 12px; color: #16a34a; }
        .prod-stock.out { color: #ef4444; }
        
        .btn-add { background: #0f172a; color: white; border: none; height: 32px; padding: 0 12px; border-radius: 6px; cursor: pointer; font-size: 12px; font-weight: 600; transition: background 0.2s; }
        .btn-add:hover:not(:disabled) { background: #2563eb; }
        .btn-add:disabled { background: #cbd5e1; cursor: not-allowed; }
        
        /* Right Column: Checkout */
        .cart-section { flex: 1; overflow-y: auto; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 15px; min-height: 120px; }
        .cart-table { width: 100%; border-collapse: collapse; }
        .cart-table th { background: #f8fafc; padding: 8px; font-size: 12px; font-weight: 600; color: #475569; border-bottom: 1px solid #e2e8f0; text-align: left; }
        .cart-table td { padding: 8px; border-bottom: 1px solid #f1f5f9; font-size: 13px; vertical-align: middle; }
        
        .qty-controls { display: flex; align-items: center; gap: 4px; }
        .qty-btn { width: 22px; height: 22px; border-radius: 4px; border: 1px solid #cbd5e1; background: white; font-size: 12px; cursor: pointer; display: flex; align-items: center; justify-content: center; }
        .qty-btn:hover { background: #f1f5f9; }
        .qty-val { width: 24px; text-align: center; border: none; font-size: 13px; font-weight: 600; background: transparent; }
        
        .btn-del { color: #ef4444; background: none; border: none; cursor: pointer; font-size: 16px; font-weight: bold; }
        .btn-del:hover { color: #b91c1c; }
        
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-top: 10px; }
        .form-group { display: flex; flex-direction: column; gap: 5px; }
        .form-group.full { grid-column: 1 / -1; }
        .form-group label { font-size: 12px; font-weight: 600; color: #475569; }
        .form-group input, .form-group select { height: 38px; padding: 0 10px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 13px; outline: none; }
        .form-group input:focus, .form-group select:focus { border-color: #2563eb; }
        
        .summary-row { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-top: 1px dashed #e2e8f0; margin-top: 10px; font-size: 15px; font-weight: 700; color: #0f172a; }
        .summary-row .total-amount { color: #b91c1c; font-size: 18px; }
        
        .buttons-row { display: flex; gap: 10px; margin-top: 15px; }
        .btn-submit { flex: 1; height: 42px; background: #2563eb; color: white; border: none; border-radius: 6px; font-size: 14px; font-weight: 600; cursor: pointer; transition: background 0.2s; }
        .btn-submit:hover { background: #1d4ed8; }
        .btn-cancel { height: 42px; background: #e2e8f0; color: #475569; border: none; border-radius: 6px; font-size: 14px; font-weight: 600; padding: 0 16px; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; }
        .btn-cancel:hover { background: #cbd5e1; }
        
        .flash-error {
            background: #fef2f2; border: 1px solid #fca5a5; color: #b91c1c;
            padding: 10px 14px; border-radius: 8px; margin-bottom: 15px; font-size: 13px;
        }

        /* Suggest dropdown styling */
        .suggest-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 10px 12px;
            border-bottom: 1px solid #f1f5f9;
            cursor: pointer;
            transition: background 0.15s;
        }
        .suggest-item:hover {
            background: #f8fafc;
        }
        
        /* Modal Styling */
        .modal-backdrop {
            display: none;
            position: fixed;
            top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(15, 23, 42, 0.6);
            backdrop-filter: blur(4px);
            align-items: center;
            justify-content: center;
            z-index: 1050;
        }
        .modal-box {
            background: white;
            border-radius: 12px;
            width: 100%;
            max-width: 850px;
            height: 85%;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
            display: flex;
            flex-direction: column;
            overflow: hidden;
            animation: modalFadeIn 0.25s ease-out;
        }
        @keyframes modalFadeIn {
            from { transform: scale(0.95); opacity: 0; }
            to { transform: scale(1); opacity: 1; }
        }
        .modal-header {
            background: #f8fafc;
            padding: 16px 20px;
            border-bottom: 1px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .modal-header h3 {
            margin: 0;
            font-size: 16px;
            color: #0f172a;
            font-weight: 700;
        }
        .modal-close {
            background: none;
            border: none;
            font-size: 20px;
            color: #64748b;
            cursor: pointer;
        }
        .modal-close:hover {
            color: #0f172a;
        }
        .modal-body {
            padding: 20px;
            flex: 1;
            overflow-y: auto;
            display: flex;
            flex-direction: column;
        }
        .modal-footer {
            background: #f8fafc;
            padding: 12px 20px;
            border-top: 1px solid #e2e8f0;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }
        
        /* Table inside modal */
        .modal-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 12px;
        }
        .modal-table th {
            background: #f8fafc;
            padding: 10px;
            font-size: 12px;
            font-weight: 600;
            color: #475569;
            border-bottom: 1px solid #e2e8f0;
            text-align: left;
        }
        .modal-table td {
            padding: 10px;
            border-bottom: 1px solid #f1f5f9;
            font-size: 13px;
            vertical-align: middle;
        }
        .modal-filter-row {
            display: grid;
            grid-template-columns: 1fr 1fr 1.5fr;
            gap: 12px;
            margin-bottom: 15px;
        }
        .modal-filter-row select, .modal-filter-row input {
            height: 38px;
            padding: 0 10px;
            border: 1px solid #cbd5e1;
            border-radius: 6px;
            font-size: 13px;
            outline: none;
            width: 100%;
        }
        .modal-filter-row select:focus, .modal-filter-row input:focus {
            border-color: #2563eb;
        }
    </style>
</head>
<body>
<div class="app-container">

    <div class="app-header">
        <h1>📦 Tạo đơn hàng mới</h1>
        <a class="back-link" href="${pageContext.request.contextPath}/manage/sales/orders">← Quay lại danh sách</a>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div class="flash-error">${sessionScope.flash}</div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <div class="grid-layout">
        
        <!-- Left panel: Customer Details -->
        <div class="panel" style="height: auto;">
            <h2>👤 Thông tin khách hàng & Đơn hàng</h2>
            <form id="orderForm" method="post" action="${pageContext.request.contextPath}/manage/sales/order-add" onsubmit="return validateForm()">
                <!-- Hidden fields -->
                <input type="hidden" name="totalPrice" id="totalPriceInput" value="0">
                <input type="hidden" name="items" id="itemsPayload" value="[]">

                <div class="form-grid" style="display: flex; flex-direction: column; gap: 12px;">
                    <div class="form-group">
                        <label for="customerName">Tên khách hàng <span style="color:red;">*</span></label>
                        <input type="text" id="customerName" name="customerName" placeholder="Tên người nhận" required>
                    </div>

                    <div class="form-group">
                        <label for="phone">Số điện thoại <span style="color:red;">*</span></label>
                        <input type="tel" id="phone" name="phone" placeholder="Số điện thoại liên hệ" required>
                    </div>

                    <div class="form-group full">
                        <label for="shippingAddress">Địa chỉ giao hàng <span style="color:red;">*</span></label>
                        <input type="text" id="shippingAddress" name="shippingAddress" placeholder="Địa chỉ giao hàng chi tiết" required>
                    </div>

                    <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
                        <div class="form-group">
                            <label for="status">Trạng thái đơn hàng</label>
                            <select id="status" name="status" required>
                                <option value="PENDING">Chờ xử lý (PENDING)</option>
                                <option value="CONFIRMED">Đã xác nhận (CONFIRMED)</option>
                                <option value="COMPLETED">Hoàn thành (COMPLETED)</option>
                                <option value="CANCELLED">Đã hủy (CANCELLED)</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="paymentStatus">Trạng thái thanh toán</label>
                            <select id="paymentStatus" name="paymentStatus" required>
                                <option value="UNPAID">Chưa thanh toán (UNPAID)</option>
                                <option value="PAID">Đã thanh toán (PAID)</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="paymentMethod">Phương thức thanh toán <span style="color:red;">*</span></label>
                        <select id="paymentMethod" name="paymentMethod" required>
                            <option value="COD">Tiền mặt khi nhận hàng (COD)</option>
                            <option value="BANK_TRANSFER">Chuyển khoản (BANK_TRANSFER)</option>
                            <option value="CREDIT_CARD">Thẻ tín dụng (CREDIT_CARD)</option>
                            <option value="MOMO">Ví MoMo (MOMO)</option>
                            <option value="VNPAY">Ví VNPAY (VNPAY)</option>
                        </select>
                    </div>

                    <div class="form-group full">
                        <label for="note">Ghi chú đơn hàng (Ghi chú)</label>
                        <textarea id="note" name="note" rows="3" placeholder="Nhập ghi chú hoặc yêu cầu đặc biệt của khách..." style="width: 100%; border: 1px solid #cbd5e1; border-radius: 6px; padding: 10px; font-size: 13px; outline: none; font-family: inherit;"></textarea>
                    </div>
                </div>

                <div class="buttons-row">
                    <button type="submit" class="btn-submit">💾 Xác nhận tạo đơn</button>
                    <a href="${pageContext.request.contextPath}/manage/sales/orders" class="btn-cancel">Hủy bỏ</a>
                </div>
            </form>
        </div>

        <!-- Right panel: Products Selection & Cart -->
        <div class="panel" style="height: auto;">
            <h2>🛒 Chi tiết đơn hàng</h2>
            
            <!-- Chọn sản phẩm -->
            <div class="form-group" style="margin-bottom: 15px;">
                <label for="productSelect" style="font-weight: 600; font-size: 13px; color: #475569; display: block; margin-bottom: 6px;">Chọn sản phẩm để thêm vào đơn hàng</label>
                <div style="display: flex; gap: 8px;">
                    <select id="productSelect" style="flex: 1; height: 40px; padding: 0 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 13px; outline: none; background: white;">
                        <option value="">-- Chọn sản phẩm --</option>
                        <c:forEach var="p" items="${productList}">
                            <option value="${p.variantId}" 
                                    data-sku="${p.sku}" 
                                    data-name="${p.productName} ${p.variantName}" 
                                    data-price="${p.price}" 
                                    data-stock="${p.stock}">
                                [${p.sku}] - ${p.productName} ${p.variantName} - ${p.formattedPrice} - [Tồn kho: ${p.stock}]
                            </option>
                        </c:forEach>
                    </select>
                    <button type="button" id="btnAddProduct" style="background: #2563eb; color: white; border: none; border-radius: 6px; padding: 0 16px; font-size: 13px; font-weight: 600; cursor: pointer; height: 40px; white-space: nowrap;">Thêm sản phẩm</button>
                </div>
            </div>

            <!-- Cart Table -->
            <div class="cart-section" style="border: 1px solid #e2e8f0; border-radius: 8px; overflow-x:auto;">
                <table class="cart-table" style="width:100%; border-collapse:collapse;">
                    <thead>
                        <tr style="background:#f8fafc; border-bottom:1px solid #e2e8f0;">
                            <th style="padding:10px; text-align:center; width:120px;">Mã SP/SKU</th>
                            <th style="padding:10px; text-align:left;">Tên sản phẩm</th>
                            <th style="padding:10px; text-align:center; width:100px;">Số lượng</th>
                            <th style="padding:10px; text-align:right; width:120px;">Đơn giá</th>
                            <th style="padding:10px; text-align:right; width:120px;">Thành tiền</th>
                            <th style="padding:10px; text-align:center; width:60px;">Xóa</th>
                        </tr>
                    </thead>
                    <tbody id="cartBody">
                        <tr>
                            <td colspan="6" style="text-align: center; color: #94a3b8; padding: 25px;">Chưa chọn sản phẩm nào.</td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="summary-row">
                <span>Tổng tiền hàng:</span>
                <span class="total-amount" id="totalDisplay">0 ₫</span>
            </div>
        </div>

    </div>

</div>
<script>
    const contextPath = '${pageContext.request.contextPath}';
    let cart = []; // [{variantId, productName, sku, price, stock, quantity}]

    document.addEventListener("DOMContentLoaded", function() {
        let btnAdd = document.getElementById("btnAddProduct");
        let select = document.getElementById("productSelect");

        btnAdd.addEventListener("click", function() {
            let selectedOption = select.options[select.selectedIndex];
            let variantIdVal = select.value;
            if (!variantIdVal || variantIdVal === "") {
                alert("Vui lòng chọn một sản phẩm từ danh sách!");
                return;
            }

            let variantId = parseInt(variantIdVal);
            let sku = selectedOption.getAttribute("data-sku");
            let productName = selectedOption.getAttribute("data-name");
            let price = parseFloat(selectedOption.getAttribute("data-price"));
            let stock = parseInt(selectedOption.getAttribute("data-stock"));

            if (stock <= 0) {
                alert("Sản phẩm này đã hết hàng trong kho!");
                return;
            }

            addToCart({
                variantId: variantId,
                sku: sku,
                productName: productName,
                price: price,
                stock: stock
            });

            // Reset selection dropdown
            select.value = "";
        });
    });

    function addToCart(product) {
        if (!product) return;

        let existing = cart.find(item => item.variantId === product.variantId);
        if (existing) {
            let newQty = existing.quantity + 1;
            if (newQty > product.stock) {
                alert("Số lượng sản phẩm vượt quá tồn kho hiện tại (" + product.stock + ")!");
                return;
            }
            existing.quantity = newQty;
        } else {
            cart.push({
                variantId: product.variantId,
                productName: product.productName,
                sku: product.sku,
                price: product.price,
                stock: product.stock,
                quantity: 1
            });
        }

        renderCart();
    }

    function renderCart() {
        let body = document.getElementById("cartBody");
        body.innerHTML = "";

        if (cart.length === 0) {
            body.innerHTML = '<tr><td colspan="6" style="text-align: center; color: #94a3b8; padding: 25px;">Chưa chọn sản phẩm nào.</td></tr>';
            updateTotals();
            return;
        }

        cart.forEach((item, index) => {
            let tr = document.createElement("tr");
            tr.innerHTML = '<td style="text-align: center; font-weight: bold; color: #2563eb;">' + escapeHtml(item.sku) + '</td>' +
                           '<td>' + escapeHtml(item.productName) + '</td>' +
                           '<td style="text-align: center;">' +
                           '  <input type="number" class="qty-val" value="' + item.quantity + '" min="1" max="' + item.stock + '" oninput="updateItemQty(' + index + ', this.value)" style="width: 60px; text-align: center; border: 1px solid #cbd5e1; border-radius: 4px; padding: 3px;">' +
                           '  <div style="font-size:10px; color:#64748b; margin-top:2px;">Kho: ' + item.stock + '</div>' +
                           '</td>' +
                           '<td style="text-align: right;">' + formatMoney(item.price) + '</td>' +
                           '<td style="text-align: right; font-weight: 700; color: #b91c1c;" id="subtotal_' + index + '">' + formatMoney(item.price * item.quantity) + '</td>' +
                           '<td style="text-align: center;">' +
                           '  <button type="button" class="btn-del" onclick="removeItem(' + index + ')" style="border:none;background:none;color:#ef4444;font-size:18px;cursor:pointer;">×</button>' +
                           '</td>';
            body.appendChild(tr);
        });

        updateTotals();
    }

    function updateItemQty(index, val) {
        let qty = parseInt(val);
        if (isNaN(qty) || qty < 1) qty = 1;
        let item = cart[index];
        if (qty > item.stock) {
            alert('Sản phẩm chỉ còn ' + item.stock + ' chiếc trong kho!');
            qty = item.stock;
        }
        item.quantity = qty;
        renderCart();
    }

    function removeItem(index) {
        cart.splice(index, 1);
        renderCart();
    }

    function updateTotals() {
        let total = 0;
        cart.forEach(item => {
            total += item.price * item.quantity;
        });

        document.getElementById("totalDisplay").textContent = formatMoney(total);
        document.getElementById("totalPriceInput").value = total;

        let itemsPayload = cart.map(item => {
            return {
                variantId: item.variantId,
                quantity: item.quantity,
                unitPrice: item.price
            };
        });
        document.getElementById("itemsPayload").value = JSON.stringify(itemsPayload);
    }

    function validateForm() {
        if (cart.length === 0) {
            alert("Vui lòng chọn ít nhất 01 sản phẩm để tạo đơn hàng!");
            return false;
        }
        return true;
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, "&amp;")
                  .replace(/</g, "&lt;")
                  .replace(/>/g, "&gt;")
                  .replace(/"/g, "&quot;")
                  .replace(/'/g, "&#039;");
    }

    function formatNumber(num) {
        return new Intl.NumberFormat('vi-VN').format(num);
    }

    function formatMoney(amount) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' })
            .format(amount)
            .replace("₫", "₫");
    }
</script>
</body>
</html>
