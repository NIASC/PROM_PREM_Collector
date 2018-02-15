package servlet.core.statistics;

import java.util.List;

public class Question
{
    public int getID() { return id; }

    public boolean isOptional() { return optional; }

    public String getStatement() { return question; }

    public String getDescription() { return description; }

    public String getOption(int id) {
    	return id >= 0 && id < options.size() ? options.get(id) : null;
    }

    public Question(int id, String type, String question, String description,
                     List<String> options, boolean optional, Integer upper, Integer lower)
    {
        this.id = id;
        this.type = type;
        this.question = question;
        this.description = description;
        this.options = options;
        this.optional = optional;
        this.upper = upper;
        this.lower = lower;
    }

    private int id;
    private boolean optional;
    private String type;
    private String question;
    private String description;
    private List<String> options;
    private Integer upper;
    private Integer lower;
}
