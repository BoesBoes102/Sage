package com.boes.sage.commands.QOLCommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class FlyCommand {
    @Command("fly [player] [state]")
    @Permission("sage.fly")
    public void onCommand(
            Player sender,
            @Argument(value = "player", suggestions = "players") String targetName,
            @Argument("state") String state
    ) {
        Player target = sender;

        if (targetName != null && !isStateValue(targetName)) {
            target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return;
            }

            if (!target.equals(sender) && !sender.hasPermission("sage.staff.admin")) {
                sender.sendMessage("§cYou don't have permission to change other players' flight status!");
                return;
            }
        } else if (targetName != null && isStateValue(targetName)) {
            state = targetName;
        }

        if (state == null) {
            boolean newState = !target.getAllowFlight();
            setFlightState(target, newState, sender);
            return;
        }

        boolean newState;
        if (state.equalsIgnoreCase("on") || state.equalsIgnoreCase("true")) {
            newState = true;
        } else if (state.equalsIgnoreCase("off") || state.equalsIgnoreCase("false")) {
            newState = false;
        } else {
            sender.sendMessage("§cUsage: /fly [player] [on/off]");
            return;
        }

        setFlightState(target, newState, sender);
    }

    private boolean isStateValue(String value) {
        return value.equalsIgnoreCase("on") || value.equalsIgnoreCase("off") ||
               value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    private void setFlightState(Player target, boolean state, Player sender) {
        target.setAllowFlight(state);
        target.setFlying(state);

        if (sender.equals(target)) {
            target.sendMessage(state ? "§aFlight enabled!" : "§cFlight disabled!");
        } else {
            target.sendMessage(state ? "§aYour flight was enabled!" : "§cYour flight was disabled!");
            sender.sendMessage(state ? "§aEnabled flight for " + target.getName() + "!" : "§cDisabled flight for " + target.getName() + "!");
        }
    }
}
