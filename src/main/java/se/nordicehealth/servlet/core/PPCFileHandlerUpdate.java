package se.nordicehealth.servlet.core;

import java.io.IOException;
import java.util.logging.Handler;

public interface PPCFileHandlerUpdate {
	Handler updateHandler() throws IOException, SecurityException;
}
