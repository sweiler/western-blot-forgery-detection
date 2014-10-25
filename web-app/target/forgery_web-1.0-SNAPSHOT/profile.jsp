<%@page import="forgery.web.model.User"%>
<%@page import="java.util.List"%>
<%@page import="forgery.web.WebUtil" %>
<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@include file="WEB-INF/header.jsp"%>
<%
tr.setScope("profile");
User user = (User) request.getAttribute("user");
%>

<div class="content">
	<h2><%= tr.__("headline") %></h2>
	<a href="${pageContext.request.contextPath}/index.html" class="backlink"><%= tr.__("back") %></a>
	<% if(request.getAttribute("passwordEqual") != null && request.getAttribute("passwordEqual") == Boolean.FALSE) { %>
	<div class="error-msg">
		<%= tr.__("passwordsNotEqual") %>
	</div>
	<% } %>
	<% if(request.getAttribute("changedMail") != null) { %>
	<div class="ok-msg">
		<%= tr.__("changedMail") %>
	</div>
	<% } %>
	<% if(request.getAttribute("changedPw") != null) { %>
	<div class="ok-msg">
		<%= tr.__("changedPw") %>
	</div>
	<% } %>
	<form method="POST">
		<table class="formtable">
		 <tr>
		   <th><%= tr.__("email") %>:</th>
		   <td><input type="email" name="email" value="<%= user != null && user.getMail() != null ? user.getMail() : "" %>" /></td>
		 </tr>
		 <tr>
	     <th><%= tr.__("password") %>:</th>
	     <td><input type="password" name="password" /></td>
	   </tr>
	   <tr>
	     <th><%= tr.__("retypePassword") %>:</th>
	     <td><input type="password" name="passwordRe" /></td>
	   </tr>
	   <tr>
	     <td colspan="2"><input type="submit" value="<%= tr.__("save") %>" /></td>
	   </tr>
		</table>
	</form>
	<br />
	<br />
	<form id="deleteForm" method="POST">
	 <input type="hidden" name="delete" value="true" />
	 <input type="submit" value="<%= tr.__("delete") %>" />
	</form>
</div>

<script type="text/javascript">
$(document).ready(function () {
	$("#deleteForm").submit(function () {
		return window.confirm("<%= tr.__("confirm_delete") %>");
	});
});
</script>

<%@include file="WEB-INF/footer.jsp"%>
