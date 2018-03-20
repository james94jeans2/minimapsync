package james94jeans2.minimapsync.util;

import java.util.LinkedList;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.network.packet.MinimapsyncDonePacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncInfoPacket;
import james94jeans2.minimapsync.network.packet.MinimapsyncWaypointPacket;
import james94jeans2.minimapsync.network.packethandling.MinimapsyncPacketPipeline;
import james94jeans2.minimapsync.util.loader.IWaypointLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public abstract class AbstractWaypointManager {

	protected int mode;
	private static AbstractWaypointManager instance;
	protected Logger logger;
	protected MinimapsyncPacketPipeline pipeline;
	protected LinkedList<WaypointList> dimensions;
	protected LinkedList<Pair<EntityPlayer, WaypointList>> remoteIn, remoteOut;
	protected boolean noWaypoints;
	protected IWaypointLoader loader;
	
	public void setMode (int pMode) {
		mode = pMode;
	}
	
	public int getMode () {
		return mode;
	}
	
	public static AbstractWaypointManager getInstance () {
		return instance;
	}
	
	public AbstractWaypointManager () {
		instance = this;
		logger = Minimapsync.instance.logger;
		pipeline = Minimapsync.pipeline;
		dimensions = new LinkedList<WaypointList>();
		remoteIn = new LinkedList<Pair<EntityPlayer,WaypointList>>();
		remoteOut = new LinkedList<Pair<EntityPlayer,WaypointList>>();
		noWaypoints = true;
	}
	
	public boolean anyWaypointsInDimension(int pDim)
    {
		if (noWaypoints)
			loadWaypoints();
        if(!noWaypoints)
        {
	        for(WaypointList tempList : dimensions)
	        {
	        	if(tempList.getDim() == pDim && !tempList.isEmpty())
	        	{
	        		return true;
	        	}
	        }
	    }
        return false;
    }
	
	public abstract void sendCheckPacket (EntityPlayer player);
	
	protected WaypointList loadWaypointsForDimension (int pDim) {
		return loader.loadWaypointsForDimension(pDim);
	}
	
	protected void loadWaypoints () {
		dimensions = loader.loadAllWaypoints();
		if (dimensions == null) {
			logger.info("No waypoints saved up to now!");
			noWaypoints = true;
		} else {
			for (WaypointList list : dimensions) {
				if (!list.isEmpty()) {
					noWaypoints = false;
					return;
				}
			}
		}
	}
	
	protected void saveWaypoints () {
		loader.saveAllWaypoints(dimensions);
	}
	
	public abstract void afterStartup ();
	
	protected void chat (String message, EntityPlayer player) {
    	((EntityPlayerMP) player).addChatMessage(new ChatComponentText(message));
    }
	
	protected WaypointList checkMissingRemoteWaypoints (WaypointList remoteIn) {
		WaypointList compareList = null;
		if (!noWaypoints) {
			for (WaypointList tempList : dimensions) {
				if (remoteIn.getDim() == tempList.getDim()) {
					compareList = tempList;
					break;
				}
			}
		}
		if (compareList != null) {
			return remoteIn.getMissingPoints(compareList);
		}
		return null;
	}
	
	protected void checkMissingRemoteWaypoints (EntityPlayer player) {
		for (Pair<EntityPlayer, WaypointList> pair : remoteIn) {
			if (pair.getKey() == player) {
				remoteOut.add(Pair.of(player, checkMissingRemoteWaypoints(pair.getValue())));
				remoteIn.remove(pair);
				handleRemoteOut();
				return;
			}
		}
	}
	
	protected void handleRemoteOut() {
		for (Pair<EntityPlayer, WaypointList> pair : remoteOut) {
			if (pair.getValue() != null) {
				if (!pair.getValue().isEmpty()) {
					if (pair.getKey().getCommandSenderName().equals(Minecraft.getMinecraft().thePlayer.getCommandSenderName())) {
						sendWaypointListToServer(pair.getValue());
					} else {
						sendWaypointListToClient(pair.getValue(), pair.getKey());
					}
				}
			} else {
				sendWaypointListToServer(new WaypointList(0, true));
			}
			remoteOut.remove(pair);
		}
	}

	protected void checkMissingLocalWaypoints (EntityPlayer player) {
		for (Pair<EntityPlayer, WaypointList> pair : remoteIn) {
			if (pair.getKey() == player) {
				checkMissingLocalWaypoints(pair.getValue());
				return;
			}
		}
	}
	
	protected void checkMissingLocalWaypoints (WaypointList remoteIn) {
		WaypointList compareList = null;
		if (!noWaypoints) {
			for (WaypointList tempList : dimensions) {
				if (remoteIn.getDim() == tempList.getDim()) {
					compareList = tempList;
					break;
				}
			}
		}
		if (compareList != null) {
			compareList.removeAll(compareList.getDoublePoints(remoteIn.getPublic()));
			compareList.addAll(compareList.getMissingPoints(remoteIn));
		} else {
			WaypointList newList = new WaypointList(remoteIn.getDim(), true);
			newList.addAll(remoteIn);
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

	public void sendAllWaypointsForDimensionToClient(int dimension, EntityPlayer player) {
		if (!noWaypoints) {
			for (WaypointList tempList : dimensions) {
				if (dimension == tempList.getDim()) {
					sendWaypointListToClient(tempList, player);
					return;
				}
			}
			
		}
		sendWaypointListToClient(new WaypointList(dimension, true), player);
	}
	
	public void sendAllWaypointsToClient (EntityPlayer player) {
		if (!noWaypoints) {
			for (WaypointList tempList : dimensions) {
				sendReadOnlyWaypointListToClient(tempList, player);
			}
		}
	}
	
	protected void sendReadOnlyWaypointListToClient (WaypointList waypoints, EntityPlayer player) {
		for (Waypoint waypoint : waypoints) {
			sendWaypointToClient(waypoint, player);
		}
		pipeline.sendTo(new MinimapsyncDonePacket(1, waypoints.isPrivate()), (EntityPlayerMP)player);
	}
	
	protected void sendWaypointListToClient (WaypointList waypoints, EntityPlayer player) {
		if (waypoints.isPrivate()) {
			pipeline.sendTo(new MinimapsyncInfoPacket(waypoints.size()), (EntityPlayerMP) player);
		}
		for (Waypoint waypoint : waypoints) {
			sendWaypointToClient(waypoint, player);
		}
		pipeline.sendTo(new MinimapsyncDonePacket(0, waypoints.isPrivate()), (EntityPlayerMP)player);
	}
	
	protected void sendWaypointToClient (Waypoint waypoint, EntityPlayer player) {
		pipeline.sendTo(new MinimapsyncWaypointPacket(waypoint.getNetworkLine(), waypoint.getDim()), (EntityPlayerMP)player);
	}
	
	protected void sendWaypointListToServer (WaypointList waypoints) {
		if (mode != 1 && waypoints.isPrivate()) {
			waypoints.removeAll(waypoints.getPublic().clone());
		}
		for (Waypoint waypoint : waypoints) {
			sendWaypointToServer((mode == 1 ? 0 : -1),waypoint);
		}
		pipeline.sendToServer(new MinimapsyncDonePacket((mode == 1 ? 0 : -1), waypoints.isPrivate()));
	}
	
	protected void sendWaypointToServer (Waypoint waypoint) {
		pipeline.sendToServer(new MinimapsyncWaypointPacket(waypoint.getNetworkLine(), waypoint.getDim()));
	}
	
	protected void sendWaypointToServer (int key, Waypoint waypoint) {
		pipeline.sendToServer(new MinimapsyncWaypointPacket(waypoint.getNetworkLine(), waypoint.getDim(), key));
	}
	
	public void handleDonePacket (MinimapsyncDonePacket packet, EntityPlayer player) {
		logger.info("handling " + (packet.getPriv() ? "private" : "public") + " done packet");
		boolean loaded = false;
		for (Pair<EntityPlayer, WaypointList> pair : remoteIn) {
			if (pair.getKey().equals(player)) {
				pair.getValue().setPrivate(packet.getPriv());
				loaded = true;
				break;
			}
		}
		if (loaded) {
			loadWaypoints();
			checkMissingLocalWaypoints(player);
			checkMissingRemoteWaypoints(player);
		} else {
			loadWaypoints();
			if (player.getCommandSenderName().equals(Minecraft.getMinecraft().thePlayer.getCommandSenderName())) {
				remoteIn.add(Pair.of(player, new WaypointList(Minecraft.getMinecraft().thePlayer.dimension, !packet.getPriv())));
				checkMissingRemoteWaypoints(player);
			} else {
				logger.fatal("Player: " + player.getCommandSenderName());
				logger.fatal("sent donePacket but no waypoints!");
			}
		}
	}
	
	public void handleWaypointPacket (MinimapsyncWaypointPacket packet, EntityPlayer player) {
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
			WaypointList temp = match.getValue();
			temp.add(new Waypoint(packet.getWaypointString(), packet.getDimension()));
			remoteIn.add(Pair.of(player, temp));
			remoteIn.remove(match);
		} else {
			WaypointList temp = new WaypointList(packet.getDimension(), false);
			temp.add(new Waypoint(packet.getWaypointString(), packet.getDimension()));
			remoteIn.add(Pair.of(player, temp));
		}
	}
	
	protected WaypointList getWaypointsForName (String name)
    {
        return getWaypointForName(dimensions, name);
    }
	
	protected WaypointList getWaypointForName (LinkedList<WaypointList> lists, String name) {
		if(noWaypoints)
        {
            return null;
        }
        WaypointList returnList = new WaypointList(0, true);
        for(WaypointList tempList : lists)
        {
        	returnList.addAll(tempList.getWaypointsForName(name));
        }
        return returnList;
	}
	
	public WaypointList getWaypointsForDimension (int dimension) {
		if (!noWaypoints) {
			for (WaypointList list : dimensions) {
				if (list.getDim() == dimension) {
					return list;
				}
			}
		}
		return null;
	}
	
	public boolean noWaypoints () {
		return noWaypoints;
	}
	
	public WaypointList checkWaypointTeleport (String name, EntityPlayerMP player) {
		if (noWaypoints) {
			return null;
		}
		for (WaypointList list : dimensions) {
			if (list.getDim() == player.dimension) {
				return list.getWaypointsForNameWithCase(name);
			}
		}
		return null;
	}
	
	public WaypointList getWaypoints () {
		if (noWaypoints) {
			return null;
		}
		WaypointList returnList = new WaypointList(0, true);
		for (WaypointList tempList : dimensions) {
			returnList.addAll(tempList);
		}
		return returnList;
	}

}
