package james94jeans2.minimapsync.network.packet;

import james94jeans2.minimapsync.server.ServerWaypointManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class MinimapsyncSendWaypointPacket extends AbstractMinimapsyncPacket {
	
	private String waypoint;
	private int dimension, key;
	
	public MinimapsyncSendWaypointPacket () {
		
	}
	
	public MinimapsyncSendWaypointPacket (String waypointString, int dim, int key) {
		waypoint = waypointString;
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
		logger.info("dimension: " + dimension);
		logger.info("waypoint: " + waypoint);
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		//this packet should never be send to the client
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		((ServerWaypointManager)ServerWaypointManager.getInstance()).addAndCheckWaypoint(this);
	}
	
	public String getWaypoint () {
		return waypoint;
	}
	
	public int getDimension () {
		return dimension;
	}
	
	public int getKey() {
		return key;
	}

}
