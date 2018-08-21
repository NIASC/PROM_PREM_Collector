package se.nordicehealth.servlet.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.Util;
import se.nordicehealth.servlet.core.PPCFileHandlerUpdate;

public class ServletLoggerTest {
	ServletLogger logger;
	String logdir;
	String file_prefix = "unittest";
	File flogdir;
	
	@Before
	public void setUp() throws Exception {
		File f = File.createTempFile("ppcunit", null);
		logdir = f.getParent() + File.separator + "prom_prem_collector_unit_test";
		f.delete();
		flogdir = new File(logdir);
		if ((flogdir.exists() && !flogdir.isDirectory()) || (!flogdir.exists() && !flogdir.mkdir())) {
			throw new IOException("Error creating temporary log directory.");
		}
		
		PPCFileHandlerUpdate handler = new DiskFileHandlerUpdate(logdir, 0x10000, 1, file_prefix);
		Logger lgr = Logger.getLogger(ServletLoggerTest.class.getName());
		lgr.setLevel(Level.FINEST);
		logger = new ServletLogger(handler, lgr);
	}
	
	@After
	public void tearDown() throws Exception {
		File dir = new File(logdir);
		for (File f : dir.listFiles()) {
			f.delete();
		}
		dir.delete();
	}
	
	private String loadFile(File files[]) {
		try {
			for (File f : files) {
				if (!f.getName().endsWith(".lck")) {
					return new String(Util.readFile(new FileInputStream(f)), Charset.forName("UTF-8"));
				}
			}
		} catch (IOException e) { }
		return "";
	}

	@Test
	public void testLogDefaultLevel() {
		String message = "Test log message";
		logger.log(message);

		File logfiles[] = flogdir.listFiles();
		Assert.assertTrue(logfiles.length > 0);
		Assert.assertTrue(loadFile(logfiles).contains(message));
	}

	@Test
	public void testLogWithLevel() {
		String message = "Test log message";
		logger.log(Level.FINE, message);

		File logfiles[] = flogdir.listFiles();
		Assert.assertTrue(logfiles.length > 0);
		Assert.assertTrue(loadFile(logfiles).contains(message));
	}

	@Test
	public void testLogWithTooLowLevel() {
		String message = "Test log message";
		logger.log(Level.ALL, message);

		File logfiles[] = flogdir.listFiles();
		Assert.assertFalse(loadFile(logfiles).contains(message));
	}

	@Test
	public void testFatalLogAndAction() {
		String message = "Test log message";
		logger.log(message);

		File logfiles[] = flogdir.listFiles();
		Assert.assertTrue(logfiles.length > 0);
		Assert.assertTrue(loadFile(logfiles).contains(message));
	}

	@Test
	public void testLogStringException() {
		String message = "Test log message";
		String exceptionMessage = "A NullPointerException";
		try {
			throw new NullPointerException(exceptionMessage);
		} catch(NullPointerException e) {
			logger.log(message, e);
		}

		File logfiles[] = flogdir.listFiles();
		Assert.assertTrue(logfiles.length > 0);
		String logfile_contents = loadFile(logfiles);
		Assert.assertTrue(logfile_contents.contains(message));
		Assert.assertTrue(logfile_contents.contains(exceptionMessage));
	}

	@Test
	public void testLogLevelStringException() {
		String message = "Test log message";
		String exceptionMessage = "A NullPointerException";
		try {
			throw new NullPointerException(exceptionMessage);
		} catch(NullPointerException e) {
			logger.log(Level.FINER, message, e);
		}

		File logfiles[] = flogdir.listFiles();
		Assert.assertTrue(logfiles.length > 0);
		String logfile_contents = loadFile(logfiles);
		Assert.assertTrue(logfile_contents.contains(message));
		Assert.assertTrue(logfile_contents.contains(exceptionMessage));
	}

}
