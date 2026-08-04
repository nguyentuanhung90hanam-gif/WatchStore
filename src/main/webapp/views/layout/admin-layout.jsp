<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="adminLayout" value="true" scope="request" />
<jsp:include page="header.jsp" />
<div class="portal-shell">
    <jsp:include page="sidebar.jsp" />
    <main class="portal-main">
        <header class="portal-topbar"><button class="icon-button portal-menu" data-sidebar-toggle>☰</button><div><small>WATCHSTORE / ${adminArea}</small><h1>${pageTitle}</h1></div><div class="topbar-actions"><a href="${cp}/page/notifications">♢</a><span>${empty sessionScope.user ? 'TN' : fn:substring(sessionScope.user.fullName, 0, 1)}</span></div></header>
        <section class="portal-content"><jsp:include page="${contentPage}" /></section>
    </main>
</div>
<jsp:include page="footer.jsp" />
