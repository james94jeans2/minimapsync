
package james94jeans2.minimapsync.network;

import ibxm.Player;
import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.client.ClientWaypointManager;
import james94jeans2.minimapsync.client.MinimapKeyHandler;
import james94jeans2.minimapsync.network.packet.MinimapsyncCheckPacket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class MinimapsyncConnectionHandler
{
	
	public static MinimapsyncConnectionHandler instance;

    public MinimapsyncConnectionHandler()
    {
    	instance = this;
    }
    
    @SubscribeEvent
    public void clientLoggedIn(ClientConnectedToServerEvent event)
    {
        if(!Minimapsync.instance.getEnabled())
        {
        	Thread errorThread = new Thread () {
            	
            	public void run () {
            		boolean send = false;
            		while (!send) {
            			if (Minecraft.getMinecraft().thePlayer != null) {
            				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[Minimapsync]Synchronization-feature disabled due to the fact that no minimap was found during startup!"));
            				send = true;
            			}else {
            				try {
    							this.sleep(5000);
    						} catch (InterruptedException e) {
    						}
            			}
            		}
            	}
            	
            };
            errorThread.setName("Minimapsync Message Thread");
            errorThread.start();
        }
        Thread loginThread = new Thread () {
        	
        	public void run () {
        		boolean send = false;
        		while (!send) {
        			if (Minecraft.getMinecraft().theWorld != null) {
        				((ClientWaypointManager) ClientWaypointManager.getInstance()).sendCheckPacket();
        				send = true;
        			}else {
        				try {
							this.sleep(5000);
						} catch (InterruptedException e) {
						}
        			}
        		}
        	}
        	
        };
        loginThread.setName("Minimapsync Handshake Thread");
        loginThread.start();
    }

    @SubscribeEvent
    public void connectionClosed(ClientDisconnectionFromServerEvent event)
    {
    	Minimapsync.instance.logger.info("Connection closed!");
	    if(FMLCommonHandler.instance().getSide().isClient())
	    {
	    	ClientWaypointManager manager = (ClientWaypointManager) ClientWaypointManager.getInstance();
	        manager.setSyncOnServer(false);
	        manager.setLAN(false);
	        manager.setMode(1);
	        manager.resetWaypoints();
	        MinimapKeyHandler.getInstance().disable();
	    }
    }

    @SubscribeEvent
    public void playerLoggedIn(ServerConnectionFromClientEvent event)
    {
    	//TODO Message to players without minimapsync
        //if (WaypointHandler.getInstance().playerHasMinimapSync(((NetHandlerPlayServer)event.handler).playerEntity.getCommandSenderName())) {
       // 	Minimapsync.logger.info("Hehey, it works!");
       //}
    }
}
