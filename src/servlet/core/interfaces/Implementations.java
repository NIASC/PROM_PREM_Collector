/** Implementations.java
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
package servlet.core.interfaces;

import servlet.implementation.MySQL_Database;
import servlet.implementation.SHA_Encryption;

/**
 * This class acts as an interface between the implementation of the
 * interfaces and the core program itself. It contains constructor calls
 * to the classes that implements the interfaces.
 * 
 * @author Marcus Malmquist
 *
 */
public abstract class Implementations
{
	/**
	 * Constructor for the implementation of {@code Database}.
	 * 
	 * @return A new/running instance of the current implementation of
	 * 		{@code Database}.
	 * 
	 * @see Database
	 */
	public static Database Database()
	{
		return MySQL_Database.getDatabase();
	}
	
	/**
	 * Constructor for the implementation of {@code Encryption}.
	 * 
	 * @return A new instance of the current implementation of
	 * 		{@code Encryption}.
	 * 
	 * @see Encryption
	 */
	public static Encryption Encryption()
	{
		return new SHA_Encryption();
	}
	
	/* Protected */
	
	/* Private */
}
