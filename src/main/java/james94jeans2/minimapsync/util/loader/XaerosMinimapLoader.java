package james94jeans2.minimapsync.util.loader;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import com.minimap.XaeroMinimap;
import com.minimap.minimap.Minimap;
import com.minimap.minimap.WaypointSet;
import com.minimap.minimap.WaypointWorld;
import com.minimap.settings.ModSettings;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.util.FileReader;
import james94jeans2.minimapsync.util.Waypoint;
import james94jeans2.minimapsync.util.WaypointList;
import net.minecraft.client.Minecraft;

public class XaerosMinimapLoader implements IWaypointLoader {

	private String file;
	private static final String[] colors = {"000000","0000aa","00aa00","00aaaa","aa0000","aa00aa","ffaa00","aaaaaa","555555","5555ff","55ff55","55ffff","ff5555","ff55ff","ffff55"};
	
	public XaerosMinimapLoader () {
		StringBuilder fileName = new StringBuilder();
		fileName.append(Minecraft.getMinecraft().mcDataDir);
		fileName.append("/config");
		fileName.append("/xaerowaypoints.txt");
		file = fileName.toString();
	}
	
	@Override
	public WaypointList loadWaypointsForDimension(int pDim) {
		WaypointList waypointsInDimension = new WaypointList(pDim, true);
		LinkedList<WaypointList> all = loadAllWaypoints();
		if (all != null) {
			for (WaypointList list : all) {
				if (list.getDim() == pDim) {
					waypointsInDimension = list;
					break;
				}
			}
		}
		return waypointsInDimension.isEmpty() ? null : waypointsInDimension;
	}

	@Override
	public LinkedList<WaypointList> loadAllWaypoints() {
		LinkedList<WaypointList> returnList = new LinkedList<WaypointList>();
		HashMap<String, WaypointWorld> worlds = Minimap.waypointMap;
		String world = Minecraft.getMinecraft().func_147104_D().serverIP.split(":")[0];
		
		if (worlds == null) {
			return returnList;
		} else {
			for (String worldKey : worlds.keySet()) {
				if (!"Multiplayer".equals(worldKey.split("_")[0])) {
					continue;
				}
				if (!worldKey.contains(world)) {
					continue;
				}
				int dim = ("null".equals(worldKey.split("_")[2]) ? 0 : Integer.parseInt(worldKey.split("_")[2].substring(3)));
				HashMap<String, WaypointSet> sets = worlds.get(worldKey).sets;
				if (sets == null) {
					continue;
				}
				WaypointList addList = new WaypointList(dim, true);
				Set<String> keys = sets.keySet();
				for (String key : keys) {
					WaypointSet currentSet = sets.get(key);
					for (com.minimap.minimap.Waypoint wp : currentSet.list) {
						addList.add(convertPoint(wp, dim));
					}
				}
				if (!addList.isEmpty()) {
					returnList.add(addList);
				}
			}
			return (returnList.isEmpty() ? null : returnList);
		}
	}

