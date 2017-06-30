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

import java.util.regex.Pattern;

import core.interfaces.UserInterface;
import core.interfaces.UserInterface.FormComponentDisplay;

/**
 * This class handles entry field object. It allows you to present a
 * statement or a question that you want the user to respond to. The
 * response can then be stored in this container and retrieved later.
 * The purpose of this class is to encapsulate this information into a
 * class so it can be passed as an argument and be easily modifiable.
 * 
 * @author Marcus Malmquist
 *
 */
public class FieldContainer extends FormContainer
{
	private FieldEntry field;
	private boolean secret;
	
	/**
	 * Initializes a field container that does not allow empty entries and
	 * does not have secret entries.
	 */
	public FieldContainer()
	{
		this(false, false, null);
	}
	
	/**
	 * Initializes this container with form with the supplied
	 * statement.
	 * @param allowEmptyEntries True if this container allows empty entry
	 * 		(answer/response).
	 * @param secretEntry True if the input should be hidden. Useful for
	 * 		entering sensitive information such as passwords.
	 * @param statement The statement to initialize this form with.
	 */
	public FieldContainer(boolean allowEmptyEntries, boolean secretEntry,
			String statement)
	{
		super(allowEmptyEntries);
		field = new FieldEntry(statement);
		secret = secretEntry;
	}
	
	/**
	 * Sets the content of this container. A field container is only
	 * allowed to contain one field so if thie container already have
	 * an entry it will be overwritten
	 * 
	 * @param statement The statement that you want the user to respond
	 * 		to.
	 */
	public void setField(String statement)
	{
		if (statement != null)
		{
			statement = statement.trim();
			if (!allowEmpty && statement.isEmpty())
				return;
		}
		field = new FieldEntry(statement);
	}
	
	/**
	 * Retrieves the statement (i.e. the statement that you request the
	 * user to respond to)
	 * 
	 * @return This container's statement.
	 */
	public String getStatement()
	{
		return field.getKey();
	}
	
	/**
	 * Retrieves the entry (i.e. the user input in response to the
	 * field's statement) of this container's field.
	 * 
	 * @return This container's user entry.
	 */
	public String getEntry()
	{
		return field.getValue();
	}
	
	/**
	 * 
	 * @return True if this form's entry should be hidden.
	 */
	public boolean isSecret()
	{
		return secret;
	}
	
	/**
	 * Sets the entry (i.e. the user input in response to the field's
	 * statement) of this container's field. Setting the entry to null
	 * can be used to reset the field.
	 * 
	 * @param entry The user entry to set.
	 */
	public void setEntry(String entry)
	{
		field.setValue(entry);
	}

	@Override
	public <T extends FormComponentDisplay> T getDisplayable(UserInterface ui)
	{
		return ui.createField(this);
	}

	@Override
	public boolean hasEntry()
	{
		return field.getValue() != null
				&& (allowEmpty || !field.getValue().isEmpty());
	}
	
	/**
	 * This class is a data container for field entries. It is designed
	 * with extensibility in mind and it should be possible to modify
	 * FieldContainer to be able to container several field entries
	 * using this class as a container for the entries.
	 * 
	 * @author Marcus Malmquist
	 *
	 */
	private class FieldEntry
	{
		private String entryKey;
		private String entryValue;
		
		/**
		 * Creates a field entry. The field is represented on the form
		 * of a key and a value. The key is typically the description
		 * of what you expect the value to be, e.g. in the form
		 * 'Age: 30' the key is 'Age' and the value is '30'.
		 * 
		 * @param key This field's key/description.
		 */
		public FieldEntry(String key)
		{
			entryKey = key;
			entryValue = null;
		}
		
		/**
		 * Copies this FieldEntry.
		 * 
		 * @return The copied FieldEntry.
		 */
		public FieldEntry copy()
		{
			FieldEntry fe = new FieldEntry(entryKey);
			fe.setValue(entryValue);
			return fe;
		}
		
		/**
		 * 
		 * @return This field's key.
		 */
		public String getKey()
		{
			return entryKey;
		}
		
		/**
		 * 
		 * @return This field's value.
		 */
		public String getValue()
		{
			return entryValue;
		}
		
		/**
		 * Sets the value of this field.
		 * 
		 * @param value The value to set for this field.
		 */
		public void setValue(String value)
		{
			entryValue = value;
		}
	}
}
