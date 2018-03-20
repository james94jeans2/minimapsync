package james94jeans2.minimapsync.client;

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

public class MinimapsyncClientConfiguration extends MinimapsyncConfiguration{
	
	private Configuration config;
	private boolean enableGui;
	
	public MinimapsyncClientConfiguration (Configuration config) {
		this.config = config;
		config.load();
        Property enableGui = config.get(Configuration.CATEGORY_GENERAL, "EnableGui", true);
        enableGui.comment = "Set this to disable the minimapsync GUI";
        this.enableGui = enableGui.getBoolean(true);
        config.save();
	}
	
	private void writeSettings () {
		config.load();
		Property enableGui = config.get(Configuration.CATEGORY_GENERAL, "EnableGui", true);
        enableGui.comment = "Set this to disable the minimapsync GUI";
        enableGui.set(this.enableGui);
        config.save();
	}
	
	public boolean getEnableGui () {
		return enableGui;
	}
	
}
