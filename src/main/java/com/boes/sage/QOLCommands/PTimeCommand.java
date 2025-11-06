package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.boes.sage.Sage;
import org.bukkit.entity.Player;

@CommandAlias("ptime")
@Description("Set personal time of day")
public class PTimeCommand extends BaseCommand {

    private final Sage plugin;
    private static final long TIME_MIDNIGHT = 18000;
    private static final long TIME_DAWN = 22000;
    private static final long TIME_SUNRISE = 23000;
    private static final long TIME_MORNING = 1000;
    private static final long TIME_NOON = 6000;
    private static final long TIME_AFTERNOON = 9000;
    private static final long TIME_SUNSET = 12000;
    private static final long TIME_DUSK = 13000;
    private static final long TIME_NIGHT = 14000;

    public PTimeCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Subcommand("reset")
    @Conditions("permission:sage.ptime")
    public void onReset(Player player) {
        player.resetPlayerTime();
        plugin.getConfig().set("player-times." + player.getUniqueId(), null);
        plugin.saveConfig();
        player.sendMessage("§aPlayer time reset!");
    }

    @CommandCompletion("midnight|dawn|sunrise|morning|noon|afternoon|sunset|dusk|night|0|6000|12000|18000")
    @Conditions("permission:sage.ptime")
    public void onCommand(Player player, String timeArg) {
        timeArg = timeArg.toLowerCase();

        long time;
        try {
            switch (timeArg) {
                case "midnight" -> time = TIME_MIDNIGHT;
                case "dawn" -> time = TIME_DAWN;
                case "sunrise" -> time = TIME_SUNRISE;
                case "morning" -> time = TIME_MORNING;
                case "noon" -> time = TIME_NOON;
                case "afternoon" -> time = TIME_AFTERNOON;
                case "sunset" -> time = TIME_SUNSET;
                case "dusk" -> time = TIME_DUSK;
                case "night" -> time = TIME_NIGHT;
                default -> {
                    time = Long.parseLong(timeArg);
                    if (time < 0 || time >= 24000) {
                        player.sendMessage("§cTime must be between 0 and 23999!");
                        return;
                    }
                }
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid time format!");
            player.sendMessage("§7Times: midnight, dawn, sunrise, morning, noon, afternoon, sunset, dusk, night");
            player.sendMessage("§7Or use a number (0-23999)");
            return;
        }

        player.setPlayerTime(time, false);
        plugin.getConfig().set("player-times." + player.getUniqueId(), time);
        plugin.saveConfig();

        player.sendMessage("§aPlayer time set to " + timeArg + "!");
    }
}