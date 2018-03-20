package james94jeans2.minimapsync.network.packet;

import james94jeans2.minimapsync.Minimapsync;
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

public class MinimapsyncRequestPacket extends AbstractMinimapsyncPacket{

	public MinimapsyncRequestPacket () {
		gettingAll = false;
	}
	
	public MinimapsyncRequestPacket (boolean getAll) {
		gettingAll = getAll;
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeBoolean(gettingAll);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		gettingAll = buffer.readBoolean();
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		ClientWaypointManager.getInstance().sendAllWaypointsForDimensionToClient(player.dimension, player);
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		if (gettingAll) {
			((ServerWaypointManager) ServerWaypointManager.getInstance()).sendAllPrivateWaypointsForDimensionToClient(player.dimension, player);
		} else {
			ServerWaypointManager.getInstance().sendAllWaypointsForDimensionToClient(player.dimension, player);
		}
	}
	
	private boolean gettingAll;

}
