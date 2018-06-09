package se.nordicehealth.servlet.core;

import java.text.ParseException;

public interface PPCLocale {
	String formatPersonalID(String pID) throws ParseException;
}
