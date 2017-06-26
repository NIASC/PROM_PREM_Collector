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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import core.containers.form.Field;
import core.containers.form.FieldContainer;
import core.containers.form.SingleOptionContainer;

/**
 * This class is an example of an implementation of
 * UserInterface_Interface. This implementation is done using
 * command-line interface (CLI) for simplicity of development.
 * 
 * @author Marcus Malmquist
 *
 */
public class UserInterface implements UserInterface_Interface
{
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	private Scanner in;
	private static final int LINE_LENGTH = 80;
	private static final String SEPARATION_CHARACTER = "-";
	private static String separation;
	
	static
	{
		StringBuilder sb = new StringBuilder(LINE_LENGTH);
		for (int i = 0 ; i < LINE_LENGTH; ++i)
			sb.append(SEPARATION_CHARACTER);
		separation = sb.toString();
	}
	
	public UserInterface()
	{
		in = new Scanner(System.in);
	}
	
	@Override
	public void close()
	{
		if (in != null)
			in.close();
	}
	
	/**
	 * Prints a line of characters to make it simple for the user
	 * to separate active interface stuff (forms, options, messages
	 * etc.) from inactive interface stuff. This part may only be
	 * relevant when using CLI.
	 * @param ps TODO
	 */
	private void separate(PrintStream ps)
	{
		ps.printf("%s\n", separation);
	}
	
	@Override
	public void displayError(String message)
	{
		separate(System.out);
		System.out.println(ANSI_RED);
		print(message, System.out);
		System.out.println(ANSI_RESET);
	}

	@Override
	public void displayMessage(String message)
	{
		print(message, System.out);
	}

	@Override
	public int displayLoginScreen()
	{
		separate(System.out);
		System.out.printf(
				"What would you like to do?\n%s\n%s\n%s\n",
				"1: Login", "2: Register", "0: Exit");
		int input = in.nextInt();
		in.reset();
		int out = UserInterface_Interface.ERROR;
		switch (input)
		{
		case 0: //exit
			out = UserInterface_Interface.EXIT;
			break;
		case 1:
			out = UserInterface_Interface.LOGIN;
			break;
		case 2:
			out = UserInterface_Interface.REGISTER;
			break;
		default:
			break;
		}
		return out;
	}
	
	@Override
	public HashMap<String, String> requestLoginDetails(String usernameKey, String passwordKey)
	{
		separate(System.out);
		HashMap<String, String> details = new HashMap<String, String>(2);
		System.out.printf("%s\n", "Enter username");
		details.put(usernameKey, in.next());
		in.reset();
		System.out.printf("%s\n", "Enter password");
		details.put(passwordKey, in.next());
		in.reset();
		return details;
	}

	@Override
	public int selectOption(SingleOptionContainer options)
	{
		separate(System.out);
		System.out.printf("Select option\n");
		HashMap<Integer, String> opt = options.getSOptions();
		for (Entry<Integer, String> e : opt.entrySet())
			System.out.printf("%d: %s\n", e.getKey(), e.getValue());
		int input = in.nextInt();
		in.reset();
		return input;
	}

	@Override
	public void displayForm(FieldContainer form)
	{
		separate(System.out);
		for (Entry<Integer, Field> e : form.get().entrySet())
		{
			Field f = e.getValue();
			System.out.printf("%d) %s: %s\n", e.getKey(), f.getKey(),
					(f.getValue() == null ? "" : f.getValue()));
			/* Sometimes the input is empty. not allowed. */
			String entry;
			while ((entry = in.nextLine()).equals(""));
			f.setValue(entry);
			in.reset();
		}
	}
	
	private void print(String message, PrintStream ps)
	{
		/* If the message is too wide it must be split up */
		StringBuilder sb = new StringBuilder();
		final int msgLength = message.length();
		int beginIndex = 0, step;
		do
		{
			step = (msgLength - beginIndex > LINE_LENGTH)
					? LINE_LENGTH : msgLength - beginIndex;
			sb.append(String.format("%s\n", message.substring(
					beginIndex, beginIndex + step)));
			beginIndex += step;
		} while (step == LINE_LENGTH); // while >LINE_LENGTH remains
		ps.printf("%s\n", sb.toString());
	}
	
	public Integer createSingleOption(SingleOptionContainer soc)
	{
		separate(System.out);
		System.out.printf("Select option\n");
		HashMap<Integer, String> opt = soc.getSOptions();
		Integer selected = soc.getSelected();
		for (Entry<Integer, String> e : opt.entrySet())
		{
			Integer id = e.getKey();
			if (selected != null && id == selected)
				System.out.printf("[%d]: %s\n", id, e.getValue());
			else
				System.out.printf(" %d : %s\n", id, e.getValue());
		}
		int input = in.nextInt();
		in.reset();
		return input;
	}
}
