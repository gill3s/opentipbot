<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!doctype html>
<html>
<head>
    <title><spring:message  code="project.title" /></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <link href="<c:url value='/resources/css/bootstrap.min.css'/>" rel="stylesheet"/>
    <link href="<c:url value='/resources/css/bootstrap.icon-large.min.css'/>" rel="stylesheet"/>
    <%--<link href="/resources/css/docs.min.css" rel="stylesheet">--%>
   <link href="data:text/css;charset=utf-8," data-href="/resources/css/bootstrap-theme.min.css" rel="stylesheet" id="bs-theme-stylesheet">
    <link href="<c:url value='/resources/css/bootstrap-social.css'  />" rel="stylesheet"/>
    <link href="<c:url value='/resources/css/jQuery.dataTables.css'  />" rel="stylesheet"/>
    <link href="<c:url value='/resources/css/opentipbot.css'  />" rel="stylesheet"/>
    <link href="<c:url value='/resources/css/font-awesome.css'  />" rel="stylesheet"/>
    <script src="<c:url value='/resources/js/jquery-1.9.1.min.js' />"></script>
    <script src="<c:url value='/resources/js/jquery.dataTables.min.js' />"></script>
    <link rel="icon" type="image/png" href="/resource/img/favicon.png">
</head>
<body>
<a class="sr-only sr-only-focusable" href="#content">Skip to main content</a>
<div id=wrap">

    <tiles:insertAttribute name="header" />

    <tiles:insertAttribute name="body" />
</div>
<!--[if IE]>
<script src="<c:url value='/resources/js/bootstrap.min.ie.js' />"></script>
<![endif]-->
<!--[if !IE]><!-->
<script src="<c:url value='/resources/js/bootstrap.min.js' />"></script>
<!--<![endif]-->

<tiles:insertAttribute name="footer" />
</body>
</html>