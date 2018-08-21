package se.nordicehealth.servlet.core.stats.containers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Statistics  {
    public int question() { return question; }
    public List<Object> answerIdentifierAndText() {
        return Collections.unmodifiableList(answerIdentifiersWithText);
    }

    Statistics(int questionID) {
        this.question = questionID;
        answerIdentifiersWithText = new ArrayList<Object>();
    }

    void put(Object key) {
        answerIdentifiersWithText.add(key);
    }

    private int question;
    private List<Object> answerIdentifiersWithText;
}