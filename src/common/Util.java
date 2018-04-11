package common;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import res.Resources;
import servlet.core.ServletLogger;

public class Util {
	private static int maxFileSize = 10*1024*1024; // 10 MB
	
	public static String fileToString(String filepath, String charset) throws IOException, UnsupportedCharsetException {
		byte[] out = readFileFully(filepath);
		try {
			return new String(out, Charset.forName(charset));
		} catch (UnsupportedCharsetException e) {
			return new String(out, Charset.defaultCharset());
		}
	}
	
	public static byte[] readFileFully(String filepath) throws IOException {
		BufferedInputStream is = new BufferedInputStream(Resources.getStream(filepath));
		if (is.available() > maxFileSize) {
			ServletLogger.LOGGER.log(Level.WARNING, String.format("Resource %s too big (> %d bytes)", filepath, maxFileSize));
			throw new IOException("Resource is too large!");
		} else {
			byte[] buffer = new byte[maxFileSize];
			return copyToFit(buffer, bufferedRead(buffer, is));
		}
	}

	private static byte[] copyToFit(byte[] buffer, int size) {
		byte out[] = new byte[size];
		for(int i = 0; i < size; ++i) {
			out[i] = buffer[i];
		}
		return out;
	}

	private static int bufferedRead(byte[] buffer, BufferedInputStream buffIn) throws IOException {
		final int EOF = -1;
		int offset = 0;
		for (int size = 0; (size = buffIn.read(buffer, offset, 4096)) != EOF;) {
			offset += size;
		}
		return offset;
	}

	public static <T extends Object> List<T> joinLists(List<T> l1, List<T> l2) {
		List<T> out = new ArrayList<T>(l1.size() + l2.size());
		out.addAll(l1);
		out.addAll(l2);
		return out;
	}
}
