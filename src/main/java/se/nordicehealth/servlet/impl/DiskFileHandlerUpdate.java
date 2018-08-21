package se.nordicehealth.servlet.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;

import se.nordicehealth.servlet.core.PPCFileHandlerUpdate;

public class DiskFileHandlerUpdate implements PPCFileHandlerUpdate {
	private final SimpleDateFormat datefmt = new SimpleDateFormat("yyyy-MM-dd");
	private String logdir, filePrefix;
	private int logsize, logcount;
	
	public DiskFileHandlerUpdate(String logdir, int logsize, int logcount, String filePrefix) {
		this.logdir = logdir;
		this.logsize = logsize;
		this.logcount = logcount;
		this.filePrefix = String.format("%s_%s", filePrefix, new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
	}

	@Override
	public Handler updateHandler() throws IOException, SecurityException {
		return new FileHandler(String.format("%s/%s.log", logdir, genFileName(new Date())), logsize, logcount);
	}

	private String genFileName(Date today) {
		return String.format("%s@%s", filePrefix, datefmt.format(today));
	}

}
