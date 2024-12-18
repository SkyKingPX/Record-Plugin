package de.skyking_px.record;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
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

public final class Record extends JavaPlugin implements CommandExecutor {

    RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("[Record] Loading Plugin...\n[Record] By SkyKing_PX | Version: 1.21-1.0.0\n[Record] Please make sure to create LuckPerms Groups named 'rec' and 'live'!");
        this.getCommand("rec").setExecutor(this);
        this.getCommand("rec").setTabCompleter(this);
        this.getCommand("live").setExecutor(this);
        this.getCommand("live").setTabCompleter(this);

        if (provider != null) {
            LuckPerms luckPerms = provider.getProvider();
        } else {
            getLogger().severe("LuckPerms API not found, disabling!");
            Bukkit.getPluginManager().disablePlugin(this);
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("[Record] Disabling Plugin...");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            LuckPerms luckPerms = provider.getProvider();
            if (sender.hasPermission("lsrec.rec")) {

                if (p.hasPermission("group.rec")) {
                    luckPerms.getUserManager().modifyUser(p.getUniqueId(), user -> {
                        user.data().remove(Node.builder("group.rec").build());
                    });
                    getLogger().info("[Record] Added status '[Rec]' for " + p.getName());
                } else {
                    luckPerms.getUserManager().modifyUser(p.getUniqueId(), user -> {
                        user.data().add(Node.builder("group.rec").build());
                    });
                    getLogger().info("[Record] Removed status '[Rec]' for " + p.getName());
                }

            } else if (sender.hasPermission("lsrec.live")) {

                if (p.hasPermission("group.live")) {
                    luckPerms.getUserManager().modifyUser(p.getUniqueId(), user -> {
                        user.data().remove(Node.builder("group.live").build());
                    });
                    getLogger().info("[Record] Added status '[Live]' for " + p.getName());
                } else {
                    luckPerms.getUserManager().modifyUser(p.getUniqueId(), user -> {
                        user.data().add(Node.builder("group.live").build());
                    });
                    getLogger().info("[Record] Removed status '[Live]' for " + p.getName());
                }

            } else sender.sendMessage(ChatColor.RED + "Du hast nicht die Berechtigung diesen Befehl auszuführen!");
        } else sender.sendMessage(ChatColor.RED + "Bitte führe diesen Befehl als Spieler aus!");
        return true;
    }
}
