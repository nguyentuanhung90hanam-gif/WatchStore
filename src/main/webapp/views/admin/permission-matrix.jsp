<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">MA TRẬN PHÂN QUYỀN GỐC</p>
        <h2>Phân quyền Nhân viên</h2>
        <p class="module-desc">
            Cấu hình quyền Xem, Thêm, Sửa, Duyệt và Xuất báo cáo chi tiết theo từng module chức năng cho tài khoản Nhân viên.
        </p>
    </div>
</div>

<c:if test="${not empty sessionScope.flash}">
    <div class="alert alert-success" style="margin-bottom: 20px;">
        ${sessionScope.flash}
    </div>
    <c:remove var="flash" scope="session" />
</c:if>

<div class="dashboard-card" style="margin-bottom: 25px;">
    <!-- CHỌN NHÂN VIÊN CẦN PHÂN QUYỀN -->
    <form action="${cp}/manage/admin/permissions" method="get" style="display: flex; align-items: center; gap: 15px; flex-wrap: wrap;">
        <label for="selectEmp" style="font-weight: 700; font-size: 15px; color: var(--gold-dark, #b8860b);">
            👤 Chọn Nhân viên:
        </label>
        <select id="selectEmp" name="userId" onchange="this.form.submit()"
                style="height: 42px; padding: 0 16px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 15px; font-weight: 600; min-width: 320px; background: white;">
            <c:forEach var="emp" items="${employees}">
                <option value="${emp.userId}" ${emp.userId == selectedUserId ? 'selected' : ''}>
                    ${emp.fullName} (${emp.email})
                </option>
            </c:forEach>
        </select>
        <button type="submit" class="button button-gold" style="padding: 10px 20px; border-radius: 8px;">
            Tải dữ liệu
        </button>
    </form>
</div>

