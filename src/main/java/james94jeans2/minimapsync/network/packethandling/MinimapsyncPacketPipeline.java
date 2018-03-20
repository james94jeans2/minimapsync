package james94jeans2.minimapsync.network.packethandling;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.network.packet.AbstractMinimapsyncPacket;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.network.NetHandlerPlayServer;

@ChannelHandler.Sharable
public class MinimapsyncPacketPipeline extends
		MessageToMessageCodec<FMLProxyPacket, AbstractMinimapsyncPacket> {

	private EnumMap<Side, FMLEmbeddedChannel> channels;
	private LinkedList<Class<? extends AbstractMinimapsyncPacket>> packets = new LinkedList<Class<? extends AbstractMinimapsyncPacket>>();
	private boolean isPostInitialised = false;
	private Logger logger;
	
	public boolean registerPacket (Class<? extends AbstractMinimapsyncPacket> clazz) {
		if (packets.size() > 256) {
			logger.fatal("PacketPipeline out of space!");
			return false;
		}
		if (packets.contains(clazz)) {
			logger.fatal("PacketPipeline allready contains packet: " + clazz.getName());
			return false;
		}
		if (isPostInitialised) {
			logger.fatal("PacketPipeline allready postinitilaised, unable to register packet: " + clazz.getName());
			return false;
		}
		packets.add(clazz);
		return true;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx,
			AbstractMinimapsyncPacket msg, List<Object> out) throws Exception {
		ByteBuf buffer = Unpooled.buffer();
		Class<? extends AbstractMinimapsyncPacket> clazz = msg.getClass();
		if (!packets.contains(clazz)) {
			throw new NullPointerException("Minimapsync has no packet registered for: " + clazz.getCanonicalName());
		}
		byte discriminator = (byte) packets.indexOf(clazz);
		buffer.writeByte(discriminator);
		msg.encodeInto(ctx, buffer);
		FMLProxyPacket proxyPacket = new FMLProxyPacket(buffer.copy(), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
		out.add(proxyPacket);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg,
			List<Object> out) throws Exception {
		ByteBuf payload = msg.payload();
		byte discriminator = payload.readByte();
		Class<? extends AbstractMinimapsyncPacket> clazz = packets.get(discriminator);
		if (clazz == null) {
			throw new NullPointerException("Minimapsync has no packet registered for discriminator: " + discriminator);
		}
		AbstractMinimapsyncPacket pkt = clazz.newInstance();
		pkt.decodeInto(ctx, payload.slice());
		EntityPlayer player;
		if (FMLCommonHandler.instance().getSide().isClient()) {
			if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
				player = getClientPlayer();
				pkt.handleClientSide(player);
			}else {
				INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
				player = ((NetHandlerPlayServer) netHandler).playerEntity;
				pkt.handleClientSide(player);
			}
		}else {
			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			player = ((NetHandlerPlayServer) netHandler).playerEntity;
			pkt.handleServerSide(player);
		}
		out.add(pkt);
	}
	
	public void initialise () {
		channels = NetworkRegistry.INSTANCE.newChannel("minimapsync", this);
		logger = Minimapsync.instance.logger;
	}
	
	public void postInitialise () {
		if (isPostInitialised) {
			return;
		}
		isPostInitialised = true;
		Collections.sort(packets, new Comparator<Class<? extends AbstractMinimapsyncPacket>> () {

			@Override
			public int compare(Class<? extends AbstractMinimapsyncPacket> clazz1,
					Class<? extends AbstractMinimapsyncPacket> clazz2) {
				int com = String.CASE_INSENSITIVE_ORDER.compare(clazz1.getCanonicalName(), clazz2.getCanonicalName());
				if (com == 0) {
					com = clazz1.getCanonicalName().compareTo(clazz2.getCanonicalName());
				}
				return com;
			}
			
		});
	}
	
	@SideOnly(Side.CLIENT)
	private EntityPlayer getClientPlayer () {
		return Minecraft.getMinecraft().thePlayer;
	}
	
	public void sendToAll (AbstractMinimapsyncPacket message) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		channels.get(Side.SERVER).writeAndFlush(message);
	}
	
	public void sendTo (AbstractMinimapsyncPacket message, EntityPlayerMP player) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		channels.get(Side.SERVER).writeAndFlush(message);
	}
	
	public void sendToServer (AbstractMinimapsyncPacket message) {
		channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		channels.get(Side.CLIENT).writeAndFlush(message);
	}

}
