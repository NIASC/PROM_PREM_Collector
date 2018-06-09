package se.nordicehealth.zzphony.servlet;

import se.nordicehealth.servlet.core.PPCClientRequestProcesser;

public class PhonyClientRequestProcesser implements PPCClientRequestProcesser {
	@Override
	public String handleRequest(String message, String remoteAddr, String hostAddr) {
		return message;
	}
	@Override
	public void terminate() { }
}
