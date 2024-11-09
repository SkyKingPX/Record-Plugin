package de.skyking_px.record;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Record extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("[Record] Loading Plugin...");
        getLogger().info("[Record] By SkyKing_PX | Version: 1.21.1-1.0.0");
        this.getCommand("rec").setExecutor(this);
        this.getCommand("rec").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("[Record] Disabling Plugin...");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("rec.rec")){
            if (sender instanceof Player){
                Player p = (Player) sender;
                if (p.getDisplayName().contains("Recording")){
                    p.setDisplayName(p.getName());
                } else p.setDisplayName((Color.PURPLE + "[") + (Color.RED + "Recording") + (Color.PURPLE + "]") + " " + p.getName());

            } else sender.sendMessage(ChatColor.RED + "Bitte führe diesen Befehl als Spieler aus!");
        } else sender.sendMessage(ChatColor.RED + "Du hast nicht die Berechtigung diesen Befehl auszuführen!");
        return true;
    }
}
