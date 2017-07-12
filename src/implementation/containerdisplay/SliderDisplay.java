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
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import core.containers.form.FieldContainer;
import core.containers.form.SliderContainer;
import core.interfaces.UserInterface.FormComponentDisplay;
import implementation.SwingComponents;

/**
 * This class is a displayable wrapper for {@code SliderContainer}.
 * It handles placing the {@code SliderContainer} in an object that
 * the implementation of the {@code UserInterface} can display.
 * 
 * @author Marcus Malmquist
 * 
 * @see SliderContainer
 * @see UserInterface
 * 
 */
public class SliderDisplay extends JPanel implements FormComponentDisplay, ChangeListener
{
	/* Public */
	
	@Override
	public void requestFocus()
	{
		
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() instanceof JSlider)
		{
			JSlider s = (JSlider) e.getSource();
			if (!s.getValueIsAdjusting())
			{
				response = s.getValue();
			}
		}
	}

	@Override
	public boolean fillEntry()
	{
		if (response == null)
			return false;
		return sc.setEntry(response);
	}

	@Override
	public boolean entryFilled()
	{
		fillEntry();
		return sc.hasEntry();
	}
	
	/* Protected */
	
	/**
	 * Creates a displayable wrapper for {@code sc}.
	 * 
	 * @param sc The instance of the {@code SliderContainer} that
	 * 		the instance of this {@code SliderDisplay} should act as a
	 * 		wrapper for.
	 * 
	 * @see SliderContainer
	 */
	protected SliderDisplay(SliderContainer sc)
	{
		setLayout(new BorderLayout());
		this.sc = sc;
		response = null;

		JTextArea jtf = AddTextArea(sc.getStatement()
				+ (sc.getDescription() != null ? "\n"+sc.getDescription() : ""),
				0, 35);
		add(jtf, BorderLayout.NORTH);
		
		slider = makeSlider(this, sc.getLowerBound(), sc.getUpperBound());
		add(slider, BorderLayout.CENTER);
	}
	
	/* Private */
	
	private static final long serialVersionUID = 244108485873521112L;
	private SliderContainer sc;
	private Integer response;
	private JSlider slider;
	
	private static JTextArea AddTextArea(
			String text, int rows, int columns)
	{
		return SwingComponents.makeTextArea(text, null, null, false,
				null, null, null, null, null, false, rows, columns);
	}

	private static JSlider makeSlider(
			ChangeListener listener, int lowerBound, int upperBound)
	{
		return SwingComponents.makeSlider(null, null, null, false, null,
				null, null, null, listener, lowerBound, upperBound);
	}
}
