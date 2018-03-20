
package james94jeans2.minimapsync.server;

import java.util.logging.Logger;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.util.CommonProxy;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class ServerProxy extends CommonProxy{
    
    @Override
    public void setup()
    {
        MinimapsyncServerConfiguration config = (MinimapsyncServerConfiguration)Minimapsync.instance.getConfiguration();
        int mode = config.getMode();
        boolean autosync = config.getAutosync();
        boolean dynmap = config.getDynmap();
        Minimapsync.instance.logger.info("Minimapsync Configuration: " + mode + "|" + autosync + "|" + dynmap);
        boolean dynmapInstalled = false;
        try{
        	boolean test = org.dynmap.DynmapCore.ignore_chunk_loads;
        	dynmapInstalled = true;
        }catch(Throwable e)
        {
        	dynmapInstalled = false;
        }
        ServerWaypointManager manager = new ServerWaypointManager();
        manager.setMode(mode);
        manager.setDynmapEnabled(dynmap && dynmapInstalled);
        manager.setAutoSync(autosync);
        Minimapsync.instance.setEnabled(true);
        //Logger.getLogger("Minecraft").addHandler(new WaypointHandler());
        //FMLLog.getLogger().addHandler(new WaypointHandler());//Used to check if a player has minimapsync installed
    }
    
    @Override
    public EntityPlayer getPlayer (MessageContext ctx) {
    	return ctx.getServerHandler().playerEntity;
    }
    
}
