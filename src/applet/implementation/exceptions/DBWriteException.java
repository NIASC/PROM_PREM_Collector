/** CalendarPanel.java
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
package applet.implementation.exceptions;

import java.util.Calendar;

/**
 * This class is an exception that should be thrown if an attempt to
 * write to the database fails.
 * 
 * @author Marcus Malmquist
 *
 * @see Calendar
 */
public class DBWriteException extends RuntimeException {

	public DBWriteException()
	{
		super();
	}
	
	public DBWriteException(String message)
	{
		super(message);
	}
	
	private static final long serialVersionUID = -7992403920499044994L;

}