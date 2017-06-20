package core.containers;

import java.util.HashMap;

public class FormContainer
{
	private HashMap<Integer, Form> form;
	
	public FormContainer()
	{
		form = new HashMap<Integer, Form>();
	}
	
	/**
	 * Fills this container with form entries.
	 * 
	 * @param frm The form that should be stored in this container.
	 * 
	 * @return boolean True if the container was filled. False if
	 * 		an error occurred.
	 */
	public boolean fill(Form[] frm)
	{
		if (frm == null)
			return false;

		int id = 0;
		for (int i = 0; i < frm.length; ++i)
		{
			if (frm[i] != null)
				form.put(id++, frm[i]);
		}
		return true;
	}
	
	/**
	 * Puts the Form data contained in this class in an Integer-Form map.
	 * The Integer value is not related to the identifier in the Options class.
	 * 
	 * @return A map containing a map id and a Form.
	 */
	public HashMap<Integer, Form> get()
	{
		return form;
	}
}
