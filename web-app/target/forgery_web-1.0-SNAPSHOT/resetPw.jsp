<%@page import="forgery.web.model.User"%>
<%@page import="java.util.List"%>
<%@page import="forgery.web.WebUtil" %>
<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@include file="WEB-INF/header.jsp"%>
<%
tr.setScope("resetPw");
%>

<div class="content">
	<h2><%= tr.__("headline") %></h2>
	<a href="${pageContext.request.contextPath}/index.html" class="backlink"><%= tr.__("back") %></a>
	<% if(request.getAttribute("sentMail") != null) { %>
	<div class="info-msg">
		<%= tr.__("sentMail") %>
	</div>
	<% } %>
	<% if(request.getAttribute("noUserFound") != null) { %>
	<div class="error-msg">
		<%= tr.__("noUserFound") %>
	</div>
	<% } %>
	<form method="POST">
		<table class="formtable">
		 <tr>
		   <th><%= tr.__("email") %>:</th>
		   <td><input type="email" name="email" /></td>
		 </tr>
	   <tr>
	     <td colspan="2"><input type="submit" value="<%= tr.__("requestLink") %>" /></td>
	   </tr>
		</table>
	</form>
</div>

<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/register.js"></script>

<%@include file="WEB-INF/footer.jsp"%>
