package core.containers.form;

import java.util.HashMap;

/**
 * This class is a handles for Form objects. It allows you to group
 * several Form objects to create a complete Form that the user can
 * fill in.
 * The purpose of this class is to encapsulate this information into a
 * class so it can be passed as an argument and be easily modifiable.
 * 
 * @author Marcus Malmquist
 *
 */
public class FieldContainer
{
	private HashMap<Integer, Field> form;
	private HashMap<String, Integer> keyToID;
	private int formID;
	
	/**
	 * Initializes variables.
	 */
	public FieldContainer()
	{
		form = new HashMap<Integer, Field>();
		keyToID = new HashMap<String, Integer>();
		formID = 0;
	}
	
	/**
	 * Adds a Form to this container.
	 * 
	 * @param form The Form to add.
	 */
	public void addForm(Field form)
	{
		if (form == null)
			return;
		this.form.put(formID, form);
		keyToID.put(form.getKey(), formID++);
	}
	
	/**
	 * Retrieves the value from the Form associated with the
	 * supplied id.
	 * 
	 * @param id The form's id.
	 * @return The value contained in the form associated with
	 * 		the id.
	 */
	public String getValue(int id)
	{
		return form.get(id).getValue();
	}

	
	/**
	 * Retrieves the value from the Form associated with the
	 * supplied key.
	 * 
	 * @param key The form's name.
	 * @return The value contained in the form associated with
	 * 		the key.
	 */
	public String getValue(String key)
	{
		return getValue(keyToID.get(key));
	}
	
	/**
	 * Retrieves the ID of the form with the given key.
	 * 
	 * @param key The form's name.
	 * @return The ID of the form associated with the key.
	 */
	public int getID(String key)
	{
		return keyToID.get(key);
	}
	
	/**
	 * Puts the Form data contained in this class in an Integer-Form
	 * map.
	 * 
	 * @return A map containing a map-id and a Form.
	 */
	public HashMap<Integer, Field> get()
	{
		return form;
	}
}