<c:if test="${not empty selectedEmployee}">
    <div class="dashboard-card">
        <div style="margin-bottom: 20px; padding: 12px 16px; background: #f8fafc; border-left: 4px solid var(--gold-dark, #b8860b); border-radius: 4px;">
            Đang thiết lập ma trận phân quyền cho tài khoản: <strong>${selectedEmployee.fullName}</strong> (${selectedEmployee.email})
        </div>

        <form action="${cp}/manage/admin/permissions" method="post">
            <input type="hidden" name="userId" value="${selectedUserId}" />

            <div class="table-wrap">
                <table class="table-matrix" style="width: 100%; border-collapse: collapse; text-align: center;">
                    <thead>
                        <tr style="background: #1e293b; color: white; height: 48px;">
                            <th style="text-align: left; padding-left: 20px; width: 220px; font-size: 14px; text-transform: uppercase; letter-spacing: 0.5px;">Module</th>
                            <th style="width: 130px; font-size: 14px;">Xem</th>
                            <th style="width: 130px; font-size: 14px;">Thêm</th>
                            <th style="width: 130px; font-size: 14px;">Sửa</th>
                            <th style="width: 130px; font-size: 14px;">Duyệt</th>
                            <th style="width: 140px; font-size: 14px;">Xuất báo cáo</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%-- 1. SẢN PHẨM --%>
                        <tr style="border-bottom: 1px solid #e2e8f0; height: 56px;">
                            <td style="text-align: left; padding-left: 20px; font-weight: 700; color: #1e293b;">
                                📦 Sản phẩm
                            </td>
                            <%-- Xem --%>
                            <td>
                                <c:if test="${not empty permByCode['PRODUCT_VIEW']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['PRODUCT_VIEW'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['PRODUCT_VIEW'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Thêm --%>
                            <td>
                                <c:if test="${not empty permByCode['PRODUCT_CREATE']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['PRODUCT_CREATE'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['PRODUCT_CREATE'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Sửa --%>
                            <td>
                                <c:if test="${not empty permByCode['PRODUCT_EDIT']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['PRODUCT_EDIT'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['PRODUCT_EDIT'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Duyệt --%>
                            <td style="color: #94a3b8; font-weight: 600;">—</td>
                            <%-- Xuất báo cáo --%>
                            <td style="color: #94a3b8; font-weight: 600;">—</td>
                        </tr>

                        <%-- 2. ĐƠN HÀNG --%>
                        <tr style="border-bottom: 1px solid #e2e8f0; height: 56px; background: #fdfbf7;">
                            <td style="text-align: left; padding-left: 20px; font-weight: 700; color: #1e293b;">
                                🛒 Đơn hàng
                            </td>
                            <%-- Xem --%>
                            <td>
                                <c:if test="${not empty permByCode['ORDER_VIEW']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['ORDER_VIEW'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['ORDER_VIEW'].permissionId) || activePermissionIds.contains(permByCode['SALES_ORDER'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Thêm --%>
                            <td>
                                <c:if test="${not empty permByCode['ORDER_CREATE']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['ORDER_CREATE'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['ORDER_CREATE'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Sửa --%>
                            <td>
                                <c:if test="${not empty permByCode['ORDER_EDIT']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['ORDER_EDIT'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['ORDER_EDIT'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Duyệt --%>
                            <td>
                                <c:if test="${not empty permByCode['ORDER_APPROVE']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['ORDER_APPROVE'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['ORDER_APPROVE'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Xuất báo cáo --%>
                            <td>
                                <c:if test="${not empty permByCode['ORDER_EXPORT']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['ORDER_EXPORT'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['ORDER_EXPORT'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                        </tr>

                        <%-- 3. KHÁCH HÀNG --%>
                        <tr style="border-bottom: 1px solid #e2e8f0; height: 56px;">
                            <td style="text-align: left; padding-left: 20px; font-weight: 700; color: #1e293b;">
                                👥 Khách hàng
                            </td>
                            <%-- Xem --%>
                            <td>
                                <c:if test="${not empty permByCode['CUSTOMER_VIEW']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['CUSTOMER_VIEW'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['CUSTOMER_VIEW'].permissionId) || activePermissionIds.contains(permByCode['SALES_CUSTOMER'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Thêm --%>
                            <td>
                                <c:if test="${not empty permByCode['CUSTOMER_CREATE']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['CUSTOMER_CREATE'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['CUSTOMER_CREATE'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Sửa --%>
                            <td>
                                <c:if test="${not empty permByCode['CUSTOMER_EDIT']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['CUSTOMER_EDIT'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['CUSTOMER_EDIT'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Duyệt --%>
                            <td style="color: #94a3b8; font-weight: 600;">—</td>
                            <%-- Xuất báo cáo --%>
                            <td style="color: #94a3b8; font-weight: 600;">—</td>
                        </tr>

                        <%-- 4. KHO HÀNG --%>
                        <tr style="border-bottom: 1px solid #e2e8f0; height: 56px; background: #fdfbf7;">
                            <td style="text-align: left; padding-left: 20px; font-weight: 700; color: #1e293b;">
                                🏢 Kho hàng
                            </td>
                            <%-- Xem --%>
                            <td>
                                <c:if test="${not empty permByCode['INVENTORY_VIEW']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['INVENTORY_VIEW'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['INVENTORY_VIEW'].permissionId) || activePermissionIds.contains(permByCode['WAREHOUSE_INVENTORY'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Thêm --%>
                            <td>
                                <c:if test="${not empty permByCode['INVENTORY_CREATE']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['INVENTORY_CREATE'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['INVENTORY_CREATE'].permissionId) || activePermissionIds.contains(permByCode['WAREHOUSE_RECEIPT'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Sửa --%>
                            <td>
                                <c:if test="${not empty permByCode['INVENTORY_EDIT']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['INVENTORY_EDIT'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['INVENTORY_EDIT'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Duyệt --%>
                            <td>
                                <c:if test="${not empty permByCode['INVENTORY_APPROVE']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['INVENTORY_APPROVE'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['INVENTORY_APPROVE'].permissionId) || activePermissionIds.contains(permByCode['WAREHOUSE_EXPORT'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Xuất báo cáo --%>
                            <td>
                                <c:if test="${not empty permByCode['INVENTORY_EXPORT']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['INVENTORY_EXPORT'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['INVENTORY_EXPORT'].permissionId) || activePermissionIds.contains(permByCode['WAREHOUSE_REPORT'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                        </tr>

                        <%-- 5. VOUCHER --%>
                        <tr style="border-bottom: 1px solid #e2e8f0; height: 56px;">
                            <td style="text-align: left; padding-left: 20px; font-weight: 700; color: #1e293b;">
                                🎟️ Voucher
                            </td>
                            <%-- Xem --%>
                            <td>
                                <c:if test="${not empty permByCode['VOUCHER_VIEW']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['VOUCHER_VIEW'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['VOUCHER_VIEW'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Thêm --%>
                            <td>
                                <c:if test="${not empty permByCode['VOUCHER_CREATE']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['VOUCHER_CREATE'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['VOUCHER_CREATE'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Sửa --%>
                            <td>
                                <c:if test="${not empty permByCode['VOUCHER_EDIT']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['VOUCHER_EDIT'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['VOUCHER_EDIT'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Duyệt --%>
                            <td style="color: #94a3b8; font-weight: 600;">—</td>
                            <%-- Xuất báo cáo --%>
                            <td style="color: #94a3b8; font-weight: 600;">—</td>
                        </tr>

                        <%-- 6. BÁO CÁO --%>
                        <tr style="border-bottom: 1px solid #e2e8f0; height: 56px; background: #fdfbf7;">
                            <td style="text-align: left; padding-left: 20px; font-weight: 700; color: #1e293b;">
                                📊 Báo cáo
                            </td>
                            <%-- Xem --%>
                            <td>
                                <c:if test="${not empty permByCode['REPORT_VIEW']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['REPORT_VIEW'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['REPORT_VIEW'].permissionId) || activePermissionIds.contains(permByCode['SALES_REPORT'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                            <%-- Thêm --%>
                            <td style="color: #94a3b8; font-weight: 600;">—</td>
                            <%-- Sửa --%>
                            <td style="color: #94a3b8; font-weight: 600;">—</td>
                            <%-- Duyệt --%>
                            <td style="color: #94a3b8; font-weight: 600;">—</td>
                            <%-- Xuất báo cáo --%>
                            <td>
                                <c:if test="${not empty permByCode['REPORT_EXPORT']}">
                                    <input type="checkbox" name="permissionIds" value="${permByCode['REPORT_EXPORT'].permissionId}"
                                           style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                           ${activePermissionIds.contains(permByCode['REPORT_EXPORT'].permissionId) ? 'checked' : ''} />
                                </c:if>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 15px; margin-top: 25px;">
                <a href="${cp}/manage/admin/dashboard" class="button button-outline" style="padding: 12px 24px; border-radius: 8px;">
                    Hủy bỏ
                </a>
                <button type="submit" class="button button-gold" style="padding: 12px 36px; font-weight: 700; font-size: 15px; border-radius: 8px; cursor: pointer;">
                    💾 Lưu phân quyền
                </button>
            </div>
        </form>
    </div>
</c:if>
