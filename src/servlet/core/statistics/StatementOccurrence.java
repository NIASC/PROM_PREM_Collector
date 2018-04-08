package servlet.core.statistics;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import servlet.core.statistics.containers.Statistics;

public class StatementOccurrence {
    private int question;
    private Map<Object, Occurrence> sortedStatementCount;

    private class Occurrence {
        int count;
        Occurrence() { count = 0; }
    }

    public StatementOccurrence(int question) {
        this.question = question;
        sortedStatementCount = new TreeMap<Object, Occurrence>();
    }

    public int getQuestionID() {
        return question;
    }

    public Map<Object, Integer> getStatementCount() {
        Map<Object, Integer> statementCount = new TreeMap<Object, Integer>();
        for (Entry<Object, Occurrence> e : sortedStatementCount.entrySet()) {
            statementCount.put(e.getKey(), e.getValue().count);
        }
        return statementCount;
    }

    public void addAnswer(Statistics statistics) {
    	if (statistics == null || statistics.question() != question) {
    		return;
    	}
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