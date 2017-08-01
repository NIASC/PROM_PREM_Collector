/** MultipleOptionDisplay.java
 * 
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
package applet.implementation.containerdisplay;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import applet.core.containers.form.MultipleOptionContainer;
import applet.core.containers.form.SingleOptionContainer;
import applet.core.interfaces.Messages;
import applet.core.interfaces.UserInterface;
import applet.core.interfaces.UserInterface.FormComponentDisplay;
import applet.implementation.SwingComponents;

/**
 * This class is a displayable wrapper the for
 * {@code SingleOptionContainer}.
 * It handles placing the {@code SingleOptionContainer} in an object
 * that the implementation of the {@code UserInterface} can display.
 * 
 * @author Marcus Malmquist
 * 
 * @see SingleOptionContainer
 * @see UserInterface
 *
 */
public class MultipleOptionDisplay extends JPanel implements FormComponentDisplay, ItemListener
{
	/* Public */
	
	@Override
	public void requestFocus()
	{
		
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		if (e.getItemSelectable() instanceof AbstractButton)
		{
			AbstractButton button = (AbstractButton) e.getItemSelectable();
			if (options.get(button.getName()) == null)
				return;
			
			if (e.getStateChange() == ItemEvent.SELECTED)
				responseID.add(Integer.parseInt(button.getName()));
			else if (e.getStateChange() == ItemEvent.DESELECTED)
				responseID.remove(new Integer(button.getName()));
		}
	}

	@Override
	public boolean fillEntry()
	{
		return moc.setEntry(responseID);
	}

	@Override
	public boolean entryFilled() 
	{
		return moc.hasEntry();
	}
	
	/* Protected */
	
	/**
	 * Creates a displayable wrapper for {@code moc}.
	 * 
	 * @param moc The instance of the MultipleOptionContainer that
	 * 		the instance of this MultipleOptionDisplay should act as
	 * 		a wrapper for.
	 */
	protected MultipleOptionDisplay(MultipleOptionContainer moc)
	{
		setLayout(new BorderLayout());
		this.moc = moc;
		responseID = new ArrayList<Integer>();

		String description = "";
		String optional = Messages.getMessages().getInfo(
				Messages.INFO_UI_FORM_OPTIONAL);
		if (moc.getDescription() != null && !moc.getDescription().isEmpty())
			description = "\n\n"+moc.getDescription();
		JTextArea jta = AddTextArea(
				(moc.allowsEmpty() ? "("+optional+") " : "") + moc.getStatement()
				+ description + "\n", 0, 35);
		add(jta, BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets.bottom = gbc.insets.top = 1;
		gbc.gridx = 0;

		Map<Integer, String> opt = moc.getOptions();
		
		List<Integer> selected = moc.getSelectedIDs();
		
		options = new TreeMap<String, JCheckBox>();
		int gridy = 0;
		for (Entry<Integer, String> e : opt.entrySet())
		{
			gbc.gridy = gridy++;
			String buttonName = Integer.toString(e.getKey());
			JCheckBox btn = addToggleButton(
					e.getValue(), buttonName, this);
			if (selected.contains(e.getKey()))
				btn.setSelected(true);
			buttonPanel.add(btn, gbc);
			options.put(buttonName, btn);
		}
		add(buttonPanel, BorderLayout.WEST);
	}
	
	/* Private */
	
	private static final long serialVersionUID = 7314170750059865699L;
	private MultipleOptionContainer moc;
	private List<Integer> responseID;
	
	private Map<String, JCheckBox> options;
	
	private static JCheckBox addToggleButton(
			String buttonText, String name, ItemListener listener)
	{
		return SwingComponents.addToggleButton2(buttonText, name,
				null, false, null, null, null, null, listener);
	}
	
	private static JTextArea AddTextArea(String text, int rows, int columns)
	{
		return SwingComponents.makeTextArea(text, null, null, false,
				null, null, null, null, null, false, rows, columns);
	}
}
