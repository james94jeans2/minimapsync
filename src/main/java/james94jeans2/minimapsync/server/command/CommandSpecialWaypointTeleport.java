
package james94jeans2.minimapsync.server.command;

import james94jeans2.minimapsync.util.Waypoint;
import james94jeans2.minimapsync.util.WaypointList;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

/**
 * 
 * @version b0.8
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class CommandSpecialWaypointTeleport extends net.minecraft.command.CommandBase
{

    private WaypointList waypoints;
    private int waypoint;
    
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }
    
    @Override
    public List getCommandAliases()
    {
        ArrayList list;
        list = new ArrayList();
        list.add("wtwx");
        return list;
    }
    
    @Override
    public String getCommandName()
    {
        return "warptowaypointx";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/wtwx <told number> //only use when told to!";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws PlayerNotFoundException, WrongUsageException
    {
        if(args.length == 0)
        {
            throw new WrongUsageException(getCommandUsage(sender));
        }else
        {
            if(args.length > 1)
            {
                throw new WrongUsageException(getCommandUsage(sender));
            }else
            {
                waypoint = Integer.parseInt(args[0]);
                waypoints = CommandWaypointTeleport.getWaypointList();
                if(waypoints == null)
                {
                    throw new WrongUsageException(getCommandUsage(sender));
                }else{
                    if(getCommandSenderAsPlayer(sender).getCommandSenderName().equals(CommandWaypointTeleport.getUser()))
                    {
                        if(waypoint > (waypoints.size() - 1))
                        {
                        	getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]This number is not in the list that was given to you!"));
                        }else
                        {
                        	Waypoint teleportPoint = waypoints.get(waypoint);
                        	if(teleportPoint !=  null)
                        	{
                        		double x = teleportPoint.getXCord();
		                        double y = teleportPoint.getYCord();
		                        double z = teleportPoint.getZCord();
		                        getCommandSenderAsPlayer(sender).setPositionAndUpdate(x, y, z);
		                        CommandWaypointTeleport.resetWaypoints();
                        	}else
                        	{
                        		getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]There was a problem finding the waypoint you were searching for!"));
                        	}
			                waypoints = null;
                    	}
                    }else
                    {
                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]There is currently no decision to be made by you!"));
                    }
                }
            }
        }
    }
    
}
