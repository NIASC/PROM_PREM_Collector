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
package implement;

/**
 * This interface contains the methods required by the core part of
 * this program to function. The purpose of this interface is to give
 * the freedom of choosing your own method of encryption (if any!).
 * 
 * @author Marcus Malmquist
 *
 */
public interface Encryption_interface
{
	/**
	 * Creates a hashed version of the input string.
	 * 
	 * @param s The string to be hashed
	 * @param salt The salt to be used for hashing
	 * 
	 * @return The hashed version of the string
	 */
	public String hashString(String s, String salt);
	
	/**
	 * Generates a new salt to be used for hashing.
	 * 
	 * @return A new salt
	 */
	public String getNewSalt();
}
