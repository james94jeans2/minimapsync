
package james94jeans2.minimapsync.server.command;

import james94jeans2.minimapsync.client.ClientWaypointManager;
import james94jeans2.minimapsync.server.ServerWaypointManager;
import james94jeans2.minimapsync.util.Waypoint;
import james94jeans2.minimapsync.util.WaypointList;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class CommandSync extends net.minecraft.command.CommandBase {

    @Override
    public List getCommandAliases()
    {
        ArrayList list;
        list = new ArrayList();
        list.add("sync");
        return list;
    }

    @Override
    public String getCommandName()
    {
        return "minimapsync";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/minimapsync or /sync to sync your waypoints with the server";
    }
    
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws PlayerNotFoundException
    {
        if(FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
	    	((ServerWaypointManager)ServerWaypointManager.getInstance()).sendKeyPacket(getCommandSenderAsPlayer(sender));
        }
    }
    
}
