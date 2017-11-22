/** PPCServlet.java
 * 
 * Copyright 2017 Marcus Malmquist
 * 
 * This file is part of PROM_PREM_Collector.
 * 
 * PROM_PREM_Collector is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * PROM_PREM_Collector is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PROM_PREM_Collector.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package servlet.implementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import servlet.core.PPCLogger;

/**
 * This class is the PROM/PREM Collector servlet. It serves the applet with
 * information from the database and keeps track of which users that are
 * online.
 * 
 * @author Marcus Malmquist
 *
 */
public class PPCServlet extends HttpServlet
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
		ppc = new PPC();
	}

	@Override
	public void destroy() {
		ppc.terminate();
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException
	{
		response.setContentType("text/html");
		response.getWriter().printf(message);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException
	{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		BufferedReader br = null;
		PrintWriter pw = null;
		try {
			br = request.getReader();
			String in = readRequest(br);
			String out = ppc.handleRequest(in,
					request.getRemoteAddr(),
					request.getLocalAddr());
			pw = response.getWriter();
			writeResponse(pw, out);
		} catch (IOException e) {
			String msg = e.getMessage();
			logger.log(msg != null ? msg : "Could not process request", e);
		} finally {
			if (br != null)
				br.close();
			if (pw != null)
				pw.close();
		}
	}
	
	private String readRequest(BufferedReader br)
			throws IOException
	{
		StringBuilder sb = new StringBuilder();
		for (String str; (str = br.readLine()) != null;)
			sb.append(str);
		return sb.toString();
	}
	
	private void writeResponse(PrintWriter out, String response)
			throws IOException
	{
		out.print(response);
		out.flush();
	}

	private static final long serialVersionUID = -2340346250534805168L;
	private PPCLogger logger = PPCLogger.getLogger();
	private String message;
	private PPC ppc;
}
