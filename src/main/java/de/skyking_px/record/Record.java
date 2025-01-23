package de.skyking_px.record;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class Record extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Instant loadingStart = Instant.now();
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

//        Not updatet anymore as it is WIP
//        else {
//            @Nullable String[] valueCheck = {String.valueOf(config.getBoolean("reset-on-logout")), String.valueOf(config.getBoolean("delete-groups-on-shutdown")),
//                    config.getString("rec-prefix"), config.getString("live-prefix"), String.valueOf(config.getInt("rec-prefix-weight")),
//                    String.valueOf(config.getInt("live-prefix-weight")), config.getString("messages.console.invalid-config"),
//                    config.getString("messages.console.rec-group-already-exists"),
//                    config.getString("messages.console.live-group-already-exists"), config.getString("messages.console.rec-group-created"),
//                    config.getString("messages.console.live-group-created"), config.getString("messages.console.error-creating-group-rec"),
//                    config.getString("messages.console.error-creating-group-live"), config.getString("messages.console.rec-group-added"),
//                    config.getString("messages.player.rec-group-removed"), config.getString("messages.player.live-group-added"),
//                    config.getString("messages.player.live-group-removed"), config.getString("messages.player.cant-run-as-op"),
//                    config.getString("messages.player.insufficient-perms"), config.getString("messages.player.please-run-as-player"),
//                    config.getString("messages.console.added-status-1"), config.getString("messages.console.added-status-2"),
//                    config.getString("messages.console.removed-status-1"), config.getString("messages.console.removed-status-2")};
//
//            for (String value : valueCheck) {
//                if (value == null || value.isEmpty()) {
//                    try {
//                        getLogger().severe(config.getString("messages.console.invalid-config"));
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
            getLogger().info("LuckPerms API found! Version: " + luckPerms.getPluginMetadata().getVersion());
            Instant loadingEnd = Instant.now();
            Duration loadingPlugin = Duration.between(loadingStart, loadingEnd);
            getLogger().info("Loading took " + loadingPlugin.toMillis() + "ms.");
        } else {
            getLogger().severe("LuckPerms API not found, disabling!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        LuckPerms luckPerms = provider.getProvider();

        // Create Group 'rec'
        if (luckPerms.getGroupManager().getGroup("rec") != null) {
            try {
                getLogger().info(getConfig().getString("messages.console.rec-group-already-exists"));
            } catch (NullPointerException e) {
                getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.console.rec-group-already-exists'");
            }
            try {
                getLogger().info(getConfig().getString("messages.console.updating-prefix-rec"));
            } catch (NullPointerException e) {
                getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.console.updating-prefix-rec'");
            }

            Group group = luckPerms.getGroupManager().getGroup("rec");
            group.data().clear(NodeType.PREFIX::matches);
            String prefix = Objects.requireNonNull(getConfig().getString("rec-prefix"));
            PrefixNode prefixNode = PrefixNode.builder(prefix, getConfig().getInt("rec-prefix-weight")).build();
            group.data().add(prefixNode);
            luckPerms.getGroupManager().saveGroup(group).join();
        } else {
            @NonNull CompletableFuture<Group> groupFuture = luckPerms.getGroupManager().createAndLoadGroup("rec");
            groupFuture.thenAccept(group -> {
                String prefix = Objects.requireNonNull(getConfig().getString("rec-prefix"));
                PrefixNode prefixNode = PrefixNode.builder(prefix, getConfig().getInt("rec-prefix-weight")).build();
                PermissionNode permissionNode = PermissionNode.builder("group.rec").value(true).build();
                group.data().add(prefixNode);
                group.data().add(permissionNode);
                luckPerms.getGroupManager().saveGroup(group).thenRun(() -> {
                    try {
                        getLogger().info(getConfig().getString("messages.console.rec-group-created"));
                    } catch (NullPointerException e) {
                        getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.console.rec-group-created'");
                    }
                });
            }).exceptionally(throwable -> {
                try {
                    getLogger().severe(getConfig().getString("messages.console.error-creating-group-rec") + throwable.getMessage());
                } catch (NullPointerException e) {
                    getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.console.error-creating-group-rec'");
                }
                return null;
            });
        }

        // Create Group 'Live'
        if (luckPerms.getGroupManager().getGroup("live") != null) {
            try {
                getLogger().info(getConfig().getString("messages.console.live-group-already-exists"));
            } catch (NullPointerException e) {
                getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.console.live-group-already-exists'");
            }
            try {
                getLogger().info(getConfig().getString("messages.console.updating-prefix-live"));
            } catch (NullPointerException e) {
                getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.console.updating-prefix-live'");
            }

            Group group = luckPerms.getGroupManager().getGroup("live");
            group.data().clear(NodeType.PREFIX::matches);
            String prefix = Objects.requireNonNull(getConfig().getString("live-prefix"));
            PrefixNode prefixNode = PrefixNode.builder(prefix, getConfig().getInt("live-prefix-weight")).build();
            group.data().add(prefixNode);
            luckPerms.getGroupManager().saveGroup(group).join();
        } else {
            @NonNull CompletableFuture<Group> groupFuture = luckPerms.getGroupManager().createAndLoadGroup("live");
            groupFuture.thenAccept(group -> {
                String prefix = Objects.requireNonNull(getConfig().getString("live-prefix"));
                PrefixNode prefixNode = PrefixNode.builder(prefix, getConfig().getInt("live-prefix-weight")).build();
                PermissionNode permissionNode = PermissionNode.builder("group.live").value(true).build();
                group.data().add(prefixNode);
                group.data().add(permissionNode);
                luckPerms.getGroupManager().saveGroup(group).thenRun(() -> {
                    try {
                        getLogger().info(getConfig().getString("messages.console.live-group-created"));
                    } catch (NullPointerException e) {
                        getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.console.live-group-created'");
                    }
                });
            }).exceptionally(throwable -> {
                try {
                    getLogger().severe(getConfig().getString("messages.console.error-creating-group-live") + throwable.getMessage());
                } catch (NullPointerException e) {
                    getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.console.error-creating-group-live'");
                }
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
                        if (getConfig().getBoolean("enable-private-feedback")){
                            try {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.rec-group-added").replace("{prefix}", getConfig().getString("rec-prefix"))));
                            } catch (NullPointerException e) {
                                p.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                                getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.rec-group-added'");
                            }

                        }
                        if (getConfig().getBoolean("enable-public-broadcast")) {
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                if (onlinePlayer == p && getConfig().getBoolean("exclude-executor-from-broadcast")) {
                                    continue;
                                }
                                try {
                                    onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.broadcast-add-rec").replace("{player}", p.getName())));
                                } catch (NullPointerException e) {
                                    onlinePlayer.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                                    getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.broadcast-add-rec'");
                                }

                            }
                        }
                    } else if (p.hasPermission("group.live")) {
                        removeGroup("live", user, luckPerms, p);
                        addGroup("rec", user, luckPerms, p);
                        if (getConfig().getBoolean("enable-private-feedback")){
                            try {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.rec-group-added").replace("{prefix}", getConfig().getString("rec-prefix"))));
                            } catch (NullPointerException e) {
                                p.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                                getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.rec-group-added'");
                            }
                        }
                        if (getConfig().getBoolean("enable-public-broadcast")) {
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                if (onlinePlayer == p && getConfig().getBoolean("exclude-executor-from-broadcast")) {
                                    continue;
                                }
                                try {
                                    onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.broadcast-add-live").replace("{player}", p.getName())));
                                } catch (NullPointerException e) {
                                    onlinePlayer.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                                    getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.broadcast-add-live'");
                                }

                            }
                        }
                    } else {
                        removeGroup("rec", user, luckPerms, p);
                        if (getConfig().getBoolean("enable-private-feedback")){
                            try {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.rec-group-removed").replace("{prefix}", getConfig().getString("rec-prefix"))));
                            } catch (NullPointerException e) {
                                p.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                                getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.rec-group-removed'");
                            }
                        }
                        if (getConfig().getBoolean("enable-public-broadcast")) {
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                if (onlinePlayer == p && getConfig().getBoolean("exclude-executor-from-broadcast")) {
                                    continue;
                                }
                                try {
                                    onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.broadcast-remove-rec").replace("{player}", p.getName())));
                                } catch (NullPointerException e) {
                                    onlinePlayer.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                                    getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.broadcast-remove-rec'");
                                }
                            }
                        }
                    }
                } else {
                    try {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "messages.player.cannot-run-with-op"));
                    } catch (NullPointerException e) {
                        p.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                        getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.cannot-run-with-op'");
                    }

                }
            } else if (sender.hasPermission("rec.live") && (label.equalsIgnoreCase("l") || label.equalsIgnoreCase("live"))) {
                if (!sender.isOp()) {
                    if (!p.hasPermission("group.live") && !p.hasPermission("group.rec")) {
                        addGroup("live", user, luckPerms, p);
                        if (getConfig().getBoolean("enable-private-feedback")){
                            try {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.live-group-added").replace("{prefix}", getConfig().getString("live-prefix"))));
                            } catch (NullPointerException e) {
                                p.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                                getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.live-group-added'");
                            }
                        }
                        if (getConfig().getBoolean("enable-public-broadcast")) {
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                if (onlinePlayer == p && getConfig().getBoolean("exclude-executor-from-broadcast")) {
                                    continue;
                                }
                                try {
                                    onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.broadcast-add-live").replace("{player}", p.getName())));
                                } catch (NullPointerException e) {
                                    onlinePlayer.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                                    getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.broadcast-add-live'");
                                }
                            }
                        }
                    } else if (p.hasPermission("group.rec")) {
                        removeGroup("rec", user, luckPerms, p);
                        addGroup("live", user, luckPerms, p);
                        if (getConfig().getBoolean("enable-private-feedback")){
                            try {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.live-group-added").replace("{prefix}", getConfig().getString("live-prefix"))));
                            } catch (NullPointerException e) {
                                p.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                                getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.live-group-added'");
                            }
                        }
                        if (getConfig().getBoolean("enable-public-broadcast")) {
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                if (onlinePlayer == p && getConfig().getBoolean("exclude-executor-from-broadcast")) {
                                    continue;
                                }
                                try {
                                    onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.broadcast-add-rec").replace("{player}", p.getName())));
                                } catch (NullPointerException e) {
                                    onlinePlayer.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                                    getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.broadcast-add-rec'");
                                }
                            }
                        }
                    } else {
                        removeGroup("live", user, luckPerms, p);
                        if (getConfig().getBoolean("enable-private-feedback")){
                            try {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.live-group-removed").replace("{prefix}", getConfig().getString("live-prefix"))));
                            } catch (NullPointerException e) {
                                p.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                                getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.live-group-removed'");
                            }
                        }
                        if (getConfig().getBoolean("enable-public-broadcast")) {
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                if (onlinePlayer == p && getConfig().getBoolean("exclude-executor-from-broadcast")) {
                                    continue;
                                }
                                try {
                                    onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.broadcast-remove-live").replace("{player}", p.getName())));
                                } catch (NullPointerException e) {
                                    onlinePlayer.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                                    getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.broadcast-remove-live'");
                                }
                            }
                        }
                    }
                } else {
                    try {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.cannot-run-with-op")));
                    } catch (NullPointerException e) {
                        p.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                        getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.cannot-run-with-op'");
                    }
                }
            } else {
                try {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.insufficient-perms")));
                } catch (NullPointerException e) {
                    sender.sendMessage(ChatColor.RED + "A problem occurred while running this command. Please check the console for more information.");
                    getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.insufficient-perms'");
                }
            }
        } else {
            try {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player.please-run-as-player")));
            } catch (NullPointerException e) {
                sender.sendMessage("A problem occurred while running this command. Please check the error message below.");
                getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.player.please-run-as-player'");
            }
        }
        return true;
    }

    public void addGroup(String groupName, User user, LuckPerms luckPerms, Player p) {
        user.data().add(Node.builder("group." + groupName).build());
        luckPerms.getUserManager().saveUser(user).thenRun(() -> {
            try {
                getLogger().info(getConfig().getString("messages.console.added-group").replace("{group}", groupName).replace("{player}", p.getName()));
            } catch (NullPointerException e) {
                getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.console.added-group'");
            }
        });
    }

    public void removeGroup(String groupName, User user, LuckPerms luckPerms, Player p) {
        user.data().remove(Node.builder("group." + groupName).build());
        luckPerms.getUserManager().saveUser(user).thenRun(() -> {
            try {
                getLogger().info(getConfig().getString("messages.console.removed-group").replace("{group}", groupName).replace("{player}", p.getName()));
            } catch (NullPointerException e) {
                getLogger().severe("Invalid message in configuration file! Please check the value: 'messages.console.removed-group'");
            }
        });
    }

}
