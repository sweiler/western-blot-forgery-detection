<%@page import="forgery.web.model.Report"%>
<%@page import="java.util.Map"%>
<%@page import="forgery.web.model.FileState"%>
<%@page import="forgery.web.model.UploadedFile"%>
<%@page import="java.util.List"%>
<%@page import="forgery.web.WebUtil" %>
<%@page import="forgery.web.I18N" %>
<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@include file="WEB-INF/header.jsp"%>
<%
tr.setScope("homepage");

%>

<div class="content">
	<div id="dropfield" class="drophere">
		<%
			@SuppressWarnings("unchecked")
			List<UploadedFile> files = (List<UploadedFile>) request
					.getAttribute("files");
			if (files != null) {
				for (UploadedFile f : files) {
					
		%>
		<div class='fileitem' data-id="<%= f.getId() %>">
		  <div class='deleteButton'></div>
			<div class='filethumb'>
				<img src='/ForgeryWeb/Thumbnails/<%=f.getId()%>' width='244'
					height='110' />
			</div>
			<div class='filename'><%= WebUtil.shorten(f.getFilename()) %></div>
			<div class='filestate' data-state="<%= f.getState().name() %>"><%= tr.__("filestate_" + f.getState().name()) %></div>
			<% if(f.getReports().size() > 0) { %>
			<a class='toReports' href="reports/<%= f.getId() %>"><%= tr.__("toReports") %></a>
			<% } %>
			<% if(f.getState() == FileState.NEWLY_CREATED || f.getState() == FileState.FINISHED) {
				String href = "javascript:void(0)";
				%>
			<div class="bigbutton" data-id="<%= f.getId() %>">
				<a href="<%= href %>"><%= tr.__("continue") %></a>
			</div>
			<% } %>
		</div>
		<%
			}
			}
		%>
		<div class="dropcontent">
			<p><%= tr.__("drop_files") %></p>
			<span class="description"><%= tr.__("acceptable_files") %></span>
		</div>
	</div>
</div>
<div id="comebacklater" class="menubutton register">
  <a href="javascript:void(0)"><%= tr.__("comeback") %></a>
</div>
<script type="text/javascript">
var lang_continue = "<%= tr.__("continue") %>";
var lang_toReports = "<%= tr.__("toReports") %>";
var lang_start = "<%= tr.__("start") %>";
var lang_roi = "<%= tr.__("roi_headline") %>";
var lang_comeback = "<%= tr.__("comeback_headline") %>";
var lang_comeback_desc = "<%= tr.__("comeback_desc") %>";
var lang_delete = "<%= tr.__("delete_confirm") %>";
var lang_states = {<% for(FileState s : FileState.values()) { %>"<%= s.name() %>" : "<%= tr.__("filestate_" + s.name()) %>",<% } %>"EXTRACTING" : "<%= tr.__("filestate_EXTRACTING") %>"};
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/spin.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.spin.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/drop.js"></script>

<%@include file="WEB-INF/footer.jsp"%>
