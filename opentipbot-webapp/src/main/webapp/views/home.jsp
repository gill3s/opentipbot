<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div class="container">
    <div class="page-header" id="banner">

        <br>
        <br><br>
        <p class="lead"><span><img src="${user.profileImageUrl}"/></span>&nbsp;&nbsp;&nbsp;<spring:message code='home.dashboard'/> ${user.displayName}</p>
    </div>
</div>
<div class="container container-xs-height">
    <div class="row> row-xs-height">
        <div class="col-md-6">
            <div class="panel panel-success">
                <div class="panel-heading text-center">
                    <h3 class="panel-title"><spring:message code='home.available.balance'/></h3>
                </div>
                <div class="panel-body text-center">



                    <h5><spring:message code='home.balance'/></h5>

                    <p class="lead"><fmt:formatNumber type="number" minIntegerDigits="1" value="${user.balance}" /> BTC</p>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="panel panel-default">
                <div class="panel-heading text-center">
                    <h3 class="panel-title"><spring:message code='home.address'/></h3>
                </div>
                <div class="panel-body text-center">
                    <p><small>${user.coinAddress}</small></p>
                    <img src="/images/${user.id}"/>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="hidden-sm hidden-xs">
    <div class="container">
        <div class="panel panel-default">
            <div class="panel-heading text-center">
                <h3 class="panel-title"><spring:message code='home.tips.sent'/></h3>
            </div>
            <div class="panel-body">


                <table id="tips-sent-table" class="table table-hover " cellspacing="0" width="100%">
                    <thead>
                    <tr>
                        <th><spring:message code='home.stats.header.amount'/></th>
                        <th><spring:message code='home.stats.header.to'/></th>
                        <th><spring:message code='home.stats.header.date'/></th>
                        <th><spring:message code='home.stats.header.type'/></th>
                        <th><spring:message code='home.stats.header.status'/></th>
                    </tr>
                    </thead>
                </table>

            </div>
        </div>

        <div class="panel panel-default">
            <div class="panel-heading text-center">
                <h3 class="panel-title"><spring:message code='home.tips.received'/></h3>
            </div>
            <div class="panel-body">

                <table id="tips-received-table" class="table table-hover " cellspacing="0" width="100%">
                    <thead>
                    <tr>
                        <th><spring:message code='home.stats.header.amount'/></th>
                        <th><spring:message code='home.stats.header.from'/></th>
                        <th><spring:message code='home.stats.header.date'/></th>
                        <th><spring:message code='home.stats.header.type'/></th>
                        <th><spring:message code='home.stats.header.status'/></th>
                    </tr>
                    </thead>
                </table>

            </div>
        </div>

        <div class="panel panel-default .visible-lg-*">
            <div class="panel-heading text-center">
                <h3 class="panel-title"><spring:message code='home.withdrawals'/></h3>
            </div>
            <div class="panel-body">

                <table id="withdrawals-table" class="table table-hover " cellspacing="0" width="100%">
                    <thead>
                    <tr>
                        <th><spring:message code='home.stats.header.amount'/></th>
                        <th><spring:message code='home.stats.header.toAddress'/></th>
                        <th><spring:message code='home.stats.header.date'/></th>
                        <th><spring:message code='home.stats.header.txId'/></th>
                        <th><spring:message code='home.stats.header.status'/></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>

    </div>
</div>

<script>

    $(document).ready(function() {
        $('#tips-sent-table').dataTable( {
            "ajax": "/data/tipssent",
            "display": "compact",
            "searching": false,
            "sAjaxDataProp":"",
            "columns": [
                { "data": "amount" },
                { "data": "toUserName" },
                { "data": "when" },
                { "data": "type" },
                {
                    "data": "status",
                    "width": "12px",
                    "render": function ( data, type, row ) {
                        if (data == "0"){ return "<i class='glyphicon glyphicon-phone'></i>"}
                        else if (data == "1") { return "<i class='glyphicon glyphicon-refresh'></i>"}
                        else if (data == "2") { return "<i class='glyphicon glyphicon-ok-sign green'></i>"}
                        else if (data == "3") { return "<a href='#' data-toggle='tooltip' data-original-title='"+row.errorMessage+"'><i class='glyphicon glyphicon-warning-sign red'></i></a>"}
                        else return "";
                    }
                }
            ],
            "fnInitComplete": function(oSettings, json) {
                $('a').tooltip({placement: 'left'});
            }
        } );
    } );
    $(document).ready(function() {
        $('#tips-received-table').dataTable( {
            "ajax": "/data/tipsreceived",
            "display": "compact",
            "searching": false,
            "sAjaxDataProp":"",
            "columns": [
                { "data": "amount" },
                { "data": "fromUserName" },
                { "data": "when" },
                { "data": "type" },
                {
                    "data": "status",
                    "width": "12px",
                    "render": function ( data, type, row ) {
                        if (data == "0"){ return "<i class='glyphicon glyphicon-phone'></i>"}
                        else if (data == "1") { return "<i class='glyphicon glyphicon-refresh'></i>"}
                        else if (data == "2") { return "<i class='glyphicon glyphicon-ok-sign green'></i>"}
                        else if (data == "3") { return "<a href='#' data-toggle='tooltip' data-original-title='"+row.errorMessage+"'><i class='glyphicon glyphicon-warning-sign red'></i></a>"}
                        else return "";
                    }
                }
            ],
            "fnInitComplete": function(oSettings, json) {
                $('a').tooltip({placement: 'left'});
            }
        } );
    } );
    $(document).ready(function() {
        $('#withdrawals-table').dataTable( {
            "ajax": "/data/withdrawals",
            "display": "compact",
            "searching": false,
            "sAjaxDataProp":"",
            "columns": [
                { "data": "amount" },
                { "data": "toAddress" },
                { "data": "when" },
                { "data": "txId" },
                {
                    "data": "status",
                    "width": "12px",
                    "render": function ( data, type, row ) {
                        if (data == "0"){ return  "<i class='glyphicon glyphicon-phone'></i>"}
                        else if (data == "1") { return "<i class='glyphicon glyphicon-refresh'></i>"}
                        else if (data == "2") { return "<i class='glyphicon glyphicon-ok-sign green'></i>"}
                        else if (data == "3") { return "<a href='#' data-toggle='tooltip' data-original-title='"+row.errorMessage+"'><i class='glyphicon glyphicon-warning-sign red'></i></a>"}
                        else return "";
                    }
                }
            ],
            "fnInitComplete": function(oSettings, json) {
                $('a').tooltip({placement: 'left'});
            }

        } );
    } );
</script>
