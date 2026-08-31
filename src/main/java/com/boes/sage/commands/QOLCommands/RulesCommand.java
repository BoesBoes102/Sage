package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.List;

public class RulesCommand {

    private final Sage plugin;

    public RulesCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("rules")
    @Permission("sage.rules")
    public void onCommand(Player player) {
        List<String> rules = plugin.getRulesConfig().getStringList("rules.content");

        if (rules.isEmpty()) {
            player.sendMessage("§cNo rules configured!");
            return;
        }

        player.sendMessage("");
        String title = plugin.getRulesConfig().getString("rules.title", "§e§lSERVER RULES");
        title = ChatColor.translateAlternateColorCodes('&', title);
        player.sendMessage(title);
        player.sendMessage("");

        for (String rule : rules) {
            String processedRule = ChatColor.translateAlternateColorCodes('&', rule);
            player.sendMessage(processedRule);
        }

        player.sendMessage("");
    }
}
