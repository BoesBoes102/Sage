package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("customgradient|cg")
@Description("Create custom gradient messages")
@CommandPermission("sage.gradient")
public class CustomGradientCommand extends BaseCommand {

    private final Sage plugin;

    public CustomGradientCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Subcommand("message")
    @CommandCompletion("@nothing")
    public void onMessage(Player player, String message, String... hexCodes) {
        try {
            if (hexCodes.length == 0) {
                player.sendMessage("/customgradient message <message> <hex1> <hex2> [hex3]");
                return;
            }
            String gradientMessage = applyGradient(message, hexCodes);
            String prefix = "§f" + player.getName();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(prefix + gradientMessage);
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c" + e.getMessage());
            player.sendMessage("/customgradient message <message> <hex1> <hex2> [hex3]");
        }
    }

    @Subcommand("code")
    @CommandCompletion("@nothing")
    public void onCode(Player player, String message, String... hexCodes) {
        try {
            if (hexCodes.length == 0) {
                player.sendMessage("/customgradient code <message> <hex1> <hex2> [hex3]");
                return;
            }
            String gradientCode = applyGradient(message, hexCodes).replace("§", "&");
            
            TextComponent codeComponent = new TextComponent("§aGradient code: §b" + gradientCode);
            codeComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, gradientCode));
            codeComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("§aClick to copy code to clipboard!").create()));
            
            player.spigot().sendMessage(codeComponent);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c" + e.getMessage());
            player.sendMessage("/customgradient code <message> <hex1> <hex2> [hex3]");
        }
    }


    private String applyGradient(String message, String[] hexCodes) {
        if (hexCodes.length < 2) {
            throw new IllegalArgumentException("At least 2 hex codes required");
        }
        int[][] colors = new int[hexCodes.length][];
        for (int i = 0; i < hexCodes.length; i++) {
            colors[i] = parseHex(hexCodes[i]);
        }
        StringBuilder sb = new StringBuilder();
        int len = message.length();
        if (len == 0) return "";
        int segments = colors.length - 1;
        for (int i = 0; i < len; i++) {
            double pos = (double) i / (len - 1);
            int segment = (int) (pos * segments);
            if (segment >= segments) segment = segments - 1;
            double t = (pos * segments) - segment;
            int[] color = interpolate(colors[segment], colors[segment + 1], t);
            sb.append(String.format("§x§%X§%X§%X§%X§%X§%X%c",
                color[0] >> 4, color[0] & 0xF,
                color[1] >> 4, color[1] & 0xF,
                color[2] >> 4, color[2] & 0xF,
                message.charAt(i)));
        }
        return sb.toString();
    }

    private int[] parseHex(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() != 6) {
            throw new IllegalArgumentException("Invalid hex code: " + hex);
        }
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new int[]{r, g, b};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex code: " + hex);
        }
    }

    private int[] interpolate(int[] c1, int[] c2, double t) {
        int r = (int) (c1[0] + (c2[0] - c1[0]) * t);
        int g = (int) (c1[1] + (c2[1] - c1[1]) * t);
        int b = (int) (c1[2] + (c2[2] - c1[2]) * t);
        return new int[]{r, g, b};
    }

}