package james94jeans2.minimapsync.util.loader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeSet;

import cpw.mods.fml.common.FMLCommonHandler;
import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.util.FileReader;
import james94jeans2.minimapsync.util.Waypoint;
import james94jeans2.minimapsync.util.WaypointList;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerLoader implements IWaypointLoader {

	private String folder, file;
	private StringBuilder fileName;
	private FileReader dimensionReader, waypointReader;
	private File save;
	
	public PlayerLoader (String pFolder) {
		folder = pFolder;
	}
	
	public PlayerLoader () {
		fileName = new StringBuilder();
		fileName.append(FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName());
		fileName.append("/waypoints/players");
		folder = fileName.toString();
	}
	
	@Override
	public WaypointList loadWaypointsForDimension(int pDim) {
		WaypointList returnList = new WaypointList(pDim, false);
		fileName = new StringBuilder();
		fileName.append(folder);
		fileName.append("/DIM");
		fileName.append(pDim);
		fileName.append(".minimapsync");
		file = fileName.toString();
		try {
			waypointReader = new FileReader(file);
			while (waypointReader.hasNextLine()) {
				returnList.add(new Waypoint(waypointReader.readNextLine(), pDim, false));
			}
		}catch (IOException e) {
		}finally {
			if (waypointReader != null) {
				waypointReader.closeScanner();
			}
			waypointReader = null;
		}
		return (returnList.isEmpty() ? null : returnList);
	}

	@Override
	public LinkedList<WaypointList> loadAllWaypoints() {
		fileName = new StringBuilder();
		fileName.append(folder);
        fileName.append("/dimensions.minimapsync");
		file = fileName.toString();
		LinkedList<WaypointList> returnList = null;
		try {
			dimensionReader = new FileReader(file);
			TreeSet<Integer> dimensions = new TreeSet<Integer>();
			while (dimensionReader.hasNextLine()) {
				dimensions.add(Integer.parseInt(dimensionReader.readNextLine()));
			}
			returnList = new LinkedList<WaypointList>();
			WaypointList addList = null;
			for (Integer integer : dimensions) {
				addList = loadWaypointsForDimension(integer);
				if (addList != null) {
					returnList.add(addList);
				}
			}
			if (returnList.isEmpty()) {
				returnList = null;
			}
		} catch (IOException e) {
		}finally {
			if (dimensionReader != null) {
				dimensionReader.closeScanner();
			}
			dimensionReader = null;
		}
		return returnList;
	}
	
	public void saveWaypointsForDimension(WaypointList list) {
		fileName = new StringBuilder();
		fileName.append(folder);
		fileName.append("/DIM");
		fileName.append(list.getDim());
		fileName.append(".minimapsync");
		file = fileName.toString();
		save = new File(file);
		FileWriter fw = null;
		try {
			fw = new FileWriter(save);
			for (Waypoint waypoint : list) {
				fw.write(waypoint.getServerLine() + "\n");
			}
		} catch (IOException e) {
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void saveAllWaypoints(LinkedList<WaypointList> list) {
		boolean saved = false;
		if (list == null || list.isEmpty()) {
			fileName = new StringBuilder();
	        fileName.append(folder);
	        fileName.append("/dimensions.minimapsync");
	        file = fileName.toString();
	        save = new File(file);
	        save.delete();
	        save = new File(folder);
	        if (save.listFiles() != null) {
		        for (File file : save.listFiles()) {
					String fileExtension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('.'));
					if (fileExtension.equalsIgnoreCase(".minimapsync")) {
						file.delete();
					}
				}
	        }
			return;
		}
		file = folder;
        save = new File(file);
        if (save.exists() == false) {
            save.mkdirs();
        }
        fileName = new StringBuilder();
        fileName.append(folder);
        fileName.append("/dimensions.minimapsync");
        file = fileName.toString();
        save = new File(file);
        FileWriter fw = null;
        boolean written = false;
        try {
        	fw = new FileWriter(save);
	        for(WaypointList wlist : list)
	        {
	        	if (!wlist.isEmpty()) {
	        		fw.write(wlist.getDim() + "\n");
	        		written = true;
	        	}
	        }
	        if (!written) {
	        	fw.close();
	        	save.delete();
	        }
	        saved = written;
        } catch (IOException e) {
        	Minimapsync.instance.logger.fatal("Unable to save waypoints!");
        } finally {
        	if (fw != null) {
	        	try {
					fw.close();
				} catch (IOException e) {
				}
        	}
        }
        if (saved) {
        	for (WaypointList waypointList : list) {
        		if (!waypointList.isEmpty()) {
        			saveWaypointsForDimension(waypointList);
        		} else {
        			fileName = new StringBuilder();
        			fileName.append(folder);
        			fileName.append("/DIM");
        			fileName.append(waypointList.getDim());
        			fileName.append(".minimapsync");
        			file = fileName.toString();
        			save = new File(file);
        			save.delete();
        		}
			}
        } else {
    		for(WaypointList wlist : list)
	        {
    			fileName = new StringBuilder();
    			fileName.append(folder);
    			fileName.append("/DIM");
    			fileName.append(wlist.getDim());
    			fileName.append(".minimapsync");
    			file = fileName.toString();
    			save = new File(file);
    			save.delete();
	        }
        }
	}
	
	public IWaypointLoader createLoader(EntityPlayer player) {
		StringBuilder newFolder = new StringBuilder(folder);
		newFolder.append("/" + player.getCommandSenderName());
		return new PlayerLoader(newFolder.toString());
	}

}
