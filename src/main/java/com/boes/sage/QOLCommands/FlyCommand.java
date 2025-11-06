package com.boes.sage.QOLCommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import org.bukkit.entity.Player;

@CommandAlias("fly")
@Description("Enable or disable flight for yourself or another player")
public class FlyCommand extends BaseCommand {

    @CommandCompletion("@players on|off")
    @Conditions("permission:sage.fly")
    public void onCommand(Player sender, @Optional Player target, @Optional String state) {
        if (target == null) {
            target = sender;
            if (state == null) {
                boolean newState = !target.getAllowFlight();
                setFlightState(target, newState, sender, target);
                return;
            }
        } else if (state == null) {
            boolean newState = !target.getAllowFlight();
            setFlightState(target, newState, sender, target);
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

        setFlightState(target, newState, sender, target);
    }

    private void setFlightState(Player target, boolean state, Player sender, Player executor) {
        target.setAllowFlight(state);
        target.setFlying(state);

        if (sender.equals(target)) {
            target.sendMessage(state ? "§aFlight enabled!" : "§cFlight disabled!");
        } else {
            target.sendMessage(state ? "§aFlight enabled by " + sender.getName() + "!" : "§cFlight disabled by " + sender.getName() + "!");
            sender.sendMessage(state ? "§aEnabled flight for " + target.getName() + "!" : "§cDisabled flight for " + target.getName() + "!");
        }
    }
}