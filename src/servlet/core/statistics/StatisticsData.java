package servlet.core.statistics;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class StatisticsData  {
    
    public int getQuestionID() { return questionID; }
    
    public Set<Entry<Object, Integer>> getIdentifiersAndCount() {
    	return Collections.unmodifiableSet(identifiersAndCount.entrySet());
    }
    
    final int questionID;
    final Map<Object, Integer> identifiersAndCount;

    StatisticsData(int questionID, Map<Object, Integer> identifierAndCount)  {
        this.questionID = questionID;
        this.identifiersAndCount = identifierAndCount;
    }
}
