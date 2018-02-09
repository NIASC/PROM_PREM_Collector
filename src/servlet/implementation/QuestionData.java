package servlet.implementation;

import java.util.ArrayList;
import java.util.List;

public class QuestionData
{
	public List<String> options;
	public String type;
	public int id;
	public String question;
	public String description;
	public boolean optional;
	public int max_val;
	public int min_val;
	
	public QuestionData() {
		options = new ArrayList<String>();
	}
}
