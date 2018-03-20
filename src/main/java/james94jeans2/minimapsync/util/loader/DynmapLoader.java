package james94jeans2.minimapsync.util.loader;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.server.ServerWaypointManager;
import james94jeans2.minimapsync.util.Waypoint;
import james94jeans2.minimapsync.util.WaypointList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.dynmap.ComponentManager;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCore;
import org.dynmap.MapManager;
import org.dynmap.MarkersComponent;
import org.dynmap.forge.DynmapMod;
import org.dynmap.forge.DynmapPlugin;
import org.dynmap.markers.CircleMarker;
import org.dynmap.markers.GenericMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.impl.MarkerAPIImpl;
import org.dynmap.markers.impl.MarkerAPIImpl.CircleMarkerUpdated;
import org.dynmap.markers.impl.MarkerAPIImpl.MarkerSetUpdated;
import org.dynmap.markers.impl.MarkerSignManager;
import org.dynmap.storage.MapStorage;

import cpw.mods.fml.common.FMLCommonHandler;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class DynmapLoader implements IWaypointLoader {
	
	private MarkerAPI api;
	private boolean initialized, forceSave;
	private Set<MarkerIcon> icons;
	
	public DynmapLoader() {
		forceSave = false;
		initialized = false;
		Thread waitingThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				Field field;
				try {
					field = DynmapPlugin.class.getDeclaredField("core");
					field.setAccessible(true);
					DynmapCore core = (DynmapCore)field.get(DynmapPlugin.plugin);
					while (!core.markerAPIInitialized()) {
						
					}
					api = core.getMarkerAPI();
				} catch (NoSuchFieldException e1) {
					e1.printStackTrace();
				} catch (SecurityException e1) {
					e1.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				icons = new HashSet<MarkerIcon>();
				MarkerIcon icon = api.getMarkerIcon("mms_icon");
				if (icon != null) {
					icons.add(icon);
				}else{
					
					try {
						icons.add(api.createMarkerIcon("mms_icon", "minimapsyncIcon", Minimapsync.class.getResource("/assets/mmsmarker.png").openStream()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				api.createMarkerSet("mms", "waypoints", icons, false);
				initialized = true;
				if (forceSave) {
					((ServerWaypointManager) ServerWaypointManager.getInstance()).updateDynmap();
					forceSave = false;
				}
			}
		});
		waitingThread.start();
	}

	@Override
	public WaypointList loadWaypointsForDimension(int pDim) {
		return null;
	}

	@Override
	public LinkedList<WaypointList> loadAllWaypoints() {
		return null;
	}

	@Override
	public void saveAllWaypoints(LinkedList<WaypointList> list) {
		if (initialized) {
			if (list != null) {
				String compareString;
				MarkerSet mms = api.getMarkerSet("mms");
				mms.deleteMarkerSet();
				api.createMarkerSet("mms", "waypoints", icons, false);
				mms = api.getMarkerSet("mms");
				for (WaypointList waypointList : list) {
					if (waypointList.getDim() == 0) {
						compareString = FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName();
					} else {
						compareString = "DIM" + waypointList.getDim();
					}
					for (Waypoint waypoint : waypointList) {
						mms.createMarker(null, waypoint.getName(), compareString, waypoint.getXCord(), waypoint.getYCord(), waypoint.getZCord(), api.getMarkerIcon("mms_icon"), false);
					}
				}
			}
		} else {
			forceSave = true;
		}
	}
	
	public void destroy () {
		api.getMarkerSet("mms").deleteMarkerSet();
	}
	
}
