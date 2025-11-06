package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import com.boes.sage.Sage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("uuid")
@Description("Get a player's UUID")
public class UuidCommand extends BaseCommand {

    private final Sage plugin;

    public UuidCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @CommandCompletion("@players")
    @Conditions("permission:sage.uuid")
    public void onCommand(CommandSender sender, Player target) {
        String uuid = target.getUniqueId().toString();

        TextComponent message = new TextComponent("§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        TextComponent line1 = new TextComponent("\n§e" + target.getName() + "'s UUID:\n");
        TextComponent uuidComponent = new TextComponent("§b" + uuid);

        uuidComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid));
        uuidComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§aClick to copy UUID to clipboard!").create()));

        TextComponent line2 = new TextComponent("\n§7(Click to copy)");
        TextComponent footer = new TextComponent("\n§7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        message.addExtra(line1);
        message.addExtra(uuidComponent);
        message.addExtra(line2);
        message.addExtra(footer);

        sender.spigot().sendMessage(message);
    }
}