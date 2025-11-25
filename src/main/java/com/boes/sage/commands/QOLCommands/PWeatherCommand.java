package com.boes.sage.commands.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.boes.sage.Sage;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

@CommandAlias("pweather")
@Description("Set personal weather")
@CommandPermission("sage.pweather")
public class PWeatherCommand extends BaseCommand {

    private final Sage plugin;

    public PWeatherCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Subcommand("reset")
    public void onReset(Player player) {
        player.resetPlayerWeather();
        plugin.getConfig().set("player-weather." + player.getUniqueId(), null);
        plugin.saveConfig();
        player.sendMessage("§aPlayer weather reset!");
    }
    @Default
    @CommandCompletion("clear|rain|thunder")
    public void onCommand(Player player, String weatherArg) {
        weatherArg = weatherArg.toLowerCase();

        switch (weatherArg) {
            case "clear":
                player.setPlayerWeather(WeatherType.CLEAR);
                break;
            case "rain", "thunder":
                player.setPlayerWeather(WeatherType.DOWNFALL);
                break;
            default:
                player.sendMessage("§cInvalid weather type!");
                player.sendMessage("§7Types: clear, rain, thunder");
                return;
        }

        plugin.getConfig().set("player-weather." + player.getUniqueId(), weatherArg);
        plugin.saveConfig();

        player.sendMessage("§aPlayer weather set to " + weatherArg + "!");
    }
}