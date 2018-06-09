package se.nordicehealth.zzphony.servlet;

import java.io.BufferedReader;
import java.io.Reader;

public class PhonyBufferedReader extends BufferedReader {
	private String nextLine;
	public void setNextLine(String str) {
		this.nextLine = str;
	}

	public PhonyBufferedReader(Reader in) {
		super(in);
		// TODO Auto-generated constructor stub
	}

	public PhonyBufferedReader(Reader in, int sz) {
		super(in, sz);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String readLine() {
		return getNextLine();
	}
	
	private String getNextLine() {
		String next = nextLine;
		nextLine = null;
		return next;
	}
	
	@Override
	public void close() {
		
	}

}
