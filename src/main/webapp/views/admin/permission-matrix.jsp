<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<div class="module-heading">
    <div class="module-title-area">
        <p class="eyebrow dark">EMPLOYEE PERMISSION MATRIX</p>
        <h2>PHÂN QUYỀN NHÂN VIÊN BÁN HÀNG</h2>
        <p class="module-desc">
            Cấu hình quyền cho tài khoản Nhân viên bán hàng. Chỉ các tài khoản EMPLOYEE được hiển thị.
        </p>
    </div>
</div>

<c:if test="${not empty sessionScope.flash}">
    <div class="alert alert-success" style="margin-bottom: 20px; padding: 12px 16px; background: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 8px; color: #065f46; font-weight: 600;">
        ✓ ${sessionScope.flash}
    </div>
    <c:remove var="flash" scope="session" />
</c:if>

<div class="dashboard-card" style="margin-bottom: 25px; padding: 20px;">
    <!-- CHỌN NHÂN VIÊN BÁN HÀNG CẦN PHÂN QUYỀN -->
    <form action="${cp}/manage/admin/permissions" method="get" style="display: flex; align-items: center; gap: 15px; flex-wrap: wrap;">
        <label for="selectEmp" style="font-weight: 700; font-size: 15px; color: var(--gold-dark, #b8860b);">
            👤 Chọn Nhân viên bán hàng:
        </label>
        <select id="selectEmp" name="userId" onchange="this.form.submit()"
                style="height: 42px; padding: 0 16px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 15px; font-weight: 600; min-width: 340px; background: white;">
            <option value="">-- Chọn Nhân viên bán hàng --</option>
            <c:forEach var="emp" items="${employees}">
                <option value="${emp.userId}" ${emp.userId == selectedUserId ? 'selected' : ''}>
                    ${emp.fullName} - ${emp.email}
                </option>
            </c:forEach>
        </select>
        <button type="submit" class="button button-gold" style="padding: 10px 24px; border-radius: 8px; font-weight: 700; cursor: pointer;">
            TẢI DỮ LIỆU
        </button>
    </form>
</div>

