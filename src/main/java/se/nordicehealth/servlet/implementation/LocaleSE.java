package se.nordicehealth.servlet.implementation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import se.nordicehealth.servlet.core.PPCLocale;

public class LocaleSE implements PPCLocale {
	private PersonalIDFormatter
	yymmddxxxx = new YYMMDDXXXX(),
	yymmdd_xxxx = new YYMMDD_XXXX(),
	yyyymmddxxxx = new YYYYMMDDXXXX(),
	yyyymmdd_xxxx = new YYYYMMDD_XXXX();

	@Override
	public String formatPersonalID(String pID) throws ParseException {
		pID = pID.trim();
		switch (pID.length()) {
		case 10: return yymmddxxxx.format(pID);
		case 11: return yymmdd_xxxx.format(pID);
		case 12: return yyyymmddxxxx.format(pID);
		case 13: return yyyymmdd_xxxx.format(pID);
		default: throw new ParseException("Unknown format", 0);
		}
	}
	
	public int calculateControlDigit(int yymmddxxx) {
		List<Integer> ai = new ArrayList<Integer>();
		int idx = 0;
		for (Integer i : intToDigits(Math.abs(yymmddxxx) % 1000000000)) {
			ai.add(i * ((idx++ & 1) == 0 ? 2 : 1));
		}
		
		int sum = 0;
		for (Integer i : ai) {
			sum += i < 10 ? i : (i/10 + i%10);
		}
		return 10 - sum%10;
	}

	private List<Integer> intToDigits(int v) {
		List<Integer> l = new ArrayList<Integer>();
		while( v > 0) {
			l.add(v % 10);
			v /= 10;
		}
		return l;
	}
	
	private abstract class PersonalIDFormatter {
		DateFormat dateFormat;
		String format;
		
		PersonalIDFormatter(String format) {
			this.format = format;
			dateFormat = new SimpleDateFormat(format, Locale.US);
			dateFormat.setLenient(false);
		}
		
		String format(String raw) throws ParseException {
			Date dateOfBirth = dateFormat.parse(raw.substring(0, format.length()));
			int xxxx = Integer.parseInt(raw.substring(format.length(), format.length()+4));
			int yymmdd = Integer.parseInt((new SimpleDateFormat("yyMMdd", Locale.US)).format(dateOfBirth));
			if (calculateControlDigit(yymmdd*1000 + xxxx/10) == xxxx % 10) {
				return String.format(Locale.US, "%s-%04d", (new SimpleDateFormat("yyyyMMdd", Locale.US)).format(dateOfBirth), xxxx);
			} else {
				throw new ParseException("Control digit mismatch: got " + (xxxx % 10) + " but expected " + calculateControlDigit(yymmdd*1000 + xxxx/10), 0);
			}
		}
	}
	
	private class YYMMDDXXXX extends PersonalIDFormatter {
		YYMMDDXXXX() {
			super("yyMMdd");
		}
		
		@Override
		public String format(String raw) throws ParseException {
			if (Pattern.compile("[^\\p{Digit}]").matcher(raw).find()) {
				throw new ParseException("Unknown format", 0);
			}
			return super.format(raw);
		}
	}
	
	private class YYMMDD_XXXX extends PersonalIDFormatter {
		YYMMDD_XXXX() {
			super("yyMMdd");
		}
		
		@Override
		public String format(String raw) throws ParseException {
			if (raw.charAt(6) != '-') {
				throw new ParseException("Unknown format", 6);
			}
			return super.format(raw.replace("-", ""));
		}
	}
	
	private class YYYYMMDDXXXX extends PersonalIDFormatter {
		YYYYMMDDXXXX() {
			super("yyyyMMdd");
		}
		
		@Override
		public String format(String raw) throws ParseException {
			if (Pattern.compile("[^\\p{Digit}]").matcher(raw).find()) {
				throw new ParseException("Unknown format", 0);
			}
			return super.format(raw);
		}
	}
	
	private class YYYYMMDD_XXXX extends PersonalIDFormatter {
		YYYYMMDD_XXXX() {
			super("yyyyMMdd");
		}

		@Override
		public String format(String raw) throws ParseException {
			if (raw.charAt(8) != '-') {
				throw new ParseException("Unknown format", 8);
			}
			return super.format(raw.replace("-", ""));
		}
	}
}
