package james94jeans2.minimapsync.server;

import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * 
 * @version b0.8
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class WaypointHandler extends Handler {

	private LinkedList<String> data = new LinkedList<String>();
	private static WaypointHandler instance;
	
	public WaypointHandler () {
		instance = this;
	}
	
	public static WaypointHandler getInstance() {
		return instance;
	}
	
	@Override
	public void publish(LogRecord record) {
		String text = record.getMessage();
		if (text.contains("connecting with mods"))
		{
			if (text.contains("Minimapsync"))
			{
				String[] texts = text.split(" ");
				data.add(texts[1]);
			}
		}
	}
	
	public boolean playerHasMinimapSync (String username) {
		if (data.contains(username))
		{
			data.remove(username);
			return true;
		}else{
			return false;
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}

}
