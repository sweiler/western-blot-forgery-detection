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
	tr.setScope("report");
	UploadedFile file = (UploadedFile) request.getAttribute("file");
	Report report = (Report) request.getAttribute("report");
	@SuppressWarnings("unchecked")
	List<ReportPair> pairs = (List<ReportPair>) request
			.getAttribute("pairs");

	DateFormat formatter = new SimpleDateFormat("dd.MM.yy HH:mm");
%>

<div class="content">
	<h2><%=tr.__("reportHeadline", file.getFilename())%></h2>
	<a href="${pageContext.request.contextPath}/index.html" class="backlink"><%= tr.__("back") %></a>
  <br />
	<div class="report_content">
		<img src='/ForgeryWeb/report/<%=report.getId()%>/img' 
		  data-width="<%= request.getAttribute("imgWidth") %>"
		  data-height="<%= request.getAttribute("imgHeight") %>"
		   />
	   <img class="print" src='/ForgeryWeb/report/<%=report.getId()%>/printImg' 
      data-width="<%= request.getAttribute("imgWidth") %>"
      data-height="<%= request.getAttribute("imgHeight") %>"
       />
		<div class="report_rects">
			<%
				for (ReportPair p : pairs) {
			%>
			<div class="report_pair" data-id="<%= p.getId() %>">
				<div class="report_rect" data-x="<%=p.getFirst().x%>"
					data-y="<%=p.getFirst().y%>"
					data-width="<%=p.getFirst().width%>"
					data-height="<%=p.getFirst().height%>"></div>
				<div class="report_rect" data-x="<%=p.getSecond().x%>"
					data-y="<%=p.getSecond().y%>"
					data-width="<%=p.getSecond().width%>"
					data-height="<%=p.getSecond().height%>"></div>
			</div>
			<%
				}
			%>
		</div>
		
	</div>
	<span style="font-weight: bold;"><%= tr.__("magnified") %></span>
	<div class="report_images">
   <%
     for (ReportPair p : pairs) {
   %>
   <img data-id="<%= p.getId() %>" src="${pageContext.request.contextPath}/reportPairs/<%= report.getId() %>/<%= p.getId() %>" />
   <%
     }
   %>
  </div>
	
	 <div class="report_header">
    <div class="report_filedate"><%=tr.__("fileDate",
          formatter.format(file.getCreated().getTime()))%></div>
    <div class="report_version"><%=tr.__("programVersion", report.getVersion())%></div>
    <div class="report_options"><%= tr.__("options", report.getOptions()) %></div>
  </div>
</div>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/enquire.min.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/report.js"></script>
<%@include file="WEB-INF/footer.jsp"%>