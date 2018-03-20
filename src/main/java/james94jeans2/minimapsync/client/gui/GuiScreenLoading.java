package james94jeans2.minimapsync.client.gui;

import java.io.IOException;

import james94jeans2.minimapsync.Minimapsync;
import net.minecraft.client.gui.GuiScreenWorking;

public class GuiScreenLoading extends GuiScreenWorking {
	
	@Override
	public void keyTyped (char par1, int par2) {
		if (par2 != 1) {
			super.keyTyped(par1, par2);
		}
	}

}
