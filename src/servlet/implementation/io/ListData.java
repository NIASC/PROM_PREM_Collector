package servlet.implementation.io;

import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;

public class ListData
{
	@Override
	public String toString() { return a.toString(); }
	public void add(String v) { l.add(v); }
	public Iterable<String> iterable() { return Collections.unmodifiableList(l); }
	
	
	@SuppressWarnings("unchecked")
	ListData(JSONArray a) {
		this.a = a != null ? a : new JSONArray();
		this.l = (List<String>) this.a;
	}

	private JSONArray a;
	private List<String> l;
}