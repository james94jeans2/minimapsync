package james94jeans2.minimapsync.util.loader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.client.Minecraft;

import com.thevoxelbox.voxelmap.l;
//import com.thevoxelbox.voxelmap.c.A;

import com.thevoxelbox.voxelmap.interfaces.AbstractVoxelMap;
import com.thevoxelbox.voxelmap.interfaces.IWaypointManager;
import com.thevoxelbox.voxelmap.util.Waypoint;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.util.AbstractWaypointManager;
import james94jeans2.minimapsync.util.WaypointList;

/**
 * 
 * @version b0.9.1
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class VoxelmapLoader implements IWaypointLoader {

	@Override
	public WaypointList loadWaypointsForDimension(int pDim) {
		WaypointList waypointsInDimension = new WaypointList(pDim, true);
		ArrayList<Waypoint> waypoints = AbstractVoxelMap.getInstance().getWaypointManager().getWaypoints();
		for (Waypoint waypoint : waypoints) {
			if (waypoint.dimensions.contains(pDim) || waypoint.dimensions.size() == 0) {
				waypointsInDimension.add(new james94jeans2.minimapsync.util.Waypoint(true, waypoint.name, waypoint.x, waypoint.y, waypoint.z, waypoint.enabled, waypoint.red, waypoint.green, waypoint.blue, waypoint.imageSuffix, waypoint.dimensions, pDim, false));
			}
		}
		return waypointsInDimension.isEmpty() ? null : waypointsInDimension;
	}

	@Override
	public LinkedList<WaypointList> loadAllWaypoints() {
		Set<Integer> loadedDimensions = getDimensions();
		if (loadedDimensions != null) {
			LinkedList<WaypointList> returnLists = new LinkedList<WaypointList>();
			Iterator<Integer> iterator = loadedDimensions.iterator();
			WaypointList newList = null;
			while (iterator.hasNext()) {
				newList = loadWaypointsForDimension(iterator.next());
				if (newList != null) {
					returnLists.add(newList);
				}
			}
			return (returnLists.isEmpty() ? null : returnLists);
		}
		return null;
	}

	public void saveWaypointsForDimension(WaypointList list) {
		WaypointList tempList = loadWaypointsForDimension(list.getDim());
		if (tempList == null) {
			tempList = new WaypointList(list.getDim(), true);
		}
		WaypointList toAdd = tempList.getMissingPoints(list);
		WaypointList toDelete = list.getMissingPoints(tempList);
		
		ArrayList<Waypoint> waypoints = AbstractVoxelMap.getInstance().getWaypointManager().getWaypoints();
		for (james94jeans2.minimapsync.util.Waypoint wp : toDelete) {
			for (Waypoint waypoint : waypoints) {
				if (waypoint.dimensions.contains(wp.getDim()) || waypoint.dimensions.size() == 0) {
					james94jeans2.minimapsync.util.Waypoint comp = new james94jeans2.minimapsync.util.Waypoint(true, waypoint.name, waypoint.x, waypoint.y, waypoint.z, waypoint.enabled, waypoint.red, waypoint.green, waypoint.blue, waypoint.imageSuffix, waypoint.dimensions, wp.getDim(), false);
					if (comp.getCompareStr().equals(wp.getCompareStr())) {
						//TODO wait for MamiyaOtaru
//						AbstractVoxelMap.getInstance().getWaypointManager().do(waypoint);
						waypoints.remove(waypoint);
						break;
					}
				}
			}
		}
		
		TreeSet<Integer> dimension = new TreeSet<Integer>();
		dimension.add(list.getDim());
		IWaypointManager manager = AbstractVoxelMap.getInstance().getWaypointManager();
		for (james94jeans2.minimapsync.util.Waypoint waypoint : toAdd) {
			Waypoint addPoint = new Waypoint(waypoint.getName(), waypoint.getZanXCord(), waypoint.getZanZCord(), waypoint.getZanYCord(), true, waypoint.getR(), waypoint.getG(), waypoint.getB(), "", "", dimension);
			manager.addWaypoint(addPoint);
		}
	}

	@Override
	public void saveAllWaypoints(LinkedList<WaypointList> list) {
		for (WaypointList waypointList : list) {
			saveWaypointsForDimension(waypointList);
		}
		ArrayList<Waypoint> waypoints = AbstractVoxelMap.getInstance().getWaypointManager().getWaypoints();
		int dim = Minecraft.getMinecraft().thePlayer.dimension;
		for (Waypoint waypoint : waypoints) {
			if (waypoint.dimensions.size() == 0 || waypoint.dimensions.contains(dim)) {
				waypoint.inDimension = true;
			} else {
				waypoint.inDimension = false;
			}
		}
	}
	
	private Set<Integer> getDimensions () {
		Set<Integer> loaded = new TreeSet<Integer>();
		ArrayList<Waypoint> waypoints = AbstractVoxelMap.getInstance().getWaypointManager().getWaypoints();
		for (Waypoint waypoint : waypoints) {
			loaded.addAll(waypoint.dimensions);
		}
		return loaded.isEmpty() ? null : loaded;
	}
		
}
