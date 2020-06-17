package org.Locations;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.Command;

/**
 * @author leee leee
 */
public class TeleportCommand extends Command {

    Teleporter tele;
    
    public TeleportCommand(String name, Teleporter tele) {
        super(name);
        this.tele = tele;
    }

    @Override
    public execute(CommandSender cs, String string, String[] args) {
        if (cs instanceof Player) {
            Player player = (Player) cs;
            tele.teleport(player, name)
        }
    }

}
