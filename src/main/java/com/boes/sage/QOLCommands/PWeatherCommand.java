package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.boes.sage.Sage;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

@CommandAlias("pweather")
@Description("Set personal weather")
public class PWeatherCommand extends BaseCommand {

    private final Sage plugin;

    public PWeatherCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Subcommand("reset")
    @Conditions("permission:sage.pweather")
    public void onReset(Player player) {
        player.resetPlayerWeather();
        plugin.getConfig().set("player-weather." + player.getUniqueId(), null);
        plugin.saveConfig();
        player.sendMessage("§aPlayer weather reset!");
    }

    @CommandCompletion("clear|rain|thunder")
    @Conditions("permission:sage.pweather")
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