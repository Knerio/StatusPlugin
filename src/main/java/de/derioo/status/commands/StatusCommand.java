package de.derioo.status.commands;

import de.derioo.status.Main;
import de.derioo.status.config.Config;
import de.derioo.status.config.data.Playerdata;
import de.derioo.status.config.data.Status;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StatusCommand extends Command {


    private final Main plugin;
    private final Config config;
    private final Playerdata data;

    private final Map<Status, List<UUID>> invites = new HashMap<>();

    public StatusCommand(Main plugin) {
        super("status");
        this.plugin = plugin;
        this.config = plugin.getMessageConfiguration();
        this.data = plugin.getPlayerdata();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        switch (args.length) {
            case 1 -> {
                switch (args[0].toLowerCase()) {
                    case "reload" -> {
                        if (config.hasPermission(sender, "admin-permission")) {
                            config.reload();
                            sender.sendMessage(config.getComponent("status.reload.reloaded"));
                        } else {
                            sender.sendMessage(config.getComponent("no-permission"));
                        }
                    }
                    case "delete" -> {
                        if (this.data.getStatusByOwner(player) == null) {
                            sender.sendMessage(config.getComponent("no-permission"));
                            return false;
                        }
                        this.data.getStatuses().remove(this.data.getStatusByOwner(player));
                        this.data.reloadStatus();
                        this.data.save();
                        sender.sendMessage(config.getComponent("status.delete.deleted"));
                    }
                    case "members" -> {
                        Status status = this.data.getStatusByOwner(player);
                        if (status == null) {
                            sender.sendMessage(config.getComponent("no-permission"));
                            return false;
                        }
                        sender.sendMessage(config.getComponent("status.members.header"));
                        status.getMembers().forEach(member -> {
                            Player target = plugin.getServer().getPlayer(UUID.fromString(member));
                            if (target == null) return;
                            sender.sendMessage(config.getComponent("status.members.format", target.getName()));
                        });
                    }
                    case "leave" -> {
                        Status status = this.data.getStatusbyMember(player);
                        if (status == null) {
                            sender.sendMessage(config.getComponent("status.leave.not-in-status"));
                            return false;
                        }
                        if (status.getOwner().equals(player.getUniqueId().toString())) {
                            sender.sendMessage(config.getComponent("status.leave.is-owner"));
                            return false;
                        }
                        status.getMembers().remove(player.getUniqueId().toString());
                        this.data.reloadStatus();
                        this.data.save();
                        sender.sendMessage(config.getComponent("status.leave.left"));
                    }
                    default -> sendHelp(sender);
                }
            }
            default -> {
                switch (args[0].toLowerCase()) {
                    case "create" -> {
                        StringBuilder name = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            name.append(args[i]);
                        }
                        if (this.data.getStatusbyMember(player) != null) {
                            sender.sendMessage(config.getComponent("status.create.already-in-status"));
                            return false;
                        }
                        this.data.getStatuses().add(Status.builder().name(name.toString())
                                .members(new ArrayList<>(List.of(player.getUniqueId().toString())))
                                .owner(player.getUniqueId().toString())
                                .build());
                        this.data.setStatus(player, name.toString());
                        this.data.save();
                        sender.sendMessage(config.getComponent("status.create.created"));
                    }
                }
            }
            case 2 -> {
                switch (args[0].toLowerCase()) {
                    case "clear" -> {
                        Player target = plugin.getServer().getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage(config.getComponent("status.clear.not-online"));
                            return false;
                        }
                        if (!config.hasPermission(sender, "admin-permission")) {
                            sender.sendMessage(config.getComponent("no-permission"));
                            return false;
                        }
                        for (Status dataStatus : this.data.getStatuses()) {
                            if (dataStatus.getMembers().contains(target.getUniqueId().toString())) {
                                this.data.getStatuses().remove(dataStatus);
                                break;
                            }
                        }
                        this.data.reloadStatus();
                        this.data.save();
                        sender.sendMessage(config.getComponent("status.clear.cleared"));
                    }
                    case "kick" -> {
                        Player target = plugin.getServer().getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage(config.getComponent("status.kick.not-online"));
                            return false;
                        }
                        Status status = this.data.getStatusByOwner(target);
                        if (status == null && !player.hasPermission(config.getConfig().getString("admin-permission"))) {
                            sender.sendMessage(config.getComponent("status.kick.not-in-status"));
                            return false;
                        }
                        for (Status dataStatus : this.data.getStatuses()) {
                            if (dataStatus.getOwner().equals(target.getUniqueId().toString())) {
                                sender.sendMessage(config.getComponent("status.kick.is-owner"));
                                return false;
                            }
                            dataStatus.getMembers().remove(target.getUniqueId().toString());
                        }
                        this.data.save();
                        this.data.reloadStatus();
                        sender.sendMessage(config.getComponent("status.kick.kicked", target.getName()));
                    }
                    case "invite" -> {
                        Player target = plugin.getServer().getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage(config.getComponent("status.invite.not-online"));
                            return false;
                        }
                        Status status = this.data.getStatusByOwner(player);
                        if (status == null) {
                            sender.sendMessage(config.getComponent("status.invite.not-in-status"));
                            return false;
                        }
                        if (status.getMembers().contains(target.getUniqueId().toString())) {
                            sender.sendMessage(config.getComponent("status.invite.already-member"));
                            return false;
                        }
                        if (invites.containsKey(status)) {
                            if (invites.get(status).contains(target.getUniqueId())) {
                                sender.sendMessage(config.getComponent("status.invite.already-invited"));
                                return false;
                            }
                        }
                        invites.putIfAbsent(status, new ArrayList<>());
                        invites.get(status).add(target.getUniqueId());
                        target.sendMessage(config.getComponent("status.invite.invited", status.getName(), player.getName(), player.getUniqueId().toString()));
                        sender.sendMessage(config.getComponent("status.invite.invited-sender", target.getName()));
                        this.data.save();
                    }
                    case "accept" -> {
                        args[1] = args[1].replace("\"", "");
                        try {
                            UUID.fromString(args[1]);
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(config.getComponent("status.accept.invalid-invite"));
                            return false;
                        }
                        Status status = this.data.getStatusByOwner(UUID.fromString(args[1]));
                        if (status == null) {
                            sender.sendMessage(config.getComponent("status.accept.invalid-invite"));
                            return false;
                        }
                        if (this.data.getStatusByOwner(UUID.fromString(args[1])).getMembers().contains(player.getUniqueId().toString())) {
                            sender.sendMessage(config.getComponent("status.accept.already-member"));
                            return false;
                        }
                        status.getMembers().add(player.getUniqueId().toString());
                        this.data.reloadStatus();
                        this.data.save();
                        sender.sendMessage(config.getComponent("status.accept.accepted", status.getName()));
                    }

                    default -> sendHelp(sender);

                }
            }

        }
        return false;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(config.getComponent("status.format"));
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        List<String> completions = new ArrayList<>();

        switch (args.length) {
            case 1:
                completions.add("create");
                completions.add("kick");
                completions.add("invite");
                completions.add("members");
                if (config.hasPermission(sender, "admin-permission")) {
                    completions.add("reload");
                    completions.add("delete");
                    completions.add("clear");
                }
                break;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "create":
                        completions.add("<name>");
                        break;
                    case "delete":
                        completions.addAll(this.data.getStatusNamesList());
                        break;
                    case "clear":
                    case "kick":
                    case "invite":
                        plugin.getServer().getOnlinePlayers().forEach(player -> completions.add(player.getName()));
                        break;
                }
                break;
        }

        return completions.stream().filter(completion -> completion.startsWith(args[args.length - 1])).toList();
    }
}
