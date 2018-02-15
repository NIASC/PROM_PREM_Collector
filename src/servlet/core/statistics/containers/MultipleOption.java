package servlet.core.statistics.containers;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import servlet.core.statistics.Question;

public class MultipleOption extends Statistics {
	public MultipleOption(int questionID, List<Integer> optionIdx) {
        super(questionID);
        for (Integer i : optionIdx) { put(i); }
    }
}
