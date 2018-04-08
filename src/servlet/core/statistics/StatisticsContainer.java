package servlet.core.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import servlet.core.statistics.containers.Statistics;

public class StatisticsContainer
{
	public StatisticsContainer() {
	    answers = new TreeMap<Integer, StatementOccurrence>();
	}

	public void addResult(Statistics answer) {
		if (answer != null) {
		    addAnswer(answer.question(), answer);
		}
	}

	public List<StatisticsData> getStatistics() {
        List<StatisticsData> out = new ArrayList<StatisticsData>();
        for (StatementOccurrence occurrence : answers.values()) {
            out.add(new StatisticsData(occurrence.getQuestionID(), occurrence.getStatementCount()));
        }
        return out;
	}

    private void addAnswer(int q, Statistics answer) {
        if (!answers.containsKey(q)) {
            answers.put(q, new StatementOccurrence(q));
        }
        answers.get(q).addAnswer(answer);
    }

    private Map<Integer, StatementOccurrence> answers;
}
