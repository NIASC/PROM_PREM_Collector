/**
 * Copyright 2017 Marcus Malmquist
 * 
 * This file is part of PROM_PREM_Collector.
 * 
 * PROM_PREM_Collector is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * PROM_PREM_Collector is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PROM_PREM_Collector.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package core.containers.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * This class handles multiple-option objects. It allows you to
 * group several options with individual identifiers and text to form
 * a complete set of options to choose from as well as a way of
 * retrieving the selected option.
 * A multiple-option object consists of a statement and multiple options
 * to choose from and it allows the user to choose any number of the
 * options.
 * 
 * @author Marcus Malmquist
 * 
 * @see FormContainer
 *
 */
public class MultipleOptionContainer extends FormContainer
{
	/* Public */
	
	/**
	 * Creates a container for multiple-option objects.
	 * 
	 * @param allowEmptyEntry {@code true} if this container allows
	 * 		empty entry (answer/response). {@code false} if not.
	 * @param statement The statement that the user should respond to. The
	 * 		statement should be relevant to the options that are added
	 * 		later.
	 */
	public MultipleOptionContainer(boolean allowEmptyEntry, String statement)
	{
		super(allowEmptyEntry);
		this.statement = statement;
		
		options = new HashMap<Integer, Option>();
		selected = new HashMap<Integer, Boolean>();
		nextOption = 0;
		
		entries = new ArrayList<Integer>();
		entriesID = new ArrayList<Integer>();
		anySelected = false;
	}

	@Override
	public boolean hasEntry()
	{
		return allowEmpty || anySelected;
	}

	@Override
	public MultipleOptionContainer copy()
	{
		MultipleOptionContainer moc = new MultipleOptionContainer(allowEmpty, statement);
		for (Entry<Integer, Option> e : options.entrySet())
		{
			moc.addOption(e.getKey(), e.getValue().text);
			for (Integer i : entriesID)
				moc.setEntry(i);
		}
		return moc;
	}
	
	@Override
	public List<Integer> getEntry()
	{
		return Collections.unmodifiableList(entries);
	}
	
	/**
	 * Marks the option with the supplied ID as selected, if an option
	 * with that ID exists in the list of options.
	 * 
	 * @param id The ID of the selected option.
	 * @return True if an option with the supplied ID exists (and that
	 * 		option was marked as selected).
	 * 		False if not (and no option has been marked as selected).
	 */
	@Override
	public <T extends Object> boolean setEntry(T id)
	{
		if (!(id instanceof Integer))
			return false;
		
		return updateSelected((Integer) id);
	}
	
	/**
	 * Adds a new option to the container.
	 * 
	 * @param identifier The identifier of the option. This is
	 * 		typically used to identify what action should be taken
	 * 		if this option is selected.
	 * @param text The text that describes what this option means.
	 */
	public synchronized void addOption(int identifier, String text)
	{
		options.put(nextOption, new Option(identifier, text));
		selected.put(nextOption, false);
		nextOption++;
	}
	
	/**
	 * Puts the options in a map that contains the ID of the option as well
	 * as the option's text. The ID should be used to mark the selected
	 * option through the method setSelected.<br>
	 * The ID ranges from 0 <= ID < <no. entries>.
	 * 
	 * @return A map of the options, the keys are the ID of the options
	 * 		(the order at which they where added to this container,
	 * 		starting from 0).
	 */
	public HashMap<Integer, String> getOptions()
	{
		HashMap<Integer, String> sopts = new HashMap<Integer, String>();
		for (Entry<Integer, Option> e : options.entrySet())
			sopts.put(e.getKey(), e.getValue().text);
		return sopts;
	}
	
	/**
	 * 
	 * @return A list of the selected IDs
	 */
	public List<Integer> getSelectedIDs()
	{
		return Collections.unmodifiableList(entriesID);
	}
	
	/**
	 * 
	 * @return This container's statement.
	 */
	public String getStatement()
	{
		return statement;
	}
	
	/* Protected */
	
	/* Private */
	
	private String statement;
	private HashMap<Integer, Option> options;
	private int nextOption;
	private HashMap<Integer, Boolean> selected;
	private boolean anySelected;
	
	private List<Integer> entries, entriesID;
	
	private boolean updateSelected(Integer id)
	{
		if (id == null || selected.get(id) == null)
			return false;
		
		if (selected.get(id))
		{ // this id is selected; deselect
			entries.remove(new Integer(options.get(id).identifier));
			entriesID.remove(id);
		}
		else
		{ // this id is not selected; select
			entries.add(new Integer(options.get(id).identifier));
			entriesID.add(id);
		}
		selected.put(id, !selected.get(id));
		anySelected = entries.size() > 0;
		return true;
	}
	
	/**
	 * This class is a data container for single-option form entries.
	 * 
	 * @author Marcus Malmquist
	 *
	 */
	private final class Option
	{
		/**
		 * The identifier for this Option.
		 */
		final int identifier;
		/**
		 * The text for this Option.
		 */
		final String text;
		
		/**
		 * Initializes the class with an identifier and a text.
		 * The identifier is useful to make it easier to identify what
		 * action should be taken if this option is selected.
		 * 
		 * @param identifier The option identifier.
		 * @param text The option text.
		 */
		Option(int identifier, String text)
		{
			this.identifier = identifier;
			this.text = text;
		}
	}
}
