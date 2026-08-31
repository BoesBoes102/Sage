package com.boes.sage.commands.QOLCommands;

import com.boes.sage.Sage;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class PWeatherCommand {

    private final Sage plugin;

    public PWeatherCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Command("pweather reset")
    @Permission("sage.pweather")
    public void onReset(Player player) {
        player.resetPlayerWeather();
        plugin.getPlayerRuntimeDataManager().clearPlayerWeather(player.getUniqueId());
        player.sendMessage("§aPlayer weather reset!");
    }

    @Command("pweather <weather>")
    @Permission("sage.pweather")
    public void onCommand(Player player, @Argument(value = "weather", suggestions = "pweatherOptions") String weatherArg) {
        weatherArg = weatherArg.toLowerCase();

        if (weatherArg.equals("reset")) {
            onReset(player);
            return;
        }

        switch (weatherArg) {
            case "clear":
                player.setPlayerWeather(WeatherType.CLEAR);
                break;
            case "rain", "thunder":
                player.setPlayerWeather(WeatherType.DOWNFALL);
                break;
            default:
                player.sendMessage("§cInvalid weather type!");
                player.sendMessage("§7Types: clear, rain, thunder, reset");
                return;
        }

        plugin.getPlayerRuntimeDataManager().setPlayerWeather(player.getUniqueId(), weatherArg);

        player.sendMessage("§aPlayer weather set to " + weatherArg + "!");
    }
}
