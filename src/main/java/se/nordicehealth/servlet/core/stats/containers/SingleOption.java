package se.nordicehealth.servlet.core.stats.containers;

public class SingleOption extends Statistics {
	public SingleOption(int questionID, int optionIdx) {
        super(questionID);
        put(optionIdx);
    }
}
