
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
import net.minecraft.util.ChatComponentText;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class CommandWaypointManager extends net.minecraft.command.CommandBase{
    
    private StringBuilder waypointBuilder;
    private String waypointName;
    private static String player;
    private static WaypointList waypoints;

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
    
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if(FMLCommonHandler.instance().getSide().isServer())
        {
        	if (args.length == 1) {
        		return getListOfStringsMatchingLastWord(args, new String[]{"dp", "rp", "delpoint", "recpoint"});
        	}else {
        		String[] args2 = new String[args.length - 1];
        		for (int i = 1; i < args.length; ++i) {
        			args2[i - 1] = args[i];
        		}
        		if (args[0].equals("delpoint") || args[0].equals("dp")) {
        			return deltab(sender, args2);
        		}
        		if (args[0].equals("recpoint") || args[0].equals("rp")) {
        			return rectab(sender, args2);
        		}
        	}
        }
        return new ArrayList();
    }
    
    private List deltab (ICommandSender sender, String [] args) {
    	if(!ServerWaypointManager.getInstance().noWaypoints())
        {
            WaypointList tempList = ServerWaypointManager.getInstance().getWaypoints();
            String[] names = new String[tempList.size()];
            for(Waypoint tempPoint : tempList)
            {
            	names[tempList.indexOf(tempPoint)] = tempPoint.getName();
            }
            return getTabCompletions(args, names);
        }
    	return new ArrayList();
    }
    
    private List getTabCompletions (String[] args, String[] waypoints) {
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
    
    private List rectab (ICommandSender sender, String [] args) {
    	if(!((ServerWaypointManager)ServerWaypointManager.getInstance()).noBlacklists())
        {
            WaypointList tempList = ((ServerWaypointManager)ServerWaypointManager.getInstance()).getBlacklistedWaypoints();
            String[] names = new String[tempList.size()];
            for(Waypoint tempPoint : tempList)
            {
            	names[tempList.indexOf(tempPoint)] = tempPoint.getName();
            }
            return getTabCompletions(args, names);
        }
    	return new ArrayList();
    }
    
    @Override
    public List getCommandAliases()
    {
        ArrayList list;
        list = new ArrayList();
        list.add("wpm");
        return list;
    }

    @Override
    public String getCommandName()
    {
        return "waypointmanager";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/waypointmanager or /wpm <command> <name of waypoint>\n"
        		+ "possible commands:\n"
        		+ " - delpoint or dp\n"
        		+ " - recpoint or rp";
    }
    
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws WrongUsageException, PlayerNotFoundException
    {
        if(FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("This command is only available in SMP!")); 
        }else
        {
	    	if(args.length == 0)
	        {
	            throw new WrongUsageException(getCommandUsage(sender));
	        }else
	        {
	            if (args.length == 1) {
	            	if (args[0].equals("delpoint") || args[0].equals("dp")) {
	            		getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("Insufficient arguments: /wpm dp <name of waypoint>"));
	            		return;
	            	}
	            	if (args[0].equals("recpoint") || args[0].equals("rp")) {
	            		getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("Insufficient arguments: /wpm rp <name of waypoint>"));
	            		return;
	            	}
	            }else {
	            	String[] args2 = new String[args.length - 1];
            		for (int i = 1; i < args.length; ++i) {
            			args2[i - 1] = args[i];
            		}
	            	if (args[0].equals("delpoint") || args[0].equals("dp")) {
	            		delpoint(sender, args2);
	            		return;
	            	}
	            	if (args[0].equals("recpoint") || args[0].equals("rp")) {
	            		recpoint(sender, args2);
	            		return;
	            	}
	            }
	        }
        }
    }
    
    private void delpoint (ICommandSender sender, String[] args) throws PlayerNotFoundException {
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
        {
            if((waypoints = ((ServerWaypointManager)ServerWaypointManager.getInstance()).checkWaypointList(waypointName)) != null)
            {
                if(waypoints.size() == 1){
                    if(((ServerWaypointManager)ServerWaypointManager.getInstance()).delWaypoint(waypoints.get(0).getDim(), waypoints.get(0)))
                    {
                    	getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]Deleted the waypoint \"" + waypoints.get(0).getName() + "\" from the server's list!"));
                    	waypoints = null;
                    }else{
                    	getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]Could not delete the waypoint \"" + waypoints.get(0).getName() + "\" from the server's list!"));
                    }
                }else
                {
                    if(waypoints.size() == 0)
                    {
                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]The waypoint \"" + waypointName + "\" could not be found on this server!"));
                    }else
                    {
                        player = getCommandSenderAsPlayer(sender).getCommandSenderName();
                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]There are " + waypoints.size() + " waypoints matching the name: \"" + waypointName + "\" on this server!"));
                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("There are the following waypoints:"));
                        for(Waypoint tempPoint : waypoints)
                        {
                            getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText(tempPoint.getName() + "  [ " + tempPoint.getXCord() + " | " + tempPoint.getYCord() + " | " + tempPoint.getZCord() + " ] [dim:" + tempPoint.getDim() + "]   [" + waypoints.indexOf(tempPoint) + "]"));
                        }
                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("Type /wpmx dp followed by the number of the waypoint you want to delete!"));
                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("The number can be found at the end of each waypoint line \"[number]\""));
                    }
                }
            }else
            {
            	getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]The waypoint \"" + waypointName + "\" could not be found on this server!")); 
            }
        }
    }
    
    private void recpoint (ICommandSender sender, String[] args) throws PlayerNotFoundException {
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
        {
            if((waypoints = ((ServerWaypointManager)ServerWaypointManager.getInstance()).checkWaypointBlacklist(waypointName)) != null)
            {
                if(waypoints.size() == 1){
                    if(((ServerWaypointManager)ServerWaypointManager.getInstance()).recWaypoint(waypoints.get(0).getDim(), waypoints.get(0)))
                    {
                    	getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]Recovered the waypoint \"" + waypoints.get(0).getName() + "\" to the server's list!"));
                    	waypoints = null;
                    }else{
                    	getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]Could not recover the waypoint \"" + waypoints.get(0).getName() + "\" to the server's list!"));
                    }
                }else
                {
                    if(waypoints.size() == 0)
                    {
                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]The waypoint \"" + waypointName + "\" could not be found on this server!"));
                    }else
                    {
                        player = getCommandSenderAsPlayer(sender).getCommandSenderName();
                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]There are " + waypoints.size() + " waypoints matching the name: \"" + waypointName + "\" on this server's blacklist!"));
                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("There are the following waypoints:"));
                        for(Waypoint tempPoint : waypoints)
                        {
                            getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText(tempPoint.getName() + "  [ " + tempPoint.getXCord() + " | " + tempPoint.getYCord() + " | " + tempPoint.getZCord() + " ] [dim:" + tempPoint.getDim() + "]   [" + waypoints.indexOf(tempPoint) + "]"));
                        }
                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("Type /wpmx rp followed by the number of the waypoint you want to delete!"));
                        getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("The number can be found at the end of each waypoint line \"[number]\""));
                    }
                }
            }else
            {
            	getCommandSenderAsPlayer(sender).addChatMessage(new ChatComponentText("[Minimapsync]The waypoint \"" + waypointName + "\" could not be found on this server!")); 
            }
        }
    }
    
}
