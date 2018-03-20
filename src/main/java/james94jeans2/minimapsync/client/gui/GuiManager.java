package james94jeans2.minimapsync.client.gui;

import cpw.mods.fml.client.FMLClientHandler;
import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.client.ClientWaypointManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.MinecraftForgeClient;

public class GuiManager {

private static GuiManager instance;
private Minecraft client;
private ClientWaypointManager wpm;
private int maxPoints, currentPoints;
private GuiScreenLoading loading;
private boolean overriden;

//TODO reset on disconnect

	private GuiManager () {
		client = FMLClientHandler.instance().getClient();
		wpm = (ClientWaypointManager) ClientWaypointManager.getInstance();
	}

	public static GuiManager instance() {
		if (instance == null) {
			instance = new GuiManager();
		}
		return instance;
	}

	public void displaySyncList() {
		wpm.requestAllRemoteWaypoints();
		loading = new GuiScreenLoading();
		loading.displayProgressMessage("Loading Waypoints");
		client.displayGuiScreen(loading);
		overriden = true;
	}
	
	public void setMaxWaypoints (int max) {
		maxPoints = max;
	}
	
	public void upCount () {
		++currentPoints;
		update();
	}
	
	public void update () {
		if (loading != null) {
			loading.setLoadingProgress(currentPoints/maxPoints*100);
			if (currentPoints == maxPoints) {
				showSyncList();
			}
		}
	}
	
	public void showSyncList () {
		loading.func_146586_a();
		wpm.setCounting(false);
		GuiScreen toShow = new GuiScreenSyncList(wpm);
		client.displayGuiScreen(toShow);
		overriden = false;
		loading = null;
	}

	public void initiateCounting(int count) {
		if (wpm.getMode() == 1 || overriden) {
			maxPoints = count;
			currentPoints = 0;
			wpm.setCounting(true);
		}
	}

}
