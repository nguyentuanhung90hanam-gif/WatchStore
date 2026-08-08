<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="dashboardLabel" value="TRUNG TÂM KHO HÀNG"/>
<c:set var="metric1Label" value="Tồn kho khả dụng"/>
<c:set var="metric1Value" value="${totalQuantity}"/>
<c:set var="metric2Label" value="Cảnh báo sắp hết"/>
<c:set var="metric2Value" value="${lowStockCount}"/>
<c:set var="metric3Label" value="Phiếu nhập gần đây"/>
<c:set var="metric3Value" value="Xem danh sách"/>
<c:set var="metric4Label" value="Sắp hết hàng"/>
<c:set var="metric4Value" value="${lowStockCount}"/>
<c:set var="chartTitle" value="Nhập — xuất — tồn"/>
<c:set var="task1" value="Sản phẩm sắp hết"/>
<c:set var="task2" value="Phiếu nhập chờ duyệt"/>
<c:set var="task3" value="Chênh lệch kiểm kê"/>
<c:set var="tableTitle" value="Cảnh báo tồn kho"/>
<jsp:include page="/views/shared/management-dashboard.jsp"/>
