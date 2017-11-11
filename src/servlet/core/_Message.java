package servlet.core;

import java.util.HashMap;
import java.util.Map;

public class _Message
{
	public String name;
	public String code;
	public Map<String, String> msg;
	
	public _Message() {
		msg = new HashMap<String, String>();
	}
	
	public void addMessage(String locale, String message)
	{
		msg.put(locale, message);
	}
}
