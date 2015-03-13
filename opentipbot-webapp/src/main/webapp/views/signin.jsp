<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<style>

    /* http://css-tricks.com/perfect-full-page-background-image/ */
    html {
        background: url('') no-repeat center center fixed;
        -webkit-background-size: cover;
        -moz-background-size: cover;
        -o-background-size: cover;
        background-size: cover;
    }

    body {
        height:100%;
        padding-top: 45px;
        background: transparent;
    }


    /* Override B3 .panel adding a subtly transparent background */
    .panel {
        background-color: rgba(255, 255, 255, 0.7);
        padding-top: 10px;;
    }

    .margin-base-vertical {
        margin: 11px 0;
    }

    .btn-primary{
        background-color:#00aced;
        border-color: #00aaca;
        -moz-border-radius: 4px;
        border-radius: 4px;
    }


</style>

<div class=" panel panel-default">
    <p class="text-center">
        <img src="/resources/img/opentipbot-beta-small.png"/>
    </p>

    <h3 class="text-center"><spring:message code='project.name'/></h3>


    <p class="lead text-center"><spring:message code='welcomePage.description'/></p>
    <form action="/signin/twitter" method="POST">
        <p class="text-center">
            <button type="submit" class="margin-base-vertical btn btn-primary btn-md"> <i class="fa fa-twitter"></i>&nbsp;&nbsp;<spring:message code='signin.with.twitter'/></button>
        </p>
    </form>

</div>
<div class=" panel panel-default">
    <div class="row">
        <div class="col-md-offset-1 col-md-5">
            <H2><spring:message code='signin.simple.title'/></H2>
            <p class="lead"><spring:message code='signin.simple'/></p>
        </div>
        <div class="col-md-5 col-md-offset-0">

            <H2><spring:message code='signin.last.tips'/></H2>
            <table class="table">
                <c:forEach items="${lastTips}" var="tip">
                    <tr>
                        <td><img src="${tip.profilePicUrl}"></td>
                        <td><p>${tip.tweet}</p></td>
                        <td><p>${tip.tipDate}</p></td>
                    </tr>

                </c:forEach>
            </table>
        </div>
    </div>

</div>


