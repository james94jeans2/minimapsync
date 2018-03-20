
package james94jeans2.minimapsync.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Logger;

import com.minimap.XaeroMinimap;
import com.minimap.minimap.Minimap;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.util.CommonProxy;
import journeymap.common.Journeymap;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class ClientProxy extends CommonProxy
{
    
    private String version;
    private Logger logger;
    
    @Override
    public void setup()
    {
    	logger = Minimapsync.instance.logger;
        try{
            version = reifnsk.minimap.ReiMinimap.version;
            Minimapsync.instance.setMap(2);
        }catch(NoClassDefFoundError e)
        {
            logger.fatal("Cannot find mod_ReiMinimap, so Minimapsync will check for voxelmap!");
        }
        if(version == null)
        {
        	try {
				logger.info("Found class " + com.thevoxelbox.voxelmap.l.class.getName() + " from VoxelMap.litemod");
				version = "version";
				Minimapsync.instance.setMap(1);
			} catch (Throwable e) {
				logger.fatal("Cannot find voxelmap or mod_ReiMinimap, so Minimapsync will check for JourneyMap!");
			}
        }
        if (version == null) {
        	try {
        		version = Journeymap.JM_VERSION.toString();
        		Minimapsync.instance.setMap(3);
        	} catch (Throwable e) {
        		logger.fatal("Cannot find voxelmap, mod_ReiMinimap or JourneyMap, looking for Xaeros Minimap!");
        	}
        }
        if (version == null) {
        	try {
        		version = XaeroMinimap.versionID;
        		Minimapsync.instance.setMap(4);
        	} catch (Throwable e) {
        		logger.fatal("Cannot find voxelmap, mod_ReiMinimap, JourneyMap or Xaeros Minimap, so Minimapsync will not work!");
        	}
        }
        if(version != null)
        {
            Minimapsync.instance.setEnabled(true);
        }else
        {
            Minimapsync.instance.setEnabled(false);
        }
        ClientWaypointManager manager = new ClientWaypointManager();
    }
    
    @Override
    public EntityPlayer getPlayer (MessageContext ctx) {
    	if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
    		return Minecraft.getMinecraft().thePlayer;
    	}
    	return ctx.getServerHandler().playerEntity;
    }
    
}