	@Override
	public void saveAllWaypoints(LinkedList<WaypointList> list) {
		LinkedList<WaypointList> in = loadAllWaypoints();
		LinkedList<WaypointList> toDelete = new LinkedList<WaypointList>();
		if (in != null) {
			for (WaypointList inList : in) {
				WaypointList compareList = null;
				for (WaypointList toSave : list) {
					if (toSave.getDim() == inList.getDim()) {
						compareList = toSave;
						break;
					}
				}
				if (compareList == null) {
					toDelete.add(inList);
				} else {
					WaypointList toAddToDelete = new WaypointList(inList.getDim(),true);
					for (Waypoint inP : inList) {
						boolean exi = false;
						for (Waypoint sP : compareList) {
							if (inP.getCompareStr().equals(sP.getCompareStr())) {
								exi = true;
								break;
							}
						}
						if (!exi) {
							toAddToDelete.add(inP);
						}
					}
					toDelete.add(toAddToDelete);
				}
			}
		}
		
		LinkedList<WaypointList> toAdd = new LinkedList<WaypointList>();
		
		if (in != null) {
			for (WaypointList sList : list) {
				WaypointList compareList = null;
				for (WaypointList existing : in) {
					if (existing.getDim() == sList.getDim()) {
						compareList = existing;
						break;
					}
				}
				if (compareList == null) {
					toAdd.add(sList);
				} else {
					WaypointList toAddToAdd = new WaypointList(sList.getDim(),true);
					for (Waypoint inP : sList) {
						boolean exi = false;
						for (Waypoint sP : compareList) {
							if (inP.getCompareStr().equals(sP.getCompareStr())) {
								exi = true;
								break;
							}
						}
						if (!exi) {
							toAddToAdd.add(inP);
						}
					}
					toAdd.add(toAddToAdd);
				}
			}
		} else {
			System.out.println("Adding all");
			toAdd.addAll(list);
		}
		
		HashMap<String,WaypointWorld> worlds = Minimap.waypointMap;
		String world = "Multiplayer_" + Minecraft.getMinecraft().func_147104_D().serverIP.split(":")[0] + "_";
		
		for (WaypointList l : toDelete) {
			String key = world  + (l.getDim() == 0 ? "null" : "DIM" + l.getDim());
			HashMap<String, WaypointSet> sets = worlds.get(key).sets;
			for (Waypoint p : l) {
				for (String k : sets.keySet()) {
					WaypointSet set = sets.get(k);
					boolean toBreak = false;
					ArrayList<com.minimap.minimap.Waypoint> waypointList = (ArrayList<com.minimap.minimap.Waypoint>)set.list.clone();
					for (com.minimap.minimap.Waypoint fp : set.list) {
						if (p.getCompareStr().equals(convertPoint(fp, l.getDim()).getCompareStr())) {
							waypointList.remove(fp);
							toBreak = true;
							break;
						}
					}
					if (toBreak) {
						set.list = waypointList;
						break;
					}
				}
			}
		}
		
		for (WaypointList l : toAdd) {
			String key = world  + (l.getDim() == 0 ? "null" : "DIM" + l.getDim());
			if (worlds == null) {
				Minimap.waypointMap = new HashMap<String, WaypointWorld>();
				worlds = Minimap.waypointMap;
			}
			HashMap<String, WaypointSet> sets = worlds.get(key).sets;
			if (sets == null) {
				sets = new HashMap<String, WaypointSet>();
				WaypointWorld newWorld = new WaypointWorld();
				newWorld.sets = sets;
				Minimap.waypointMap.put(world, newWorld);
			}
			WaypointSet set = sets.get("gui.xaero_default");
			if (set == null) {
				set = new WaypointSet(worlds.get(key));
				sets.put("gui.xaero_default", set);
			}
			for (Waypoint p : l) {
				set.list.add(new com.minimap.minimap.Waypoint(p.getXCord(), p.getYCord(), p.getZCord(), p.getName(), p.getName().substring(0, 1).toUpperCase(), getColor(p.getFirstColor(), p.getSecondColor(), p.getThirdColor())));
			}
		}
		
		try {
			XaeroMinimap.getSettings().saveWaypoints();
		} catch (IOException e) {
			Minimapsync.instance.logger.error("Failed to save Xaero's Waypoints!");
		}
	}
	
	private int getColor (int r, int g, int b) {
		String colorS = getClosest(r) + getClosest(g) + getClosest(b);
		int index = 0;
		for (String comp : colors) {
			if (comp.equals(colorS)) {
				return index;
			}
			index++;
		}
		return 0;
	}
	
	private String getClosest (int i) {
		if (i <= 43) {
			return "00";
		} else {
			if (i <= 128) {
				return "55";
			} else {
				if (i <= 213) {
					return "aa";
				} else {
					return "ff";
				}
			}
		}
	}
	
	private Waypoint convertPoint (com.minimap.minimap.Waypoint wp, int dim) {
		return new Waypoint(wp.name,wp.x,wp.y,wp.z,!wp.disabled,colors[wp.color],dim);
	}

}
