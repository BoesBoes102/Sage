package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import com.boes.sage.Sage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

@CommandAlias("rules")
@Description("View server rules")
public class RulesCommand extends BaseCommand {

    private final Sage plugin;

    public RulesCommand(Sage plugin) {
        this.plugin = plugin;
    }

    public void onCommand(Player player) {
        List<String> rules = plugin.getConfig().getStringList("rules.content");

        if (rules.isEmpty()) {
            player.sendMessage("§cNo rules configured!");
            return;
        }

        player.sendMessage("");
        String title = plugin.getConfig().getString("rules.title", "§e§lSERVER RULES");
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