
package james94jeans2.minimapsync.client;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.client.gui.GuiManager;
import james94jeans2.minimapsync.client.gui.GuiScreenLoading;
import james94jeans2.minimapsync.client.gui.GuiScreenSyncList;
import james94jeans2.minimapsync.util.AbstractWaypointManager;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenWorking;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class MinimapKeyHandler extends Thread
{
	
     private ClientWaypointManager manager;
     private KeyBinding binding = new KeyBinding("Minimapsync", 88, "key.categories.gameplay");
     private static MinimapKeyHandler instance;
     private boolean enabled, guiEnabled;
     private GuiManager guiMng;
        
     public MinimapKeyHandler (ClientWaypointManager pManager)
     {
        manager = pManager;
    	ClientRegistry.registerKeyBinding(binding);
    	instance = this;
    	guiMng = GuiManager.instance();
    	guiEnabled = ((MinimapsyncClientConfiguration) Minimapsync.instance.getConfiguration()).getEnableGui();
     }
     
     public void enable () {
    	 enabled = true;
     }
     
     public void disable () {
    	 enabled = false;
     }
     
     @SubscribeEvent
     public void keyPressed (KeyInputEvent event) {
    	 if (binding.isPressed() && enabled) {
    		 if (guiEnabled) {
    			 enabled = false;
    			 if (manager.getMode() == 1) {
    				 guiMng.displaySyncList();
    			 } else {
    				 manager.requestKey();
    			 }
    		 } else {
    			 if (manager.getLAN()) {
    				 manager.requestRemoteWaypoints();
    			 } else {
    				 manager.requestAllRemoteWaypoints();
    			 }
    		 }
    	 }
     }
     
     public static MinimapKeyHandler getInstance () {
    	 return instance;
     }
     
}
