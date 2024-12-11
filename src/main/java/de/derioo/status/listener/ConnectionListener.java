package de.derioo.status.listener;

import de.derioo.status.Main;
import de.derioo.status.config.data.Status;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    private final Main plugin;

    public ConnectionListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getPersistentDataContainer().has(new NamespacedKey("status", "custom"))) entity.remove();
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (Status status : plugin.getPlayerdata().getStatuses()) {
            if (!status.getMembers().contains(event.getPlayer().getUniqueId().toString())) continue;
            plugin.getPlayerdata().setStatus(event.getPlayer(), status.getName());
        }
    }

}
