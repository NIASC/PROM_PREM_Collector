package servlet.implementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import servlet.core.ServletLogger;

public class Servlet extends HttpServlet
{
	@Override
	public void init() throws ServletException
	{
		message = String.join("\n", Arrays.asList(
				"<html>", 
				"<head>", 
				"<title>PROM/PREM Collector</title>", 
				"</head>",
				"<body bgcolor=white>", 
				"<table border=\"0\">", 
				"<tr>",
				"<td>",
				"<div align=\"center\">", 
				"<h1>PROM/PREM Collector</h1>", 
				"</div>",
				"<div align=\"center\">", 
				"<img src=\"images/NIASC_black.jpg\">",
				"</div>",
				"<div align=\"center\">", 
				"<p>",
				"The PROM/PREM Collector is developed by NIASC under the GPLv3 license. The web version is under development,<br>", 
				"but the application is available for download at the",
				"<a href=\"https://github.com/NIASC/PROM_PREM_Collector/tree/local_version\">NIASC GitHub page</a>.", 
				"</p>",
				"</div>", 
				"</td>",
				"</tr>",
				"</table>",
				"</body>",
				"</html>"
		));
		ppc = new ClientRequestProcesser();
	}

	@Override
	public void destroy() {
		ppc.terminate();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/html");
		response.getWriter().printf(message);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		setHttpAttributes(request, response);
		writeResponse(response, processRequest(request, readRequest(request)));
	}

	private void writeResponse(HttpServletResponse response, String out) {
		try (PrintWriter pw = response.getWriter()) {
			writeResponse(pw, out);
		} catch (IOException e) {
			String msg = e.getMessage();
			logger.log(msg != null ? msg : "Error writing HttpServletResponse", e);
		}
	}

	private String processRequest(HttpServletRequest request, String in) {
		String out = null;
		try {
			out = ppc.handleRequest(in, request.getRemoteAddr(), request.getLocalAddr());
		} catch (Exception e) {
			String msg = e.getMessage();
			logger.log(msg != null ? msg : "Could not process request", e);
		}
		return out;
	}

	private String readRequest(HttpServletRequest request) {
		String in = null;
		try (BufferedReader br = request.getReader()) {
			in = readRequest(br);
		} catch (IOException e) {
			String msg = e.getMessage();
			logger.log(msg != null ? msg : "Error reading HttpServletRequest", e);
		}
		return in;
	}

	private void setHttpAttributes(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException
	{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
	}
	
	private String readRequest(BufferedReader br) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (String str; (str = br.readLine()) != null; sb.append(str));
		return sb.toString();
	}
	
	private void writeResponse(PrintWriter out, String response) throws IOException {
		out.print(response);
		out.flush();
	}

	private static final long serialVersionUID = -2340346250534805168L;
	private ServletLogger logger = ServletLogger.LOGGER;
	private String message;
	private ClientRequestProcesser ppc;
}
