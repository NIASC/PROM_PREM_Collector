package se.nordicehealth.zzphony.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class PhonyPrintWriter extends PrintWriter {
	private StringBuilder sb;
	private String str;
	public String getMessage() {
		return str;
	}

	public PhonyPrintWriter(Writer out) {
		super(out);
		// TODO Auto-generated constructor stub
	}

	public PhonyPrintWriter(OutputStream out) {
		super(out);
		sb = new StringBuilder();
		// TODO Auto-generated constructor stub
	}

	public PhonyPrintWriter(String fileName) throws FileNotFoundException {
		super(fileName);
		// TODO Auto-generated constructor stub
	}

	public PhonyPrintWriter(File file) throws FileNotFoundException {
		super(file);
		// TODO Auto-generated constructor stub
	}

	public PhonyPrintWriter(Writer out, boolean autoFlush) {
		super(out, autoFlush);
		// TODO Auto-generated constructor stub
	}

	public PhonyPrintWriter(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
		// TODO Auto-generated constructor stub
	}

	public PhonyPrintWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(fileName, csn);
		// TODO Auto-generated constructor stub
	}

	public PhonyPrintWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(file, csn);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public void print(String s) {
		sb.append(s);
	}
	
	@Override
	public void flush() {
		str = sb.toString();
		sb = new StringBuilder();
	}
	
	@Override
	public void close() {
		
	}

}
