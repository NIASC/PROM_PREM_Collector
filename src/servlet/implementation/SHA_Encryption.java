/** SHA_Encryption.java
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
package servlet.implementation;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import servlet.core.interfaces.Encryption;

/**
 * This class is an example of an implementation of
 * Entryption_Interface.
 * 
 * @author Marcus Malmquist
 *
 */
public class SHA_Encryption implements Encryption
{
	/* Public */
	
	/**
	 * Initializes variables.
	 */
	public SHA_Encryption() throws NullPointerException
	{
		try
		{
			sr = SecureRandom.getInstance("SHA1PRNG");
			md = MessageDigest.getInstance("SHA-256");
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new NullPointerException(String.format(
					"WARNING: Hashing algorithms %s and/or %s is not "
					+ "available. You should not add sensitive information "
					+ "to the database.", "SHA1PRNG", "SHA-256"));
		}
	}

	@Override
	public String hashString(String s, String salt)
	{
		return encryptMessage("", s, salt);
	}

	@Override
	public String getNewSalt()
	{
    	byte[] salt = new byte[8];
    	sr.nextBytes(salt);
		return String.format("%016x", new BigInteger(1, salt));
	}
	
	@Override
	public String encryptMessage(
			String prepend, String message, String append)
					throws NullPointerException
	{
		if (prepend == null || message == null || append == null)
			throw new NullPointerException("got null string(s)!");
		
		String messageDigest = prepend + message + append;
		return String.format("%064x", new BigInteger(1,
				md.digest(messageDigest.getBytes())));
	}
	
	/* Protected */
	
	/* Private */
	private SecureRandom sr;
	private MessageDigest md;
}
