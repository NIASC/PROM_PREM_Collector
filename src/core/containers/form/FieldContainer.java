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
	 * 
	 * @param allowEmptyEntries True if this container allows empty
	 * 		entry (answer/response).
	 * @param secretEntry True if the input should be hidden. Useful
	 * 		for entering sensitive information such as passwords.
	 * @param statement The statement to initialize this form with.
	 */
	public FieldContainer(boolean allowEmptyEntries, boolean secretEntry,
			String statement)
	{
		super(allowEmptyEntries);
		field = new FieldEntry(statement);
		secret = secretEntry;
	}

	@Override
	public boolean hasEntry()
	{
		return field.getEntry() != null
				&& (allowEmpty || !field.getEntry().isEmpty());
	}

	@Override
	public FormContainer copy()
	{
		FieldContainer fc = new FieldContainer(allowEmpty, secret, field.getStatement());
		fc.setEntry(field.getEntry());
		return fc;
	}
	
	@Override
	public String getEntry()
	{
		return field.getEntry();
	}
	
	/**
	 * Sets the content of this container. A field container is only
	 * allowed to contain one field so if this container already have
	 * an entry it will be overwritten
	 * 
	 * @param statement The statement that you want the user to
	 * 		respond to.
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
	 * Retrieves the statement (i.e. the statement that you request
	 * the user to respond to)
	 * 
	 * @return This container's statement.
	 */
	public String getStatement()
	{
		return field.getStatement();
	}
	
	/**
	 * 
	 * @return {@code true} if this form's entry should be hidden.
	 * 		{@code false} if it should be displayed in plain text.
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
	 * @return TODO
	 */
	public boolean setEntry(String entry)
	{
		if (entry == null)
			field.setEntry(entry);
		else
		{
			if (entry.trim().isEmpty() && !allowEmpty)
				return false;
			field.setEntry(entry.trim());
		}
		return true;
	}
	
	/* Protected */
	
	/* Private */

	private FieldEntry field;
	private boolean secret;
	
	/**
	 * This class is a data container for field entries. It is
	 * designed with extensibility in mind and it should be possible
	 * to modify FieldContainer to be able to container several field
	 * entries using this class as a container for the entries.
	 * 
	 * @author Marcus Malmquist
	 *
	 */
	private class FieldEntry
	{
		/* Public */
		
		/**
		 * Creates a field entry. The field is represented on the form
		 * of a key and a value. The key is typically the description
		 * of what you expect the value to be, e.g. in the form
		 * 'Age: 30' the key is 'Age' and the value is '30'.
		 * 
		 * @param statement This field's key/description.
		 */
		public FieldEntry(String statement)
		{
			this.statement = statement;
			entry = null;
		}
		
		/**
		 * 
		 * @return This field's key.
		 */
		public String getStatement()
		{
			return statement;
		}
		
		/**
		 * 
		 * @return This field's value.
		 */
		public String getEntry()
		{
			return entry;
		}
		
		/**
		 * Sets the value of this field.
		 * 
		 * @param entry The value to set for this field.
		 */
		public void setEntry(String entry)
		{
			this.entry = entry;
		}
		
		/* Protected */
		
		/* Private */

		private String statement;
		private String entry;
	}
}
