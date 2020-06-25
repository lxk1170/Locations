package org.Locations;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.lang.Math;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.World;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/**
 * @author leee leee
 */
public class Teleporter {

    private BukkitScheduler scheduler;
    private Map<String, Location> locations;
    private Map<String, Command> shortcuts;
    private Map<String, WorldLocations> lastLocs;
    private Plugin plugin;


    public Teleporter(Plugin plugin) {
        this.plugin = plugin;
        scheduler = plugin.getServer().getScheduler();

        locations = Util.loadShortcuts(plugin);
        shortcuts = Util.createShortcuts(plugin, locations);
        lastLocs = Util.loadWorlds(plugin);

        // load worlds
        for (World world : plugin.getServer().getWorlds()) {
            String worldName = world.getName();
            if (!lastLocs.containsKey(worldName)) {
                lastLocs.put(worldName, new WorldLocations());
            }
        }
    }


    // ACTIONS
    //
    
    public void teleport(Player player, String name) {
        World world = player.getWorld();

        if (lastLocs.containsKey(world)) {
            WorldLocations worldLocs = lastLocs.get(world);

            if (worldLocs != null) {

                // save world location
                if (worldLocs.remember){
                    worldLocs.save(player);
                }

                // if moving don't teleport
                Vector vel = player.getVelocity();
                if (vel.getX() != 0 || vel.getZ() != 0 || Math.abs(vel.getY()) >= 0.1) {
                    player.sendMessage("You're moving to fast, can't teleport");
                    return;
                }

                // teleport after delay
                int delay = worldLocs.delay;
                long tickDelay = delay * 20l;
                if (delay > 0) {
                    player.sendMessage("Stand still for " + ChatColor.AQUA + delay + ChatColor.RESET + " seconds");
                }

                // teleport after delay
                Location before = player.getLocation().getBlock().getLocation().clone();
                scheduler.runTaskLater(plugin, () -> {
                    // check if moved
                    if (tickDelay == 0 || before.equals(player.getLocation().getBlock().getLocation())) {
                        try {
                            teleportNow(player, name);
                        } catch (Exception e) {
                            player.sendMessage(ChatColor.RED + "failed to teleport, tell an admin")
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "you moved! failed tp");
                    }
                }, tickDelay);
            }
        }
    }

    private void teleportNow(Player player, String locationName) throws Exception {
        // determine destination
        Location destiLoc = locations.get(locationName);
        String destiWorldName = destiLoc.getWorld().getName();
        WorldLocations destiWorldLocs = getWL(destiWorldName);
        Location prevLoc = destiWorldLocs.getLastLoc(player, destiWorldName);

        // if previous location exists, and world set to remember
        if (prevLoc != null && destiWorldLocs.remember) {
            if (player.getWorld().getName().equals(destiWorldName)) {
                player.sendMessage(ChatColor.RED + "You're already in that world silly");
            } else {
                player.sendMessage(ChatColor.AQUA + "Returning to " + ChatColor.GREEN + destiWorldName);
                player.teleport(prevLoc);
            }
        }

        // otherwise, use locations
        else {
            player.sendMessage(ChatColor.AQUA + "Teleporting to " + ChatColor.GOLD + locationName);
            player.teleport(destiLoc);
        }
    }

    // assumes player is already in the desired world
    public void back(Player player) {
        String destiWorldName = player.getWorld().getName();
        WorldLocations worldLocs = lastLocs.get(destiWorldName);
        if (worldLocs != null) {
            worldLocs.teleport(player, destiWorldName);
        }
    }

    public void set(String name, Location location) throws Exception {
        Command cmd = Util.registerTeleport(plugin, name);
        shortcuts.put(name, cmd);
        locations.put(name, location);
    }

    public void delete(String name) throws Exception {
        Command cmd = shortcuts.remove(name);
        Util.unregisterTeleport(plugin, cmd);
        locations.remove(name);
    }

    /**
     * toggle wether to remember the players location in that world
     */
    public void remember(String name, boolean remember) throws Exception {
        getWL(name).remember = remember;
    }
    
    public boolean remember(String name) throws Exception {
        return getWL(name).remember;
    }
    
    public void delay(String name, int delay) throws Exception {
        getWL(name).delay = delay;
    }

    public int delay(String name) throws Exception {
        return getWL(name).delay;
    }

    public void save() {
        Util.saveShortcuts(plugin, locations);
        Util.saveWorlds(plugin, lastLocs);
    }

    // UTIL
    //
    
    public Set<String> getNames() {
        return locations.keySet();
    }

    private WorldLocations getWL(String name) throws Exception {
        // get locations world name
        if (locations.containsKey(name)) {
            name = locations.get(name).getWorld().getName();
        }

        // return the location if exists
        if (lastLocs.containsKey(name)) {
            return lastLocs.get(name);
        }

        // create if its a non existant real world
        if (plugin.getServer().getWorlds().contains(name)) {
            WorldLocations worldLocs = new WorldLocations();
            lastLocs.put(name, worldLocs);
            return worldLocs;
        }

        throw new Exception("Location not defined");
    }

    public String toString() {
        return (locations.size() > 0) ? getNames().toString() : "None exist yet";
    }

}
