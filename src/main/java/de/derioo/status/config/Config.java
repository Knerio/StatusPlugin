package de.derioo.status.config;

import de.derioo.status.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private final YamlConfiguration config;

    public Config(Main plugin, String fileName) throws IOException {
        File dir = new File("plugins/Status");
        File file = new File(dir, fileName + ".yml");
        dir.mkdirs();
        if (file.createNewFile()) {
            plugin.getResource(fileName + ".yml").transferTo(new FileOutputStream(file));
        }

        this.config = YamlConfiguration.loadConfiguration(new File("plugins/Status", fileName + ".yml"));
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public boolean hasPermission(CommandSender player, String permissionPath) {
        if (!config.contains(permissionPath)) {
            return false;
        }

        return player.hasPermission(config.getString("admin-permission"));
    }

    public String getPrefix() {
        if (!config.contains("prefix")) {
            return "<white>[<green>Status<white>] <gray>";
        }
        return config.getString("prefix");
    }

    public Component getComponent(String path, String... args) {
        List<TagResolver> resolvers = new ArrayList<>();
        resolvers.add(Placeholder.parsed("prefix", getPrefix()));

        for (int i = 0; i < args.length; i++) {
            resolvers.add(Placeholder.parsed(String.valueOf(i + 1), args[i]));
        }

        if (!config.contains(path)) {
            return MiniMessage.miniMessage().deserialize("<prefix><red>Diese Nachricht wurde nicht gefunden!", resolvers.toArray(new TagResolver[0]));
        }
        return MiniMessage.miniMessage().deserialize(config.getString(path), resolvers.toArray(new TagResolver[0]));
    }


    public void reload() {
        try {
            config.load(new File("plugins/Status", "config.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
