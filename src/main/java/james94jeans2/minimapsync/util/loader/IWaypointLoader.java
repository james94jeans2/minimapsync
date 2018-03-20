package james94jeans2.minimapsync.util.loader;

import james94jeans2.minimapsync.util.WaypointList;

import java.util.LinkedList;

/**
 * 
 * @version b0.8
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public interface IWaypointLoader {
	
	public WaypointList loadWaypointsForDimension (int pDim);
	
	public LinkedList<WaypointList> loadAllWaypoints ();
	
	public void saveAllWaypoints (LinkedList<WaypointList> list);

}
