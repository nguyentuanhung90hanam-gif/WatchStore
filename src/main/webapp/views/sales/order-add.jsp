<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<style>
    * { box-sizing: border-box; }
    body { margin: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f4f6f9; color: #333; }
    .container { max-width: 850px; margin: auto; padding: 30px; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
    .header h1 { margin: 0; font-size: 24px; font-weight: 700; color: #1e293b; }
    .back { color: #2563eb; text-decoration: none; font-size: 14px; font-weight: 500; }
    .back:hover { text-decoration: underline; }
    
    .box { background: white; padding: 25px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,.05); border: 1px solid #e2e8f0; margin-bottom: 20px; }
    .box h2 { margin-top: 0; font-size: 18px; margin-bottom: 20px; color: #0f172a; border-bottom: 2px solid #f1f5f9; padding-bottom: 10px; }
    
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    .form-group { margin-bottom: 15px; }
    .form-group.full { grid-column: 1 / -1; }
    
    label { display: block; margin-bottom: 8px; font-weight: 600; font-size: 13px; color: #475569; }
    input, select, textarea {
        width: 100%; padding: 10px 12px; border: 1px solid #cbd5e1;
        border-radius: 6px; font-size: 14px; outline: none; transition: all .2s;
        height: 42px; background-color: #fff;
    }
    input:focus, select:focus, textarea:focus { border-color: #2563eb; box-shadow: 0 0 0 3px rgba(37,99,235,0.15); }
    
    .required-mark { color: #e11d48; }
    
    /* SUGGESTION STYLES */
    .search-wrapper { position: relative; }
    .suggestions-box {
        display: none; position: absolute; top: 100%; left: 0; right: 0;
        background: white; border: 1px solid #cbd5e1; border-radius: 6px;
        box-shadow: 0 10px 25px rgba(0,0,0,0.1); z-index: 1000;
        max-height: 280px; overflow-y: auto; margin-top: 5px;
    }
    .suggestion-item {
        display: flex; align-items: center; padding: 10px 15px;
        border-bottom: 1px solid #f1f5f9; cursor: pointer; transition: background .15s;
    }
    .suggestion-item:hover { background: #f8fafc; }
    .suggestion-item:last-child { border-bottom: none; }
    .suggestion-item img { width: 40px; height: 40px; border-radius: 4px; object-fit: cover; margin-right: 12px; border: 1px solid #e2e8f0; }
    .suggestion-details { flex-grow: 1; display: flex; flex-direction: column; }
    .suggestion-name { font-size: 13px; font-weight: 600; color: #1e293b; }
    .suggestion-sku { font-size: 11px; color: #64748b; margin-top: 2px; }
    .suggestion-meta { display: flex; justify-content: space-between; font-size: 12px; margin-top: 4px; }
    .suggestion-price { font-weight: 600; color: #0f172a; }
    .suggestion-stock { font-weight: 500; }
    .stock-ok { color: #16a34a; }
    .stock-out { color: #dc2626; font-weight: bold; }
    
    /* CART TABLE */
    .cart-table-wrapper { width: 100%; overflow-x: auto; margin-top: 15px; border-radius: 8px; border: 1px solid #e2e8f0; }
    .cart-table { width: 100%; border-collapse: collapse; text-align: left; font-size: 13px; min-width: 600px; }
    .cart-table th, .cart-table td { padding: 12px; border-bottom: 1px solid #e2e8f0; }
    .cart-table th { background: #f8fafc; color: #475569; font-weight: 600; }
    .cart-table td { vertical-align: middle; }
    .cart-table img { width: 45px; height: 45px; object-fit: cover; border-radius: 6px; border: 1px solid #e2e8f0; }
    .cart-qty-input { width: 65px; height: 32px; text-align: center; padding: 4px; border: 1px solid #cbd5e1; border-radius: 4px; }
    .btn-remove { background: #fef2f2; color: #dc2626; border: 1px solid #fee2e2; border-radius: 4px; padding: 5px 8px; cursor: pointer; font-size: 11px; font-weight: 600; transition: all .15s; }
    .btn-remove:hover { background: #fee2e2; color: #b91c1c; }
    
    /* TOTAL STYLES */
    .total-summary-box {
        display: flex; justify-content: flex-end; align-items: center;
        margin-top: 20px; padding: 15px; background: #f8fafc;
        border-radius: 8px; border: 1px solid #e2e8f0; font-size: 16px;
    }
    .total-title { font-weight: 500; color: #475569; margin-right: 15px; }
    .total-value { font-weight: 700; color: #2563eb; font-size: 20px; }

    .buttons { display: flex; gap: 12px; margin-top: 25px; }
    .btn {
        height: 44px; padding: 0 24px; border: none; border-radius: 6px;
        cursor: pointer; font-size: 14px; font-weight: 600; text-decoration: none;
        display: inline-flex; align-items: center; justify-content: center; transition: all .15s;
    }
    .btn-save { background: #2563eb; color: white; }
    .btn-save:hover { background: #1d4ed8; }
    .btn-back { background: #e2e8f0; color: #333; border: 1px solid #cbd5e1; }
    .btn-back:hover { background: #cbd5e1; }
    
    .flash-error {
        background: #fef2f2; border: 1px solid #fca5a5; color: #b91c1c;
        padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-size: 14px;
    }
    
    .empty-cart-msg { text-align: center; padding: 30px; color: #64748b; font-style: italic; }
</style>

<div class="container">

    <div class="header">
        <div>
            <h1> Tạo đơn hàng mới</h1>
        </div>
        <a class="back" href="${pageContext.request.contextPath}/manage/sales/orders">← Quay lại danh sách</a>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div class="flash-error">${sessionScope.flash}</div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <form id="orderForm" method="post" action="${pageContext.request.contextPath}/manage/sales/order-add">
        
        <!-- THÔNG TIN KHÁCH HÀNG -->
        <div class="box">
            <h2>1. THÔNG TIN KHÁCH HÀNG</h2>
            
            <div class="form-group">
                <label for="customerSelect">Chọn khách hàng từ hệ thống</label>
                <select id="customerSelect" name="customerId">
                    <option value="new">-- Khách hàng mới (Nhập thông tin bên dưới) --</option>
                    <c:forEach var="c" items="${customers}">
                        <option value="${c.id}" data-name="${c.fullName}" data-phone="${c.phone}" data-address="${c.address}">${c.fullName} - ${c.phone}</option>
                    </c:forEach>
                </select>
            </div>
            
            <div class="form-grid">
                <div class="form-group">
                    <label for="customerName">Tên khách hàng <span class="required-mark">*</span></label>
                    <input type="text" id="customerName" name="customerName" placeholder="Nhập tên khách hàng" required>
                </div>

                <div class="form-group">
                    <label for="phone">Số điện thoại <span class="required-mark">*</span></label>
                    <input type="tel" id="phone" name="phone" placeholder="Ví dụ: 0988666888" required oninput="this.value = this.value.replace(/[^0-9]/g, '')" pattern="[0-9]{9,11}" title="Số điện thoại phải từ 9 đến 11 chữ số và chỉ gồm số">
                </div>

                <div class="form-group full">
                    <label for="shippingAddress">Địa chỉ giao hàng <span class="required-mark">*</span></label>
                    <input type="text" id="shippingAddress" name="shippingAddress" placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành..." required>
                </div>
            </div>
        </div>
        
        <!-- SẢN PHẨM TRONG ĐƠN HÀNG -->
        <div class="box">
            <h2>2. SẢN PHẨM TRONG ĐƠN HÀNG</h2>
            
            <div class="form-group full search-wrapper">
                <label for="productSearch"> Tìm kiếm sản phẩm theo tên hoặc mã sản phẩm</label>
                <input type="text" id="productSearch" placeholder="Nhập tên sản phẩm hoặc mã SKU..." autocomplete="off">
                <div id="suggestions" class="suggestions-box"></div>
            </div>
            
            <div class="cart-table-wrapper">
                <table class="cart-table">
                    <thead>
                        <tr>
                            <th style="width: 70px;">Ảnh</th>
                            <th>Sản phẩm</th>
                            <th style="width: 140px;">Mã SKU</th>
                            <th style="width: 110px; text-align: right;">Giá bán</th>
                            <th style="width: 100px; text-align: center;">Tồn kho</th>
                            <th style="width: 90px; text-align: center;">Số lượng</th>
                            <th style="width: 120px; text-align: right;">Thành tiền</th>
                            <th style="width: 60px; text-align: center;">Xóa</th>
                        </tr>
                    </thead>
                    <tbody id="cartItemsContainer">
                        <!-- Sản phẩm được chọn sẽ tự động sinh mã HTML ở đây -->
                    </tbody>
                </table>
                <div id="emptyCartMsg" class="empty-cart-msg">Chưa có sản phẩm nào được chọn trong đơn hàng.</div>
            </div>
            
            <!-- TỔNG TIỀN -->
            <div class="total-summary-box">
                <span class="total-title">Tổng cộng:</span>
                <span id="displayTotal" class="total-value">0 đ</span>
            </div>
        </div>
        
        <!-- TRẠNG THÁI & HÌNH THỨC -->
        <div class="box">
            <h2>3. TRẠNG THÁI ĐƠN HÀNG</h2>
            <div class="form-grid">
                <div class="form-group">
                    <label for="status">Trạng thái đơn hàng <span class="required-mark">*</span></label>
                    <select id="status" name="status" required>
                        <option value="PENDING">Chờ xử lý / Chờ xác nhận</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="paymentStatus">Trạng thái thanh toán <span class="required-mark">*</span></label>
                    <select id="paymentStatus" name="paymentStatus" required>
                        <option value="UNPAID">Chưa thanh toán</option>
                        <option value="PAID">Đã thanh toán</option>
                    </select>
                </div>
            </div>
        </div>
        
        <!-- GIỎ HÀNG ẨN GỬI ĐI -->
        <input type="hidden" name="cartItemsJson" id="cartItemsJson" value="[]">

        <div class="buttons">
            <button type="submit" class="btn btn-save"> Tạo đơn hàng</button>
            <a href="${pageContext.request.contextPath}/manage/sales/orders" class="btn btn-back">Hủy</a>
        </div>
        
    </form>

</div>

<script>
    // Trạng thái giỏ hàng trong bộ nhớ
    let cart = [];

    // Lắng nghe sự kiện chọn khách hàng có sẵn
    document.getElementById('customerSelect').addEventListener('change', function() {
        const option = this.options[this.selectedIndex];
        if (this.value === 'new') {
            document.getElementById('customerName').value = '';
            document.getElementById('phone').value = '';
            document.getElementById('shippingAddress').value = '';
        } else {
            document.getElementById('customerName').value = option.getAttribute('data-name') || '';
            document.getElementById('phone').value = option.getAttribute('data-phone') || '';
            document.getElementById('shippingAddress').value = option.getAttribute('data-address') || '';
        }
    });

    // Tìm kiếm sản phẩm Ajax
    const productSearchInput = document.getElementById('productSearch');
    const suggestionsBox = document.getElementById('suggestions');

    let debounceTimer;
    productSearchInput.addEventListener('input', function() {
        clearTimeout(debounceTimer);
        const query = this.value.trim();
        
        if (query.length < 1) {
            suggestionsBox.style.display = 'none';
            return;
        }

        debounceTimer = setTimeout(() => {
            fetch(`${pageContext.request.contextPath}/manage/sales/api/products-search?query=` + encodeURIComponent(query))
                .then(res => res.json())
                .then(data => {
                    renderSuggestions(data);
                })
                .catch(err => {
                    console.error('Lỗi tải sản phẩm:', err);
                });
        }, 200);
    });

    // Đóng danh sách gợi ý khi bấm ra ngoài
    document.addEventListener('click', function(e) {
        if (e.target !== productSearchInput) {
            suggestionsBox.style.display = 'none';
        }
    });

    // Kết xuất gợi ý
    function renderSuggestions(products) {
        if (!products || products.length === 0) {
            suggestionsBox.innerHTML = '<div style="padding: 12px; color: #888; font-size: 13px; text-align: center;">Không tìm thấy sản phẩm phù hợp.</div>';
            suggestionsBox.style.display = 'block';
            return;
        }

        suggestionsBox.innerHTML = '';
        products.forEach(p => {
            const item = document.createElement('div');
            item.className = 'suggestion-item';
            
            const isOutOfStock = p.stock <= 0;
            const stockHtml = isOutOfStock 
                ? '<span class="suggestion-stock stock-out">Hết hàng</span>' 
                : '<span class="suggestion-stock stock-ok">Còn: ' + p.stock + ' sản phẩm</span>';

            const imgUrl = p.imageUrl ? p.imageUrl : '${pageContext.request.contextPath}/assets/images/default-watch.jpg';

            item.innerHTML = 
                '<img src="' + imgUrl + '" alt="watch">' +
                '<div class="suggestion-details">' +
                    '<span class="suggestion-name">' + p.productName + ' (' + p.variantName + ')</span>' +
                    '<span class="suggestion-sku">SKU: ' + p.sku + '</span>' +
                    '<div class="suggestion-meta">' +
                        '<span class="suggestion-price">' + formatMoney(p.salePrice) + '</span>' +
                        stockHtml +
                    '</div>' +
                '</div>';

            if (!isOutOfStock) {
                item.addEventListener('click', () => {
                    addProductToCart(p);
                    productSearchInput.value = '';
                    suggestionsBox.style.display = 'none';
                });
            } else {
                item.style.opacity = '0.5';
                item.style.cursor = 'not-allowed';
            }

            suggestionsBox.appendChild(item);
        });
        suggestionsBox.style.display = 'block';
    }

    // Thêm sản phẩm vào giỏ hàng
    function addProductToCart(product) {
        const existing = cart.find(item => item.variantId === product.variantId);
        
        if (existing) {
            if (existing.quantity >= product.stock) {
                alert(`Không thể thêm. Số lượng đã đạt giới hạn tồn kho (${product.stock}).`);
                return;
            }
            existing.quantity += 1;
        } else {
            cart.push({
                variantId: product.variantId,
                productName: product.productName,
                variantName: product.variantName,
                sku: product.sku,
                price: product.salePrice,
                stock: product.stock,
                imageUrl: product.imageUrl,
                quantity: 1
            });
        }
        
        updateCartUI();
    }

    // Cập nhật giao diện giỏ hàng
    function updateCartUI() {
        const container = document.getElementById('cartItemsContainer');
        const emptyMsg = document.getElementById('emptyCartMsg');
        
        if (cart.length === 0) {
            container.innerHTML = '';
            emptyMsg.style.display = 'block';
            document.getElementById('displayTotal').innerText = '0 đ';
            document.getElementById('cartItemsJson').value = '[]';
            return;
        }

        emptyMsg.style.display = 'none';
        container.innerHTML = '';

        let total = 0;

        cart.forEach((item, index) => {
            const lineTotal = item.price * item.quantity;
            total += lineTotal;

            const tr = document.createElement('tr');
            const imgUrl = item.imageUrl ? item.imageUrl : '${pageContext.request.contextPath}/assets/images/default-watch.jpg';

            tr.innerHTML = 
                '<td><img src="' + imgUrl + '" alt="product"></td>' +
                '<td>' +
                    '<div style="font-weight: 600; color: #1e293b;">' + item.productName + '</div>' +
                    '<div style="font-size: 11px; color: #64748b; margin-top: 2px;">' + item.variantName + '</div>' +
                '</td>' +
                '<td style="font-family: monospace; font-size: 12px; color: #334155;">' + item.sku + '</td>' +
                '<td style="text-align: right; font-weight: 500; color: #0f172a;">' + formatMoney(item.price) + '</td>' +
                '<td style="text-align: center; color: #475569; font-weight: 500;">' + item.stock + '</td>' +
                '<td style="text-align: center;">' +
                    '<input type="number" class="cart-qty-input" min="1" max="' + item.stock + '" value="' + item.quantity + '" ' +
                           'onchange="updateQuantity(' + index + ', this.value)">' +
                '</td>' +
                '<td style="text-align: right; font-weight: 700; color: #2563eb;">' + formatMoney(lineTotal) + '</td>' +
                '<td style="text-align: center;">' +
                    '<button type="button" class="btn-remove" onclick="removeCartItem(' + index + ')">Xóa</button>' +
                '</td>';
            container.appendChild(tr);
        });

        document.getElementById('displayTotal').innerText = formatMoney(total);
        
        // Cập nhật giá trị gửi đi
        const submitList = cart.map(item => ({
            variantId: item.variantId,
            quantity: item.quantity
        }));
        document.getElementById('cartItemsJson').value = JSON.stringify(submitList);
    }

    // Cập nhật số lượng
    window.updateQuantity = function(index, value) {
        const qty = parseInt(value);
        const item = cart[index];
        
        if (isNaN(qty) || qty < 1) {
            item.quantity = 1;
        } else if (qty > item.stock) {
            alert(`Số lượng yêu cầu vượt quá số lượng còn lại trong kho (${item.stock}).`);
            item.quantity = item.stock;
        } else {
            item.quantity = qty;
        }
        
        updateCartUI();
    };

    // Xóa sản phẩm
    window.removeCartItem = function(index) {
        cart.splice(index, 1);
        updateCartUI();
    };

    // Hàm định dạng tiền
    function formatMoney(amount) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount).replace('₫', 'đ');
    }

    // Kiểm tra tính hợp lệ trước khi gửi
    document.getElementById('orderForm').addEventListener('submit', function(e) {
        if (cart.length === 0) {
            e.preventDefault();
            alert('Vui lòng thêm ít nhất một sản phẩm vào đơn hàng để tiếp tục.');
        }
    });
</script>
