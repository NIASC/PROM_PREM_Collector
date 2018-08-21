package se.nordicehealth.common;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

import se.nordicehealth.res.Resources;

public class Util {

	public static <T extends Object> List<T> joinLists(List<T> l1, List<T> l2) {
		List<T> out = new ArrayList<T>(l1.size() + l2.size());
		out.addAll(l1);
		out.addAll(l2);
		return out;
	}
	
	public static String loadFile(String filename) throws IOException {
		byte[] data = readFile(Resources.getStream(filename));
		try {
			return new String(data, Charset.forName("UTF-8"));
		} catch (UnsupportedCharsetException e) {
			return new String(data, Charset.defaultCharset());
		}
	}

	public static byte[] readFile(InputStream filepath) throws IOException {
		BufferedInputStream buffIn = new BufferedInputStream(filepath);
		int size = 0;
		byte dataBuffer[] = new byte[0x1000], buffer[] = new byte[0x400];
		for (int k; (k = buffIn.read(buffer, 0, 0x400)) != -1; size += k) {
			if (size + k > dataBuffer.length) {
				dataBuffer = memcpy(new byte[dataBuffer.length << 1], 0, dataBuffer, 0, size);
			}
			memcpy(dataBuffer, size, buffer, 0, k);
		}
		buffIn.close();
		return memcpy(new byte[size], 0, dataBuffer, 0, size);
	}
	
	private static byte[] memcpy(byte dst[], int dstOffset, byte src[], int srcOffset, int size) {
		while (size-- > 0) {
			dst[dstOffset++] = src[srcOffset++];
		}
		return dst;
	}
}
