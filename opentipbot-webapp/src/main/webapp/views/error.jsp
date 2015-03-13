<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div class="container">
    <div class="row">
        <div class="page-header">
            <br><br><br>
            <h1><spring:message code='error.generic.header'/></h1>

        </div>

        <p><spring:message code='error.default.message'/> </p>
<%--        <p>
            Failed URL: ${url}
            Exception:  ${exception.message}
            <c:forEach items="${exception.stackTrace}" var="ste">    ${ste}
            </c:forEach>
        </p>--%>

    </div>
</div>