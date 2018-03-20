
package james94jeans2.minimapsync.server.command;

import james94jeans2.minimapsync.Minimapsync;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class CommandWaypointTeleport extends net.minecraft.command.CommandBase
{

    private StringBuilder waypointBuilder;
    private String waypointName;
    private static String player;
    private static WaypointList waypoints;
    private static CommandWaypointTeleport instance;
    
    public CommandWaypointTeleport () {
    	instance = this;
    }
    
    public static CommandWaypointTeleport getInstance () {
    	return instance;
    }
    
    static String getUser()
    {
        return player;
    }
    
    static WaypointList getWaypointList()
    {
        return waypoints;
    }
    
    static void resetWaypoints()
    {
        waypoints = null;
    }
    
    public static List<String> getCompletionList (EntityPlayer sender, String[] stringArray) {
    	ArrayList completions = new ArrayList();
        WaypointList localWaypoints = null;
        if(FMLCommonHandler.instance().getSide().isClient())
        {
        	if(ClientWaypointManager.getInstance().anyWaypointsInDimension(sender.dimension))
            {
                localWaypoints = ClientWaypointManager.getInstance().getWaypointsForDimension(sender.dimension);
            }
        }else
        {
            if(ServerWaypointManager.getInstance().anyWaypointsInDimension(sender.dimension))
            {
                localWaypoints = ServerWaypointManager.getInstance().getWaypointsForDimension(sender.dimension);
            }
        }
        if (localWaypoints != null) {
		    String[] names = new String[localWaypoints.size()];
		    for(Waypoint tempPoint : localWaypoints)
		    {
		    	names[localWaypoints.indexOf(tempPoint)] = tempPoint.getName();
		    }
		    completions = (ArrayList<String>) getTabCompletions(stringArray, names);
        }
        return completions;
    }
    
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] stringArray)
    {
        try {
			return this.getCompletionList(getCommandSenderAsPlayer(sender), stringArray);
		} catch (PlayerNotFoundException e) {
			return new ArrayList<String>();
		}
    }
    
    private static List getTabCompletions (String[] args, String[] waypoints) {
    	String[] argu = new String[1];
    	StringBuilder builder = new StringBuilder();
    	for (String string : args) {
    		if (!string.isEmpty()) {
    			builder.append(string + "_");
    		}
		}
    	if (!args[args.length - 1].isEmpty() && builder.length() > 0 && builder.charAt(builder.length() - 1) == '_') {
    		builder.deleteCharAt(builder.length() - 1);
    	}
    	argu[0] = builder.toString();
    	int length = 0;
    	for (String string : args) {
			if (!string.isEmpty()) {
				++length;
			}
		}
    	for (int i = 0; i < waypoints.length; ++i) {
    		waypoints[i] = waypoints[i].replace(' ', '_');
    	}
    	waypoints = ((ArrayList<String>)getListOfStringsMatchingLastWord(argu, waypoints)).toArray(new String[0]);
    	ArrayList<String> returnList = new ArrayList<String>();
    	if (waypoints.length > 1) {
	    	for (String string : waypoints) {
				String[] split = string.split("_");
				if (split.length > length) {
					if (args[args.length - 1].isEmpty()) {
						returnList.add(split[length]);
					} else {
						if (length > 0) {
							returnList.add(split[length - 1]);
						}
					}
				}
			}
    	} else {
    		if (waypoints.length > 0) {
				String[] split = waypoints[waypoints.length - 1].split("_");
				StringBuilder name = new StringBuilder();
				int i;
				if (args[args.length - 1].isEmpty()) {
					i = length;
				} else {
					i = length - 1;
				}
				for (; i < split.length; ++i) {
					name.append(split[i] + " ");
				}
				returnList.add(name.toString());
    		}
    	}
    	return returnList;
    }
    
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
        list.add("wtw");
        return list;
    }
    
    @Override
    public String getCommandName()
    {
        return "warptowaypoint";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/warptowaypoint or /wtw <name of waypoint>";
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws PlayerNotFoundException, WrongUsageException
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
            	if((waypoints = ClientWaypointManager.getInstance().checkWaypointTeleport(waypointName, getCommandSenderAsPlayer(sender))) != null)
                {
                    if(waypoints.size() == 1){
	                    double x = waypoints.get(0).getXCord();
	                    double y = waypoints.get(0).getYCord();
	                    double z = waypoints.get(0).getZCord();
	                    getCommandSenderAsPlayer(sender).setPositionAndUpdate(x, y, z);
	                    waypoints = null;
                    }else
	                {
	                    if(waypoints.size() == 0)
	                    {
	                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("The waypoint \"" + waypointName + "\" could not be found in your dimension!"));
	                        waypoints = null;
	                    }else
	                    {
	                        player = getCommandSenderAsPlayer(sender).getCommandSenderName();
	                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("There are " + waypoints.size() + " waypoints matching the name: \"" + waypointName + "\" in your dimension!"));
	                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("There are the following waypoints:"));
	                        for(Waypoint tempPoint : waypoints)
	                        {
	                            getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText(tempPoint.getName() + "  [ " + tempPoint.getXCord() + " | " + tempPoint.getYCord() + " | " + tempPoint.getZCord() + " ]  [" + waypoints.indexOf(tempPoint) + "]"));
	                        }
	                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("Type /wtwx followed by the number of the waypoint you want to teleport to!"));
	                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("The number can be found at the end of each waypoint line \"[number]\""));
	                    }
                    }
                }else
                {
                   getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("The waypoint \"" + waypointName + "\" could not be found in your dimension!")); 
                }
            }else
            {
                if((waypoints = ((ServerWaypointManager)ServerWaypointManager.getInstance()).checkWaypointTeleport(waypointName, getCommandSenderAsPlayer(sender))) != null)
                {
                    if(waypoints.size() == 1){
	                    double x = waypoints.get(0).getXCord();
	                    double y = waypoints.get(0).getYCord();
	                    double z = waypoints.get(0).getZCord();
	                    getCommandSenderAsPlayer(sender).setPositionAndUpdate(x, y, z);
	                    waypoints = null;
                    }else
	                {
	                    if(waypoints.size() == 0)
	                    {
	                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("The waypoint \"" + waypointName + "\" could not be found in your dimension!"));
	                        waypoints = null;
	                    }else
	                    {
	                        player = getCommandSenderAsPlayer(sender).getCommandSenderName();
	                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("There are " + waypoints.size() + " waypoints matching the name: \"" + waypointName + "\" in your dimension!"));
	                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("There are the following waypoints:"));
	                        for(Waypoint tempPoint : waypoints)
	                        {
	                            getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText(tempPoint.getName() + "  [ " + tempPoint.getXCord() + " | " + tempPoint.getYCord() + " | " + tempPoint.getZCord() + " ]  [" + waypoints.indexOf(tempPoint) + "]"));
	                        }
	                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("Type /wtwx followed by the number of the waypoint you want to teleport to!"));
	                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("The number can be found at the end of each waypoint line \"[number]\""));
	                    }
                    }
                }else
                {
                   getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("The waypoint \"" + waypointName + "\" could not be found in your dimension!")); 
                }
            }
        }
    }
    
}
