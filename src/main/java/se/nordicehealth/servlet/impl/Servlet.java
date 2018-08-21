package se.nordicehealth.servlet.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import se.nordicehealth.servlet.core.PPCClientRequestProcesser;
import se.nordicehealth.servlet.core.PPCLogger;

public class Servlet {

	public Servlet(String mainPage, PPCClientRequestProcesser ppc, PPCLogger logger) {
		this.message = mainPage;
		this.ppc = ppc;
		this.logger = logger;
	}
	
	public void terminate() {
		ppc.terminate();
	}

	public void presentMainPage(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html; charset=utf-8");
		
		writeResponse(resp, message);
	}

	public void handleRequest(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding("UTF-8");
		req.setCharacterEncoding("UTF-8");
		
		writeResponse(resp, processRequest(req.getRemoteAddr(), req.getLocalAddr(), readRequest(req)));
	}

	private void writeResponse(ServletResponse resp, String out) {
		PrintWriter pw = null;
		try {
			pw = resp.getWriter();
			pw.print(out);
			pw.flush();
		} catch (IOException e) {
			logger.log(Level.INFO, "Could not get writer from ServletResponse", e);
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	private String processRequest(String remoteAddr, String hostAddr, String request) {
		try {
			return ppc.handleRequest(request, remoteAddr, hostAddr);
		} catch (Exception e) {
			logger.log(e.getMessage() != null ? e.getMessage() : "Could not process request", e);
			return "";
		}
	}

	private String readRequest(ServletRequest req) {
		BufferedReader br = null;
		try {
			br = req.getReader();
			StringBuilder sb = new StringBuilder();
			for (String str; (str = br.readLine()) != null; sb.append(str))
				;
			return sb.toString();
		} catch (IOException e) {
			logger.log(e.getMessage() != null ? e.getMessage() : "Error reading ServletRequest", e);
			return "";
		} finally {
			if (br != null) {
				try { br.close(); } catch (IOException e) { }
			}
		}
	}

	private String message;
	private PPCClientRequestProcesser ppc;
	private PPCLogger logger;

}
