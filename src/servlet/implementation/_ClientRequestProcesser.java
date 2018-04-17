package servlet.implementation;

public interface _ClientRequestProcesser {
	String handleRequest(String message, String remoteAddr, String hostAddr);
	void terminate();
}