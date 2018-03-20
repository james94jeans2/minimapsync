package james94jeans2.minimapsync.client.gui;

import java.io.IOException;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.client.ClientWaypointManager;
import james94jeans2.minimapsync.client.MinimapKeyHandler;
import james94jeans2.minimapsync.util.Waypoint;
import james94jeans2.minimapsync.util.WaypointList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.MathHelper;

public class GuiScreenSyncList extends GuiScreen {
	
	private List clientList, serverList;
	private WaypointList clientData, serverData;
	private ClientWaypointManager manager;
	private GuiButton delete;
	
	public GuiScreenSyncList (ClientWaypointManager manager) {
		super();
		this.manager = manager;
		WaypointList tempList = manager.getWaypointsForDimension(Minecraft.getMinecraft().thePlayer.dimension);
		if (tempList != null) {
			clientData = tempList.getPrivate();
			serverData = tempList.getPublic();
		} else {
			clientData = new WaypointList(Minecraft.getMinecraft().thePlayer.dimension, false);
			serverData = new WaypointList(Minecraft.getMinecraft().thePlayer.dimension, true);
		}
	}
	
	@Override
	public void initGui () {
		clientList = new List(this.width/2, this.width/2 - 50, 0, clientData, true);
		serverList = new List(this.width/2 + 1,this.width/2 - 50, this.width/2, serverData, false);
		buttonList.clear();
		this.buttonList.add(new GuiButton(0, this.width / 2 + 45, this.height - 26, 60, 20, "Cancel"));
		this.buttonList.add(new GuiButton(1, this.width / 2 - 30, this.height - 26, 60, 20, "Apply"));
		this.buttonList.add(new GuiButton(2, this.width/2 - 15, clientList.bottom / 2 - 20, 30, 20, ">"));
		this.buttonList.add(new GuiButton(3, this.width/2 - 15, clientList.bottom / 2 + 20, 30, 20, "<"));
		delete = new GuiButton(4, this.width/2 - 105, this.height - 26, 60, 20, "Delete");
		this.buttonList.add(delete);
		delete.enabled = !clientData.isEmpty();
	}
	
	@Override
	public void confirmClicked (boolean result, int id) {
		
	}
	
	@Override
	public void drawScreen (int mouseX, int mouseY, float f) {
		if (clientList != null && serverList != null) {
			this.clientList.drawScreen(mouseX, mouseY, f);
			this.serverList.drawScreen(mouseX, mouseY, f);
			overlayMiddle();
			drawCenteredString(fontRendererObj, "Minimapsync", width/2, 10, -1);
			drawCenteredString(fontRendererObj, "private waypoints:", (clientList.listWidth + 6)/2, 20, -1);
			drawCenteredString(fontRendererObj, "public waypoints:", width - (serverList.listWidth + 6)/2, 20, -1);
		}
		super.drawScreen(mouseX, mouseY, f);
	}
	
	@Override
	public void keyTyped (char par1, int par2) {
		switch (par2) {
			case 1:
				cancel();
				break;
			case 28:
				saveChanges();
				break;
			case 211:
				if (delete.enabled) {
					deleteWaypoint();
				}
				break;
		}
		super.keyTyped(par1, par2);
	}
	
	private void deleteWaypoint () {
		Waypoint toDelete = clientList.getSelected();
		if (toDelete != null) {
			clientData.remove(toDelete);
			delete.enabled = !clientData.isEmpty();
		}
	}
	
