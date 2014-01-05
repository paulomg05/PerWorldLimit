/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jabyftw.pwl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Rafael
 */
public class PerWorldLimit extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private Map<World, Integer> limit = new HashMap();
    private World defaultW;

    @Override
    public void onEnable() {
        config = getConfig();
        config.addDefault("worlds.world_nether.limit", 3);
        config.addDefault("defaultWorldWhenFull", "world");
        config.addDefault("lang.worldIsFull", "&cThe world is full, sorry.");
        config.addDefault("lang.worldIsFullTeleported", "&cThe world is full, sorry. &4You've been teleported");
        config.options().copyDefaults(true);
        saveConfig();
        reloadConfig();
        defaultW = getServer().getWorld(config.getString("defaultWorldWhenFull"));
        for (String key : config.getConfigurationSection("worlds").getKeys(false)) {
            limit.put(getServer().getWorld(key), config.getInt("worlds." + key + ".limit"));
        }
        getLogger().log(Level.INFO, "Enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Disabled.");
    }

    private String getLang(String path) {
        return config.getString("lang." + path).replaceAll("&", "§");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeleport(PlayerTeleportEvent e) {
        if (!e.getTo().getWorld().equals(e.getFrom().getWorld())) {
            World w = e.getTo().getWorld();
            if (limit.containsKey(w)) {
                if (w.getPlayers().size() >= limit.get(w)) {
                    e.getPlayer().sendMessage(getLang("worldIsFull"));
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        World w = e.getPlayer().getLocation().getWorld();
        if (limit.containsKey(w)) {
            if (w.getPlayers().size() >= limit.get(w)) {
                e.getPlayer().sendMessage(getLang("worldIsFullTeleported"));
                e.getPlayer().teleport(defaultW.getSpawnLocation());
            }
        }
    }
}
