package com.boes.sage.Utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class AsyncUtil {
    private AsyncUtil() {
    }

    public static void async(Plugin plugin, Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static void sync(Plugin plugin, Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }
}
