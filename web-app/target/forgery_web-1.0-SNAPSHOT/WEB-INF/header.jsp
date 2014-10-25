<%@page import="java.util.Locale"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="forgery.web.model.User"%>
<%@page import="forgery.web.I18N"%>
<%
	I18N tr = (I18N) request.getAttribute("tr");
tr.setScope("header");
String email = (String) request.getAttribute("email");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
        "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link
	href="${pageContext.request.contextPath}/stylesheets/normalize.css"
	rel="stylesheet" type="text/css" />
<link href="${pageContext.request.contextPath}/stylesheets/screen.css"
	media="screen, projection" rel="stylesheet" type="text/css" />
<link href="${pageContext.request.contextPath}/stylesheets/print.css"
	media="print" rel="stylesheet" type="text/css" />
<!--[if IE]>
      <link href="${pageContext.request.contextPath}/stylesheets/ie.css" media="screen, projection" rel="stylesheet" type="text/css" />
  <![endif]-->
<script src="${pageContext.request.contextPath}/js/jquery-1.10.2.min.js"></script>
<script type="text/javascript">
	var contentPath = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/js/header.js"></script>
<%
	if (request.getAttribute("title") == null) {
%>
<title>Western blot Forgery Detection</title>
<%
	} else {
%>
<title><%=request.getAttribute("title")%> &lsaquo; Western
	blot Forgery Detection</title>
<%
	}
%>
</head>
<body>
	<div id="page">
		<div class="container">
			<div class="langselect">
				<form method="GET">
					<select name="L">
						<%
							for (String key : tr.supportedLanguages.keySet()) {
								String options = "";
								if (tr.getLocale().getLanguage()
										.equals(new Locale(key).getLanguage())) {
									options = " selected";
								}
						%>
						<option value="<%=key%>" <%=options%>><%=tr.supportedLanguages.get(key)%></option>
						<%
							}
						%>
					</select>
				</form>
			</div>
			<div class="header">Western blot Forgery Detection</div>
			<%
				if (request.getAttribute("user") == null) {
			%>
			<div class="menubutton loginButton"><%=tr.__("login")%></div>
			<div class="menubutton register">
				<a href="${pageContext.request.contextPath}/register"><%=tr.__("register")%></a>
			</div>
			<br style="clear: both" />
			<%
				if (request.getAttribute("login_error") != null) {
			%>
			<div class="login" data-error="true">
				<div class="error-msg"><%=tr.__("login_error")%></div>
				<%
					} else {
				%>
				<div class="login" data-error="false">
					<%
						}
					%>

					<form method="POST">
						<span><%=tr.__("email_address")%>:</span> <input type="email"
							name="emailLogin" value="<%=email%>" /> <span><%=tr.__("password")%>:</span>
						<input type="password" name="passwordLogin" /> <input
							type="submit" value="Login" />
					</form>
					<a href="${pageContext.request.contextPath}/resetPw" class="backlink"><%= tr.__("passwordReset") %></a>
				</div>
				<%
					} else {
				%>
				<%=tr.__("logged_in",
						((User) request.getAttribute("user")).getMail())%>
				<div class="logout">
					<form id="logoutForm" method="POST" action="${pageContext.request.contextPath}/logout">
					</form>
					<a id="logoutLink" href="javascript:void(0)">
						<%=tr.__("logout")%>
					</a>
					
				</div>
				<a class="profileLink" href="${pageContext.request.contextPath}/profile"><%= tr.__("profile") %></a>
				<%
					}
				%>