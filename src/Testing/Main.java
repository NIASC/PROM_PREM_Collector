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
package Testing;

import core.interfaces.Messages;
import implementation.GUI_UserInterface;

public class Main
{
	public static void main(String[] args)
	{
		//printFonts();
		if (!Messages.getMessages().loadMessages())
		{
			System.out.printf("%s\n", Messages.DATABASE_ERROR);
			System.exit(1);
		}
		GUI_UserInterface qf1 = new GUI_UserInterface(false);
		qf1.start();
	}
}
