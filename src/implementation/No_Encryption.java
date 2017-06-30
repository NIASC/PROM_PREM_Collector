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
package implementation;

import core.interfaces.Encryption;

/**
 * This class is an example of an implementation of
 * Entryption_Interface.
 * 
 * @author Marcus Malmquist
 *
 */
public class No_Encryption implements Encryption
{
	/* Public */
	
	/**
	 * Initializes variables.
	 */
	public No_Encryption()
	{
		
	}

	@Override
	public String hashString(String s, String salt)
	{
		return s + salt;
	}

	@Override
	public String getNewSalt()
	{
		return "";
	}
	
	/* Protected */
	
	/* Private */
}
