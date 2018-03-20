
package james94jeans2.minimapsync;

import james94jeans2.minimapsync.client.ClientWaypointManager;
import james94jeans2.minimapsync.client.MinimapKeyHandler;
import james94jeans2.minimapsync.client.MinimapsyncClientConfiguration;
import james94jeans2.minimapsync.network.MinimapsyncConnectionHandler;
import james94jeans2.minimapsync.network.packet.MinimapsyncCheckPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncDonePacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncInfoPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncKeyPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncRequestPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncSendPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncSendWaypointPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncTabCompletionsPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncWaypointPacket;
import james94jeans2.minimapsync.network.packethandling.MinimapsyncPacketPipeline;
import james94jeans2.minimapsync.server.MinimapsyncServerConfiguration;
import james94jeans2.minimapsync.server.ServerWaypointManager;
import james94jeans2.minimapsync.server.command.CommandSettings;
import james94jeans2.minimapsync.server.command.CommandSpecialWaypointManager;
import james94jeans2.minimapsync.server.command.CommandSpecialWaypointTeleport;
import james94jeans2.minimapsync.server.command.CommandWaypointManager;
import james94jeans2.minimapsync.server.command.CommandWaypointTeleport;
import james94jeans2.minimapsync.util.CommonProxy;
import james94jeans2.minimapsync.util.MinimapsyncConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import java.util.Map;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

/**
 * 
 * @version b0.9.1
 * @author james94jeans2 (Jens Leicht)
 * 
 */

@Mod (modid = "Minimapsync", useMetadata = false, name = "Minimapsync", version = Minimapsync.version, dependencies = "after:*")

public class Minimapsync
{
    
    public static final String version = "1.7.10-b0.9.1";
    public static MinimapsyncPacketPipeline pipeline;
    private MinimapKeyHandler runningKeyHandler;
    private MinimapsyncConfiguration config;
    public Logger logger;
    private int map;
    private boolean enabled = false;
    
    public Minimapsync () {
    	instance = this;
    }
    
    @Instance (value = "Minimapsync")
    public static Minimapsync instance;
    
    @SidedProxy (clientSide="james94jeans2.minimapsync.client.ClientProxy", serverSide="james94jeans2.minimapsync.server.ServerProxy")
    public static CommonProxy proxy;
    
    public boolean getEnabled()
    {
        return enabled;
    }
    
    public int getMap()
    {
        return map;
    }
    
    @EventHandler
    public void load (FMLInitializationEvent event)
    {
        proxy.setup();
    }
    
    @EventHandler
    public void postInit (FMLPostInitializationEvent event)
    {
    	if (FMLCommonHandler.instance().getSide().isClient()) {
    		FMLCommonHandler.instance().bus().register(new MinimapKeyHandler((ClientWaypointManager)ClientWaypointManager.getInstance()));
    	}
    }
    
    @EventHandler
    public void preInit (FMLPreInitializationEvent event)
    {
    	if (FMLCommonHandler.instance().getSide().isServer()) {
    		config = new MinimapsyncServerConfiguration(new Configuration(event.getSuggestedConfigurationFile()));
    	} else {
    		config = new MinimapsyncClientConfiguration(new Configuration(event.getSuggestedConfigurationFile()));
    	}
    	logger = event.getModLog();
    	logger.info("Minimapsync installed!");
    	pipeline = new MinimapsyncPacketPipeline();
    	pipeline.initialise();
    	pipeline.registerPacket(MinimapsyncWaypointPacket.class);
    	pipeline.registerPacket(MinimapsyncDonePacket.class);
    	pipeline.registerPacket(MinimapsyncKeyPacket.class);
    	pipeline.registerPacket(MinimapsyncCheckPacket.class);
    	pipeline.registerPacket(MinimapsyncSendPacket.class);
    	pipeline.registerPacket(MinimapsyncSendWaypointPacket.class);
    	pipeline.registerPacket(MinimapsyncRequestPacket.class);
    	pipeline.registerPacket(MinimapsyncTabCompletionsPacket.class);
    	pipeline.registerPacket(MinimapsyncInfoPacket.class);
    	FMLCommonHandler.instance().bus().register(new MinimapsyncConnectionHandler());
    }
    
    @EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        if(FMLCommonHandler.instance().getSide().isServer())
        {
        	ServerWaypointManager.getInstance().afterStartup();
        	//TODO disable weather changes
        	//MinecraftServer.getServer().getEntityWorld().getWorldInfo().setRainTime(Integer.MAX_VALUE);
        }
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
    	if (enabled) {
    		event.registerServerCommand(new CommandWaypointTeleport());
    		event.registerServerCommand(new CommandSpecialWaypointTeleport());
    	}
        if(event.getServer().isDedicatedServer())
        {
        	event.registerServerCommand(new CommandWaypointManager());
        	event.registerServerCommand(new CommandSpecialWaypointManager());
        	event.registerServerCommand(new CommandSettings());
        }
    }
    
    public void setEnabled(boolean pEnabled)
    {
        enabled = pEnabled;
    }
    
    public void setMap(int pMap)
    {
        map = pMap;
    }
    
    public MinimapsyncConfiguration getConfiguration () {
    	return config;
    }
    
    @NetworkCheckHandler
    public boolean checkConnection (Map<String,String> map, Side side) {
		if (map.containsKey("Minimapsync")) {
			if (!version.equals(map.get("Minimapsync"))) {
				return false;
			}
		}
    	return true;
    }
    
}
