package com.boes.sage.features.notification.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import org.bukkit.entity.Player;

@CommandAlias("adminchat|ac")
@CommandPermission("sage.adminchat")
public class AdminChatCommand extends BaseCommand {

    private final Sage plugin;

    public AdminChatCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("<message>")
    public void onCommand(Player player, String[] messageArgs) {
        if (messageArgs.length == 0) {
            player.sendMessage("\u00A7cUsage: /adminchat <message>");
            return;
        }

        plugin.getNotificationService().sendAdminChat(player, String.join(" ", messageArgs));
    }
}
