package james94jeans2.minimapsync.util.loader;

import james94jeans2.minimapsync.util.WaypointList;

import java.util.LinkedList;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class ReisMinimapLoader implements IWaypointLoader {

	@Override
	public WaypointList loadWaypointsForDimension(int pDim) {
		return null;
	}

	@Override
	public LinkedList<WaypointList> loadAllWaypoints() {
		return null;
	}

	public void saveWaypointsForDimension(WaypointList list) {
	}

	@Override
	public void saveAllWaypoints(LinkedList<WaypointList> list) {
	}

}
