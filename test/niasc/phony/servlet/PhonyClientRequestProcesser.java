package niasc.phony.servlet;

import servlet.core.PPCClientRequestProcesser;

public class PhonyClientRequestProcesser implements PPCClientRequestProcesser {
	@Override
	public String handleRequest(String message, String remoteAddr, String hostAddr) {
		return message;
	}
	@Override
	public void terminate() { }
}
