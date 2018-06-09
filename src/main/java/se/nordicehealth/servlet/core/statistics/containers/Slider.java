package se.nordicehealth.servlet.core.statistics.containers;

public class Slider extends Statistics  {
	public Slider(int questionID, int val) {
        super(questionID);
        put(val);
    }
}
