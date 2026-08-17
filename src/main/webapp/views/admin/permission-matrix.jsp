<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<style>
    .perm-container {
        max-width: 1100px;
        margin: 0 auto;
    }
    .perm-header-card {
        background: #fff;
        border-radius: 12px;
        padding: 24px;
        box-shadow: 0 1px 4px rgba(0,0,0,0.06);
        margin-bottom: 24px;
        border: 1px solid #e2e8f0;
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 20px;
    }
    .perm-user-info {
        display: flex;
        align-items: center;
        gap: 16px;
    }
    .perm-avatar {
        width: 52px;
        height: 52px;
        border-radius: 50%;
        background: linear-gradient(135deg, #d4af37, #b8860b);
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        font-weight: 700;
        box-shadow: 0 4px 10px rgba(184,134,11,0.25);
    }
    .perm-user-title {
        font-size: 20px;
        font-weight: 700;
        color: #1e293b;
        margin: 0 0 4px;
    }
    .perm-user-meta {
        font-size: 14px;
        color: #64748b;
        display: flex;
        gap: 12px;
        align-items: center;
    }
    .badge-role-emp {
        display: inline-block;
        padding: 3px 10px;
        background: #eff6ff;
        color: #1d4ed8;
        font-weight: 600;
        font-size: 12px;
        border-radius: 20px;
        border: 1px solid #bfdbfe;
    }
    .perm-section-card {
        background: #fff;
        border-radius: 12px;
        border: 1px solid #e2e8f0;
        box-shadow: 0 1px 4px rgba(0,0,0,0.05);
        margin-bottom: 20px;
        overflow: hidden;
    }
    .perm-section-header {
        padding: 16px 20px;
        background: #f8fafc;
        border-bottom: 1px solid #e2e8f0;
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
    .perm-section-title {
        font-size: 15px;
        font-weight: 700;
        color: #1e293b;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .perm-section-header.system-header {
        background: #fef2f2;
        border-bottom-color: #fecaca;
    }
    .perm-section-header.system-header .perm-section-title {
        color: #991b1b;
    }
    .perm-group-list {
        padding: 8px 20px;
    }
    .perm-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 14px 0;
        border-bottom: 1px solid #f1f5f9;
        gap: 15px;
    }
    .perm-item:last-child {
        border-bottom: none;
    }
    .perm-item-info {
        flex: 1;
    }
    .perm-item-name {
        font-size: 15px;
        font-weight: 600;
        color: #1e293b;
        margin-bottom: 3px;
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .perm-item-desc {
        font-size: 13px;
        color: #64748b;
    }
    .perm-item-controls {
        display: flex;
        align-items: center;
        gap: 12px;
        flex-wrap: wrap;
    }
    .perm-checkbox-label {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        font-weight: 500;
        color: #334155;
        cursor: pointer;
        padding: 6px 12px;
        border-radius: 6px;
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        transition: all 0.2s;
    }
    .perm-checkbox-label:hover {
        background: #f1f5f9;
        border-color: #cbd5e1;
    }
    .perm-checkbox-label input[type="checkbox"] {
        width: 17px;
        height: 17px;
        cursor: pointer;
        accent-color: #b8860b;
    }
    .perm-locked-tag {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 6px 14px;
        background: #fee2e2;
        color: #991b1b;
        font-weight: 700;
        font-size: 12px;
        border-radius: 6px;
        border: 1px solid #fca5a5;
    }
    .perm-save-bar {
        position: sticky;
        bottom: 20px;
        background: rgba(255, 255, 255, 0.95);
        backdrop-filter: blur(8px);
        border: 1px solid #cbd5e1;
        border-radius: 12px;
        padding: 16px 24px;
        box-shadow: 0 8px 24px rgba(0,0,0,0.12);
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 25px;
        z-index: 100;
    }
</style>

<div class="perm-container">

    <div class="module-heading" style="margin-bottom: 20px;">
        <div class="module-title-area">
            <p class="eyebrow dark">EMPLOYEE PERMISSION MATRIX</p>
            <h2>PHÂN QUYỀN NHÂN VIÊN</h2>
            <p class="module-desc">
                Cấu hình phạm vi chức năng cho tài khoản Nhân viên Sales. Nhóm Hệ Thống bị khóa vĩnh viễn (Chỉ dành riêng cho Admin).
            </p>
        </div>
    </div>

    <c:if test="${not empty sessionScope.flash}">
        <div style="margin-bottom: 20px; padding: 14px 18px; background: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 8px; color: #065f46; font-weight: 600; display: flex; align-items: center; gap: 8px;">
            <span>✓</span> ${sessionScope.flash}
        </div>
        <c:remove var="flash" scope="session" />
    </c:if>

    <!-- THÔNG TIN NHÂN VIÊN SALES ĐƯỢC CHỌN -->
    <div class="perm-header-card">
        <div class="perm-user-info">
            <div class="perm-avatar">
                ${empty selectedEmployee.fullName ? 'S' : fn:substring(selectedEmployee.fullName, 0, 1)}
            </div>
            <div>
                <h3 class="perm-user-title">
                    ${empty selectedEmployee.fullName ? 'Nhân viên Sales' : selectedEmployee.fullName}
                </h3>
                <div class="perm-user-meta">
                    <span>📧 <strong>${empty selectedEmployee.email ? 'sales@watchstore.vn' : selectedEmployee.email}</strong></span>
                    <c:if test="${not empty selectedEmployee.phone}">
                        <span>• 📞 ${selectedEmployee.phone}</span>
                    </c:if>
                    <span class="badge-role-emp">Role: EMPLOYEE</span>
                </div>
            </div>
        </div>

        <c:if test="${fn:length(employees) > 1}">
            <form action="${cp}/manage/admin/permissions" method="get" style="display: flex; align-items: center; gap: 10px;">
                <label for="selectEmp" style="font-size: 13px; font-weight: 600; color: #475569;">Chọn nhân viên:</label>
                <select id="selectEmp" name="userId" onchange="this.form.submit()"
                        style="height: 38px; padding: 0 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 14px; font-weight: 600; background: white;">
                    <c:forEach var="emp" items="${employees}">
                        <option value="${emp.userId}" ${emp.userId == selectedUserId ? 'selected' : ''}>
                            ${emp.fullName} (${emp.email})
                        </option>
                    </c:forEach>
                </select>
            </form>
        </c:if>
    </div>

    <!-- FORM LƯU PHÂN QUYỀN -->
    <form action="${cp}/manage/admin/permissions" method="post">
        <input type="hidden" name="userId" value="${selectedUserId}" />

        <!-- 1. HỆ THỐNG (KHÓA VĨNH VIỄN) -->
        <div class="perm-section-card">
            <div class="perm-section-header system-header">
                <span class="perm-section-title">🔒 HỆ THỐNG (ADMIN ONLY — KHÓA CỨNG)</span>
                <span style="font-size: 12px; font-weight: 600; color: #991b1b; background: #fee2e2; padding: 3px 8px; border-radius: 4px;">
                    Không thể phân quyền cho Employee
                </span>
            </div>
            <div class="perm-group-list">
                <div class="perm-item">
                    <div class="perm-item-info">
                        <div class="perm-item-name" style="color: #64748b;">
                            👤 Quản lý Tài khoản người dùng
                        </div>
                        <div class="perm-item-desc">Tạo mới, sửa thông tin, khóa tài khoản Quản trị và Khách hàng</div>
                    </div>
                    <div class="perm-item-controls">
                        <span class="perm-locked-tag">🔒 Chỉ Admin</span>
                    </div>
                </div>

                <div class="perm-item">
                    <div class="perm-item-info">
                        <div class="perm-item-name" style="color: #64748b;">
                            🛡️ Quản lý Vai trò (Roles)
                        </div>
                        <div class="perm-item-desc">Cấu hình vai trò ADMIN, EMPLOYEE, CUSTOMER trong hệ thống</div>
                    </div>
                    <div class="perm-item-controls">
                        <span class="perm-locked-tag">🔒 Chỉ Admin</span>
                    </div>
                </div>

                <div class="perm-item">
                    <div class="perm-item-info">
                        <div class="perm-item-name" style="color: #64748b;">
                            🔑 Quản lý Phân quyền (Permissions)
                        </div>
                        <div class="perm-item-desc">Cấp và thu hồi quyền hạn nhân viên bán hàng</div>
                    </div>
                    <div class="perm-item-controls">
                        <span class="perm-locked-tag">🔒 Chỉ Admin</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 2. SẢN PHẨM -->
        <div class="perm-section-card">
            <div class="perm-section-header">
                <span class="perm-section-title">📦 SẢN PHẨM</span>
                <span style="font-size: 12px; color: #64748b;">Sản phẩm, Danh mục &amp; Thương hiệu</span>
            </div>
            <div class="perm-group-list">
                <div class="perm-item">
                    <div class="perm-item-info">
                        <div class="perm-item-name">Quản lý Sản phẩm</div>
                        <div class="perm-item-desc">Xem danh sách, tạo mới, chỉnh sửa thông tin sản phẩm và biến thể</div>
                    </div>
                    <div class="perm-item-controls">
                        <c:if test="${not empty permByCode['PRODUCT_VIEW']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['PRODUCT_VIEW'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['PRODUCT_VIEW'].permissionId) ? 'checked' : ''} />
                                Xem sản phẩm
                            </label>
                        </c:if>
                        <c:if test="${not empty permByCode['PRODUCT_CREATE']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['PRODUCT_CREATE'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['PRODUCT_CREATE'].permissionId) ? 'checked' : ''} />
                                Thêm sản phẩm
                            </label>
                        </c:if>
                        <c:if test="${not empty permByCode['PRODUCT_EDIT']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['PRODUCT_EDIT'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['PRODUCT_EDIT'].permissionId) ? 'checked' : ''} />
                                Sửa sản phẩm
                            </label>
                        </c:if>
                    </div>
                </div>

                <div class="perm-item">
                    <div class="perm-item-info">
                        <div class="perm-item-name">Danh mục &amp; Thương hiệu</div>
                        <div class="perm-item-desc">Quản lý danh mục đồng hồ và các thương hiệu (Seiko, Orient, Casio, Tissot,...)</div>
                    </div>
                    <div class="perm-item-controls">
                        <span style="font-size: 13px; color: #64748b; font-style: italic;">(Đồng bộ theo quyền Xem/Sửa Sản phẩm)</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 3. BÁN HÀNG -->
        <div class="perm-section-card">
            <div class="perm-section-header">
                <span class="perm-section-title">🛒 BÁN HÀNG</span>
                <span style="font-size: 12px; color: #64748b;">Đơn hàng, Khách hàng, Đánh giá &amp; Bình luận</span>
            </div>
            <div class="perm-group-list">
                <div class="perm-item">
                    <div class="perm-item-info">
                        <div class="perm-item-name">Quản lý Đơn hàng</div>
                        <div class="perm-item-desc">Xem đơn, tạo đơn quầy POS, chỉnh sửa, xác nhận/hủy đơn và xuất báo cáo</div>
                    </div>
                    <div class="perm-item-controls">
                        <c:if test="${not empty permByCode['ORDER_VIEW']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['ORDER_VIEW'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['ORDER_VIEW'].permissionId) || activePermissionIds.contains(permByCode['SALES_ORDER'].permissionId) ? 'checked' : ''} />
                                Xem đơn hàng
                            </label>
                        </c:if>
                        <c:if test="${not empty permByCode['ORDER_CREATE']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['ORDER_CREATE'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['ORDER_CREATE'].permissionId) ? 'checked' : ''} />
                                Tạo đơn hàng
                            </label>
                        </c:if>
                        <c:if test="${not empty permByCode['ORDER_EDIT']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['ORDER_EDIT'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['ORDER_EDIT'].permissionId) ? 'checked' : ''} />
                                Sửa đơn hàng
                            </label>
                        </c:if>
                        <c:if test="${not empty permByCode['ORDER_APPROVE']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['ORDER_APPROVE'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['ORDER_APPROVE'].permissionId) ? 'checked' : ''} />
                                Duyệt / Hủy đơn
                            </label>
                        </c:if>
                        <c:if test="${not empty permByCode['ORDER_EXPORT']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['ORDER_EXPORT'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['ORDER_EXPORT'].permissionId) ? 'checked' : ''} />
                                Xuất hóa đơn
                            </label>
                        </c:if>
                    </div>
                </div>

                <div class="perm-item">
                    <div class="perm-item-info">
                        <div class="perm-item-name">Quản lý Khách hàng</div>
                        <div class="perm-item-desc">Xem hồ sơ, lịch sử mua sắm và cập nhật thông tin khách hàng</div>
                    </div>
                    <div class="perm-item-controls">
                        <c:if test="${not empty permByCode['CUSTOMER_VIEW']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['CUSTOMER_VIEW'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['CUSTOMER_VIEW'].permissionId) || activePermissionIds.contains(permByCode['SALES_CUSTOMER'].permissionId) ? 'checked' : ''} />
                                Xem khách hàng
                            </label>
                        </c:if>
                        <c:if test="${not empty permByCode['CUSTOMER_CREATE']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['CUSTOMER_CREATE'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['CUSTOMER_CREATE'].permissionId) ? 'checked' : ''} />
                                Thêm khách hàng
                            </label>
                        </c:if>
                        <c:if test="${not empty permByCode['CUSTOMER_EDIT']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['CUSTOMER_EDIT'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['CUSTOMER_EDIT'].permissionId) ? 'checked' : ''} />
                                Sửa khách hàng
                            </label>
                        </c:if>
                    </div>
                </div>

                <div class="perm-item">
                    <div class="perm-item-info">
                        <div class="perm-item-name">Đánh giá &amp; Bình luận</div>
                        <div class="perm-item-desc">Kiểm duyệt review đánh giá sao và trả lời bình luận của khách hàng</div>
                    </div>
                    <div class="perm-item-controls">
                        <span style="font-size: 13px; color: #64748b; font-style: italic;">(Kèm theo quyền Bán hàng &amp; Xem sản phẩm)</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 4. BẢO HÀNH -->
        <div class="perm-section-card">
            <div class="perm-section-header">
                <span class="perm-section-title">🛡️ BẢO HÀNH</span>
                <span style="font-size: 12px; color: #64748b;">Quản lý phiếu bảo hành &amp; sửa chữa</span>
            </div>
            <div class="perm-group-list">
                <div class="perm-item">
                    <div class="perm-item-info">
                        <div class="perm-item-name">Quản lý Bảo hành</div>
                        <div class="perm-item-desc">Tiếp nhận bảo hành, tạo phiếu, tra cứu thời hạn và cập nhật trạng thái sửa chữa</div>
                    </div>
                    <div class="perm-item-controls">
                        <c:if test="${not empty permByCode['SALES_WARRANTY']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['SALES_WARRANTY'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['SALES_WARRANTY'].permissionId) ? 'checked' : ''} />
                                Quản lý bảo hành
                            </label>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>

        <!-- 5. MARKETING & NỘI DUNG -->
        <div class="perm-section-card">
            <div class="perm-section-header">
                <span class="perm-section-title">🏷️ MARKETING &amp; NỘI DUNG</span>
                <span style="font-size: 12px; color: #64748b;">Mã giảm giá, Banner quảng cáo &amp; Bài viết</span>
            </div>
            <div class="perm-group-list">
                <div class="perm-item">
                    <div class="perm-item-info">
                        <div class="perm-item-name">Quản lý Voucher (Mã giảm giá)</div>
                        <div class="perm-item-desc">Xem danh sách, tạo mã khuyến mãi mới và điều chỉnh thời hạn áp dụng</div>
                    </div>
                    <div class="perm-item-controls">
                        <c:if test="${not empty permByCode['VOUCHER_VIEW']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['VOUCHER_VIEW'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['VOUCHER_VIEW'].permissionId) ? 'checked' : ''} />
                                Xem voucher
                            </label>
                        </c:if>
                        <c:if test="${not empty permByCode['VOUCHER_CREATE']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['VOUCHER_CREATE'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['VOUCHER_CREATE'].permissionId) ? 'checked' : ''} />
                                Thêm voucher
                            </label>
                        </c:if>
                        <c:if test="${not empty permByCode['VOUCHER_EDIT']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['VOUCHER_EDIT'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['VOUCHER_EDIT'].permissionId) ? 'checked' : ''} />
                                Sửa voucher
                            </label>
                        </c:if>
                    </div>
                </div>

                <div class="perm-item">
                    <div class="perm-item-info">
                        <div class="perm-item-name">Banner &amp; Bài viết</div>
                        <div class="perm-item-desc">Quản lý banner khuyến mãi và tin tức bài viết trên website</div>
                    </div>
                    <div class="perm-item-controls">
                        <span style="font-size: 13px; color: #64748b; font-style: italic;">(Đồng bộ theo quyền Marketing / Voucher)</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 6. BÁO CÁO -->
        <div class="perm-section-card">
            <div class="perm-section-header">
                <span class="perm-section-title">📊 BÁO CÁO</span>
                <span style="font-size: 12px; color: #64748b;">Thống kê doanh thu &amp; Phân tích bán hàng</span>
            </div>
            <div class="perm-group-list">
                <div class="perm-item">
                    <div class="perm-item-info">
                        <div class="perm-item-name">Thống kê &amp; Báo cáo bán hàng</div>
                        <div class="perm-item-desc">Xem biểu đồ doanh thu, số lượng đơn hàng và xuất file báo cáo</div>
                    </div>
                    <div class="perm-item-controls">
                        <c:if test="${not empty permByCode['REPORT_VIEW']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['REPORT_VIEW'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['REPORT_VIEW'].permissionId) || activePermissionIds.contains(permByCode['SALES_REPORT'].permissionId) ? 'checked' : ''} />
                                Xem báo cáo
                            </label>
                        </c:if>
                        <c:if test="${not empty permByCode['REPORT_EXPORT']}">
                            <label class="perm-checkbox-label">
                                <input type="checkbox" name="permissionIds" value="${permByCode['REPORT_EXPORT'].permissionId}"
                                       ${activePermissionIds.contains(permByCode['REPORT_EXPORT'].permissionId) ? 'checked' : ''} />
                                Xuất báo cáo (Excel/PDF)
                            </label>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>

        <!-- STICKY ACTION BAR -->
        <div class="perm-save-bar">
            <div style="font-size: 14px; color: #475569;">
                Đang cấu hình cho: <strong>${selectedEmployee.fullName}</strong> (${selectedEmployee.email})
            </div>
            <div style="display: flex; gap: 12px;">
                <a href="${cp}/manage/admin/dashboard" class="button button-outline" style="padding: 10px 20px; border-radius: 8px; text-decoration: none;">
                    Hủy bỏ
                </a>
                <button type="submit" class="button button-gold" style="padding: 10px 28px; font-weight: 700; font-size: 14px; border-radius: 8px; cursor: pointer; display: inline-flex; align-items: center; gap: 6px;">
                    💾 LƯU PHÂN QUYỀN
                </button>
            </div>
        </div>

    </form>

</div>
