package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class PTimeCommand {

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

    @Command("ptime reset")
    @Permission("sage.ptime")
    public void onReset(Player player) {
        player.resetPlayerTime();
        plugin.getPlayerRuntimeDataManager().clearPlayerTime(player.getUniqueId());
        player.sendMessage("§aPlayer time reset!");
    }

    @Command("ptime <time>")
    @Permission("sage.ptime")
    public void onCommand(Player player, @Argument(value = "time", suggestions = "ptimeOptions") String timeArg) {
        timeArg = timeArg.toLowerCase();

        if (timeArg.equals("reset")) {
            onReset(player);
            return;
        }

        long time;
        try {
            switch (timeArg) {
                case "day" -> time = TIME_MORNING;
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
            player.sendMessage("§7Times: day, midnight, dawn, sunrise, morning, noon, afternoon, sunset, dusk, night");
            player.sendMessage("§7Or use a number (0-23999), or 'reset'");
            return;
        }

        player.setPlayerTime(time, false);
        plugin.getPlayerRuntimeDataManager().setPlayerTime(player.getUniqueId(), time);

        player.sendMessage("§aPlayer time set to " + timeArg + "!");
    }
}
