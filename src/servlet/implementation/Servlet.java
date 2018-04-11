package servlet.implementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Util;
import servlet.core.ServletLogger;

public class Servlet extends HttpServlet
{
	@Override
	public void init() throws ServletException {
		try {
			message = Util.fileToString(res.Resources.MAIN_PAGE, "UTF-8");
		} catch (IOException e) {
			message = "<html><head>404 - Page not found</head><body>The requested page was not found.</body></html>";
		} catch (UnsupportedCharsetException e) {
			message = "<html><head>404 - Page not found</head><body>The requested page was not found.</body></html>";
		}
		ppc = new ClientRequestProcesser();
	}

	@Override
	public void destroy() {
		ppc.terminate();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=utf-8");
		response.getWriter().printf(message);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setHttpAttributes(request, response);
		writeResponse(response, processRequest(request, readRequest(request)));
	}

	private void writeResponse(HttpServletResponse response, String out) {
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
			writeResponse(pw, out);
		} catch (IOException e) {
			String msg = e.getMessage();
			logger.log(msg != null ? msg : "Error writing HttpServletResponse", e);
		} finally {
			if (pw != null) {
				pw.close();
			}
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
		BufferedReader br = null;
		try {
			br = request.getReader();
			in = readRequest(br);
		} catch (IOException e) {
			String msg = e.getMessage();
			logger.log(msg != null ? msg : "Error reading HttpServletRequest", e);
		} finally {
			if (br != null) {
				try { br.close(); } catch (IOException e) { }
			}
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
