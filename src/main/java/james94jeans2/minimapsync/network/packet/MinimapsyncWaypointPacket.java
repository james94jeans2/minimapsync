package james94jeans2.minimapsync.network.packet;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.client.ClientWaypointManager;
import james94jeans2.minimapsync.server.ServerWaypointManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class MinimapsyncWaypointPacket extends AbstractMinimapsyncPacket {

	private String waypoint;
	private int dimension, key;
	
	public MinimapsyncWaypointPacket () {
		
	}
	
	public MinimapsyncWaypointPacket (String waypointString, int dim) {
		this(waypointString, dim, 0);
	}
	
	public MinimapsyncWaypointPacket (String waypointString, int dim, int syncKey) {
		waypoint = waypointString;
		dimension = dim;
		key = syncKey;
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(key);
		buffer.writeInt(dimension);
		for (char c : waypoint.toCharArray()) {
			buffer.writeChar(c);
		}
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		key = buffer.readInt();
		dimension = buffer.readInt();
		waypoint = "";
		while (buffer.isReadable()) {
			waypoint += buffer.readChar();
		}
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
			//ClientWaypointManager.getInstance().handleLANWaypointPacket(this, player);
        }else{
        	ClientWaypointManager.getInstance().handleWaypointPacket(this, player);
        }
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
            ServerWaypointManager.getInstance().handleWaypointPacket(this, player);
	}
	
	public String getWaypointString () {
		return waypoint;
	}
	
	public int getDimension () {
		return dimension;
	}
	
	public int getKey () {
		return key;
	}

}
