
package james94jeans2.minimapsync.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class Waypoint
{
    
    private String name, color, original, zan, colorPart, compareStr, serverLine;
    private int xCord, yCord, zCord, dim, zanXCord, zanYCord, zanZCord;
    private int[] dims;
    private float rColor, gColor, bColor;
    private boolean visible, zanPoint, death, isPublic;
    private String[] parts, dimensions;
    private StringBuilder originals;
    
    /**
     * Convert Networkline to waypoint
     * 
     * @param pNetwork networkline
     * @param pDim dimension
     */
    
    public Waypoint (String pNetwork, int pDim) {
    	zanPoint = false;
        original = pNetwork.substring(0, pNetwork.lastIndexOf(":"));
        parts = new String[7];
    	parts = pNetwork.split(":",7);
        name = parts[0].replaceAll("~colon~", ":");
        xCord = Integer.parseInt(parts[1]);
        yCord = Integer.parseInt(parts[2]);
        zCord = Integer.parseInt(parts[3]);
        if(parts[4].equalsIgnoreCase("true"))
        {
            visible = true;
        }else
        {
            visible = false;
        }
        color = parts[5];
        this.isPublic = parts[6].equals("true");
        dim = pDim;
        dims = new int [1];
        dims[0] = dim;
        buildRGB();
        buildCompareStr();
        if(FMLCommonHandler.instance().getSide().isClient()){
        if(dim == -1)
        {
            convertToZanNether();
        }else
        {
            zanXCord = xCord;
            zanYCord = yCord;
            zanZCord = zCord;
        }
        }else
        {
            zanXCord = xCord;
            zanYCord = yCord;
            zanZCord = zCord;
        }
        buildServerLine();    	
    }
    
    /**
     * Convert serverline for non voxelmap (zan) points
     * 
     * @param pWaypoint serverline
     * @param pDim dimension
     */
    public Waypoint(String pWaypoint, int pDim, boolean isPublic)
    {
        zanPoint = false;
        original = pWaypoint;
        parts = new String[6];
        parts = pWaypoint.split(":", 6);
        name = parts[0].replaceAll("~colon~", ":");
        xCord = Integer.parseInt(parts[1]);
        yCord = Integer.parseInt(parts[2]);
        zCord = Integer.parseInt(parts[3]);
        if(parts[4].equalsIgnoreCase("true"))
        {
            visible = true;
        }else
        {
            visible = false;
        }
        this.isPublic = isPublic;
        color = parts[5];
        dim = pDim;
        dims = new int [1];
        dims[0] = dim;
        buildRGB();
        buildCompareStr();
        if(FMLCommonHandler.instance().getSide().isClient()){
        if(dim == -1)
        {
            convertToZanNether();
        }else
        {
            zanXCord = xCord;
            zanYCord = yCord;
            zanZCord = zCord;
        }
        }else
        {
            zanXCord = xCord;
            zanYCord = yCord;
            zanZCord = zCord;
        }
        buildServerLine();
    }
    
    /**
     * Convert clientline to waypoint
     * 
     * @param pWaypoint client line
     * @param isPublic is it public?
     */
    public Waypoint(String pWaypoint, boolean isPublic)
    {
    	this.isPublic = isPublic;
        zanPoint = false;
        int splitPoint = pWaypoint.lastIndexOf(':');
        original = pWaypoint.substring(0, splitPoint);
        parts = new String[7];
        parts = pWaypoint.split(":", 7);
        name = parts[0].replaceAll("~colon~", ":");
        xCord = Integer.parseInt(parts[1]);
        yCord = Integer.parseInt(parts[2]);
        zCord = Integer.parseInt(parts[3]);
        if(parts[4].equalsIgnoreCase("true"))
        {
            visible = true;
        }else
        {
            visible = false;
        }
        color = parts[5];
        dim = Integer.parseInt(parts[6]);
        dims = new int [1];
        dims[0] = dim;
        buildRGB();
        buildCompareStr();
        if(FMLCommonHandler.instance().getSide().isClient()){
        if(dim == -1)
        {
            convertToZanNether();
        }else
        {
            zanXCord = xCord;
            zanYCord = yCord;
            zanZCord = zCord;
        }
        }else
        {
            zanXCord = xCord;
            zanYCord = yCord;
            zanZCord = zCord;
        }
        buildServerLine();
    }
    
    /**
     * Create Waypoint from zan parameters
     * 
     * @param pZan zan point?
     * @param pName name
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param enabled visible
     * @param r red
     * @param g green
     * @param b blue
     * @param suffix suffix
     * @param dimensions dimensions <TreeSet>
     * @param pDim dimension context
     */
    
    public Waypoint (boolean pZan, String pName, int x, int y, int z, boolean enabled, float r, float g, float b, String suffix, TreeSet<Integer> dimensions, int pDim, boolean isPublic) {
    	this.isPublic = isPublic;
    	zanPoint = pZan;
        name = pName;
        zanXCord = x;
        zanYCord = y;
        zanZCord = z;
        visible = enabled;
        rColor = r;
        gColor = g;
        bColor = b;
        if (suffix.contains("skull")) {
        	death = true;
        }else {
        	death = false;
        }
        dim = pDim;
        dims = new int[dimensions.size()];
        Iterator<Integer> iterator = dimensions.iterator();
        int i = 0;
        while(i < dimensions.size())
        {
            dims[i] = iterator.next();
            i = i + 1;
        }
        buildColor();
        buildOriginal();
        if(pDim == -1)
        {
            convertToNormalNether();
        }else
        {
            xCord = zanXCord;
            yCord = zanYCord;
            zCord = zanZCord;
        }
        buildCompareStr();
        buildServerLine();
    }
    
    /**
     * Create Waypoint from xaeros parameters
     * 
     * @param pName name
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param enabled visible
     * @param col color
     * @param pDim dimension context
     */
    
    public Waypoint (String pName, int x, int y, int z, boolean enabled, String col, int pDim) {
        name = pName;
        xCord = x;
        yCord = y;
        zCord = z;
        visible = enabled;
        color = col;
        dim = pDim;
        if(pDim == -1)
        {
        	 convertToZanNether();
        }else
        {
        	 zanXCord = xCord;
             zanYCord = yCord;
             zanZCord = zCord;
        }
        buildRGB();
        buildOriginal();
        buildCompareStr();
        buildServerLine();
    }

	/**
     * Create Waypoint from JourneyMap Waypoint Data
     * 
     * @param pName
     * @param x
     * @param y
     * @param z
     * @param enabled
     * @param r
     * @param g
     * @param b
     * @param dimensions
     * @param pDim
     */
    
    public Waypoint (String pName, int x, int y, int z, boolean enabled, int r, int g, int b, Collection<Integer> dimensions, int pDim, boolean isPublic)
    {
    	this.isPublic = isPublic;
    	this.name = pName;
    	this.xCord = x;
    	this.yCord = y;
    	this.zCord = z;
    	this.visible = enabled;
    	this.rColor = (float)r/255;
    	this.gColor = (float)g/255;
    	this.bColor = (float)b/255;
    	zanPoint = true;
    	buildColor();
    	ArrayList<Integer> temp = new ArrayList<Integer>();
    	temp.addAll(dimensions);
    	dims = new int[temp.size()];
    	for (int i = 0; i < temp.size(); ++i) {
			dims[i] = temp.get(i);
		}
    	this.dim = pDim;
    	buildOriginal();
    	buildCompareStr();
        buildServerLine();
    }
    
    /**
     * Create Waypoint from DynMap Marker Data
     * 
     * @param pName
     * @param x
     * @param y
     * @param z
     * @param pDim
     */
    
    public Waypoint (String pName, int x, int y, int z, int pDim, boolean isPublic)
    {
    	this.isPublic = isPublic;
    	this.name = pName;
    	this.xCord = x;
    	this.yCord = y;
    	this.zCord = z;
    	this.dim = pDim;
    	buildCompareStr();
    }
    
    private void buildColor()
    {
        colorPart = Integer.toHexString(getFirstColor());
        if(colorPart.length() == 2)
        {
            color = colorPart;
        }else
        {
            colorPart = "0" + colorPart;
            color = colorPart;
        }
        colorPart = Integer.toHexString(getSecondColor());
        if(colorPart.length() == 2)
        {
            color = color + colorPart;
        }else
        {
            colorPart = "0" + colorPart;
            color = color + colorPart;
        }
        colorPart = Integer.toHexString(getThirdColor());
        if(colorPart.length() == 2)
        {
            color = color + colorPart;
        }else
        {
            colorPart = "0" + colorPart;
            color = color + colorPart;
        }
    }
    
    private void buildCompareStr()
    {
        compareStr = name;
        compareStr = compareStr + ":";
        compareStr = compareStr + xCord;
        compareStr = compareStr + ":";
        compareStr = compareStr + yCord;
        compareStr = compareStr + ":";
        compareStr = compareStr + zCord;
    }
    
    private void buildOriginal()
    {
        originals = new StringBuilder();
        originals.append(name);
        originals.append(":");
        originals.append(zanXCord);
        originals.append(":");
        originals.append(zanYCord);
        originals.append(":");
        originals.append(zanZCord);
        originals.append(":");
        originals.append(visible);
        originals.append(":");
        originals.append(color);
        original = originals.toString();
    }
    
    private void buildRGB()
    {
        rColor = (float)getFirstColor() / 255;
        gColor = (float)getSecondColor() / 255;
        bColor = (float)getThirdColor() / 255;
    }
    
    private void buildServerLine()
    {
        originals = new StringBuilder();
        originals.append(name.replaceAll(":", "~colon~"));
        originals.append(":");
        originals.append(xCord);
        originals.append(":");
        originals.append(yCord);
        originals.append(":");
        originals.append(zCord);
        originals.append(":");
        originals.append(visible);
        originals.append(":");
        originals.append(color);
        serverLine = originals.toString();
    }
    
    public void convertToNormalNether()
    {
        xCord = zanXCord / 8;
        yCord = zanYCord;
        zCord = zanZCord / 8;
    }
    
    public void convertToZanNether()
    {
        zanXCord = xCord * 8;
        zanYCord = yCord;
        zanZCord = zCord * 8;
    }
    
    public float getB()
    {
        return bColor;
    }
    
    public String getColor()
    {
        return color;
    }
    
    public String getCompareStr()
    {
        return compareStr;
    }
    
    public int getDim()
    {
        return dim;
    }
    
    public int[] getDims()
    {
        return dims;
    }
    
    public int getFirstColor()
    {
        if(zanPoint)
        {
            return (int) (rColor * 255);
        }else
        {
            return Integer.parseInt(color.substring(0, 2), 16);
        }
    }
    
    public float getG()
    {
        return gColor;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getOriginal()
    {
        return original;
    }
    
    public float getR()
    {
        return rColor;
    }
    
    public int getSecondColor()
    {
        if(zanPoint)
        {
            return (int) (gColor * 255);
        }else
        {
            return Integer.parseInt(color.substring(2,4), 16);
        }
    }
    
    public String getServerLine()
    {
        return serverLine;
    }
    
    public int getThirdColor()
    {
        if(zanPoint)
        {
            return (int) (bColor * 255);
        }else
        {
            return Integer.parseInt(color.substring(4), 16);
        }
    }
    
    public boolean getVisible()
    {
        return visible;
    }
    
    public int getXCord()
    {
        return xCord;
    }
    
    public int getYCord()
    {
        return yCord;
    }
    
    public int getZanXCord()
    {
        return zanXCord;
    }
    
    public int getZanYCord()
    {
        return zanYCord;
    }
    
    public int getZanZCord()
    {
        return zanZCord;
    }
    
    public int getZCord()
    {
        return zCord;
    }
    
    public void setColor(String pColor)
    {
        color = pColor;
    }
    
    public void setCords(int pXCord, int pYCord, int pZCord)
    {
        xCord = pXCord;
        yCord = pYCord;
        zCord = pZCord;
    }
    
    public void setDim(int pDim)
    {
        dim = pDim;
    }
    
    public void setName(String pName)
    {
        name = pName;
    }
    
    public void setOriginal(String pOriginal)
    {
        original = pOriginal;
    }
    
    public void setVisible(boolean pVisible)
    {
        visible = pVisible;
    }
    
    public boolean zan()
    {
        return zanPoint;
    }
    
    public boolean isDeath() {
    	return death;
    }
    
    public boolean isPublic () {
    	return isPublic;
    }
    
    public void setPublic (boolean isPublic) {
    	this.isPublic = isPublic;
    }
    
    public Waypoint clone () {
    	//TODO make proper cloning
//    	ArrayList<Integer> dimensions = new ArrayList<Integer>();
//    	for (int i : dims) {
//    		dimensions.add(i);
//    	}
//    	return new Waypoint(name, xCord, yCord, zCord, visible, (int)rColor*255, (int)gColor*255, (int)bColor*255, dimensions, dim, isPublic);
    	return new Waypoint(getNetworkLine(), dim);
    }
    
    public String getNetworkLine () {
    	return serverLine + ":" + (isPublic ? "true" : "false");
    }
    
}
