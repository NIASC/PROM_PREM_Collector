package servlet.core.statistics.containers;

import java.util.List;

public class MultipleOption extends Statistics {
	public MultipleOption(int questionID, List<Integer> optionIdx) {
        super(questionID);
        for (Integer i : optionIdx) { put(i); }
    }
}
