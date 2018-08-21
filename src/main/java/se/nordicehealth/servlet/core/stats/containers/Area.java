package se.nordicehealth.servlet.core.stats.containers;

public class Area extends Statistics {
	public Area(int questionID, String text) {
        super(questionID);
        put(text);
    }
}
