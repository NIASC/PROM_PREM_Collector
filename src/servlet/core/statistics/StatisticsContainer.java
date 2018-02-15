package servlet.core.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import servlet.core.statistics.containers.Statistics;

public class StatisticsContainer
{
	public StatisticsContainer() {
	    answers = new TreeMap<>();
	}

	public void addResult(Statistics answer) {
		if (answer != null) {
		    addAnswer(answer.question(), answer);
		}
	}

	public List<StatisticsData> getStatistics() {
        List<StatisticsData> out = new ArrayList<>();
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

    private class StatementOccurrence {
        int question;
        Map<Object, Occurrence> sortedStatementCount;

        class Occurrence {
            int count;
            Occurrence() { count = 0; }
        }

        StatementOccurrence(int question) {
            this.question = question;
            sortedStatementCount = new TreeMap<>();
        }

        int getQuestionID() {
            return question;
        }

        Map<Object, Integer> getStatementCount() {
            Map<Object, Integer> statementCount = new TreeMap<>();
            for (Entry<Object, Occurrence> e : sortedStatementCount.entrySet()) {
                statementCount.put(e.getKey(), e.getValue().count);
            }
            return statementCount;
        }

        void addAnswer(Statistics statistics) {
            for (Object o : statistics.answerIdentifierAndText()) {
            	if (o == null) {
            		continue;
            	}
            	
                if (!sortedStatementCount.containsKey(o)) {
                    sortedStatementCount.put(o, new Occurrence());
                }
                sortedStatementCount.get(o).count++;
            }
        }
    }
}
