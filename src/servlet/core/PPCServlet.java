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
package servlet.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	private static final long serialVersionUID = -2340346250534805168L;
	private String message;
	
	@Override
	public void init() throws ServletException
	{
		// Do required initialization
		message = "PROM/PREM Collector";
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		// Set response content type
		response.setContentType("text/html");

		// Actual logic goes here.
		PrintWriter out = response.getWriter();
		out.println("<h1>" + message + "</h1>");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try{
			StringBuilder sb = new StringBuilder();
			BufferedReader br = request.getReader();
			String str = null;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			
			out.print(PPC.handleRequest(sb.toString()));
			out.flush();
			out.close();
		} catch (Exception e) {
			String msg = e.getMessage();
			logger.log(msg != null ? msg : "Could not process request", e);
		}
	}

	@Override
	public void destroy() {
		// do nothing.
	}
	
	private static PPCLogger logger = PPCLogger.getLogger();
}
