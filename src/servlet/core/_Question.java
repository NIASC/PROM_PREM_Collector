package servlet.core;

import java.util.ArrayList;
import java.util.List;

public class _Question {
	
	public List<String> options;
	public String type;
	public int id;
	public String question;
	public String description;
	public boolean optional;
	public int max_val;
	public int min_val;
	
	public _Question()
	{
		options = new ArrayList<String>();
	}
}
