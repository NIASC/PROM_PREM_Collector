package se.nordicehealth.servlet.core.stats.containers;

public class Slider extends Statistics  {
	public Slider(int questionID, int val) {
        super(questionID);
        put(val);
    }
}
