<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!-- Docs master nav -->
<header class="navbar navbar-default navbar-fixed-top" id="top" role="banner">
    <div class="container">
        <div class="navbar-header">
            <button class="navbar-toggle collapsed" type="button" data-toggle="collapse" data-target=".bs-navbar-collapse">
                <span class="sr-only"><spring:message code="header.toggle.navigation"/></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a href="/" class="navbar-brand"><i class="glyphicon glyphicon-home"></i>&nbsp;&nbsp; <spring:message code="project.name"/></a>
        </div>
        <nav class="collapse navbar-collapse bs-navbar-collapse" role="navigation">
            <ul class="nav navbar-nav">

            </ul>
            <c:if test="${not empty user}">
            <ul class="nav navbar-nav navbar-right">
                <li class=""><a href="<c:url value='/signout' />" title='<spring:message code="header.logout"/>'><spring:message code="header.logout"/>&nbsp;( ${user.userName} )</a></li>
            </ul>
            </c:if>
        </nav>
    </div>
</header>
