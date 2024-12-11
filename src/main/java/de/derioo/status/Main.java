package de.derioo.status;

import de.derioo.status.commands.StatusCommand;
import de.derioo.status.config.Config;
import de.derioo.status.config.data.Playerdata;
import de.derioo.status.listener.ConnectionListener;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class Main extends JavaPlugin {

    private Config config;
    private Playerdata playerdata;

    @Override
    public void onEnable() {
        try {
            this.config = new Config(this, "config");

            this.playerdata = Playerdata.load(this);
            this.playerdata.save();
            this.playerdata.reloadStatus();
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Could not load config, disabling plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        StatusCommand command = new StatusCommand(this);
        Bukkit.getCommandMap().register("status", command);
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
    }

    @Override
    public void onDisable() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getPersistentDataContainer().has(new NamespacedKey("status", "custom"))) entity.remove();
            }
        }
        this.playerdata.save();
    }


    public @NotNull Config getMessageConfiguration() {
        return config;
    }

    public Playerdata getPlayerdata() {
        return playerdata;
    }
}
