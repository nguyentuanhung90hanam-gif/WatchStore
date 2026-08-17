<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div class="dashboard-card" style="max-width:950px; margin:0 auto; padding:28px; background:#fff; border-radius:12px; box-shadow:0 4px 20px rgba(0,0,0,0.05);">

    <div style="display:flex; justify-content:between; align-items:center; border-bottom:1px solid #f1f5f9; padding-bottom:16px; margin-bottom:24px;">
        <div>
            <span style="font-size:12px; text-transform:uppercase; color:#888; font-weight:700; letter-spacing:1px;">Thông tin chi tiết</span>
            <h2 style="margin:4px 0 0 0; font-size:24px; color:#1e293b;">🔍 ${product.productName}</h2>
        </div>
        <div style="margin-left:auto;">
            <a href="${pageContext.request.contextPath}/manage/admin/products" class="button" style="background:#f1f5f9; color:#475569; text-decoration:none; padding:10px 18px; border-radius:6px; font-weight:600; display:inline-flex; align-items:center; gap:6px;">
                ← Quay lại danh sách
            </a>
        </div>
    </div>

    <div style="display:grid; grid-template-columns: 1fr 1fr; gap:32px;">
        
        <!-- Left Side: Basic Info & Description -->
        <div>
            <div style="background:#fafafa; border-radius:8px; padding:20px; border:1px solid #f1f5f9; margin-bottom:24px;">
                <h3 style="margin-top:0; font-size:16px; color:#334155; border-bottom:1px solid #e2e8f0; padding-bottom:8px; margin-bottom:12px;">📊 Thông tin chung</h3>
                
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-bottom:12px;">
                    <div>
                        <span style="font-size:12px; color:#64748b;">Mã sản phẩm:</span>
                        <div style="font-weight:600; color:#1e293b; font-size:14px; margin-top:2px;">${product.productCode}</div>
                    </div>
                    <div>
                        <span style="font-size:12px; color:#64748b;">SKU Biến thể:</span>
                        <div style="font-weight:600; color:#1e293b; font-size:14px; margin-top:2px;">${product.sku != null ? product.sku : 'N/A'}</div>
                    </div>
                </div>

                <div style="display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-bottom:12px;">
                    <div>
                        <span style="font-size:12px; color:#64748b;">Giá bán:</span>
                        <div style="font-weight:700; color:#b91c1c; font-size:18px; margin-top:2px;">
                            <fmt:formatNumber value="${product.price}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                        </div>
                    </div>
                    <div>
                        <span style="font-size:12px; color:#64748b;">Số lượng tồn kho:</span>
                        <div style="font-weight:700; color:#16a34a; font-size:18px; margin-top:2px;">
                            ${product.stock} chiếc
                        </div>
                    </div>
                </div>

                <div style="display:grid; grid-template-columns:1fr 1fr; gap:16px;">
                    <div>
                        <span style="font-size:12px; color:#64748b;">Thương hiệu:</span>
                        <div style="font-weight:600; color:#1e293b; font-size:14px; margin-top:2px;">${product.brand}</div>
                    </div>
                    <div>
                        <span style="font-size:12px; color:#64748b;">Danh mục:</span>
                        <div style="font-weight:600; color:#1e293b; font-size:14px; margin-top:2px;">${product.categoryName}</div>
                    </div>
                </div>
            </div>

            <div>
                <h3 style="font-size:16px; color:#334155; margin-bottom:10px;">📝 Mô tả sản phẩm</h3>
                <div style="color:#475569; font-size:14px; line-height:1.6; background:#fff; border:1px solid #e2e8f0; border-radius:8px; padding:16px; min-height:120px; overflow-y:auto;">
                    ${empty product.description ? 'Chưa có mô tả chi tiết cho sản phẩm này.' : product.description}
                </div>
            </div>
        </div>

        <!-- Right Side: Technical Specifications Sheet -->
        <div>
            <div style="background:#fff; border:1px solid #e2e8f0; border-radius:8px; padding:20px;">
                <h3 style="margin-top:0; font-size:16px; color:#334155; border-bottom:1px solid #e2e8f0; padding-bottom:8px; margin-bottom:16px;">⚙ Thông số kỹ thuật</h3>
                
                <table style="width:100%; border-collapse:collapse; font-size:14px; color:#334155;">
                    <tr style="border-bottom:1px solid #f1f5f9;">
                        <td style="padding:10px 0; font-weight:600; color:#64748b; width:40%;">Kiểu máy (Movement):</td>
                        <td style="padding:10px 0; font-weight:600; color:#1e293b;">${product.movementType}</td>
                    </tr>
                    <tr style="border-bottom:1px solid #f1f5f9;">
                        <td style="padding:10px 0; font-weight:600; color:#64748b;">Giới tính:</td>
                        <td style="padding:10px 0; font-weight:600; color:#1e293b;">${product.gender}</td>
                    </tr>
                    <tr style="border-bottom:1px solid #f1f5f9;">
                        <td style="padding:10px 0; font-weight:600; color:#64748b;">Chất liệu kính:</td>
                        <td style="padding:10px 0; font-weight:600; color:#1e293b;">${empty product.glassMaterial ? 'Kính cứng Mineral' : product.glassMaterial}</td>
                    </tr>
                    <tr style="border-bottom:1px solid #f1f5f9;">
                        <td style="padding:10px 0; font-weight:600; color:#64748b;">Chất liệu vỏ:</td>
                        <td style="padding:10px 0; font-weight:600; color:#1e293b;">${empty product.caseMaterial ? 'Thép không gỉ 316L' : product.caseMaterial}</td>
                    </tr>
                    <tr style="border-bottom:1px solid #f1f5f9;">
                        <td style="padding:10px 0; font-weight:600; color:#64748b;">Chất liệu dây:</td>
                        <td style="padding:10px 0; font-weight:600; color:#1e293b;">${empty product.strapMaterial ? 'Dây kim loại' : product.strapMaterial}</td>
                    </tr>
                    <tr style="border-bottom:1px solid #f1f5f9;">
                        <td style="padding:10px 0; font-weight:600; color:#64748b;">Độ chống nước:</td>
                        <td style="padding:10px 0; font-weight:600; color:#1e293b;">${empty product.waterResistance ? '5 ATM' : product.waterResistance}</td>
                    </tr>
                    <tr style="border-bottom:1px solid #f1f5f9;">
                        <td style="padding:10px 0; font-weight:600; color:#64748b;">Xuất xứ thương hiệu:</td>
                        <td style="padding:10px 0; font-weight:600; color:#1e293b;">${empty product.originCountry ? 'Nhật Bản' : product.originCountry}</td>
                    </tr>
                    <tr style="border-bottom:1px solid #f1f5f9;">
                        <td style="padding:10px 0; font-weight:600; color:#64748b;">Thời gian bảo hành:</td>
                        <td style="padding:10px 0; font-weight:600; color:#1e293b;">${product.warrantyMonths} tháng</td>
                    </tr>
                    <tr>
                        <td style="padding:10px 0; font-weight:600; color:#64748b;">Trạng thái hiển thị:</td>
                        <td style="padding:10px 0; font-weight:600;">
                            <span style="background:${product.status == 'ACTIVE' ? '#e1f5fe; color:#0288d1;' : '#ffe0b2; color:#f57c00;'} padding:3px 8px; border-radius:10px; font-size:11px; font-weight:600; display:inline-block;">
                                ${product.status}
                            </span>
                        </td>
                    </tr>
                </table>
            </div>
        </div>

    </div>

</div>
