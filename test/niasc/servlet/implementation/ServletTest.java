package niasc.servlet.implementation;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import niasc.phony.servlet.PhonyBufferedReader;
import niasc.phony.servlet.PhonyClientRequestProcesser;
import niasc.phony.servlet.PhonyOutputStream;
import niasc.phony.servlet.PhonyPrintWriter;
import niasc.phony.servlet.PhonyReader;
import niasc.phony.servlet.PhonyServletRequest;
import niasc.phony.servlet.PhonyServletResponse;
import niasc.servlet.LoggerForTesting;
import servlet.core.PPCLogger;
import servlet.implementation.Servlet;

public class ServletTest {
	Servlet servlet;
	String mainPage = "<html><head><title>Phony Servlet</title></head></html>";
	String request = "phony request";
	PPCLogger logger;
	PhonyServletRequest req;
	PhonyServletResponse resp;
	
	PhonyBufferedReader pbr;
	PhonyPrintWriter ppw;
	

	@Before
	public void setUp() throws Exception {
		logger = new LoggerForTesting();
		servlet = new Servlet(mainPage, new PhonyClientRequestProcesser(), logger);
		
		pbr = new PhonyBufferedReader(new PhonyReader());
		req = new PhonyServletRequest(pbr);
		
		ppw = new PhonyPrintWriter(new PhonyOutputStream());
		resp = new PhonyServletResponse(ppw);
	}

	@Test
	public void testPresentMainPage() throws IOException, ServletException {
		servlet.presentMainPage(req, resp);
		Assert.assertEquals(mainPage, ppw.getMessage());
	}

	@Test
	public void testHandleRequest() throws IOException, ServletException {
		pbr.setNextLine(request);
		servlet.handleRequest(req, resp);
		Assert.assertEquals(request, ppw.getMessage());
	}

}
