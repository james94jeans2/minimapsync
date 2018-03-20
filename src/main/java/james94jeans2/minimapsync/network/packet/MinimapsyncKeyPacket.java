package james94jeans2.minimapsync.network.packet;

import james94jeans2.minimapsync.client.ClientWaypointManager;
import james94jeans2.minimapsync.server.ServerWaypointManager;
import james94jeans2.minimapsync.util.loader.ServerWaypointLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class MinimapsyncKeyPacket extends AbstractMinimapsyncPacket {

	private int key;
	
	public MinimapsyncKeyPacket () {
		this(0);
	}
	
	public MinimapsyncKeyPacket (int syncKey) {
		key = syncKey;
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(key);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		key = buffer.readInt();
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		((ClientWaypointManager)ClientWaypointManager.getInstance()).handleKeyPacket(key);
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		((ServerWaypointManager) ServerWaypointManager.getInstance()).sendKeyPacketCheck(player);
	}

}
