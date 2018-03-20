
package james94jeans2.minimapsync.server.command;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.client.ClientWaypointManager;
import james94jeans2.minimapsync.network.packet.MinimapsyncSendPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncTabCompletionsPacket;
import james94jeans2.minimapsync.network.packethandling.MinimapsyncPacketPipeline;
import james94jeans2.minimapsync.server.ServerWaypointManager;
import james94jeans2.minimapsync.util.Waypoint;
import james94jeans2.minimapsync.util.WaypointList;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class CommandSendWaypoint extends net.minecraft.command.CommandBase
{

    private StringBuilder waypointBuilder;
	private String waypointName;
	private MinimapsyncPacketPipeline pipeline;
	private List tabCompletions;
	private int permissionLevel;
	private static CommandSendWaypoint instance;
	private boolean timedOut;
	
	public CommandSendWaypoint () {
		pipeline = Minimapsync.pipeline;
		permissionLevel = 0;
		instance = this;
	}
	
	public static CommandSendWaypoint getInstance () {
		return instance;
	}
	
	public void setPermissionLevel (int level) {
		permissionLevel = level;
	}
	
	public int getRequiredPermissionLevel()
    {
        return permissionLevel;
    }

	@Override
    public List getCommandAliases()
    {
        ArrayList list;
        list = new ArrayList();
        list.add("sw");
        return list;
    }
    
    @Override
    public String getCommandName()
    {
        return "sendwaypoint";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/sendwaypoint or /sw <name of waypoint>";
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws WrongUsageException, PlayerNotFoundException
    {
        if(args.length == 0)
        {
            throw new WrongUsageException(getCommandUsage(sender));
        }else
        {
            waypointBuilder = new StringBuilder();
            waypointBuilder.append(args[0]);
            if(args.length > 1)
            {
                int i = 1;
                while(i < args.length)
                {
                    waypointBuilder.append(" ");
                    waypointBuilder.append(args[i]);
                    i = i + 1;
                }
            }
            waypointName = waypointBuilder.toString();
            if(FMLCommonHandler.instance().getSide().isClient())
            {
            	//TODO send waypoint in LAN mode
            }else
            {
            	pipeline.sendTo(new MinimapsyncSendPacket(waypointName, getCommandSenderAsPlayer(sender).dimension, ((ServerWaypointManager)ServerWaypointManager.getInstance()).generateKey()), getCommandSenderAsPlayer(sender));
            	System.out.println(Thread.currentThread().getName());
            }
        }
    }
    
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] stringArray)
    {
    	try {
			pipeline.sendTo(new MinimapsyncTabCompletionsPacket(stringArray), getCommandSenderAsPlayer(sender));
		} catch (PlayerNotFoundException e) {
		}
    	return null;
    }
    
}
