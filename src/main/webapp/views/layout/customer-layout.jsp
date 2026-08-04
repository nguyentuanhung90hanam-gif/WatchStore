<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="adminLayout" value="false" scope="request" />
<jsp:include page="header.jsp" />
<jsp:include page="navbar.jsp" />
<jsp:include page="${contentPage}" />
<jsp:include page="footer.jsp" />
