package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("uuid")
@Description("Get a player's UUID")
@CommandPermission("sage.uuid")
public class UuidCommand extends BaseCommand {

    private final Sage plugin;

    public UuidCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @CommandCompletion("@players")
    public void onCommand(CommandSender sender, String[] args) {

        Player target = null;

        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
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