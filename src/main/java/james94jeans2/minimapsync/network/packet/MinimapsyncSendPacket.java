package james94jeans2.minimapsync.network.packet;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.client.ClientWaypointManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class MinimapsyncSendPacket extends AbstractMinimapsyncPacket {
	
	private String waypoint;
	private int dimension, key;
	
	public MinimapsyncSendPacket () {
		
	}
	
	public MinimapsyncSendPacket (String name, int dim, int key) {
		waypoint = name;
		dimension = dim;
		this.key = key;
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
		((ClientWaypointManager)ClientWaypointManager.getInstance()).sendWaypointToServer(this);
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		//this packet should never be send to the server
	}
	
	public int getDimension () {
		return dimension;
	}
	
	public String getWaypoint () {
		return waypoint;
	}
	
	public int getKey () {
		return key;
	}
}
