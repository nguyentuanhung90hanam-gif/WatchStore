<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <style>
        .container { max-width: 750px; margin: auto; padding: 10px 0; }
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
        .page-header h1 { font-size: 26px; font-weight: 700; }
        .back-link { color: #2563eb; text-decoration: none; font-size: 14px; font-weight: 500; }
        .card { background: white; border-radius: 14px; padding: 32px; box-shadow: 0 2px 12px rgba(0,0,0,.07); }
        .card h2 { font-size: 18px; margin-bottom: 24px; color: #1e293b; border-bottom: 2px solid #f0f4ff; padding-bottom: 12px; }
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .form-group { margin-bottom: 4px; }
        .form-group.full { grid-column: 1 / -1; }
        label { display: block; margin-bottom: 7px; font-weight: 600; font-size: 13px; color: #555; }
        .req { color: #e53e3e; }
        input, select, textarea {
            width: 100%; padding: 10px 13px; border: 1.5px solid #e2e8f0;
            border-radius: 8px; font-size: 14px; outline: none; transition: border-color .2s;
            font-family: inherit;
        }
        input:focus, select:focus, textarea:focus { border-color: #2563eb; box-shadow: 0 0 0 3px rgba(37,99,235,.08); }
        textarea { height: 90px; resize: vertical; }
        .hint { font-size: 12px; color: #94a3b8; margin-top: 5px; }
        .actions { display: flex; gap: 12px; margin-top: 28px; }
        .btn { display: inline-flex; align-items: center; gap: 6px; padding: 11px 26px; border: none; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; text-decoration: none; }
        .btn-save   { background: #2563eb; color: white; }
        .btn-save:hover { background: #1d4ed8; }
        .btn-cancel { background: #f1f5f9; color: #475569; }
        .btn-cancel:hover { background: #e2e8f0; }
        .flash-error { background: #fef2f2; border: 1px solid #fca5a5; color: #b91c1c; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; }
        @media (max-width: 600px) { .form-grid { grid-template-columns: 1fr; } .actions { flex-direction: column; } }
    </style>

<div class="container">

    <div class="page-header">
        <div>
            <h1>🛡 Thêm phiếu bảo hành</h1>
        </div>
        <a href="${pageContext.request.contextPath}/manage/sales/warranty" class="back-link">← Quay lại danh sách</a>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div class="flash-error">⚠ ${sessionScope.flash}</div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <div class="card">
        <h2>📋 Thông tin phiếu bảo hành</h2>

        <form method="post" action="${pageContext.request.contextPath}/manage/sales/warranty-add">
            <div class="form-grid">

                <!-- Tìm kiếm đơn hàng nhanh -->
                <div class="form-group full" style="margin-bottom: 10px;">
                    <label for="searchQuery">🔍 Tìm kiếm đơn hàng nhanh <span class="req">*</span></label>
                    <div style="display: flex; gap: 8px;">
                        <input type="text" id="searchQuery" placeholder="Nhập tên khách hàng, số điện thoại hoặc mã đơn hàng..." style="flex: 1;"/>
                        <button type="button" id="btnSearchOrder" class="btn btn-save" style="margin-top: 0; padding: 0 20px; white-space: nowrap; height: 42px;">Tìm kiếm</button>
                    </div>
                    <div class="hint">Nhập thông tin rồi nhấn Tìm kiếm để lọc danh sách đơn hàng liên kết bên dưới.</div>
                </div>

                <div class="form-group">
                    <label for="orderId">Đơn hàng liên kết <span class="req">*</span></label>
                    <select id="orderId" name="orderId" required disabled style="background-color: #f1f5f9; cursor: not-allowed;">
                        <option value="">-- Vui lòng tìm kiếm đơn hàng trước --</option>
                    </select>
                    <div class="hint">Chọn đơn hàng mà phiếu bảo hành này thuộc về.</div>
                </div>

                <div class="form-group">
                    <label for="productName">Tên sản phẩm <span class="req">*</span></label>
                    <select id="productName" name="productName" required>
                        <option value="">-- Chọn đơn hàng trước --</option>
                    </select>
                    <div id="allProductsWarrantedWarning" style="display:none; color:#dc2626; font-size:12px; font-weight:600; margin-top:5px;">⚠️ Đơn hàng này đã có phiếu bảo hành cho tất cả các sản phẩm!</div>
                    <div class="hint">Sản phẩm thuộc đơn hàng liên kết đã chọn.</div>
                </div>

                <!-- Thẻ thông tin khách hàng liên kết -->
                <div class="form-group full" id="customerInfoCard" style="display: none; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 15px; margin: 10px 0;">
                    <h3 style="margin-top: 0; font-size: 14px; color: #334155; border-bottom: 1px solid #e2e8f0; padding-bottom: 6px;">👤 Thông tin khách hàng liên kết</h3>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; font-size: 13px; color: #475569;">
                        <div><strong>Khách hàng:</strong> <span id="infoCustomerName">-</span></div>
                        <div><strong>Số điện thoại:</strong> <span id="infoCustomerPhone">-</span></div>
                    </div>
                </div>

                <div class="form-group">
                    <label for="serial">Số Serial</label>
                    <input type="text" id="serial" name="serial" placeholder="Ví dụ: RLX-2024-001234"/>
                    <div class="hint">Số serial trên vỏ/đáy đồng hồ.</div>
                </div>

                <div class="form-group">
                    <label for="months">Thời hạn bảo hành (tháng) <span class="req">*</span></label>
                    <select id="months" name="months" required>
                        <option value="6">6 tháng</option>
                        <option value="12" selected>12 tháng</option>
                        <option value="18">18 tháng</option>
                        <option value="24">24 tháng</option>
                        <option value="36">36 tháng (3 năm)</option>
                        <option value="60">60 tháng (5 năm)</option>
                    </select>
                </div>

                <div class="form-group full">
                    <label for="note">Ghi chú</label>
                    <textarea id="note" name="note" placeholder="Ghi chú thêm về tình trạng sản phẩm khi bắt đầu bảo hành..."></textarea>
                </div>

            </div>

            <div class="actions">
                <button type="submit" class="btn btn-save">💾 Tạo phiếu bảo hành</button>
                <a href="${pageContext.request.contextPath}/manage/sales/warranty" class="btn btn-cancel">Hủy</a>
            </div>
        </form>
    </div>

</div>

<script>
document.addEventListener("DOMContentLoaded", function() {
    const searchInput = document.getElementById("searchQuery");
    const searchBtn = document.getElementById("btnSearchOrder");
    const orderSelect = document.getElementById("orderId");
    const productSelect = document.getElementById("productName");
    const customerCard = document.getElementById("customerInfoCard");
    const infoName = document.getElementById("infoCustomerName");
    const infoPhone = document.getElementById("infoCustomerPhone");
    const warningDiv = document.getElementById("allProductsWarrantedWarning");

    let currentOrderItems = [];
    let currentOrderCode = "";

    searchInput.addEventListener("keypress", function(e) {
        if (e.key === "Enter") {
            e.preventDefault();
            searchBtn.click();
        }
    });

    searchBtn.addEventListener("click", function() {
        const query = searchInput.value.trim();
        if (!query) {
            alert("Vui lòng nhập tên khách hàng hoặc số điện thoại để tìm kiếm đơn hàng.");
            return;
        }
        searchBtn.disabled = true;
        searchBtn.innerHTML = "⌛ Đang tìm...";
        
        fetch(`${pageContext.request.contextPath}/manage/sales/api/orders-search?query=` + encodeURIComponent(query))
            .then(res => res.json())
            .then(data => {
                orderSelect.innerHTML = '<option value="">-- Chọn đơn hàng --</option>';
                if (data.error) {
                    alert("Lỗi: " + data.error);
                } else if (data.length === 0) {
                    alert("Không tìm thấy đơn hàng nào khớp với thông tin tìm kiếm.");
                    orderSelect.setAttribute("disabled", "true");
                    orderSelect.style.backgroundColor = "#f1f5f9";
                    orderSelect.style.cursor = "not-allowed";
                } else {
                    orderSelect.removeAttribute("disabled");
                    orderSelect.style.backgroundColor = "";
                    orderSelect.style.cursor = "";
                    data.forEach(order => {
                        const opt = document.createElement("option");
                        opt.value = order.id;
                        opt.textContent = order.code + " – " + order.customerName + " (" + order.phone + ")";
                        orderSelect.appendChild(opt);
                    });
                }
            })
            .catch(err => {
                console.error(err);
                alert("Đã xảy ra lỗi khi tìm kiếm đơn hàng.");
            })
            .finally(() => {
                searchBtn.disabled = false;
                searchBtn.innerHTML = "Tìm kiếm";
                productSelect.innerHTML = '<option value="">-- Chọn đơn hàng trước --</option>';
                customerCard.style.display = "none";
                warningDiv.style.display = "none";
                currentOrderItems = [];
                currentOrderCode = "";
                document.getElementById("serial").value = "";
            });
    });

    orderSelect.addEventListener("change", function() {
        const orderId = orderSelect.value;
        if (!orderId) {
            productSelect.innerHTML = '<option value="">-- Chọn đơn hàng trước --</option>';
            customerCard.style.display = "none";
            warningDiv.style.display = "none";
            currentOrderItems = [];
            currentOrderCode = "";
            document.getElementById("serial").value = "";
            return;
        }

        fetch(`${pageContext.request.contextPath}/manage/sales/api/order-details?orderId=` + orderId)
            .then(res => res.json())
            .then(data => {
                warningDiv.style.display = "none";
                if (data.error) {
                    alert("Lỗi: " + data.error);
                    productSelect.innerHTML = '<option value="">-- Lỗi tải sản phẩm --</option>';
                    customerCard.style.display = "none";
                    currentOrderItems = [];
                    currentOrderCode = "";
                } else {
                    infoName.textContent = data.customerName || "N/A";
                    infoPhone.textContent = data.phone || "N/A";
                    customerCard.style.display = "block";
                    currentOrderItems = data.items || [];
                    currentOrderCode = data.code || "";

                    productSelect.innerHTML = '<option value="">-- Chọn sản phẩm --</option>';
                    if (data.items && data.items.length > 0) {
                        let allWarranted = true;
                        data.items.forEach(item => {
                            const opt = document.createElement("option");
                            const name = item.variantName ? item.variantName : item.productName;
                            opt.value = name;
                            if (item.hasWarranty) {
                                opt.textContent = name + " (Đã có phiếu bảo hành)";
                                opt.disabled = true;
                                opt.style.color = "#94a3b8";
                            } else {
                                opt.textContent = name;
                                allWarranted = false;
                            }
                            productSelect.appendChild(opt);
                        });
                        if (allWarranted) {
                            warningDiv.style.display = "block";
                        }
                    } else {
                        productSelect.innerHTML = '<option value="">-- Đơn hàng không có sản phẩm --</option>';
                    }
                }
            })
            .catch(err => {
                console.error(err);
                alert("Đã xảy ra lỗi khi lấy thông tin đơn hàng.");
                productSelect.innerHTML = '<option value="">-- Lỗi tải sản phẩm --</option>';
                customerCard.style.display = "none";
                warningDiv.style.display = "none";
                currentOrderItems = [];
                currentOrderCode = "";
            })
            .finally(() => {
                document.getElementById("serial").value = "";
            });
    });

    productSelect.addEventListener("change", function() {
        const productName = productSelect.value;
        const serialInput = document.getElementById("serial");
        if (!productName) {
            serialInput.value = "";
            return;
        }
        const item = currentOrderItems.find(i => {
            const name = i.variantName ? i.variantName : i.productName;
            return name === productName;
        });
        if (item) {
            let sku = item.sku && item.sku !== "null" && item.sku.trim() !== "" 
                ? item.sku 
                : productName.toUpperCase().replace(/[^A-Z0-9]/g, "-").replace(/-+/g, "-");
            serialInput.value = "SN-" + sku.toUpperCase() + "-" + currentOrderCode.toUpperCase();
        } else {
            serialInput.value = "";
        }
    });
});
</script>
