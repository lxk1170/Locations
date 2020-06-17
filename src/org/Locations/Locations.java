package org.Locations;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.Field;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author leee leee
 */
public class Locations extends JavaPlugin {

    Teleporter tele;

    @Override
    public void onEnable() {
        tele = new Teleporter();
        getLogger().info("starting");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("stopping");
    }

    private void help(Player player, String message) {
        player.sendMessage(ChatColor.GRAY + "[LOCS] " + ChatColor.RESET + message);
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] args) {;
        // is player
        if (cs instanceof Player) {
            Player player = (Player) cs;

            // admin commands
            if (player.isOp() && (string.equals("locs") || string.equals("locations"))) {
                locsHandler(player, new ArrayList(Arrays.asList(args)));
                return true;
            }
		}
		
        return false;
    }

    public void locsHandler(Player player, List<String> args) {
        if (args.size() > 1) {
            String cmd = args.remove(0).toLowerCase();
            String name = args.remove(0).toLowerCase();

            switch(cmd) {
                case "set":
                    tele.set(name, player.getLocation());
                    registerTeleport(name);
                    help(player, "Set spawn for " + ChatColor.GREEN + name);
                    return;
                case "delete":
                    tele.delete(name);
                    return;
                case "remember":
                    tele.remember(name);
                    return;
                case "delay":
                    int delay;
                    if (args.size() > 0) {
                        delay = Integer.parseInt(args.get(0));
                        tele.delay(name, delay);
                    } else {
                        delay = tele.delay(name);
                    }
                    help(player, "delay is set to " + ChatColor.GREEN + delay);
                    return;
                case "help":
                    break;
                default:
                    help(player, "unknown command: " + ChatColor.RED + cmd);
            }
        }

        // help
        help(player, "available commands:");
        help(player, "set [name]");
        help(player, "delete [name]");
        help(player, "remember [name]");
        help(player, "delay [name] [sec]");
    }

    /**
     * Register additional commands, used by shortcut teleports
     */
    public void registerTeleport(String command) {
        Field bukkitCmdMap = getServer().getClass().getDeclaredField("commandMap");
        bukkitCmdMap.setAccessible(true);
        CommandMap cmdMap = (CommandMap) bukkitCmdMap.get(getServer());
        cmdMap.register(command, new TeleportCommand(command, tele));
    }
}
