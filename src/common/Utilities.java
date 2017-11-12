/** Utilities.java
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
package common;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * This class consists of static functions that can be useful but don't
 * really fit anywhere.
 * 
 * @author Marcus Malmquist
 *
 */
public class Utilities
{
	/* Public */
	
	/**
	 * Finds the system path to the folder where the supplied class
	 * is located. This function is useful for finding resources that
	 * are not included in the class path and/or needs write access.
	 * 
	 * @param <T> {@code c}'s class.
	 * @param c The class that the path finding will be based on.
	 * 
	 * @return The /path/to/class/c.
	 */
	public static <T> String getClassPath(Class<T> c)
	{
		URL url = c.getProtectionDomain().getCodeSource().getLocation();
		String projectPath = null;
		try {
			projectPath = URLDecoder.decode(url.getFile(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new File(projectPath).getParentFile().getPath();
	}
	
	/**
	 * Retrieves the the supplied file path as an input stream from the
	 * class path. This function is useful for opening files located in
	 * this project's class path, such ass config files and text files.
	 * 
	 * Example:
	 * If a resource called 'file.txt' is located in the package mypackage,
	 * then filePath should be "mypackage/file.txt" and the class should be
	 * any class which has the same class loader as the resource file.
	 * 
	 * @param <T> {@code c}'s class.
	 * @param c The class to get the class path from.
	 * 
	 * @param filePath The path to the file to retrieve as an
	 * 		InpuStream.
	 * 
	 * @return The InputStream from the file.
	 */
	public static <T> InputStream getResourceStream(Class<T> c, String filePath)
	{
		return c.getClassLoader().getResourceAsStream(filePath);
	}
	
	/* Protected */
	
	/* Private */
}
