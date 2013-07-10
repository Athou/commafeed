<HTML>
<HEAD>
	<TITLE>JBossEAP6.0 JSP snoop page</TITLE>
	<%@ page import="javax.servlet.http.HttpUtils,java.util.Enumeration" %>
	<%@ page import="java.lang.management.*" %>
	<%@ page import="java.util.*" %>
</HEAD>
<BODY>

<H1>WebApp JSP Snoop page</H1>
<img src="images/jbosscorp_logo.png">

<h2>JVM Memory Monitor</h2>
 
 
<table border="0" width="100%">
 
<tbody>
<tr>
<td colspan="2" align="center">
<h3>Memory MXBean</h3>
</td>
</tr>
 
<tr>
<td width="200">Heap Memory Usage</td>
<td>
<%=ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()%>
</td>
</tr>
 
<tr>
<td>Non-Heap Memory Usage</td>
<td>
<%=ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage()%>
</td>
</tr>
 
<tr>
<td colspan="2"> </td>
</tr>
 
<tr>
<td colspan="2" align="center">
<h3>Memory Pool MXBeans</h3>
</td>
</tr>
 
</tbody>
</table>
<%
Iterator iter = ManagementFactory.getMemoryPoolMXBeans().iterator();
while (iter.hasNext()) {
MemoryPoolMXBean item = (MemoryPoolMXBean) iter.next();
%>
 
<table style="border: 1px #98AAB1 solid;" border="0" width="100%">
 
<tbody>
<tr>
<td colspan="2" align="center"><strong><%= item.getName() %></strong></td>
</tr>
 
<tr>
<td width="200">Type</td>
<td><%= item.getType() %></td>
</tr>
 
<tr>
<td>Usage</td>
<td><%= item.getUsage() %></td>
</tr>
 
<tr>
<td>Peak Usage</td>
<td><%= item.getPeakUsage() %></td>
</tr>
 
<tr>
<td>Collection Usage</td>
<td><%= item.getCollectionUsage() %></td>
</tr>
 
</tbody>
</table>
 
 
<%
}
%>

<H2>Request information</H2>

<TABLE>
<TR>
	<TH align=right>Requested URL:</TH>
	<TD><%= HttpUtils.getRequestURL(request) %></TD>
</TR>
<TR>
	<TH align=right>Request method:</TH>
	<TD><%= request.getMethod() %></TD>
</TR>
<TR>
	<TH align=right>Request URI:</TH>
	<TD><%= request.getRequestURI() %></TD>
</TR>
<TR>
	<TH align=right>Request protocol:</TH>
	<TD><%= request.getProtocol() %></TD>
</TR>
<TR>
	<TH align=right>Servlet path:</TH>
	<TD><%= request.getServletPath() %></TD>
</TR>
<TR>
	<TH align=right>Path info:</TH>
	<TD><%= request.getPathInfo() %></TD>
</TR>
<TR>
	<TH align=right>Path translated:</TH>
	<TD><%= request.getPathTranslated() %></TD>
</TR>
<TR>
	<TH align=right>Query string:</TH>
	<TD><% if(request.getQueryString()!=null) out.write(request.getQueryString().replaceAll("<", "&lt;").replaceAll(">","&gt;")); %></TD>
</TR>
<TR>
	<TH align=right>Content length:</TH>
	<TD><%= request.getContentLength() %></TD>
</TR>
<TR>
	<TH align=right>Content type:</TH>
	<TD><%= request.getContentType() %></TD>
<TR>
<TR>
	<TH align=right>Server name:</TH>
	<TD><%= request.getServerName() %></TD>
<TR>
<TR>
	<TH align=right>Server port:</TH>
	<TD><%= request.getServerPort() %></TD>
<TR>
<TR>
	<TH align=right>Remote user:</TH>
	<TD><%= request.getRemoteUser() %></TD>
<TR>
<TR>
	<TH align=right>Remote address:</TH>
	<TD><%= request.getRemoteAddr() %></TD>
<TR>
<TR>
	<TH align=right>Remote host:</TH>
	<TD><%= request.getRemoteHost() %></TD>
<TR>
<TR>
	<TH align=right>Authorization scheme:</TH>
	<TD><%= request.getAuthType() %></TD>
<TR>
</TABLE>

<%
	Enumeration e = request.getHeaderNames();
	if(e != null && e.hasMoreElements()) {
%>
<H2>Request headers</H2>

<TABLE>
<TR>
	<TH align=left>Header:</TH>
	<TH align=left>Value:</TH>
</TR>
<%
		while(e.hasMoreElements()) {
			String k = (String) e.nextElement();
%>
<TR>
	<TD><%= k %></TD>
	<TD><%= request.getHeader(k) %></TD>
</TR>
<%
		}
%>
</TABLE>
<%
	}
%>


<%
	e = request.getParameterNames();
	if(e != null && e.hasMoreElements()) {
%>
<H2>Request parameters</H2>
<TABLE>
<TR valign=top>
	<TH align=left>Parameter:</TH>
	<TH align=left>Value:</TH>
	<TH align=left>Multiple values:</TH>
</TR>
<%
		while(e.hasMoreElements()) {
			String k = (String) e.nextElement();
			String val = request.getParameter(k);
			String vals[] = request.getParameterValues(k);
%>
<TR valign=top>
	<TD><%= k.replaceAll("<", "&lt;").replaceAll(">","&gt;") %></TD>
	<TD><%= val.replaceAll("<", "&lt;").replaceAll(">","&gt;") %></TD>
	<TD><%
			for(int i = 0; i < vals.length; i++) {
				if(i > 0)
					out.print("<BR>");
				out.print(vals[i].replaceAll("<", "&lt;").replaceAll(">","&gt;"));
			}
		%></TD>
</TR>
<%
		}
%>
</TABLE>
<%
	}
%>


<%
	e = request.getAttributeNames();
	if(e != null && e.hasMoreElements()) {
%>
<H2>Request Attributes</H2>
<TABLE>
<TR valign=top>
	<TH align=left>Attribute:</TH>
	<TH align=left>Value:</TH>
</TR>
<%
		while(e.hasMoreElements()) {
			String k = (String) e.nextElement();
			Object val = request.getAttribute(k);
%>
<TR valign=top>
	<TD><%= k.replaceAll("<", "&lt;").replaceAll(">","&gt;") %></TD>
	<TD><%= val.toString().replaceAll("<", "&lt;").replaceAll(">","&gt;") %></TD>
</TR>
<%
		}
%>
</TABLE>
<%
	}
%>


<%
	e = getServletConfig().getInitParameterNames();
	if(e != null && e.hasMoreElements()) {
%>
<H2>Init parameters</H2>
<TABLE>
<TR valign=top>
	<TH align=left>Parameter:</TH>
	<TH align=left>Value:</TH>
</TR>
<%
		while(e.hasMoreElements()) {
			String k = (String) e.nextElement();
			String val = getServletConfig().getInitParameter(k);
%>
<TR valign=top>
	<TD><%= k %></TD>
	<TD><%= val %></TD>
</TR>
<%
		}
%>
</TABLE>
<%
	}
%>

</BODY>
</HTML>

