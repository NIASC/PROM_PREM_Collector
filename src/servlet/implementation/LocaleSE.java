package servlet.implementation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public enum LocaleSE implements servlet.core.interfaces.Locale
{
	SE;
	
	private static class PIDData {
		Date date;
		Integer lastFour;
	}
	
	static void parse(PIDData data, String pID, String format) throws ParseException {
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat(format, java.util.Locale.US);
		dateFormat.setLenient(false);
		data.date = dateFormat.parse(pID.substring(0, format.length()));
		data.lastFour = Integer.parseInt(pID.substring(pID.length()-4));
	}

	@Override
	public String formatPersonalID(String pID)
	{
		PIDData data = new PIDData();
		pID = pID.trim();
		try {
			switch(pID.length()) {
			
			case 11: // yymmdd-xxxx
				if (pID.charAt(6) != '-') { return null; }
			case 10: // yymmddxxx
				parse(data, pID, "yyMMdd");
				break;
				
			case 13: // yyyymmdd-xxxx
				if (pID.charAt(8) != '-') { return null; }
			case 12: // yyyymmddxxx
				parse(data, pID, "yyyyMMdd");
				break;
			default:
				throw new ParseException("Unknown format", 0);
			}
		} catch (ParseException pe) {
			return null;
		}
		return String.format(java.util.Locale.US, "%s-%04d",
				(new SimpleDateFormat("yyyyMMdd", java.util.Locale.US)).format(data.date),
				data.lastFour);
	}
}
