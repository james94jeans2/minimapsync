package james94jeans2.minimapsync.server;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.util.MinimapsyncConfiguration;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class MinimapsyncServerConfiguration extends MinimapsyncConfiguration{
	
	private Configuration config;
	private int mode;
	private boolean autosync, dynmap;
	
	public MinimapsyncServerConfiguration (Configuration config) {
		this.config = config;
		config.load();
        Property syncMode = config.get(Configuration.CATEGORY_GENERAL, "mode", 1);
        syncMode.comment = "Set this to 1 for normal synchronization,\n"
        		+ "2 for command-synchronization (ops only)";
        mode = syncMode.getInt(1);
        if (mode <= 0 || mode > 2) {
        	syncMode.set(1);
        	mode = 1;
        }
        Property autoSync = config.get(Configuration.CATEGORY_GENERAL, "syncOnJoin", false);
        autoSync.comment = "Set this to true to send all server waypoints to joining clients";
        autosync = autoSync.getBoolean(false);
        Property dynmapEnabled = config.get(Configuration.CATEGORY_GENERAL, "enableDynmapIntegration", true);
        dynmapEnabled.comment = "Set this to false to disable showing server waypoints in dynmap (if installed)";
        dynmap = dynmapEnabled.getBoolean(true);
        config.save();
	}
	
	public int getMode () {
		return mode;
	}

	public boolean getAutosync() {
		return autosync;
	}

	public boolean getDynmap() {
		return dynmap;
	}

	public void setDynmap(boolean aim) {
		dynmap = aim;
		writeSettings();
	}
	
	private void writeSettings () {
		config.load();
        Property syncMode = config.get(Configuration.CATEGORY_GENERAL, "mode", 1);
        syncMode.comment = "Set this to 1 for normal synchronization,\n"
        		+ "2 for command-synchronization (ops only)";
        syncMode.set(mode);
        Property autoSync = config.get(Configuration.CATEGORY_GENERAL, "syncOnJoin", false);
        autoSync.comment = "Set this to true to send all server waypoints to joining clients";
        autoSync.set(autosync);
        Property dynmapEnabled = config.get(Configuration.CATEGORY_GENERAL, "enableDynmapIntegration", true);
        dynmapEnabled.comment = "Set this to false to disable showing server waypoints in dynmap (if installed)";
        dynmapEnabled.set(dynmap);
        config.save();
	}

	public void setAutosync(boolean aim) {
		autosync = aim;
		writeSettings();
	}
	
}
