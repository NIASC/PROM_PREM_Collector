package servlet.core;

public interface PPCClientRequestProcesser {
	String handleRequest(String message, String remoteAddr, String hostAddr);
	void terminate();
}