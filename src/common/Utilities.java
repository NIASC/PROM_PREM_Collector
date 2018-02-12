package common;

import java.io.InputStream;

public class Utilities {
	public static <T> InputStream getResourceStream(Class<T> c, String filePath) {
		return c != null ? c.getClassLoader().getResourceAsStream(filePath) : null;
	}
}
