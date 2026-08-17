<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<div class="dashboard-card">

    <div style="margin-bottom: 20px;">
        <h2 style="margin: 0 0 6px 0;">
            <c:choose>
                <c:when test="${formMode == 'edit'}">
                    Sửa thông tin tài khoản
                </c:when>
                <c:otherwise>
                    Thêm tài khoản mới
                </c:otherwise>
            </c:choose>
        </h2>
        <p style="color: #64748b; font-size: 14px; margin: 0;">
            Quản lý hồ sơ người dùng và trạng thái tài khoản. Vai trò người dùng được quản lý độc lập tại hệ thống RBAC.
        </p>
    </div>

    <%-- Hiển thị lỗi validation --%>
    <c:if test="${not empty errorMessage}">
        <div style="background:#fef2f2;color:#991b1b;border:1px solid #fecaca;border-radius:8px;padding:12px 16px;margin-bottom:20px;font-size:14px;font-weight:600;display:flex;align-items:center;gap:8px;">
            <span>⚠</span> ${errorMessage}
        </div>
    </c:if>

    <form method="post"
          action="${pageContext.request.contextPath}/manage/admin/accounts/${formMode == 'edit' ? 'update' : 'save'}"
          autocomplete="off">

        <%-- Hidden ID khi sửa --%>
        <c:if test="${formMode == 'edit' && not empty requestScope.account && requestScope.account.userId > 0}">
            <input type="hidden" name="userId" value="${requestScope.account.userId}">
        </c:if>

        <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px;">

            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;">Email <span style="color:#dc2626;">*</span></label>
                <input type="email" name="email"
                       value="${not empty requestScope.account ? requestScope.account.email : ''}"
                       placeholder="example@watchstore.vn"
                       autocomplete="off"
                       required
                       ${formMode == 'edit' && requestScope.account.email == 'admin@watchstore.vn' ? 'readonly' : ''}
                       style="width:100%;padding:10px 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;${formMode == 'edit' && requestScope.account.email == 'admin@watchstore.vn' ? 'background:#f1f5f9;cursor:not-allowed;' : 'background:#fff;'}">
                <c:if test="${formMode == 'edit' && requestScope.account.email == 'admin@watchstore.vn'}">
                    <small style="color:#64748b;margin-top:4px;display:block;">Tài khoản Quản trị viên tối cao không được đổi Email.</small>
                </c:if>
            </div>

            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;">Mật khẩu
                    <c:choose>
                        <c:when test="${formMode == 'edit'}">
                            <small style="color:#64748b;font-weight:normal;">(để trống nếu không đổi)</small>
                        </c:when>
                        <c:otherwise>
                            <span style="color:#dc2626;">*</span>
                        </c:otherwise>
                    </c:choose>
                </label>
                <input type="password" name="password"
                       value=""
                       placeholder="${formMode == 'edit' ? 'Để trống nếu giữ mật khẩu cũ' : 'Nhập mật khẩu'}"
                       autocomplete="new-password"
                       ${formMode == 'edit' ? '' : 'required'}
                       style="width:100%;padding:10px 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;background:#fff;">
            </div>

            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;">Họ và tên <span style="color:#dc2626;">*</span></label>
                <input type="text" name="fullName"
                       value="${not empty requestScope.account ? requestScope.account.fullName : ''}"
                       placeholder="Nguyễn Văn A"
                       autocomplete="off"
                       required
                       style="width:100%;padding:10px 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;background:#fff;">
            </div>

            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;">Số điện thoại</label>
                <input type="text" name="phone"
                       value="${not empty requestScope.account ? requestScope.account.phone : ''}"
                       placeholder="0988000001"
                       autocomplete="off"
                       style="width:100%;padding:10px 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;background:#fff;">
            </div>

            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;">Giới tính</label>
                <select name="gender"
                        style="width:100%;padding:10px 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;background:#fff;">
                    <option value="">-- Chọn giới tính --</option>
                    <option value="MALE" ${not empty requestScope.account && requestScope.account.gender == 'MALE' ? 'selected' : ''}>Nam</option>
                    <option value="FEMALE" ${not empty requestScope.account && requestScope.account.gender == 'FEMALE' ? 'selected' : ''}>Nữ</option>
                    <option value="OTHER" ${not empty requestScope.account && requestScope.account.gender == 'OTHER' ? 'selected' : ''}>Khác</option>
                </select>
            </div>

            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;">Ngày sinh</label>
                <input type="date" name="dateOfBirth"
                       value="${not empty requestScope.account ? requestScope.account.dateOfBirth : ''}"
                       autocomplete="off"
                       style="width:100%;padding:10px 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;background:#fff;">
            </div>

            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;">Trạng thái tài khoản</label>
                <select name="status"
                        style="width:100%;padding:10px 14px;border:1px solid #cbd5e1;border-radius:8px;font-size:14px;background:#fff;">
                    <option value="ACTIVE"   ${empty requestScope.account || empty requestScope.account.status || requestScope.account.status == 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                    <option value="INACTIVE" ${not empty requestScope.account && requestScope.account.status == 'INACTIVE' ? 'selected' : ''}>Tạm ngưng</option>
                    <option value="LOCKED"   ${not empty requestScope.account && requestScope.account.status == 'LOCKED' ? 'selected' : ''}>Khóa</option>
                </select>
            </div>

            <div>
                <label style="display:block;font-weight:600;margin-bottom:6px;color:#334155;">Vai trò (Role) — <small style="color:#64748b;font-weight:normal;">Chỉ xem</small></label>
                <div style="padding:9px 14px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;display:flex;align-items:center;gap:10px;min-height:42px;">
                    <c:choose>
                        <c:when test="${formMode == 'edit'}">
                            <c:choose>
                                <c:when test="${requestScope.account.role == 'ADMIN' || requestScope.account.roleNames == 'ADMIN' || requestScope.account.roleNames == 'Quản trị viên'}">
                                    <span class="status-badge" style="background:#fef3c7;color:#92400e;font-weight:700;padding:4px 10px;border-radius:20px;">ADMIN</span>
                                    <span style="font-size:13px;color:#64748b;">(Quản trị viên toàn quyền)</span>
                                </c:when>
                                <c:when test="${requestScope.account.role == 'EMPLOYEE' || requestScope.account.roleNames == 'EMPLOYEE' || requestScope.account.roleNames == 'Nhân viên bán hàng'}">
                                    <span class="status-badge" style="background:#eff6ff;color:#1d4ed8;font-weight:700;padding:4px 10px;border-radius:20px;">EMPLOYEE</span>
                                    <span style="font-size:13px;color:#64748b;">(Nhân viên Sales — Phân quyền tại <a href="${pageContext.request.contextPath}/manage/admin/permissions" style="color:#b8860b;font-weight:600;">Phân quyền</a>)</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-badge" style="background:#f1f5f9;color:#475569;font-weight:600;padding:4px 10px;border-radius:20px;">CUSTOMER</span>
                                    <span style="font-size:13px;color:#64748b;">(Khách hàng mua sắm)</span>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <span class="status-badge" style="background:#f1f5f9;color:#475569;font-weight:600;padding:4px 10px;border-radius:20px;">CUSTOMER</span>
                            <span style="font-size:13px;color:#64748b;">(Mặc định là Khách hàng)</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

        </div>

        <div style="display:flex;gap:12px;margin-top:24px;border-top:1px solid #f1f5f9;padding-top:18px;">
            <button type="submit" class="button button-gold" style="padding:10px 28px;font-weight:700;border-radius:8px;cursor:pointer;">
                💾 Lưu thông tin
            </button>
            <a href="${pageContext.request.contextPath}/manage/admin/accounts"
               class="button button-outline" style="padding:10px 20px;border-radius:8px;text-decoration:none;">
                Hủy bỏ
            </a>
        </div>

    </form>

</div>