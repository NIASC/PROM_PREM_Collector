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

import core.containers.form.FieldContainer;
import core.containers.form.FormContainer;
import core.containers.form.MultipleOptionContainer;
import core.containers.form.SingleOptionContainer;
import core.containers.form.SliderContainer;
import core.containers.form.TimePeriodContainer;
import core.interfaces.UserInterface.FormComponentDisplay;

/**
 * This class acts as an interface between the implementation of the
 * UserInterface and the implementations of the different form
 * container's displayable wrapper. It contains constructor calls
 * to these displayable wrapper classes.
 * 
 * The purpose of this class is to make sure that the implementation
 * of the UserInterface does not need to know how many different types
 * of form containers exist, what they are called and what they do.
 * This should facilitate the creation of new containers and their
 * wrappers.
 * 
 * @author Marcus Malmquist
 *
 */
public abstract class ContainerDisplays
{
	/* Public */
	
	/**
	 * Puts {@code fc} in a {@code FormComponentDisplay} wrapper.
	 * 
	 * {@code fc} should be an implementation of {@code FormContainer}
	 * and the returned wrapper should be an implementation of
	 * {@code FormComponentDisplay} which is specialized in displaying
	 * {@code fc}.
	 * 
	 * @param fc The {@code FormContainer} to put in a wrapper.
	 * 
	 * @return A wrapper object for the supplied form container.
	 * 
	 * @see FormContainer
	 * @see FormComponentDisplay
	 */
	public static FormComponentDisplay getDisplay(FormContainer fc)
	{
		if (fc instanceof SingleOptionContainer)
			return new SingleOptionDisplay((SingleOptionContainer) fc);
		else if (fc instanceof MultipleOptionContainer)
			return new MultipleOptionDisplay((MultipleOptionContainer) fc);
		else if (fc instanceof FieldContainer)
			return new FieldDisplay((FieldContainer) fc);
		else if (fc instanceof SliderContainer)
			return new SliderDisplay((SliderContainer) fc);
		else if (fc instanceof TimePeriodContainer)
			return new TimePeriodDisplay((TimePeriodContainer) fc);
		else
		{
			System.out.println("Unknown Form container");
			return null;
		}
	}
	
	/* Protected */
	
	/* Private */
}
