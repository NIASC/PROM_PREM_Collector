/*! Locale.java
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

/**
 * This interface contains a collection of locale-related methods.
 * The purpose of those methods is to make this program usable
 * regardless of where it is used.
 * 
 * An example of a locale-related method is formatting a national
 * identification number.
 * 
 * @author Marcus Malmquist
 *
 */
public interface Locale
{
	/**
	 * This method creates a personal ID number from the supplied
	 * {@code pID}. The output format should be the same regardless of
	 * input format. If this method can not create a valid personal ID
	 * number from {@code pID} it should return {@code null}.
	 * 
	 * @param pID The personal ID number that is to be formatted.
	 * 
	 * @return A national identification number that has been created
	 * 		from the input {@code pID}
	 */
	String formatPersonalID(String pID);
}
