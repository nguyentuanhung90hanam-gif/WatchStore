<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%--
  stocktake.jsp — Quản lý kiểm kê kho (Có tạo & Xóa)
--%>

<div class="module-heading">
    <div>
        <p class="eyebrow dark">KIỂM KÊ</p>
        <h2>Phiếu kiểm kê kho</h2>
        <p>Tạo và theo dõi các đợt kiểm đếm hàng thực tế so với số hệ thống.</p>
    </div>
    <button class="button button-gold"
            onclick="document.getElementById('form-create-stocktake').submit()">
        ＋ Tạo phiếu kiểm kê
    </button>
</div>

<c:if test="${not empty sessionScope.flashMessage}">
    <div class="flash-message">${sessionScope.flashMessage}</div>
    <c:remove var="flashMessage" scope="session"/>
</c:if>

<div class="dashboard-card module-form" style="margin-bottom:1.5rem;">
    <form id="form-create-stocktake"
          action="${pageContext.request.contextPath}/manage/warehouse/stocktake-create"
          method="post">
        <div class="form-grid two">
            <label class="full-field">
                Ghi chú đợt kiểm kê
                <textarea name="note" rows="2"
                          placeholder="Ví dụ: Kiểm kê định kỳ tháng 8/2026"></textarea>
            </label>
        </div>
        <div class="form-actions">
            <button type="submit" class="button button-gold">
                Xác nhận tạo phiếu kiểm kê
            </button>
        </div>
    </form>
</div>

<div class="dashboard-card">
    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Mã phiếu</th>
                    <th>Ngày kiểm kê</th>
                    <th>Người tạo</th>
                    <th>Ghi chú</th>
                    <th>Trạng thái</th>
                    <th style="text-align:right;">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty stocktakes}">
                        <tr>
                            <td colspan="6" style="text-align:center;">
                                Chưa có phiếu kiểm kê nào.
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${stocktakes}" var="st">
                            <tr>
                                <td><b>${st.stocktakeCode}</b></td>
                                <td>${st.stocktakeDate}</td>
                                <td>${st.createdByName}</td>
                                <td>${st.note}</td>
                                <td>
                                    <span class="status-badge ${st.statusClass}">
                                        ${st.statusLabel}
                                    </span>
                                </td>
                                <td style="text-align:right;">
                                    <a class="table-action" style="color:var(--color-danger, #e53935);"
                                       href="${pageContext.request.contextPath}/manage/warehouse/stocktake-delete?id=${st.stocktakeID}"
                                       onclick="return confirm('Bạn có chắc muốn xóa đợt kiểm kê ${st.stocktakeCode} này không?');">
                                        🗑 Xóa
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>
