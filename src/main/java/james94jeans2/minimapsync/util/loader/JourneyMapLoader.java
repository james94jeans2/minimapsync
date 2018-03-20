package james94jeans2.minimapsync.util.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;

import james94jeans2.minimapsync.util.WaypointList;
import journeymap.client.model.Waypoint;
import journeymap.client.model.Waypoint.Origin;
import journeymap.client.model.Waypoint.Type;
import journeymap.client.waypoint.WaypointStore;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class JourneyMapLoader implements IWaypointLoader {

	@Override
	public WaypointList loadWaypointsForDimension(int pDim) {
		WaypointList returnList = new WaypointList(pDim, true);
		Collection<Waypoint> waypoints = WaypointStore.instance().getAll();
		for (Waypoint waypoint : waypoints) {
			Collection<Integer> dimensions = waypoint.getDimensions();
			if (!waypoint.isDeathPoint()) {
				for (Integer integer : dimensions) {
					if (integer == pDim) {
						returnList.add(new james94jeans2.minimapsync.util.Waypoint(waypoint.getName(), waypoint.getX(), waypoint.getY(), waypoint.getZ(), waypoint.isEnable(), waypoint.getR(), waypoint.getG(), waypoint.getB(), dimensions, pDim, false));
					}
				}
			}
		}
		return (returnList.isEmpty() ? null : returnList);
	}

	@Override
	public LinkedList<WaypointList> loadAllWaypoints() {
		LinkedList<WaypointList> returnList = new LinkedList<WaypointList>();
		Collection<Integer> dimensions = new TreeSet<Integer>();
		Collection<Waypoint> waypoints = WaypointStore.instance().getAll();
		for (Waypoint waypoint : waypoints) {
			dimensions.addAll(waypoint.getDimensions());
		}
		for (int dim : dimensions) {
			WaypointList tempList = loadWaypointsForDimension(dim);
			if (tempList != null) {
				returnList.add(tempList);
			}
		}
		return (returnList.isEmpty() ? null : returnList);
	}

	@Override
	public void saveAllWaypoints(LinkedList<WaypointList> list) {
		for (WaypointList waypointList : list) {
			saveWaypointsForDimension(waypointList);
		}
	}
	
	public void saveWaypointsForDimension (WaypointList toSave) {
		int dimse = toSave.getDim();
		Collection<Waypoint> waypointsToDelete = new LinkedList<Waypoint>();
		Collection<Waypoint> all = WaypointStore.instance().getAll();
		for (Waypoint waypoint : all) {
			Collection<Integer> dimensions = waypoint.getDimensions();
			if (!waypoint.isDeathPoint()) {
				for (Integer integer : dimensions) {
					if (integer == dimse) {
						waypointsToDelete.add(waypoint);
					}
				}
			}
		}
		for (Waypoint w : waypointsToDelete) {
			WaypointStore.instance().remove(w);
		}
		for (james94jeans2.minimapsync.util.Waypoint waypoint : toSave) {
			ArrayList<Integer> dimensions = new ArrayList<Integer>();
			for (Integer integer : waypoint.getDims()) {
				dimensions.add(integer);
			}
			WaypointStore.instance().save(new Waypoint(waypoint.getName(), waypoint.getXCord(), waypoint.getYCord(), waypoint.getZCord(), waypoint.getVisible(), waypoint.getFirstColor(), waypoint.getSecondColor(), waypoint.getThirdColor(), Type.Normal, Origin.JourneyMap, waypoint.getDim(), dimensions));
		}
		//replaced clever algorithm with stupid one
//		WaypointList compareList = loadWaypointsForDimension(toSave.getDim());
//		WaypointList toAdd;
//		if (compareList != null ) {
//			toAdd = compareList.getMissingPoints(toSave);
//		} else {
//			toAdd = toSave;
//		}
//		for (james94jeans2.minimapsync.util.Waypoint waypoint : toAdd) {
//			ArrayList<Integer> dimensions = new ArrayList<Integer>();
//			for (Integer integer : waypoint.getDims()) {
//				dimensions.add(integer);
//			}
//			WaypointStore.instance().save(new Waypoint(waypoint.getName(), waypoint.getXCord(), waypoint.getYCord(), waypoint.getZCord(), waypoint.getVisible(), waypoint.getFirstColor(), waypoint.getSecondColor(), waypoint.getThirdColor(), Type.Normal, Origin.JourneyMap, waypoint.getDim(), dimensions));
//		}
	}

}
