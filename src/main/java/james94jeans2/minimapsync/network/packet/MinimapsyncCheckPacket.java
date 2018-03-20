package james94jeans2.minimapsync.network.packet;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.client.ClientWaypointManager;
import james94jeans2.minimapsync.client.MinimapKeyHandler;
import james94jeans2.minimapsync.server.ServerWaypointManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class MinimapsyncCheckPacket extends AbstractMinimapsyncPacket {

	private char checkType;
	
	public MinimapsyncCheckPacket () {
		
	}
	
	public MinimapsyncCheckPacket (char pCheckType) {
		checkType = pCheckType;
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeChar(checkType);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		checkType = buffer.readChar();
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		ClientWaypointManager manager = (ClientWaypointManager) ClientWaypointManager.getInstance();
        if (checkType == 'c') {
        	manager.sendCheckPacket(player);
        }else{
        	logger.info("Got checkpacket answer from server!");
        	if (checkType == 'L') {
        		manager.setLAN(true);
        		logger.info("LAN-packet received!");
        	}
        	if (checkType == 'C') {
        		manager.setMode(2);;
        		logger.info("CMD-packet received!");
        	}
        	if (checkType == 'R') {
        		manager.setMode(3);
        		logger.info("READ-packet received!");
        	}
        	if (checkType == 'S') {
        		manager.setMode(1);
        		logger.info("STANDARD-packet received!");
        	}
        	manager.setSyncOnServer(true);
        	MinimapKeyHandler.getInstance().enable();
    		if(!Minecraft.getMinecraft().isSingleplayer() && !Minimapsync.instance.getEnabled())
            {
                    chat("[Minimapsync]However you can still use the warptowaypoint command to teleport to the servers public waypoints!");
            }
        }
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
        if(checkType == 'c')
        {
        	ServerWaypointManager.getInstance().sendCheckPacket(player);
        	logger.info("Sending checkpacket answer to client!");
        }else {
        	logger.warn(checkType);
        }
	}

}
