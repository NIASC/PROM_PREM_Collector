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
package implementation.containerdisplay;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import core.containers.form.FieldContainer;
import core.containers.form.SliderContainer;
import core.interfaces.UserInterface.FormComponentDisplay;

/**
 * This class is a displayable wrapper for {@code FieldContainer}.
 * It handles placing the {@code FieldContainer} in an object that
 * the implementation of the {@code UserInterface} can display.
 * 
 * @author Marcus Malmquist
 * 
 * @see FieldContainer
 * @see UserInterface
 *
 */
public class FieldDisplay extends JPanel implements FormComponentDisplay
{
	/* Public */
	
	@Override
	public void requestFocus()
	{
		field.requestFocus();
	}

	@Override
	public boolean fillEntry()
	{
		return fc.setEntry(field.getText());
	}

	@Override
	public boolean entryFilled()
	{
		fillEntry();
		return fc.hasEntry();
	}
	
	/* Protected */
	
	/**
	 * Creates a displayable wrapper for {@code fc}.
	 * 
	 * @param fc The instance of the {@code FieldContainer} that
	 * 		the instance of this {@code FieldDisplay} should act as a
	 * 		wrapper for.
	 * 
	 * @see FieldContainer
	 */
	protected FieldDisplay(FieldContainer fc)
	{
		setLayout(new BorderLayout());
		this.fc = fc;
		
		fieldLabel = new JLabel(String.format("%s: ", fc.getStatement()));
		add(fieldLabel, BorderLayout.WEST);
		if (fc.isSecret())
			field = new JPasswordField(32);
		else
		{
			field = new JTextField(32);
			field.setText(fc.getEntry());
		}
		
		field.setPreferredSize(new Dimension(80, 25));
		add(field, BorderLayout.CENTER);
	}
	
	/* Private */
	
	private static final long serialVersionUID = 2210804480530383502L;

	private FieldContainer fc;
	
	private JLabel fieldLabel;
	private JTextField field;
}
