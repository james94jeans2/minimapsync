package james94jeans2.minimapsync.network.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.client.ClientWaypointManager;
import james94jeans2.minimapsync.client.gui.GuiManager;
import net.minecraft.entity.player.EntityPlayer;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class MinimapsyncInfoPacket extends AbstractMinimapsyncPacket {
	
	private int count;
	
	public MinimapsyncInfoPacket() {
		this(0);
	}
	
	public MinimapsyncInfoPacket(int count) {
		this.count = count;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(count);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		count = buffer.readInt();
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		GuiManager.instance().initiateCounting(count+1);
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		//ignored on Server
	}

}
