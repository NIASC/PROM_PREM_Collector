package se.nordicehealth.servlet.core.stats.containers;

import java.util.List;

public class MultipleOption extends Statistics {
	public MultipleOption(int questionID, List<Integer> optionIdx) {
        super(questionID);
        for (Integer i : optionIdx) { put(i); }
    }
}
