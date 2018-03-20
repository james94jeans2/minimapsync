package james94jeans2.minimapsync.server;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.network.packet.AbstractMinimapsyncPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncCheckPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncDonePacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncInfoPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncKeyPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncSendWaypointPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncWaypointPacket;
import james94jeans2.minimapsync.network.packethandling.MinimapsyncPacketPipeline;
import james94jeans2.minimapsync.server.command.CommandSendWaypoint;
import james94jeans2.minimapsync.server.command.CommandSync;
import james94jeans2.minimapsync.util.AbstractWaypointManager;
import james94jeans2.minimapsync.util.FileReader;
import james94jeans2.minimapsync.util.Waypoint;
import james94jeans2.minimapsync.util.WaypointList;
import james94jeans2.minimapsync.util.loader.DynmapLoader;
import james94jeans2.minimapsync.util.loader.IWaypointLoader;
import james94jeans2.minimapsync.util.loader.PlayerLoader;
import james94jeans2.minimapsync.util.loader.ServerBlacklistLoader;
import james94jeans2.minimapsync.util.loader.ServerWaypointLoader;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class ServerWaypointManager extends AbstractWaypointManager
{

    private LinkedList<WaypointList> blacklists;
    private LinkedList<Integer> keys;
    private LinkedList<EntityPlayer> syncPlayers;
    protected LinkedList<Pair<EntityPlayer, WaypointList>> tabCompletions;
    private boolean noBlacklist, dynmapEnabled, autoSync;
    private CommandSync command;
    private CommandSendWaypoint send;
    private IWaypointLoader blacklistLoader, dynmapLoader, playerLoader;
    
    public void setMode (int pMode) {
    	super.setMode(pMode);
    	CommandHandler ch = (CommandHandler)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
    	if (send == null) {
    		send = new CommandSendWaypoint();
    		ch.registerCommand(send);
    	}
    	if (mode == 2) {
    		command = new CommandSync();
    		ch.registerCommand(command);
    		send.setPermissionLevel(2);
    	} else {
    		if (command != null) {
    			ch.getCommands().remove(command);
    			command = null;
    			send.setPermissionLevel(0);
    		}
    	}
    }
    
    @Override
    protected void loadWaypoints () {
    	dimensions = loader.loadAllWaypoints();
    	noWaypoints = false;
    	if (dimensions == null) {
			logger.info("No waypoints saved up to now!");
			noWaypoints = true;
		} else {
			int counter = 0;
			for (WaypointList list : dimensions) {
				counter += list.size();
			}
			logger.info("Successfully loaded " + counter + " waypoints!");
		}
    	blacklists = blacklistLoader.loadAllWaypoints();
    	noBlacklist = false;
    	if (blacklists == null) {
    		logger.info("No blacklist saved up to now!");
    		noBlacklist = true;
    	} else {
    		int counter = 0;
    		for (WaypointList list : blacklists) {
				counter += list.size();
			}
    		logger.info("Successfully loaded " + counter + " waypoints from blacklist!");
    	}
    	saveWaypoints();
    }
    
    @Override
    protected void saveWaypoints () {
    	loader.saveAllWaypoints(dimensions);
    	blacklistLoader.saveAllWaypoints(blacklists);
    	if (dynmapEnabled) {
    		dynmapLoader.saveAllWaypoints(dimensions);
    	}
    }
    
    public int generateKey () {
    	int key = new Random().nextInt();
    	while (key == 0) {
    		key = new Random().nextInt();
    	}
    	keys.add(key);
    	return key;
    }
    
    public ServerWaypointManager()
    {
    	super();
        noBlacklist = true;
        mode = 1;
        keys = new LinkedList<Integer>();
        syncPlayers = new LinkedList<EntityPlayer>();
        tabCompletions = new LinkedList<Pair<EntityPlayer,WaypointList>>();
        dynmapEnabled = false;
    }
    
    public WaypointList getTabCompletionsFor (EntityPlayer player) {
    	
    	return null;
    }
    
    public WaypointList getWaypointForNameFromBlacklist (String name)
    {
    	return getWaypointForName(blacklists, name);
    }
    
    public boolean delWaypoint(int pDim, Waypoint pPoint)
    {
        WaypointList tempList = null;
        for (WaypointList list : dimensions) {
			if (list.getDim() == pDim) {
				tempList = list;
				break;
			}
		}
        if(tempList != null && tempList.remove(pPoint))
        {
        	tempList = null;
        	if (!noBlacklist) {
        		for (WaypointList list : blacklists) {
					if (list.getDim() == pDim) {
						tempList = list;
						break;
					}
				}
        	}
        	if(tempList != null)
        	{
        		tempList.add(pPoint);
        	}else{
        		tempList = new WaypointList(pDim, true);
        		tempList.add(pPoint);
        		if (noBlacklist) {
        			blacklists = new LinkedList<WaypointList>();
        		}
        		blacklists.add(tempList);
        	}
        	updateStates();
        	return true;
        }
        return false;
    }
    
    private void updateStates() {
    	saveWaypoints();
		noWaypoints = dimensions == null || dimensions.isEmpty();
		noBlacklist = blacklists == null || blacklists.isEmpty();
	}

	public boolean recWaypoint(int pDim, Waypoint pPoint)
    {
        WaypointList tempList = null;
        for (WaypointList list : blacklists) {
			if (list.getDim() == pDim) {
				tempList = list;
				break;
			}
		}
        if(tempList != null && tempList.remove(pPoint))
        {
        	tempList = null;
        	if (!noWaypoints) {
        		for (WaypointList list : dimensions) {
					if (list.getDim() == pDim) {
						tempList = list;
						break;
					}
				}
        	}
        	if(tempList != null)
        	{
        		tempList.add(pPoint);
        	}else{
        		tempList = new WaypointList(pDim, true);
        		tempList.add(pPoint);
        		dimensions.add(tempList);
        	}
        	updateStates();
        	return true;
        }
        return false;
    }
    
    public WaypointList getWaypoints()
    {
    	if(!noWaypoints)
    	{
    		WaypointList returnList = new WaypointList(0, true);
    		for(WaypointList list : dimensions)
    		{
    			returnList.addAll(list);
    		}
    		return returnList;
    	}
        return null;
    }
    
    public WaypointList getBlacklistedWaypoints()
    {
    	if(!noBlacklist)
    	{
    		WaypointList returnList = new WaypointList(0, true);
    		for(Object tempObject : blacklists)
    		{
    			WaypointList tempList = (WaypointList) tempObject;
    			returnList.addAll(tempList);
    		}
    		return returnList;
    	}
        return null;
    }

    public WaypointList getWaypointsForDimension(int pDim)
    {
    	if(!noWaypoints)
	    {
	        for(WaypointList tempList : dimensions)
	        {
	        	if(tempList.getDim() == pDim)
	        	{
	        		return tempList;
	        	}
	        }
	    }
        return null;
    }

    public void sendCheckPacket(EntityPlayer pPlayer)
    {
    	AbstractMinimapsyncPacket pkt = null;
    	switch (mode) {
    		case 1:
    			pkt = new MinimapsyncCheckPacket('S');
    			break;
    		case 2:
    			pkt = new MinimapsyncCheckPacket('C');
    			break;
			//TODO mode 3 entfernt
    		//case 3:
    		//	pkt = new MinimapsyncCheckPacket('R');
    		//	break;
    		default:
    				break;
    	}
        pipeline.sendTo(pkt, (EntityPlayerMP) pPlayer);
        if (autoSync) {
        	sendAllWaypointsToClient(pPlayer);
        }
    }
    
    public void sendKeyPacket (EntityPlayerMP pPlayer) {
    	syncPlayers.add(pPlayer);
        AbstractMinimapsyncPacket pkt = new MinimapsyncKeyPacket(generateKey());
        pipeline.sendTo(pkt, pPlayer);
    }
    
    public boolean noWaypoints () {
    	return noWaypoints;
    }
    
    public boolean noBlacklists () {
    	return noBlacklist;
    }
    
    public void addAndCheckWaypoint (MinimapsyncSendWaypointPacket packet) {
    	Waypoint waypoint = new Waypoint(packet.getWaypoint(), packet.getDimension(), true);
    	WaypointList points = null;
    	if (!noWaypoints) {
	    	for (WaypointList list : dimensions) {
				if (list.getDim() == packet.getDimension()) {
					points = list;
					break;
				}
			}
    	}
    	if (!noBlacklist) {
    		for (WaypointList list : blacklists) {
				if  (list.getDim() == packet.getDimension()) {
					WaypointList tempList = list.getWaypointsForName(waypoint.getName());
					if (tempList != null && !tempList.isEmpty()) {
						for (Waypoint waypoint2 : tempList) {
							if (waypoint2.getCompareStr().equalsIgnoreCase(waypoint.getCompareStr())) {
								return;
							}
						}
					}
					break;
				}
			}
    	}
    	if (points == null) {
    		points = new WaypointList(packet.getDimension(), true);
    		points.add(waypoint);
    		if (dimensions != null) {
    			dimensions.add(points);
    		} else {
    			dimensions = new LinkedList<WaypointList>();
    			dimensions.add(points);
    		}
    	} else {
    		WaypointList tempList = points.getWaypointsForName(waypoint.getName());
    		if (tempList != null && !tempList.isEmpty()) {
    			for (Waypoint waypoint2 : tempList) {
    				if (waypoint2.getCompareStr().equalsIgnoreCase(waypoint.getCompareStr())) {
    					return;
    				}
    			}
    		}
    		points.add(waypoint);
    	}
    	updateStates();
    }

	@Override
	public void afterStartup() {
        loader = new ServerWaypointLoader();
        playerLoader = new PlayerLoader();
        blacklistLoader = new ServerBlacklistLoader();
        if (dynmapEnabled) {
        	dynmapLoader = new DynmapLoader();
        }
		loadWaypoints();
	}
	
	@Override
	protected void handleRemoteOut() {
		
	}
	
	@Override
	protected void checkMissingLocalWaypoints (EntityPlayer player) {
		for (Pair<EntityPlayer, WaypointList> pair : remoteIn) {
			if (pair.getKey() == player) {
				checkMissingLocalWaypoints(pair.getValue());
				if (pair.getValue().isPrivate()) {
					updatePrivateWaypoints(player, pair.getValue());
				}
				remoteIn.remove(pair);
				return;
			}
		}
	}
	
	private void updatePrivateWaypoints (EntityPlayer player, WaypointList remoteIn) {
		IWaypointLoader pLoader = ((PlayerLoader) playerLoader).createLoader(player);
		LinkedList<WaypointList> privLists = pLoader.loadAllWaypoints();
		boolean added = false;
		if (privLists != null) {
			for (WaypointList list : privLists) {
				if (list.getDim() == remoteIn.getDim()) {
					privLists.remove(list);
					privLists.add(remoteIn.getPrivate().clone());
					//just some backup
//					list.addAll(list.getMissingPoints(remoteIn.getPrivate()));
//					if (anyWaypointsInDimension(remoteIn.getDim())) {
//						list.removeAll(list.getDoublePoints(getWaypointsForDimension(remoteIn.getDim())));
//					}
					//TODO is this right?
//					if (remoteIn.getPrivate().isEmpty()) {
//						list = new WaypointList(remoteIn.getDim(), false);
//					} else {
//						list = remoteIn.getPrivate();
//					}
					added = true;
					break;
				}
			}
		} else {
			privLists = new LinkedList<WaypointList>();
		}
		if (!added) {
			WaypointList newList = new WaypointList(remoteIn.getDim(), false);
			newList.addAll(remoteIn.getPrivate());
			if (!newList.isEmpty()) {
				privLists.add(newList);
			}
		}
		pLoader.saveAllWaypoints(privLists);
	}
	
	protected void checkMissingLocalWaypoints (WaypointList remoteIn) {
		logger.info("checking Local Points");
		WaypointList compareList = null;
		if (!noWaypoints) {
			for (WaypointList tempList : dimensions) {
				if (remoteIn.getDim() == tempList.getDim()) {
					compareList = tempList;
					break;
				}
			}
		}
		if (!noBlacklist) {
			for (WaypointList list : blacklists) {
				if (list.getDim() == remoteIn.getDim()) {
					remoteIn = list.getMissingPoints(remoteIn);
					break;
				}
			}
		}
		if (compareList != null) {
			compareList.addAll(compareList.getMissingPoints(remoteIn.getPublic()));
		} else {
			WaypointList newList = new WaypointList(remoteIn.getDim(), true);
			newList.addAll(remoteIn.getPublic());
			if (!newList.isEmpty()) {
				if (noWaypoints) {
					dimensions = new LinkedList<WaypointList>();
					noWaypoints = false;
				}
				dimensions.add(newList);
			}
		}
		saveWaypoints();
	}
	
	@Override
	public void handleDonePacket (MinimapsyncDonePacket packet, EntityPlayer player) {
		logger.info("received " + (packet.getPriv() ? "private" : "public") + " done packet from client");
		if (mode == 1) {
			boolean loaded = false;
			for (Pair<EntityPlayer, WaypointList> pair : remoteIn) {
				if (pair.getKey().equals(player)) {
					pair.getValue().setPrivate(packet.getPriv());
					loaded = true;
					break;
				}
			}
			if (loaded) {
				checkMissingLocalWaypoints(player);
			} else {
				if (packet.getPriv()) {
					updatePrivateWaypoints(player, new WaypointList(player.dimension, player.getCommandSenderName()));
				} else {
					logger.fatal("Player: " + player.getCommandSenderName());
					logger.fatal("sent donePacket but no waypoints!");
				}
			}
		} else {
			if (keys.contains(packet.getKey())) {
				keys.remove((Object) packet.getKey());
				checkMissingLocalWaypoints(player);
				syncPlayers.remove(player);
			} else {
				if (packet.getKey() == -1) {
					Pair<EntityPlayer, WaypointList> pair = null;
					for (Pair<EntityPlayer, WaypointList> p : remoteIn) {
						if (p.getKey().equals(player)) {
							pair = p;
							break;
						}
					}
					if (pair != null) {
						updatePrivateWaypoints(player, pair.getValue());
						remoteIn.remove(pair);
					} else {
						updatePrivateWaypoints(player, new WaypointList(player.dimension, player.getCommandSenderName()));
					}
				} else {
					for (Pair<EntityPlayer, WaypointList> pair : remoteIn) {
						if (pair.getKey().equals(player)) {
							remoteIn.remove(pair);
							return;
						}
					}
				}
			}
		}
	}
	
	public void handleWaypointPacket (MinimapsyncWaypointPacket packet, EntityPlayer player) {
		if (mode == 1 || packet.getKey() == -1) {
			super.handleWaypointPacket(packet, player);
		} else {
			boolean loaded = false;
			Pair<EntityPlayer, WaypointList> match = null;
			for (Pair<EntityPlayer, WaypointList> pair : remoteIn) {
				if (pair.getKey().equals(player)) {
					loaded = true;
					match = pair;
					break;
				}
			}
			if (loaded) {
				if (keys.contains(packet.getKey())) {
					WaypointList temp = match.getValue();
					temp.add(new Waypoint(packet.getWaypointString(), packet.getDimension()));
					remoteIn.add(Pair.of(player, temp));
					remoteIn.remove(match);
				}
			} else {
				if (keys.contains(packet.getKey())) {
					WaypointList temp = new WaypointList(packet.getDimension(), false);
					temp.add(new Waypoint(packet.getWaypointString(), packet.getDimension()));
					remoteIn.add(Pair.of(player, temp));
					syncPlayers.add(player);
				}
			}
		}
	}

	public WaypointList checkWaypointList(String waypointName) {
		if (noWaypoints) {
			return null;
		}
		WaypointList wp = new WaypointList(0, false);
		for (WaypointList list : dimensions) {
			wp.addAll(list);
		}
		return wp.getWaypointsForName(waypointName);
	}
	
	public WaypointList checkWaypointBlacklist(String waypointName) {
		if (noBlacklist) {
			return null;
		}
		WaypointList wp = new WaypointList(0, false);
		for (WaypointList list : blacklists) {
			wp.addAll(list);
		}
		return wp.getWaypointsForName(waypointName);
	}
	
	public void updateDynmap () {
		dynmapLoader.saveAllWaypoints(dimensions);
	}
	
	public void setDynmapEnabled (boolean enabled) {
		dynmapEnabled = enabled;
		if (!enabled && dynmapLoader != null) {
			((DynmapLoader) dynmapLoader).destroy();
			dynmapLoader = null;
		} else {
			if (loader != null) {
				dynmapLoader = new DynmapLoader();
				saveWaypoints();
			}
		}
	}
	
	public boolean getDynmapEnabled () {
		return dynmapEnabled;
	}
	
	public void setAutoSync (boolean enabled) {
		autoSync = enabled;
	}
	
	public boolean getAutoSyncEnabled () {
		return autoSync;
	}
	
	public void sendAllPrivateWaypointsForDimensionToClient(int dimension, EntityPlayer player) {
		IWaypointLoader tempLoader = ((PlayerLoader) playerLoader).createLoader(player);
		WaypointList playersPoints = tempLoader.loadWaypointsForDimension(dimension);
		WaypointList publicPoints = null;
		if (anyWaypointsInDimension(dimension)) {
			for (WaypointList list : dimensions) {
				if (list.getDim() == dimension) {
					publicPoints = list;
					break;
				}
			}
			WaypointList missing = null;
			if (playersPoints != null) {
				missing = playersPoints.getMissingPoints(publicPoints);
				playersPoints.addAll(missing);
			} else {
				playersPoints = new WaypointList(dimension, false);
				playersPoints.addAll(publicPoints);
			}
		} else {
			if (playersPoints == null) {
				playersPoints = new WaypointList(dimension, false);
			}
		}
		sendWaypointListToClient(playersPoints, player);
	}
	
	@Override
	public void sendAllWaypointsToClient (EntityPlayer player) {
		IWaypointLoader pLoader = ((PlayerLoader) playerLoader).createLoader(player);
		LinkedList<WaypointList> privLists = pLoader.loadAllWaypoints();
		if (!noWaypoints) {
			for (WaypointList tempList : dimensions) {
				if (privLists != null) {
					for (WaypointList privList : privLists) {
						if (privList.getDim() == tempList.getDim()) {
							tempList.addAll(privList);
							sendReadOnlyWaypointListToClient(tempList, player);
							break;
						}
					}
				} else {
					sendReadOnlyWaypointListToClient(tempList, player);
				}
			}
		}
	}
	
	public void sendKeyPacketCheck (EntityPlayer player) {
		if (command.canCommandSenderUseCommand(player)) {
			sendKeyPacket((EntityPlayerMP) player);
		} else {
			pipeline.sendTo(new MinimapsyncKeyPacket(0), (EntityPlayerMP) player);
		}
	}
	
}