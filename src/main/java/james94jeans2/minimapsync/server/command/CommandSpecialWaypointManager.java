
package james94jeans2.minimapsync.server.command;

import james94jeans2.minimapsync.server.ServerWaypointManager;
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

public class CommandSpecialWaypointManager extends net.minecraft.command.CommandBase
{

    private WaypointList waypoints;
    private int waypoint;
    
    @Override
    public List getCommandAliases()
    {
        ArrayList list;
        list = new ArrayList();
        list.add("wpmx");
        return list;
    }
    
    @Override
    public String getCommandName()
    {
        return "waypointmanagerx";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/wpx <command> <told number> //only use when told to!";
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws WrongUsageException, PlayerNotFoundException
    {
        if(args.length < 2 || args.length > 2)
        {
            throw new WrongUsageException(getCommandUsage(sender));
        }else
        {
            waypoint = Integer.parseInt(args[1]);
            waypoints = CommandWaypointManager.getWaypointList();
            if(waypoints == null)
            {
            	getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]There is currently no decision to be made by you!"));
            	return;
            }else{
            	if (args[0].equals("delpoint") || args[0].equals("dp")) {
            		delpoint(sender);
            		return;
            	}
            	if (args[0].equals("recpoint") || args[0].equals("rp")) {
            		recpoint(sender);
            		return;
	            }
            }
        }
    }
    
    private void delpoint (ICommandSender sender) throws PlayerNotFoundException {
    	if(getCommandSenderAsPlayer(sender).getCommandSenderName().equals(CommandWaypointManager.getUser()))
        {
            if(waypoint > (waypoints.size() - 1))
            {
                getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]This number is not in the list that was given to you!"));
            }else
            {
                Waypoint tempPoint = waypoints.get(waypoint);
                if(tempPoint != null)
                {
                    if(((ServerWaypointManager)ServerWaypointManager.getInstance()).delWaypoint(tempPoint.getDim(), tempPoint))
                    {
                    	getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]Deleted the waypoint \"" + tempPoint.getName() + "\" from the server's list!"));
                    }else{
                    	getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]Could not delete the waypoint \"" + tempPoint.getName() + "\" from the server's list!"));
                    }
                    CommandWaypointManager.resetWaypoints();
                    waypoints = null;
                }
            }
        }else{
            getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]There is currently no decision to be made by you!"));
        }
    }
    
    private void recpoint (ICommandSender sender) throws PlayerNotFoundException {
    	if(getCommandSenderAsPlayer(sender).getCommandSenderName().equals(CommandWaypointManager.getUser()))
        {
            if(waypoint > (waypoints.size() - 1))
            {
                getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]This number is not in the list that was given to you!"));
            }else
            {
                Waypoint tempPoint = waypoints.get(waypoint);
                if(tempPoint != null)
                {
                    if(((ServerWaypointManager)ServerWaypointManager.getInstance()).recWaypoint(tempPoint.getDim(), tempPoint))
                    {
                    	getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]Recovered the waypoint \"" + tempPoint.getName() + "\" to the server's list!"));
                    }else{
                    	getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]Could not recover the waypoint \"" + tempPoint.getName() + "\" to the server's list!"));
                    }
                    CommandWaypointManager.resetWaypoints();
                    waypoints = null;
                }
            }
        }else{
            getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]There is currently no decision to be made by you!"));
        }
    }
    
}
