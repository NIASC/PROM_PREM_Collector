package niasc.phony.servlet;

import servlet.implementation._ClientRequestProcesser;

public class PhonyClientRequestProcesser implements _ClientRequestProcesser {
	@Override
	public String handleRequest(String message, String remoteAddr, String hostAddr) {
		return message;
	}
	@Override
	public void terminate() { }
}
