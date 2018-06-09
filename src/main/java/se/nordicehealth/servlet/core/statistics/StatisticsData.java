package se.nordicehealth.servlet.core.statistics;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class StatisticsData  {
    
    public int getQuestionID() { return questionID; }
    
    public Set<Entry<Object, Integer>> getIdentifiersAndCount() {
    	return Collections.unmodifiableSet(identifiersAndCount.entrySet());
    }
    
    private final int questionID;
    private final Map<Object, Integer> identifiersAndCount;

    public StatisticsData(int questionID, Map<Object, Integer> identifierAndCount)  {
        this.questionID = questionID;
        this.identifiersAndCount = identifierAndCount;
    }
}
