package de.skyking_px.record;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class Record extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Loading Plugin...");
        getLogger().info("By SkyKing_PX | Version: 1.21-1.0.0");
        this.getCommand("rec").setExecutor(this);
        this.getCommand("rec").setTabCompleter(this);
        this.getCommand("live").setExecutor(this);
        this.getCommand("live").setTabCompleter(this);

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms luckPerms = provider.getProvider();
        } else {
            getLogger().severe("LuckPerms API not found, disabling!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        LuckPerms luckPerms = provider.getProvider();
        PrefixNode rec = PrefixNode.builder("&5[&r&4Rec&r&5] &r", 100).build();
        PrefixNode live = PrefixNode.builder("&5[&r&bLive&r&5] &r", 100).build();

        // Create Group 'rec'
        if (luckPerms.getGroupManager().getGroup("rec") != null) {
            getLogger().info("Group 'rec' already exists");
        } else {
            @NonNull CompletableFuture<Group> groupFuture = luckPerms.getGroupManager().createAndLoadGroup("rec");
            groupFuture.thenAccept(group -> {
                PrefixNode prefixNode = PrefixNode.builder("&5[&r&4Rec&r&5] &r", 100).build();
                PermissionNode permissionNode = PermissionNode.builder("group.rec").value(true).build();
                group.data().add(prefixNode);
                group.data().add(permissionNode);
                luckPerms.getGroupManager().saveGroup(group).thenRun(() ->
                        getLogger().info("Created Group 'rec'")
                );
            }).exceptionally(throwable -> {
                getLogger().severe("Error while trying to create group 'rec': " + throwable.getMessage());
                return null;
            });
        }

        // Create Group 'Live'
        if (luckPerms.getGroupManager().getGroup("live") != null) {
            getLogger().info("Group 'live' already exists");
        } else {
            @NonNull CompletableFuture<Group> groupFuture = luckPerms.getGroupManager().createAndLoadGroup("live");
            groupFuture.thenAccept(group -> {
                PrefixNode prefixNode = PrefixNode.builder("&5[&r&bLive&r&5] &r", 100).build();
                PermissionNode permissionNode = PermissionNode.builder("group.live").value(true).build();
                group.data().add(prefixNode);
                group.data().add(permissionNode);
                luckPerms.getGroupManager().saveGroup(group).thenRun(() ->
                        getLogger().info("Created Group 'live'")
                );
            }).exceptionally(throwable -> {
                getLogger().severe("Error while trying to create group 'live': " + throwable.getMessage());
                return null;
            });
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabling Plugin...");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            LuckPerms luckPerms = provider.getProvider();
            User user = luckPerms.getUserManager().getUser(p.getUniqueId());

            if (sender.hasPermission("rec.rec") && (label.equalsIgnoreCase("r") || label.equalsIgnoreCase("rec"))) {
                if (!sender.isOp()) {
                    if (!p.hasPermission("group.rec") && !p.hasPermission("group.live")) {
                        addGroup("rec", user, luckPerms, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Added the status &5[&r&4Rec&r&5] &r&2to you."));
                    } else if (p.hasPermission("group.live")) {
                        removeGroup("live", user, luckPerms, p);
                        addGroup("rec", user, luckPerms, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Added the status &5[&r&4Rec&r&5] &r&2to you."));
                    } else {
                        removeGroup("rec", user, luckPerms, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Removed the status &5[&r&4Rec&r&5] &r&2from you."));
                    }
                } else p.sendMessage(ChatColor.RED + "You can't run this command as an Operator!");
            } else if (sender.hasPermission("rec.live") && (label.equalsIgnoreCase("l") || label.equalsIgnoreCase("live"))) {
                if (!sender.isOp()) {
                    if (!p.hasPermission("group.live") && !p.hasPermission("group.rec")) {
                        addGroup("live", user, luckPerms, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Added the status &5[&r&bLive&r&5] &r&2to you."));
                    } else if (p.hasPermission("group.rec")) {
                        removeGroup("rec", user, luckPerms, p);
                        addGroup("live", user, luckPerms, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Added the status &5[&r&bLive&r&5] &r&2to you."));
                    } else {
                        removeGroup("live", user, luckPerms, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Removed the status &5[&r&bLive&r&5] &r&2from you."));
                    }
                } else p.sendMessage(ChatColor.RED + "You can't run this command as an Operator!");
            } else sender.sendMessage(ChatColor.RED + "You have insufficient permissions to execute this command!");
        } else sender.sendMessage(ChatColor.RED + "Please execute this command as a player!");
        return true;
    }

    public void addGroup(String groupName, User user, LuckPerms luckPerms, Player p) {
        user.data().add(Node.builder("group." + groupName).build());
        luckPerms.getUserManager().saveUser(user).thenRun(() -> {
            getLogger().info("Added status '[" + groupName + "]' to " + p.getName());
        });
    }

    public void removeGroup(String groupName, User user, LuckPerms luckPerms, Player p) {
        user.data().remove(Node.builder("group." + groupName).build());
        luckPerms.getUserManager().saveUser(user).thenRun(() -> {
            getLogger().info("Removed status '[" + groupName + "]' from " + p.getName());
        });
    }
}
