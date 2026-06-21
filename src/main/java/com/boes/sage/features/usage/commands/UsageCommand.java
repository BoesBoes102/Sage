package com.boes.sage.features.usage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;

@CommandAlias("usage")
@Description("Check server resource usage")
@CommandPermission("sage.usage")
public class UsageCommand extends BaseCommand {

    private final Sage plugin;

    public UsageCommand(Sage plugin) {
        this.plugin = plugin;
    }

    @Default
    @Syntax("<ram|cpu|bar>")
    public void onDefault(Player player) {
        player.sendMessage("§cUsage: /usage <ram|cpu>");
    }

    @Subcommand("ram")
    public void onRam(Player player) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();

        long usedMB = heapMemory.getUsed() / (1024 * 1024);
        long maxMB = heapMemory.getMax() / (1024 * 1024);
        double percentage = (double) heapMemory.getUsed() / heapMemory.getMax() * 100;

        String color = percentage > 80 ? "§c" : percentage > 60 ? "§e" : "§a";

        player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§7RAM Usage:");
        player.sendMessage("§7Used: " + color + usedMB + "MB §7/ " + maxMB + "MB");
        player.sendMessage("§7Percentage: " + color + String.format("%.1f%%", percentage));
        player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━");
    }

    @Subcommand("cpu")
    public void onCpu(Player player) {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        try {
            double systemLoadAverage = osBean.getSystemLoadAverage();
            if (systemLoadAverage < 0) {
                player.sendMessage("§cSystem load average is not available.");
                return;
            }

            int availableProcessors = osBean.getAvailableProcessors();
            double cpuUsagePercent = (systemLoadAverage / availableProcessors) * 100;
            cpuUsagePercent = Math.min(cpuUsagePercent, 100);

            String color = cpuUsagePercent > 80 ? "§c" : cpuUsagePercent > 60 ? "§e" : "§a";

            player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§7CPU Usage:");
            player.sendMessage("§7System Load: " + color + String.format("%.1f%%", cpuUsagePercent));
            player.sendMessage("§7Available Processors: §b" + availableProcessors);
            player.sendMessage("§7━━━━━━━━━━━━━━━━━━━━━━");
        } catch (Exception e) {
            player.sendMessage("§cCould not retrieve CPU usage information.");
        }
    }

    @Subcommand("bar")
    public void onBar(Player player) {
        if (plugin.getUsageBossBarService().hasBossBar(player.getUniqueId())) {
            plugin.getUsageBossBarService().removeBossBar(player);
            player.sendMessage("§aUsage bossbar disabled!");
        } else {
            plugin.getUsageBossBarService().createBossBar(player);
            player.sendMessage("§aUsage bossbar enabled!");
        }
    }

    private String createBar(int filled, int total, String color) {
        StringBuilder bar = new StringBuilder(color);
        for (int i = 0; i < total; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        return bar.toString();
    }

    private String getTpsColor(double tps) {
        if (tps >= 19.5) return "§a";
        if (tps >= 18.0) return "§e";
        return "§c";
    }

    private String getMsptColor(double mspt) {
        if (mspt <= 2.5) return "§a";
        if (mspt <= 5.0) return "§e";
        return "§c";
    }

    private String getRamColor(double percentage) {
        if (percentage <= 60) return "§a";
        if (percentage <= 80) return "§e";
        return "§c";
    }

    private double getTPS() {
        try {
            Object minecraftServer = getMinecraftServer();
            if (minecraftServer == null) return 20.0;

            Object serverTickManager = minecraftServer.getClass().getMethod("getServerTickManager").invoke(minecraftServer);
            double tickRate = (double) serverTickManager.getClass().getMethod("getTickRate").invoke(serverTickManager);
            return Math.min(20.0, tickRate);
        } catch (Exception e) {
            return 20.0;
        }
    }

    private Object getMinecraftServer() {
        try {
            Object craftServer = plugin.getServer();
            return craftServer.getClass().getMethod("getServer").invoke(craftServer);
        } catch (Exception e) {
            return null;
        }
    }

    private double calculateMSPT(double tps) {
        if (tps <= 0) return 50.0;
        return (20.0 / tps) * 2.5;
    }
}
