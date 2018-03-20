package james94jeans2.minimapsync.server.command;

import james94jeans2.minimapsync.Minimapsync;
import james94jeans2.minimapsync.server.MinimapsyncServerConfiguration;
import james94jeans2.minimapsync.server.ServerWaypointManager;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * 
 * @version b0.9
 * @author james94jeans2 (Jens Leicht)
 * 
 */

public class CommandSettings extends CommandBase {

	 @Override
    public List getCommandAliases()
    {
        ArrayList list;
        list = new ArrayList();
        list.add("mmsc");
        return list;
    }
	
	@Override
	public String getCommandName() {
		return "minimapsyncconfig";
	}
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/minimapsyncconfig or /mmsc <setting> [value]\n"
        		+ "changeable settings:\n"
        		+ "    - dynmap (true/false)\n"
        		+ "    - autosync (true/false)\n"
        		+ "if no value is entered the current setting will be displayed";
    }
    
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
    
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if(FMLCommonHandler.instance().getSide().isServer())
        {
        	if (args.length == 1) {
        		return getListOfStringsMatchingLastWord(args, new String[]{"dynmap", "autosync"});
        	}else {
        		if (args.length == 2) {
        			return getListOfStringsMatchingLastWord(args, new String[]{"true", "false"});
        		}
        	}
        }
        return new ArrayList();
    }

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws WrongUsageException {
		if(args.length == 0 || args.length > 2)
        {
            throw new WrongUsageException(getCommandUsage(sender));
        }else
        {
        	if (args.length == 1) {
        		if (args[0].equalsIgnoreCase("dynmap")) {
        			func_152373_a(sender, this, "minimapsync dynmap setting is currently %s", new Object[] {((ServerWaypointManager) ServerWaypointManager.getInstance()).getDynmapEnabled()});
        			return;
        		}
        		if (args[0].equalsIgnoreCase("autosync")) {
        			func_152373_a(sender, this, "minimapsync autosync setting is currently %s", new Object[] {((ServerWaypointManager) ServerWaypointManager.getInstance()).getAutoSyncEnabled()});
        			return;
        		}
        		throw new WrongUsageException(getCommandUsage(sender));
        	} else {
        		if (args[0].equalsIgnoreCase("dynmap")) {
        			boolean aim;
        			if (args[1].equalsIgnoreCase("true")) {
        				aim = true;
        			} else if (args[1].equalsIgnoreCase("false")) {
        				aim = false;
        			} else {
        				throw new WrongUsageException(getCommandUsage(sender));
        			}
        			if (((ServerWaypointManager) ServerWaypointManager.getInstance()).getDynmapEnabled() == aim) {
        				func_152373_a(sender, this, "minimapsync dynmap setting is allready set to %s", new Object[] {args[1]});
        				return;
        			}
        			MinimapsyncServerConfiguration config = (MinimapsyncServerConfiguration)Minimapsync.instance.getConfiguration();
        			config.setDynmap(aim);
        			((ServerWaypointManager) ServerWaypointManager.getInstance()).setDynmapEnabled(aim);
        			func_152373_a(sender, this, "successfully changed minimapsync dynmap setting to %s", new Object[] {args[1]});
        			return;
        		}
        		if (args[0].equalsIgnoreCase("autosync")) {
        			boolean aim;
        			if (args[1].equalsIgnoreCase("true")) {
        				aim = true;
        			} else if (args[1].equalsIgnoreCase("false")) {
        				aim = false;
        			} else {
        				throw new WrongUsageException(getCommandUsage(sender));
        			}
        			if (((ServerWaypointManager) ServerWaypointManager.getInstance()).getAutoSyncEnabled() == aim) {
        				func_152373_a(sender, this, "minimapsync autosync setting is allready  set to %s", new Object[] {args[1]});
        				return;
        			}
        			MinimapsyncServerConfiguration config = (MinimapsyncServerConfiguration)Minimapsync.instance.getConfiguration();
        			config.setAutosync(aim);
        			((ServerWaypointManager) ServerWaypointManager.getInstance()).setAutoSync(aim);
        			func_152373_a(sender, this, "successfully changed minimapsync autosync setting to %s", new Object[] {args[1]});
        			return;
        		}
        	}
        	
        }
	}

}
