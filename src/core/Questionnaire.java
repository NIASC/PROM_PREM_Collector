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
package core;

import core.interfaces.Messages;
import core.interfaces.UserInterface;

/**
 * This class is the central point for the questionaire part of the
 * program. This is where the questionare loop is run from.
 * 
 * @author Marcus Malmquist
 *
 */
public class Questionnaire
{
	private UserHandle userHandle;
	private UserInterface userInterface;

	/**
	 * Initialize variables.
	 * 
	 * @param ui The active instance of the user interface.
	 * @param uh The active instance of the user handle.
	 */
	public Questionnaire(UserInterface ui, UserHandle uh)
	{
		userInterface = ui;
		userHandle = uh;
	}
	
	/**
	 * Starts the questionnaire.
	 */
	public void start()
	{
		if (!userHandle.isLoggedIn())
		{
			userInterface.displayError(Messages.getMessages().getError(
					Messages.ERROR_NOT_LOGGED_IN));
			return;
		}
		System.out.printf("%s\n", "Starting Questionnaire");
	}
}
