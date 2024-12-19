package de.skyking_px.record;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class Record extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Loading Plugin...");
        getLogger().info("By SkyKing_PX | Version: 1.21-1.1.0");
        this.getCommand("rec").setExecutor(this);
        this.getCommand("rec").setTabCompleter(this);
        this.getCommand("live").setExecutor(this);
        this.getCommand("live").setTabCompleter(this);

        if (getDataFolder().mkdirs())
            getLogger().info("Created plugin config directory");
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                getLogger().info("Creating config file");
                this.getConfig().options().copyDefaults(true);
                Files.copy(Objects.requireNonNull(getResource("config.yml")), configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        else {
//            @Nullable String[] valueCheck = {String.valueOf(getConfig().getBoolean("reset-on-shutdown")), String.valueOf(getConfig().getBoolean("delete-groups-on-shutdown")),
//                    getConfig().getString("rec-prefix"), getConfig().getString("live-prefix"), String.valueOf(getConfig().getInt("rec-prefix-weight")),
//                    String.valueOf(getConfig().getInt("live-prefix-weight")), getConfig().getString("messages.console.invalid-config"),
//                    getConfig().getString("messages.console.rec-group-already-exists"),
//                    getConfig().getString("messages.console.live-group-already-exists"), getConfig().getString("messages.console.rec-group-created"),
//                    getConfig().getString("messages.console.live-group-created"), getConfig().getString("messages.console.error-creating-group-rec"),
//                    getConfig().getString("messages.console.error-creating-group-live"), getConfig().getString("messages.console.rec-group-added"),
//                    getConfig().getString("messages.player.rec-group-removed"), getConfig().getString("messages.player.live-group-added"),
//                    getConfig().getString("messages.player.live-group-removed"), getConfig().getString("messages.player.cant-run-as-op"),
//                    getConfig().getString("messages.player.insufficient-perms"), getConfig().getString("messages.player.please-run-as-player"),
//                    getConfig().getString("messages.console.added-status-1"), getConfig().getString("messages.console.added-status-2"),
//                    getConfig().getString("messages.console.removed-status-1"), getConfig().getString("messages.console.removed-status-2")};
//
//            for (String value : valueCheck) {
//                if (value == null || value.isEmpty()) {
//                    try {
//                        getLogger().severe(getConfig().getString("messages.console.invalid-config"));
//                        Bukkit.getPluginManager().disablePlugin(this);
//                        break;
//                    } catch (NullPointerException e) {
//                        getLogger().severe("Invalid configuration file! Did you update it? Please check your config.yml!");
//                        e.printStackTrace();
//                        Bukkit.getPluginManager().disablePlugin(this);
//                        break;
//                    }
//
//                }
//            }
//        }

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms luckPerms = provider.getProvider();
        } else {
            getLogger().severe("LuckPerms API not found, disabling!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        LuckPerms luckPerms = provider.getProvider();

        // Create Group 'rec'
        if (luckPerms.getGroupManager().getGroup("rec") != null) {
            getLogger().info(getConfig().getString("messages.console.rec-group-already-exists"));
        } else {
            @NonNull CompletableFuture<Group> groupFuture = luckPerms.getGroupManager().createAndLoadGroup("rec");
            groupFuture.thenAccept(group -> {
                PrefixNode prefixNode = PrefixNode.builder(getConfig().getString("rec-prefix"), getConfig().getInt("rec-prefix-weight")).build();
                PermissionNode permissionNode = PermissionNode.builder("group.rec").value(true).build();
                group.data().add(prefixNode);
                group.data().add(permissionNode);
                luckPerms.getGroupManager().saveGroup(group).thenRun(() ->
                        getLogger().info(getConfig().getString("messages.console.rec-group-created"))
                );
            }).exceptionally(throwable -> {
                getLogger().severe(getConfig().getString("messages.console.error-creating-group-rec") + throwable.getMessage());
                return null;
            });
        }

        // Create Group 'Live'
        if (luckPerms.getGroupManager().getGroup("live") != null) {
            getLogger().info(getConfig().getString("messages.console.live-group-already-exists"));
        } else {
            @NonNull CompletableFuture<Group> groupFuture = luckPerms.getGroupManager().createAndLoadGroup("live");
            groupFuture.thenAccept(group -> {
                PrefixNode prefixNode = PrefixNode.builder(getConfig().getString("live-prefix"), getConfig().getInt("live-prefix-weight")).build();
                PermissionNode permissionNode = PermissionNode.builder("group.live").value(true).build();
                group.data().add(prefixNode);
                group.data().add(permissionNode);
                luckPerms.getGroupManager().saveGroup(group).thenRun(() ->
                        getLogger().info(getConfig().getString("messages.console.live-group-created"))
                );
            }).exceptionally(throwable -> {
                getLogger().severe(getConfig().getString("messages.console.error-creating-group-live") + throwable.getMessage());
                return null;
            });
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabling Plugin...");
        if (getConfig().getBoolean("reset-on-shutdown")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
                LuckPerms luckPerms = Objects.requireNonNull(provider).getProvider();
                User user = luckPerms.getUserManager().getUser(p.getUniqueId());
                if (p.hasPermission("group.rec")) {
                    removeGroup("rec", user, luckPerms, p);
                } else if (p.hasPermission("group.live")) {
                    removeGroup("live", user, luckPerms, p);
                }
            }
//            for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
//                RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
//                LuckPerms luckPerms = Objects.requireNonNull(provider).getProvider();
//                User user = luckPerms.getUserManager().getUser(p.getUniqueId());
//                if (Objects.requireNonNull(p.getPlayer()).hasPermission("group.rec")) {
//                    removeGroupOffline("rec", user, luckPerms, p);
//                } else if (Objects.requireNonNull(p.getPlayer()).hasPermission("group.live")) {
//                    removeGroupOffline("live", user, luckPerms, p);
//                }
//            }
            getLogger().info(getConfig().getString("messages.console.reset-players"));

            if (getConfig().getBoolean("delete-groups-on-shutdown")) {
                RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
                LuckPerms luckPerms = provider.getProvider();
                Group recGroup = luckPerms.getGroupManager().getGroup("rec");
                Group liveGroup = luckPerms.getGroupManager().getGroup("live");
                if (recGroup != null) {
                    luckPerms.getGroupManager().deleteGroup(recGroup);
                    getLogger().info(getConfig().getString("messages.console.deleted-group-rec"));
                }
                if (liveGroup != null) {
                    luckPerms.getGroupManager().deleteGroup(liveGroup);
                    getLogger().info(getConfig().getString("messages.console.deleted-group-live"));
                }
            }

        }
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
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.rec-group-added")));
                    } else if (p.hasPermission("group.live")) {
                        removeGroup("live", user, luckPerms, p);
                        addGroup("rec", user, luckPerms, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.rec-group-added")));
                    } else {
                        removeGroup("rec", user, luckPerms, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.rec-group-removed")));
                    }
                } else p.sendMessage(ChatColor.translateAlternateColorCodes('&', "messages.player.cant-run-as-op"));
            } else if (sender.hasPermission("rec.live") && (label.equalsIgnoreCase("l") || label.equalsIgnoreCase("live"))) {
                if (!sender.isOp()) {
                    if (!p.hasPermission("group.live") && !p.hasPermission("group.rec")) {
                        addGroup("live", user, luckPerms, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.live-group-added")));
                    } else if (p.hasPermission("group.rec")) {
                        removeGroup("rec", user, luckPerms, p);
                        addGroup("live", user, luckPerms, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.live-group-added")));
                    } else {
                        removeGroup("live", user, luckPerms, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.live-group-removed")));
                    }
                } else p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.cant-run-as-op")));
            } else sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.insufficient-perms")));
        } else sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.please-run-as-player")));
        return true;
    }

    public void addGroup(String groupName, User user, LuckPerms luckPerms, Player p) {
        user.data().add(Node.builder("group." + groupName).build());
        luckPerms.getUserManager().saveUser(user).thenRun(() -> {
            getLogger().info(getConfig().getString("messages.console.added-status-1") + groupName + getConfig().getString("messages.console.added-status-2") + p.getName());
        });
    }

    public void removeGroup(String groupName, User user, LuckPerms luckPerms, Player p) {
        user.data().remove(Node.builder("group." + groupName).build());
        luckPerms.getUserManager().saveUser(user).thenRun(() -> {
            getLogger().info(getConfig().getString("messages.console.removed-status-1") + groupName + getConfig().getString("messages.console.removed-status-2") + p.getName());
        });
    }

//    public void removeGroupOffline(String groupName, User user, LuckPerms luckPerms, OfflinePlayer p) {
//        user.data().remove(Node.builder("group." + groupName).build());
//        luckPerms.getUserManager().saveUser(user).thenRun(() -> {
//            getLogger().info(getConfig().getString("messages.console.removed-status-1") + groupName + getConfig().getString("messages.console.removed-status-2") + p.getName());
//        });
//    }
}
