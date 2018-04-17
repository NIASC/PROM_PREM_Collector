package niasc.phony.servlet;

import java.io.IOException;
import java.io.Reader;

public class PhonyReader extends Reader {

	public PhonyReader() {
		// TODO Auto-generated constructor stub
	}

	public PhonyReader(Object lock) {
		super(lock);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

}