	private void cancel () {
		manager.handleSaving();
		MinimapKeyHandler.getInstance().enable();
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
	
	@Override
	public void actionPerformed (GuiButton button) {
		switch (button.id) {
			case 0://cancel
				cancel();
				break;
			case 1://apply
				saveChanges();
				break;
			case 2:
				add();
				break;
			case 3:
				remove();
				break;
			case 4:
				deleteWaypoint();
				break;
		}
	}
	
	private void remove () {
		Waypoint toRemove = serverList.getSelected();
		if (toRemove != null) {
			toRemove.setPublic(false);
			serverData.remove(toRemove);
			clientData.add(toRemove);
			clientList.select(0);
			delete.enabled = true;
		}
	}
	
	private void add () {
		Waypoint toAdd = clientList.getSelected();
		if (toAdd != null) {
			toAdd.setPublic(true);
			clientData.remove(toAdd);
			serverData.add(toAdd);
			delete.enabled = !clientData.isEmpty();
		}
	}
	
	private void saveChanges () {
		WaypointList tempList = clientData;
		tempList.addAll(serverData);
		((ClientWaypointManager)ClientWaypointManager.getInstance()).setWaypointsForDimension(tempList.getDim(),tempList);
		manager.handleSaving();
		MinimapKeyHandler.getInstance().enable();
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
	
	public void handleMouseInput()
    {
        super.handleMouseInput();
        if (clientList != null && serverList != null) {
        	clientList.handleMouseInput();
        	serverList.handleMouseInput();
        }
    }
	
	private void overlayMiddle()
    {
        Tessellator tessellator = Tessellator.instance;
        this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        //disableLighting();
        //GlStateManager.disableFog();
        //GlStateManager.disableDepth();
        float f = 32.0F;
//        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//        worldrenderer.func_178974_a(4210752, 255);
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(4210752, 255);
//        worldrenderer.pos((double)(clientList.getListWidth() + 12), (double)(clientList.height - clientList.top), 0.0D).tex((double)((float)(clientList.getListWidth() + 12) / f), (double)((float)(clientList.height - clientList.top) / f)).color(64, 64, 64, 255).endVertex();
        tessellator.addVertexWithUV((double)(clientList.getListWidth() + 12), (double)(clientList.height - clientList.top), 0.0D, (double)((float)(clientList.getListWidth() + 12) / f), (double)((float)(clientList.height - clientList.top) / f));
//        worldrenderer.pos((double)(this.width - serverList.getListWidth() - 12), (double)(clientList.height - clientList.top), 0.0D).tex((double)((float)(this.width - serverList.getListWidth() - 12) / f), (double)((float)(clientList.height - clientList.top) / f)).color(64, 64, 64, 255).endVertex();
        tessellator.addVertexWithUV((double)(this.width - serverList.getListWidth() - 12), (double)(clientList.height - clientList.top), 0.0D, (double)((float)(this.width - serverList.getListWidth() - 12) / f), (double)((float)(clientList.height - clientList.top) / f));
//        worldrenderer.pos((double)(this.width - serverList.getListWidth() - 12), (double)clientList.top, 0.0D).tex((double)((float)(this.width - serverList.getListWidth() - 12) / f), (double)((float)clientList.top / f)).color(64, 64, 64, 255).endVertex();
        tessellator.addVertexWithUV((double)(this.width - serverList.getListWidth() - 12), (double)clientList.top, 0.0D, (double)((float)(this.width - serverList.getListWidth() - 12) / f), (double)((float)clientList.top / f));
//        worldrenderer.pos((double)(clientList.getListWidth() + 12), (double)clientList.top, 0.0D).tex((double)((float)(clientList.getListWidth() + 12) / f), (double)((float)clientList.top / f)).color(64, 64, 64, 255).endVertex();
        tessellator.addVertexWithUV((double)(clientList.getListWidth() + 12), (double)clientList.top, 0.0D, (double)((float)(clientList.getListWidth() + 12) / f), (double)((float)clientList.top / f));
        tessellator.draw();
    }

	private class List extends GuiSlot {

		private WaypointList data;
		private int listWidth, mSelectedElement;
		private float mAmountScrolled, mInitialClickY = -2.0F,mScrollMultiplier;
		private boolean isleft, mHasListHeader = false;
		private long mLastClicked;
		private Minecraft mc;
		
		public List (int width, int listWidth, int position, WaypointList data, boolean isleft) {
			super(Minecraft.getMinecraft(),width,GuiScreenSyncList.this.height,32,GuiScreenSyncList.this.height - 32,14);
			this.left = position;
			this.right = position + width;
			this.data = data;
			mSelectedElement = 0;
			this.listWidth = listWidth;
			this.isleft = isleft;
			mc = Minecraft.getMinecraft();
		}
		
		@Override
		public int getListWidth() {
			return listWidth;
		}
		
		@Override
		protected int getScrollBarX() {
			if (isleft) {
				return this.left + this.getListWidth() + 6;
			} else {
				return this.left + this.width - 6;
			}
		}
		
		@Override
		protected int getSize() {
			return data.size();
		}

		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick,
				int mouseX, int mouseY) {
			mSelectedElement = (slotIndex < getSize() ? slotIndex : -1);
		}

		@Override
		protected boolean isSelected(int slotIndex) {
			return mSelectedElement == slotIndex;
		}

		@Override
		protected void drawSlot(int var1, int var2,
				int var3, int var4,Tessellator tessl, int var5,
				int var6) {
			if (data !=  null && !data.isEmpty() && data.size() > var1) {
				String text = data.get(var1).getName();
				int stringWidth = fontRendererObj.getStringWidth(text);
				int x;
				if (isleft) {
					x = left + 5 + listWidth/2 - stringWidth/2;
				} else {
					x = left + width - listWidth/2 - stringWidth/2;
				}
				drawString(fontRendererObj, text, x, var3+1, 8421504);
			}
		}

		@Override
		protected void drawBackground() {
		}
		
		@Override
		protected void drawSelectionBox(int p_148120_1_, int p_148120_2_, int p_148120_3_, int p_148120_4_)
	    {
	        int i1 = this.getSize();
	        Tessellator tessellator = Tessellator.instance;

	        for (int j1 = 0; j1 < i1; ++j1)
	        {
	            int k1 = p_148120_2_ + j1 * this.slotHeight + this.headerPadding;
	            int l1 = this.slotHeight - 4;

//	            if (k1 > this.bottom || k1 + l1 < this.top)
//	            {
//	                this.func_178040_a(j1, p_148120_1_, k1);
//	            }

	            if (this.isSelected(j1))
	            {
	            	int i2, j2;
	            	if (isleft) {
		                i2 = this.left + 5;
		                j2 = this.left+ getListWidth() + 5;
	            	} else {
	            		i2 = this.left + this.width - this.getListWidth() - 7;
		                j2 = this.left + this.width - 7;
	            	}
	                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	                GL11.glDisable(GL11.GL_TEXTURE_2D);
	                tessellator.startDrawingQuads();
//	                worldrenderer.begin(7,DefaultVertexFormats.POSITION_TEX_COLOR);
	                //worldrenderer.func_178991_c(8421504);
	                tessellator.setColorOpaque_I(8421504);
//	                worldrenderer.pos((double)i2, (double)(k1 + l1 + 2), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
	                tessellator.addVertexWithUV((double)i2, (double)(k1 + l1 + 2), 0.0D, 0.0D, 1.0D);
//	                worldrenderer.pos((double)j2, (double)(k1 + l1 + 2), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
	                tessellator.addVertexWithUV((double)j2, (double)(k1 + l1 + 2), 0.0D, 1.0D, 1.0D);
//	                worldrenderer.pos((double)j2, (double)(k1 - 2), 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
	                tessellator.addVertexWithUV((double)j2, (double)(k1 - 2), 0.0D, 1.0D, 0.0D);
//	                worldrenderer.pos((double)i2, (double)(k1 - 2), 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
	                tessellator.addVertexWithUV((double)i2, (double)(k1 - 2), 0.0D, 0.0D, 0.0D);
	                tessellator.setColorOpaque_I(0);
//	                worldrenderer.pos((double)(i2 + 1), (double)(k1 + l1 + 1), 0.0D).tex(0.0D, 1.0).color(0, 0, 0, 255).endVertex();
	                tessellator.addVertexWithUV((double)(i2 + 1), (double)(k1 + l1 + 1), 0.0D, 0.0D, 1.0D);
//	                worldrenderer.pos((double)(j2 - 1), (double)(k1 + l1 + 1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
	                tessellator.addVertexWithUV((double)(j2 - 1), (double)(k1 + l1 + 1), 0.0D, 1.0D, 1.0D);
//	                worldrenderer.pos((double)(j2 - 1), (double)(k1 - 1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
	                tessellator.addVertexWithUV((double)(j2 - 1), (double)(k1 - 1), 0.0D, 1.0D, 0.0D);
//	                worldrenderer.pos((double)(i2 + 1), (double)(k1 - 1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
	                tessellator.addVertexWithUV((double)(i2 + 1), (double)(k1 - 1), 0.0D, 0.0D, 0.0D);
	                tessellator.draw();
	                GL11.glEnable(GL11.GL_TEXTURE_2D);
	            }

	            this.drawSlot(j1, p_148120_1_, k1, l1, tessellator, p_148120_3_, p_148120_4_);
	        }
	    }
		
		public void handleMouseInput()
	    {
	        if (this.func_148141_e(this.mouseY))
	        {
	            if (Mouse.isButtonDown(0) && this.func_148125_i())
	            {
	                if (this.mInitialClickY == -1.0F)
	                {
	                    boolean flag = true;

	                    if (this.mouseY >= this.top && this.mouseY <= this.bottom)
	                    {
	                    	int i,j;
	                    	if (isleft) {
	                    		i = left+6;
		                        j = 6+listWidth;
	                    	} else {
	                    		i = left+(width-listWidth-6);
		                        j = left+width-6;
	                    	}
	                        
	                        int k = this.mouseY - this.top - this.headerPadding + (int)this.mAmountScrolled - 4;
	                        int l = k / this.slotHeight;

	                        if (this.mouseX >= i && this.mouseX <= j && l >= 0 && k >= 0 && l < this.getSize())
	                        {
	                            boolean flag1 = l == this.mSelectedElement && Minecraft.getSystemTime() - this.mLastClicked < 250L;
	                            this.elementClicked(l, flag1, this.mouseX, this.mouseY);
	                            this.mSelectedElement = l;
	                            this.mLastClicked = Minecraft.getSystemTime();
	                        }
	                        else if (this.mouseX >= i && this.mouseX <= j && k < 0)
	                        {
	                            this.func_148132_a(this.mouseX - i, this.mouseY - this.top + (int)this.mAmountScrolled - 4);
	                            flag = false;
	                        }

	                        int i2 = this.getScrollBarX();
	                        int i1 = i2 + 6;

	                        if (this.mouseX >= i2 && this.mouseX <= i1)
	                        {
	                            this.mScrollMultiplier = -1.0F;
	                            int j1 = this.func_148135_f();

	                            if (j1 < 1)
	                            {
	                                j1 = 1;
	                            }

	                            int k1 = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getContentHeight());
	                            k1 = MathHelper.clamp_int(k1, 32, this.bottom - this.top - 8);
	                            this.mScrollMultiplier /= (float)(this.bottom - this.top - k1) / (float)j1;
	                        }
	                        else
	                        {
	                            this.mScrollMultiplier = 1.0F;
	                        }

	                        if (flag)
	                        {
	                            this.mInitialClickY = this.mouseY;
	                        }
	                        else
	                        {
	                            this.mInitialClickY = -2;
	                        }
	                    }
	                    else
	                    {
	                        this.mInitialClickY = -2;
	                    }
	                }
	                else if (this.mInitialClickY >= 0.0F)
	                {
	                    this.mAmountScrolled -= ((float)this.mouseY - this.mInitialClickY) * this.mScrollMultiplier;
	                    this.mInitialClickY = this.mouseY;
	                }
	            }
	            else
	            {
	                this.mInitialClickY = -1;
	            }

	            int l1 = Mouse.getEventDWheel();

	            if (l1 != 0)
	            {
	                if (l1 > 0)
	                {
	                    l1 = -1;
	                }
	                else if (l1 < 0)
	                {
	                    l1 = 1;
	                }

	                this.mAmountScrolled += (float)(l1 * this.slotHeight / 2);
	            }
	        }
	    }
		
		@Override
		public boolean func_148141_e(int p_148141_1_)
	    {
			if (isleft) {
				return p_148141_1_ >= this.top && p_148141_1_ <= this.bottom && this.mouseX >= this.left && this.mouseX <= left+listWidth+12;
			} else {
				return p_148141_1_ >= this.top && p_148141_1_ <= this.bottom && this.mouseX >= left + width - listWidth - 12 && this.mouseX <= left+width;
			}
	    }
		
		public void select (int index) {
			mSelectedElement = index;
		}
		
		public Waypoint getSelected () {
			if (data != null && data.size() > mSelectedElement && mSelectedElement > -1) {
				//TODO ?
//				if (mSelectedElement == (data.size() - 1)) {
//					return data.get(mSelectedElement--);
//				}
				return data.get(mSelectedElement);
			}
			return null;
		}
		
		private void mBindAmountScrolled()
	    {
	        int i = this.func_148135_f();

	        if (i < 0)
	        {
	            i /= 2;
	        }

	        if (!this.field_148163_i && i < 0)
	        {
	            i = 0;
	        }

	        if (this.mAmountScrolled < 0.0F)
	        {
	            this.mAmountScrolled = 0.0F;
	        }

	        if (this.mAmountScrolled > (float)i)
	        {
	            this.mAmountScrolled = (float)i;
	        }
	    }
		
		@Override
		public void drawScreen(int p_148128_1_, int p_148128_2_, float p_148128_3_)
	    {
	        this.mouseX = p_148128_1_;
	        this.mouseY = p_148128_2_;
	        this.drawBackground();
	        int k = this.getSize();
	        int l = this.getScrollBarX();
	        int i1 = l + 6;
	        int l1;
	        int i2;
	        int k2;
	        int i3;

	        handleMouseInput();

	        this.mBindAmountScrolled();
	        GL11.glDisable(GL11.GL_LIGHTING);
	        GL11.glDisable(GL11.GL_FOG);
	        Tessellator tessellator = Tessellator.instance;
	        drawContainerBackground(tessellator);
	        l1 = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
	        i2 = this.top + 4 - (int)this.mAmountScrolled;

	        if (this.mHasListHeader)
	        {
	            this.drawListHeader(l1, i2, tessellator);
	        }

	        this.drawSelectionBox(l1, i2, p_148128_1_, p_148128_2_);
	        GL11.glDisable(GL11.GL_DEPTH_TEST);
	        byte b0 = 4;
	        this.mOverlayBackground(0, this.top, 255, 255);
	        this.mOverlayBackground(this.bottom, this.height, 255, 255);
	        GL11.glEnable(GL11.GL_BLEND);
	        OpenGlHelper.glBlendFunc(770, 771, 0, 1);
	        GL11.glDisable(GL11.GL_ALPHA_TEST);
	        GL11.glShadeModel(GL11.GL_SMOOTH);
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	        tessellator.startDrawingQuads();
	        tessellator.setColorRGBA_I(0, 0);
	        tessellator.addVertexWithUV((double)this.left, (double)(this.top + b0), 0.0D, 0.0D, 1.0D);
	        tessellator.addVertexWithUV((double)this.right, (double)(this.top + b0), 0.0D, 1.0D, 1.0D);
	        tessellator.setColorRGBA_I(0, 255);
	        tessellator.addVertexWithUV((double)this.right, (double)this.top, 0.0D, 1.0D, 0.0D);
	        tessellator.addVertexWithUV((double)this.left, (double)this.top, 0.0D, 0.0D, 0.0D);
	        tessellator.draw();
	        tessellator.startDrawingQuads();
	        tessellator.setColorRGBA_I(0, 255);
	        tessellator.addVertexWithUV((double)this.left, (double)this.bottom, 0.0D, 0.0D, 1.0D);
	        tessellator.addVertexWithUV((double)this.right, (double)this.bottom, 0.0D, 1.0D, 1.0D);
	        tessellator.setColorRGBA_I(0, 0);
	        tessellator.addVertexWithUV((double)this.right, (double)(this.bottom - b0), 0.0D, 1.0D, 0.0D);
	        tessellator.addVertexWithUV((double)this.left, (double)(this.bottom - b0), 0.0D, 0.0D, 0.0D);
	        tessellator.draw();
	        i3 = this.func_148135_f();

	        if (i3 > 0)
	        {
	            k2 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();

	            if (k2 < 32)
	            {
	                k2 = 32;
	            }

	            if (k2 > this.bottom - this.top - 8)
	            {
	                k2 = this.bottom - this.top - 8;
	            }

	            int l2 = (int)this.mAmountScrolled * (this.bottom - this.top - k2) / i3 + this.top;

	            if (l2 < this.top)
	            {
	                l2 = this.top;
	            }

	            tessellator.startDrawingQuads();
	            tessellator.setColorRGBA_I(0, 255);
	            tessellator.addVertexWithUV((double)l, (double)this.bottom, 0.0D, 0.0D, 1.0D);
	            tessellator.addVertexWithUV((double)i1, (double)this.bottom, 0.0D, 1.0D, 1.0D);
	            tessellator.addVertexWithUV((double)i1, (double)this.top, 0.0D, 1.0D, 0.0D);
	            tessellator.addVertexWithUV((double)l, (double)this.top, 0.0D, 0.0D, 0.0D);
	            tessellator.draw();
	            tessellator.startDrawingQuads();
	            tessellator.setColorRGBA_I(8421504, 255);
	            tessellator.addVertexWithUV((double)l, (double)(l2 + k2), 0.0D, 0.0D, 1.0D);
	            tessellator.addVertexWithUV((double)i1, (double)(l2 + k2), 0.0D, 1.0D, 1.0D);
	            tessellator.addVertexWithUV((double)i1, (double)l2, 0.0D, 1.0D, 0.0D);
	            tessellator.addVertexWithUV((double)l, (double)l2, 0.0D, 0.0D, 0.0D);
	            tessellator.draw();
	            tessellator.startDrawingQuads();
	            tessellator.setColorRGBA_I(12632256, 255);
	            tessellator.addVertexWithUV((double)l, (double)(l2 + k2 - 1), 0.0D, 0.0D, 1.0D);
	            tessellator.addVertexWithUV((double)(i1 - 1), (double)(l2 + k2 - 1), 0.0D, 1.0D, 1.0D);
	            tessellator.addVertexWithUV((double)(i1 - 1), (double)l2, 0.0D, 1.0D, 0.0D);
	            tessellator.addVertexWithUV((double)l, (double)l2, 0.0D, 0.0D, 0.0D);
	            tessellator.draw();
	        }

	        this.func_148142_b(p_148128_1_, p_148128_2_);
	        GL11.glEnable(GL11.GL_TEXTURE_2D);
	        GL11.glShadeModel(GL11.GL_FLAT);
	        GL11.glEnable(GL11.GL_ALPHA_TEST);
	        GL11.glDisable(GL11.GL_BLEND);
	    }
		
		private void mOverlayBackground(int p_148136_1_, int p_148136_2_, int p_148136_3_, int p_148136_4_)
	    {
	        Tessellator tessellator = Tessellator.instance;
	        mc.getTextureManager().bindTexture(Gui.optionsBackground);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        float f = 32.0F;
	        tessellator.startDrawingQuads();
	        tessellator.setColorRGBA_I(4210752, p_148136_4_);
	        tessellator.addVertexWithUV((double)this.left, (double)p_148136_2_, 0.0D, 0.0D, (double)((float)p_148136_2_ / f));
	        tessellator.addVertexWithUV((double)(this.left + this.width), (double)p_148136_2_, 0.0D, (double)((float)this.width / f), (double)((float)p_148136_2_ / f));
	        tessellator.setColorRGBA_I(4210752, p_148136_3_);
	        tessellator.addVertexWithUV((double)(this.left + this.width), (double)p_148136_1_, 0.0D, (double)((float)this.width / f), (double)((float)p_148136_1_ / f));
	        tessellator.addVertexWithUV((double)this.left, (double)p_148136_1_, 0.0D, 0.0D, (double)((float)p_148136_1_ / f));
	        tessellator.draw();
	    }
		
	}
	
}
