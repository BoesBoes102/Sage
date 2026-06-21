package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

@CommandAlias("rules")
@Description("View server rules")
@CommandPermission("sage.rules")
public class RulesCommand extends BaseCommand {

    private final Sage plugin;

    public RulesCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("")
    public void onCommand(Player player) {
        List<String> rules = plugin.getRulesConfig().getStringList("rules.content");

        if (rules.isEmpty()) {
            player.sendMessage("\u00A7cNo rules configured!");
            return;
        }

        player.sendMessage("");
        String title = plugin.getRulesConfig().getString("rules.title", "\u00A7e\u00A7lSERVER RULES");
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
