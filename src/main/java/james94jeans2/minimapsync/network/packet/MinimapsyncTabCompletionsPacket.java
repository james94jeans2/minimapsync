package james94jeans2.minimapsync.network.packet;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.network.packethandling.MinimapsyncPacketPipeline;
import james94jeans2.minimapsync.server.command.CommandSendWaypoint;
import james94jeans2.minimapsync.server.command.CommandWaypointTeleport;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.network.play.INetHandlerPlayClient;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class MinimapsyncTabCompletionsPacket extends AbstractMinimapsyncPacket {

	private String[] stringArray;
	
	public MinimapsyncTabCompletionsPacket () {
		
	}
	
	public MinimapsyncTabCompletionsPacket(String[] stringArray) {
		this.stringArray = stringArray;
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		StringBuilder stringBuilder = new StringBuilder();
		if (stringArray.length != 0) {
			for (String string : stringArray) {
				stringBuilder.append("\"" + string + "\"" + "_:_");
			}
			stringBuilder.delete(stringBuilder.length() - 3, stringBuilder.length());
		}
		String toSend = stringBuilder.toString();
		for (char c : toSend.toCharArray()) {
			buffer.writeChar(c);
		}
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		StringBuilder stringBuilder = new StringBuilder();
		while (buffer.isReadable()) {
			stringBuilder.append(buffer.readChar());
		}
		String toConvert = stringBuilder.toString();
		stringArray = toConvert.split("_:_");
		for (int i = 0; i < stringArray.length; ++i) {
			stringArray[i] = stringArray[i].replaceAll("\"", "");
		}
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		final List<String> list = CommandWaypointTeleport.getCompletionList(player, stringArray);
		Thread thread = new Thread (new Runnable() {
			
			@Override
			public void run() {
					((INetHandlerPlayClient)FMLCommonHandler.instance().getClientPlayHandler()).handleTabComplete(new S3APacketTabComplete((String[])list.toArray(new String[list.size()])));
			}
			
		});
		thread.setName("Minimapsync Tab Thread");
		thread.start();
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		//Should never be send to the server
	}

}
