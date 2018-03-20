package james94jeans2.minimapsync.network.packet;

import james94jeans2.minimapsync.client.ClientWaypointManager;
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

public class MinimapsyncDonePacket extends AbstractMinimapsyncPacket {

	private int key;
	private boolean priv;
	
	public MinimapsyncDonePacket (int key, boolean pPriv) {
		this.key = key;
		priv = pPriv;
	}
	
	public MinimapsyncDonePacket (int key) {
		this (key, false);
	}
	
	public MinimapsyncDonePacket (boolean pPriv) {
		this (0, pPriv);
	}
	
	public MinimapsyncDonePacket () {
		this(0);
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(key);
		buffer.writeBoolean(priv);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		key = buffer.readInt();
		priv = buffer.readBoolean();
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		ClientWaypointManager.getInstance().handleDonePacket(this, player);
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		ServerWaypointManager.getInstance().handleDonePacket(this, player);
	}
	
	public int getKey () {
		return key;
	}
	
	public boolean getPriv () {
		return priv;
	}

}
