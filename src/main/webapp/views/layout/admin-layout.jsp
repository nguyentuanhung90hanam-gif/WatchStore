<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="adminLayout" value="true" scope="request" />
<jsp:include page="header.jsp" />
<div class="portal-shell">
    <jsp:include page="sidebar.jsp" />
    <main class="portal-main">
        <header class="portal-topbar">
            <button class="icon-button portal-menu" data-sidebar-toggle>☰</button>
            <div><small>WATCHSTORE / ${adminArea}</small><h1>${pageTitle}</h1></div>

            <div class="topbar-actions">
                <a href="${cp}/page/notifications">♢</a>
                <div class="user-avatar-dropdown" style="position:relative; display:inline-block;">
                    <span style="cursor:pointer; display:inline-flex; align-items:center; justify-content:center; width:36px; height:36px; border-radius:50%; background:#d4af37; color:#fff; font-weight:700; font-size:14px;">${empty sessionScope.user ? 'TN' : fn:substring(sessionScope.user.fullName, 0, 1)}</span>
                    <div class="dropdown-menu-box" style="display:none; position:absolute; right:0; top:100%; background:#fff; min-width:180px; box-shadow:0 4px 16px rgba(0,0,0,0.15); border-radius:8px; padding:8px 0; z-index:1000; border:1px solid #eee;">
                        <a href="${cp}/page/profile" style="display:block; padding:8px 16px; color:#333; font-size:13px; text-decoration:none;">Thông tin tài khoản</a>
                        <a href="${cp}/page/profile" style="display:block; padding:8px 16px; color:#333; font-size:13px; text-decoration:none;">Chỉnh sửa hồ sơ</a>
                        <a href="${cp}/page/change-password" style="display:block; padding:8px 16px; color:#333; font-size:13px; text-decoration:none;">Đổi mật khẩu</a>
                        <a href="${cp}/auth/logout" style="display:block; padding:8px 16px; color:#e53935; font-size:13px; text-decoration:none; border-top:1px solid #eee;">Đăng xuất</a>
                    </div>
                </div>
                <style>
                    .user-avatar-dropdown:hover .dropdown-menu-box,
                    .user-avatar-dropdown:focus-within .dropdown-menu-box { display: block !important; }
                    .user-avatar-dropdown .dropdown-menu-box a:hover { background: #f5f5f5; color: #d4af37 !important; }
                </style>
            </div>
        </header>
        <section class="portal-content"><jsp:include page="${contentPage}" /></section>
    </main>
</div>
<jsp:include page="footer.jsp" />
