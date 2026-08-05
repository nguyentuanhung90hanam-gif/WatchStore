<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="adminLayout" value="true" scope="request" />
<jsp:include page="header.jsp" />

<div class="portal-shell">
    <jsp:include page="sidebar.jsp" />
    <button class="portal-overlay" type="button" data-sidebar-close aria-label="Đóng menu"></button>

    <main class="portal-main">
        <header class="portal-topbar">
            <button class="portal-menu" type="button" data-sidebar-toggle aria-label="Mở menu quản trị">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M4 12h16M4 17h16"/></svg>
            </button>

            <div class="portal-title">
                <small>WATCHSTORE <span>/</span> ${adminArea == 'sales' ? 'BÁN HÀNG' : adminArea == 'warehouse' ? 'KHO HÀNG' : 'QUẢN TRỊ'}</small>
                <h1>${pageTitle}</h1>
            </div>

            <div class="topbar-actions">
                <a class="topbar-store-link" href="${cp}/page/home" target="_blank" rel="noopener">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 10h16M5 10l1-6h12l1 6M6 10v10h12V10M9 20v-6h6v6"/></svg>
                    <span>Xem cửa hàng</span>
                </a>
                <a class="topbar-icon" href="${cp}/page/notifications" aria-label="Thông báo">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/></svg>
                    <i></i>
                </a>
                <a class="topbar-profile" href="${cp}/page/profile">
                    <span>${empty sessionScope.user ? 'QT' : fn:toUpperCase(fn:substring(sessionScope.user.fullName, 0, 1))}</span>
                    <b>${empty sessionScope.user ? 'Quản trị viên' : sessionScope.user.fullName}</b>
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m8 10 4 4 4-4"/></svg>
                </a>
            </div>
        </header>

        <section class="portal-content">
            <jsp:include page="${contentPage}" />
        </section>
    </main>
</div>

<jsp:include page="footer.jsp" />
