<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Formatter"%>
<%@page import="forgery.web.model.ReportPair"%>
<%@page import="java.util.List"%>
<%@page import="forgery.web.model.Report"%>
<%@page import="forgery.web.model.UploadedFile"%>
<%@page import="forgery.web.I18N"%>
<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@include file="WEB-INF/header.jsp"%>
<%
	tr.setScope("listReports");
	UploadedFile file = (UploadedFile) request.getAttribute("file");
	Report[] reports = (Report[]) request.getAttribute("reports");
	@SuppressWarnings("unchecked")
	List<ReportPair> pairs = (List<ReportPair>) request
			.getAttribute("pairs");

	DateFormat formatter = new SimpleDateFormat("dd.MM.yy HH:mm");
%>

<div class="content">
	<h2><%=tr.__("headline", file.getFilename())%></h2>
	<a href="${pageContext.request.contextPath}/index.html" class="backlink"><%= tr.__("back") %></a>
	<table class="formtable">
		<tr><th><%= tr.__("date") %></th><th>&nbsp;</th></tr>
		<% for(Report r : reports) { %>
		<tr><td><%= formatter.format(r.getCreated().getTime()) %></td><td><a href="${pageContext.request.contextPath}/report/<%= r.getId() %>"><%= tr.__("toReport") %></a></td></tr>
		<% } %>
	</table>
</div>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/jquery.spin.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/report.js"></script>
<%@include file="WEB-INF/footer.jsp"%>