
package james94jeans2.minimapsync.client;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.client.gui.GuiManager;
import james94jeans2.minimapsync.network.packet.*;
import james94jeans2.minimapsync.util.AbstractWaypointManager;
import james94jeans2.minimapsync.util.FileReader;
import james94jeans2.minimapsync.util.MinimapsyncConfiguration;
import james94jeans2.minimapsync.util.Waypoint;
import james94jeans2.minimapsync.util.WaypointList;
import james94jeans2.minimapsync.util.loader.IWaypointLoader;
import james94jeans2.minimapsync.util.loader.JourneyMapLoader;
import james94jeans2.minimapsync.util.loader.MinimapsyncLoader;
import james94jeans2.minimapsync.util.loader.VoxelmapLoader;
import james94jeans2.minimapsync.util.loader.XaerosMinimapLoader;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeSet;
import java.util.logging.Level;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import com.thevoxelbox.voxelmap.l;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import reifnsk.minimap.MinimapsyncUpdaterRei;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class ClientWaypointManager extends AbstractWaypointManager
{
    
    private int key, map;
    private MinimapsyncUpdaterRei updaterRei;
    private boolean syncOnServer, enabled, lan, counting, guiEnabled;
    private IWaypointLoader syncloader;
    private GuiManager guiMng;
    
    public ClientWaypointManager()
    {
    	super();
    	map = Minimapsync.instance.getMap();
        enabled = Minimapsync.instance.getEnabled();
        lan = false;
        counting = false;
        guiEnabled = ((MinimapsyncClientConfiguration)Minimapsync.instance.getConfiguration()).getEnableGui();
        guiMng = GuiManager.instance();
        String mapName = "";
        switch (map) {
        	case 1:
        		loader = new VoxelmapLoader();
        		mapName = "VoxelMap";
        		break;
        	case 2:
        		updaterRei = new MinimapsyncUpdaterRei();
        		mapName = "Rei's Minimap";
        		break;
        	case 3:
        		loader = new JourneyMapLoader();
        		mapName = "JourneyMap";
        		break;
        	case 4:
        		loader = new XaerosMinimapLoader();
        		mapName = "Xaero's Minimap";
        		break;
        }
        syncloader = new MinimapsyncLoader();
        logger.info("Using map: " + map + "|" + mapName);
    }
    
    public void resetWaypoints () {
    	dimensions = new LinkedList<WaypointList>();
    	noWaypoints = true;
    }
    
    public void requestRemoteWaypoints() {
    	pipeline.sendToServer(new MinimapsyncRequestPacket());
    }
    
    public void requestAllRemoteWaypoints() {
    	pipeline.sendToServer(new MinimapsyncRequestPacket(true));
    }
    
    public WaypointList checkWaypointTeleport(String pWaypointName, EntityPlayerMP player)
    {
        loadWaypoints();
        return super.checkWaypointTeleport(pWaypointName, player);
    }
    
    public void handleKeyPacket(int pKey)
    {
    	logger.info("received key: " + pKey);
    	if (guiEnabled) {
    		if (pKey == 0) {
    			requestAllRemoteWaypoints();
    		} else {
    			key=pKey;
    			guiMng.displaySyncList();
    		}
    	} else {
	    	if (pKey != 0) {
		        key = pKey;
		        requestRemoteWaypoints();
	    	}
    	}
    }
    
    @Override
    public void handleWaypointPacket (MinimapsyncWaypointPacket packet, EntityPlayer player) {
		if (counting) {
			guiMng.upCount();
		}
		super.handleWaypointPacket(packet, player);
	}
    
    @Override
	public void handleDonePacket(MinimapsyncDonePacket packet, EntityPlayer player) {
    	if (packet.getKey() != 0) {
    		logger.info("handling keyed done packet");
    		boolean loaded = false;
    		Pair<EntityPlayer, WaypointList> localPair = null;
    		for (Pair<EntityPlayer, WaypointList> pair : remoteIn) {
    			if (pair.getKey().equals(player)) {
    				loaded = true;
    				localPair = pair;
    				break;
    			}
    		}
    		if (loaded) {
    			loadWaypoints();
    			checkMissingLocalWaypoints(player);
    			remoteIn.remove(localPair);
    		}
    	} else {
    		super.handleDonePacket(packet, player);
    	}
    	MinimapKeyHandler.getInstance().enable();
	}
    
	public void sendCheckPacket(EntityPlayer pPlayer)
    {
        AbstractMinimapsyncPacket pkt = new MinimapsyncCheckPacket('L');
        pipeline.sendTo(pkt, (EntityPlayerMP) pPlayer);
    }
    
    public void sendCheckPacket()
    {
    	if (!Minecraft.getMinecraft().isSingleplayer()) {
    		AbstractMinimapsyncPacket pkt = new MinimapsyncCheckPacket('c');
    		pipeline.sendToServer(pkt);
    	}
    }
    
    public void setLAN(boolean pBoolean)
    {
        lan = pBoolean;
    }
    
    public boolean getLAN () {
    	return lan;
    }

	public void setSyncOnServer(boolean pBoolean)
    {
        syncOnServer = pBoolean;
    }
	
	public boolean syncOnServer()
    {
        return syncOnServer;
    }
	
	@Override
	public void afterStartup() {
	}
	
	public void handleSaving () {
		Pair<EntityPlayer, WaypointList> in = remoteIn.getFirst();
		remoteIn.remove();
		WaypointList inList = in.getValue();
		WaypointList dimList = null;
		int dimension = Minecraft.getMinecraft().thePlayer.dimension;
		if (anyWaypointsInDimension(dimension)) {
			dimList = getWaypointsForDimension(dimension);
			WaypointList toSend = inList.getPublic().getMissingPoints(dimList);
			toSend.setPrivate(true);
			if (mode != 1) {
				if (key != 0) {
					sendWaypointListToServer(key, toSend);
				} else {
					sendWaypointListToServer(toSend);
				}
			} else {
				sendWaypointListToServer(toSend);
			}
			dimList.removeAll(dimList.getDoublePoints(inList.getPublic()));
			dimList.addAll(inList.getPublic());
		} else {
			if (dimensions == null) {
				logger.info("dimensions is null");
				return;
			}
			dimensions.add(inList.getPublic());
			if (mode != 1) {
				if (key != 0) {
					pipeline.sendToServer(new MinimapsyncDonePacket(key, true));
					key = 0;
				}
			} else {
				pipeline.sendToServer(new MinimapsyncDonePacket(true));
			}
		}
		saveWaypoints();
	}
	
	protected void checkMissingRemoteWaypoints (EntityPlayer player) {
		if (mode == 1 || lan) {
			if (counting) {
				guiMng.upCount();
				return;
			}
			super.checkMissingRemoteWaypoints(player);
		} else {
			if (counting) {
				guiMng.upCount();
				return;
			}
			if (key != 0) {
				for (Pair<EntityPlayer, WaypointList> pair : remoteIn) {
					if (pair.getKey() == player) {
						WaypointList list = checkMissingRemoteWaypoints(pair.getValue());
						remoteIn.remove(pair);
						handleRemoteOut(key, list);
						return;
					}
				}
			} else {
				handleSaving();
			}
		}
	}
	
	private void handleRemoteOut(int key, WaypointList list) {
			sendWaypointListToServer(key, list);
	}
	
	protected void sendWaypointListToServer (int key, WaypointList waypoints) {
		if (waypoints != null) {
			for (Waypoint waypoint : waypoints) {
				sendWaypointToServer(key, waypoint);
			}
		}
		pipeline.sendToServer(new MinimapsyncDonePacket(key, waypoints.isPrivate()));
		key = 0;
	}
	
	public void sendWaypointToServer(MinimapsyncSendPacket packet) {
		String waypointName = packet.getWaypoint();
		int dimension = packet.getDimension();
		loadWaypoints();
		if (!noWaypoints) {
			for (WaypointList list : dimensions) {
				if (list.getDim() == dimension) {
					WaypointList wp = list.getWaypointsForName(waypointName);
					if (wp.size() == 1) {
						Waypoint point = wp.get(0);
						pipeline.sendToServer(new MinimapsyncSendWaypointPacket(point.getServerLine(), point.getDim(), packet.getKey()));
					}
					return;
				}
			}
		}
	}
	
	public void setWaypointsForDimension (int dim, WaypointList newList) {
		Iterator<WaypointList> listIt = dimensions.iterator();
		while (listIt.hasNext()) {
			if (listIt.next().getDim() == dim) {
				listIt.remove();
				break;
			}
		}
		dimensions.add(newList);
	}
	
	@Override
	protected void loadWaypoints () {
		LinkedList<WaypointList> sync = syncloader.loadAllWaypoints();
		dimensions = loader.loadAllWaypoints();
		if (sync != null) {
			if (dimensions != null) {
				for (WaypointList tempList : sync) {
					boolean handled = false;
					for (WaypointList dimList : dimensions) {
						if (dimList.getDim() == tempList.getDim()) {
							WaypointList toRemove = dimList.getDoublePoints(tempList);
							dimList.removeAll(toRemove);
							WaypointList toAdd = dimList.getMissingPoints(tempList);
							dimList.addAll(toAdd);
							handled = true;
							break;
						}
					}
					if (!handled) {
						dimensions.add(tempList);
					}
				}
			} else {
				dimensions = sync;
			}
		}
		if (dimensions == null) {
			logger.info("No waypoints saved up to now!");
			noWaypoints = true;
		} else {
			for (WaypointList list : dimensions) {
				if (!list.isEmpty()) {
					noWaypoints = false;
					break;
				}
			}
		}
		saveWaypoints();
	}
	
	@Override
	protected WaypointList loadWaypointsForDimension (int pDim) {
		WaypointList returnList = loader.loadWaypointsForDimension(pDim);
		WaypointList tempList = syncloader.loadWaypointsForDimension(pDim);
		if (tempList != null) {
			WaypointList toRemove = returnList.getDoublePoints(tempList);
			WaypointList toAdd = tempList.getDoublePoints(returnList);
			returnList.removeAll(toRemove);
			returnList.addAll(toAdd);
			return returnList;
		}
		return returnList;
	}
	
	@Override
	protected void saveWaypoints () {
		if (dimensions != null) {
			loader.saveAllWaypoints(dimensions);
			syncloader.saveAllWaypoints(dimensions);
		}
	}
	
	public void setCounting (boolean state) {
		counting = state;
	}

	public void discardRemoteIn() {
		remoteIn.remove();
	}
	
	@Override
	public WaypointList getWaypointsForDimension (int dimension) {
		if (!noWaypoints) {
			for (WaypointList list : dimensions) {
				if (list.getDim() == dimension) {
					WaypointList returnList = list.clone();
					return returnList;
				}
			}
		}
		return null;
	}
	
	public void requestKey () {
		pipeline.sendToServer(new MinimapsyncKeyPacket());
	}

}