<c:choose>
    <%-- KHI CHƯA CHỌN NHÂN VIÊN --%>
    <c:when test="${empty selectedEmployee}">
        <div class="dashboard-card" style="text-align: center; padding: 48px 24px; background: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 12px;">
            <div style="font-size: 42px; margin-bottom: 12px;">📋</div>
            <h3 style="font-size: 18px; font-weight: 700; color: #1e293b; margin-bottom: 8px;">
                Vui lòng chọn nhân viên bán hàng để tải ma trận quyền.
            </h3>
            <p style="font-size: 14px; margin: 0; color: #64748b;">
                Chọn một tài khoản nhân viên từ danh sách phía trên và nhấn <strong>"TẢI DỮ LIỆU"</strong> để xem và thiết lập phân quyền.
            </p>
        </div>
    </c:when>

    <%-- KHI ĐÃ CHỌN NHÂN VIÊN --%>
    <c:otherwise>
        <div class="dashboard-card" style="padding: 24px;">
            <!-- THÔNG TIN NHÂN VIÊN BÁN HÀNG -->
            <div style="margin-bottom: 24px; padding: 16px 20px; background: #fdfbf7; border: 1px solid #fed7aa; border-left: 5px solid var(--gold-dark, #b8860b); border-radius: 8px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px;">
                <div>
                    <div style="font-size: 16px; font-weight: 700; color: #1e293b; margin-bottom: 4px;">
                        Nhân viên: <span style="color: #b8860b;">${selectedEmployee.fullName}</span>
                    </div>
                    <div style="font-size: 14px; color: #64748b;">
                        Email: <strong>${selectedEmployee.email}</strong> &nbsp;•&nbsp; SĐT: <strong>${empty selectedEmployee.phone ? 'Chưa cập nhật' : selectedEmployee.phone}</strong>
                    </div>
                </div>
                <div>
                    <span style="display: inline-block; padding: 6px 14px; background: #eff6ff; color: #1d4ed8; font-weight: 700; font-size: 13px; border-radius: 20px; border: 1px solid #bfdbfe;">
                        Role: Nhân viên bán hàng
                    </span>
                </div>
            </div>

            <!-- MA TRẬN PHÂN QUYỀN -->
            <form action="${cp}/manage/admin/permissions" method="post">
                <input type="hidden" name="userId" value="${selectedUserId}" />

                <div class="table-wrap" style="overflow-x: auto;">
                    <table class="table-matrix" style="width: 100%; border-collapse: collapse; text-align: center;">
                        <thead>
                            <tr style="background: #1e293b; color: white; height: 50px;">
                                <th style="text-align: left; padding-left: 20px; width: 220px; font-size: 14px; text-transform: uppercase; letter-spacing: 0.5px;">MODULE</th>
                                <th style="width: 140px; font-size: 14px; text-transform: uppercase;">XEM</th>
                                <th style="width: 140px; font-size: 14px; text-transform: uppercase;">THÊM</th>
                                <th style="width: 140px; font-size: 14px; text-transform: uppercase;">SỬA</th>
                                <th style="width: 140px; font-size: 14px; text-transform: uppercase;">DUYỆT</th>
                                <th style="width: 150px; font-size: 14px; text-transform: uppercase;">XUẤT BÁO CÁO</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%-- 1. SẢN PHẨM --%>
                            <tr style="border-bottom: 1px solid #e2e8f0; height: 58px;">
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
                            <tr style="border-bottom: 1px solid #e2e8f0; height: 58px; background: #fdfbf7;">
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
                            <tr style="border-bottom: 1px solid #e2e8f0; height: 58px;">
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

                            <%-- 4. BẢO HÀNH --%>
                            <tr style="border-bottom: 1px solid #e2e8f0; height: 58px; background: #fdfbf7;">
                                <td style="text-align: left; padding-left: 20px; font-weight: 700; color: #1e293b;">
                                    🛡️ Bảo hành
                                </td>
                                <%-- Xem --%>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty permByCode['WARRANTY_VIEW']}">
                                            <input type="checkbox" name="permissionIds" value="${permByCode['WARRANTY_VIEW'].permissionId}"
                                                   style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                                   ${activePermissionIds.contains(permByCode['WARRANTY_VIEW'].permissionId) ? 'checked' : ''} />
                                        </c:when>
                                        <c:when test="${not empty permByCode['SALES_WARRANTY']}">
                                            <input type="checkbox" name="permissionIds" value="${permByCode['SALES_WARRANTY'].permissionId}"
                                                   style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                                   ${activePermissionIds.contains(permByCode['SALES_WARRANTY'].permissionId) ? 'checked' : ''} />
                                        </c:when>
                                        <c:otherwise><span style="color: #94a3b8; font-weight: 600;">—</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <%-- Thêm --%>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty permByCode['WARRANTY_CREATE']}">
                                            <input type="checkbox" name="permissionIds" value="${permByCode['WARRANTY_CREATE'].permissionId}"
                                                   style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                                   ${activePermissionIds.contains(permByCode['WARRANTY_CREATE'].permissionId) ? 'checked' : ''} />
                                        </c:when>
                                        <c:otherwise><span style="color: #94a3b8; font-weight: 600;">—</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <%-- Sửa --%>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty permByCode['WARRANTY_EDIT']}">
                                            <input type="checkbox" name="permissionIds" value="${permByCode['WARRANTY_EDIT'].permissionId}"
                                                   style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                                   ${activePermissionIds.contains(permByCode['WARRANTY_EDIT'].permissionId) ? 'checked' : ''} />
                                        </c:when>
                                        <c:otherwise><span style="color: #94a3b8; font-weight: 600;">—</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <%-- Duyệt --%>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty permByCode['WARRANTY_APPROVE']}">
                                            <input type="checkbox" name="permissionIds" value="${permByCode['WARRANTY_APPROVE'].permissionId}"
                                                   style="width: 20px; height: 20px; cursor: pointer; accent-color: #b8860b;"
                                                   ${activePermissionIds.contains(permByCode['WARRANTY_APPROVE'].permissionId) ? 'checked' : ''} />
                                        </c:when>
                                        <c:otherwise><span style="color: #94a3b8; font-weight: 600;">—</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <%-- Xuất báo cáo --%>
                                <td style="color: #94a3b8; font-weight: 600;">—</td>
                            </tr>

                            <%-- 5. THỐNG KÊ BÁN HÀNG --%>
                            <tr style="border-bottom: 1px solid #e2e8f0; height: 58px;">
                                <td style="text-align: left; padding-left: 20px; font-weight: 700; color: #1e293b;">
                                    📊 Thống kê bán hàng
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
    </c:otherwise>
</c:choose>

