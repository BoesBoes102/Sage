package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class UuidCommand {

    private final Sage plugin;

    public UuidCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("uuid [player]")
    @Permission("sage.uuid")
    public void onCommand(CommandSender sender, @Argument(value = "player", suggestions = "players") String targetName) {

        Player target;

        if (targetName != null) {
            target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player when using this from console!");
                return;
            }
            target = (Player) sender;
        }

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
