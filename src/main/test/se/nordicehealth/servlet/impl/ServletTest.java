package se.nordicehealth.servlet.impl;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.LoggerForTesting;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.impl.Servlet;
import se.nordicehealth.zzphony.servlet.PhonyBufferedReader;
import se.nordicehealth.zzphony.servlet.PhonyClientRequestProcesser;
import se.nordicehealth.zzphony.servlet.PhonyOutputStream;
import se.nordicehealth.zzphony.servlet.PhonyPrintWriter;
import se.nordicehealth.zzphony.servlet.PhonyReader;
import se.nordicehealth.zzphony.servlet.PhonyServletRequest;
import se.nordicehealth.zzphony.servlet.PhonyServletResponse;

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
