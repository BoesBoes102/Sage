package com.boes.sage;

import co.aikar.commands.PaperCommandManager;
import com.boes.sage.commands.StaffCommands.*;
import com.boes.sage.commands.TeleportCommands.*;
import com.boes.sage.commands.QOLCommands.*;
import com.boes.sage.data.PunishmentData;
import com.boes.sage.listeners.ChatListener;
import com.boes.sage.listeners.CommandSpyListener;
import com.boes.sage.listeners.ConsoleSpyListener;
import com.boes.sage.listeners.InventoryClickListener;
import com.boes.sage.listeners.PlayerJoinListener;
import com.boes.sage.listeners.PlayerInteractListener;
import com.boes.sage.listeners.StaffModeListener;
import com.boes.sage.listeners.VanishListener;
import com.boes.sage.listeners.PlayerJoinSyncListener;
import com.boes.sage.managers.PunishmentManager;
import com.boes.sage.managers.SpyManager;
import com.boes.sage.managers.StaffModeManager;
import com.boes.sage.managers.VanishManager;
import com.boes.sage.managers.WarpManager;
import com.boes.sage.managers.KitManager;
import com.boes.sage.managers.NotificationManager;
import com.boes.sage.managers.ChatLogManager;
import com.boes.sage.managers.ItemDatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Sage extends JavaPlugin {

    private static Sage instance;
    private PaperCommandManager commandManager;
    private PunishmentManager punishmentManager;
    private SpyManager spyManager;
    private StaffModeManager staffModeManager;
    private VanishManager vanishManager;
    private WarpManager warpManager;
    private KitManager kitManager;
    private NotificationManager notificationManager;
    private ChatLogManager chatLogManager;
    private ItemDatabaseManager itemDatabaseManager;
    private Map<String, PunishmentData> punishmentReasons;
    private OpenInventoryCommand openInventoryCommand;
    private ConsoleSpyListener consoleSpyListener;
    private Set<String> markedChairs;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        try {
            commandManager = new PaperCommandManager(this);
            commandManager.getCommandContexts().registerContext(PotionEffectType.class, c -> {
                String input = c.popFirstArg();
                PotionEffectType type = PotionEffectType.getByName(input.toUpperCase());
                if (type == null) {
                    throw new IllegalArgumentException("Invalid potion effect type: " + input);
                }
                return type;
            });
            commandManager.getCommandContexts().registerContext(Enchantment.class, c -> {
                String input = c.popFirstArg();
                Enchantment enchantment = Enchantment.getByName(input.toUpperCase());
                if (enchantment == null) {
                    throw new IllegalArgumentException("Invalid enchantment: " + input);
                }
                return enchantment;
            });

            commandManager.getCommandCompletions().registerCompletion("potioneffecttypes", c -> 
                Arrays.stream(PotionEffectType.values())
                    .filter(type -> type != null && type.getName() != null)
                    .map(type -> type.getName().toLowerCase())
                    .collect(Collectors.toList())
            );

            commandManager.getCommandCompletions().registerCompletion("materials", c ->
                Arrays.stream(Material.values())
                    .map(m -> m.name().toLowerCase())
                    .collect(Collectors.toList())
            );


            commandManager.getCommandCompletions().registerCompletion("enchantments", c ->
                Arrays.stream(Enchantment.values())
                    .map(e -> e.getKey().getKey())
                    .collect(Collectors.toList())
            );
        } catch (Exception e) {
            getLogger().severe("Failed to initialize ACF command manager: " + e.getMessage());
            e.printStackTrace();
        }

        markedChairs = new HashSet<>();
        loadMarkedChairs();
        punishmentManager = new PunishmentManager(this);
        spyManager = new SpyManager(this);
        staffModeManager = new StaffModeManager(this);
        vanishManager = new VanishManager(this);
        warpManager = new WarpManager(this);
        kitManager = new KitManager(this);
        notificationManager = new NotificationManager(this);
        chatLogManager = new ChatLogManager(this);
        itemDatabaseManager = new ItemDatabaseManager(this);
        loadPunishmentReasons();

        try {
            commandManager.getCommandCompletions().registerCompletion("itemdb", c ->
                itemDatabaseManager.getItemNames().stream().sorted().collect(Collectors.toList())
            );

            commandManager.getCommandCompletions().registerCompletion("punishReasons", c ->
                new java.util.ArrayList<>(punishmentReasons.keySet())
            );

            commandManager.getCommandCompletions().registerCompletion("kits", c ->
                kitManager.getKitNames().stream().sorted().collect(Collectors.toList())
            );

            commandManager.getCommandCompletions().registerCompletion("warp", c ->
                    warpManager.getWarpNames(false));

            commandManager.getCommandCompletions().registerCompletion("entitytypes", c ->
                Arrays.stream(org.bukkit.entity.EntityType.values())
                    .filter(et -> et.isSpawnable() && et.isAlive())
                    .map(et -> et.name().toLowerCase())
                    .collect(Collectors.toList())
            );
        } catch (Exception e) {
            getLogger().severe("Failed to register command completions: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            commandManager.registerCommand(new PunishCommand(this));
            commandManager.registerCommand(new WarnCommand(this));
            commandManager.registerCommand(new MuteCommand(this));
            commandManager.registerCommand(new BanCommand(this));
            commandManager.registerCommand(new BlacklistCommand(this));
            commandManager.registerCommand(new KickCommand(this));
            commandManager.registerCommand(new UnwarnCommand(this));
            commandManager.registerCommand(new UnmuteCommand(this));
            commandManager.registerCommand(new UnbanCommand(this));
            commandManager.registerCommand(new UnblacklistCommand(this));
            commandManager.registerCommand(new HistoryCommand(this));

            commandManager.registerCommand(new StaffModeCommand(this));
            commandManager.registerCommand(new BroadcastCommand(this));
            commandManager.registerCommand(new StaffBroadcastCommand(this));
            commandManager.registerCommand(new MuteChatCommand(this));
            commandManager.registerCommand(new ChatLogsCommand(this));
            commandManager.registerCommand(new KitCommand(this));
            commandManager.registerCommand(new ItemDBCommand(this));
            commandManager.registerCommand(new StaffChatCommand(this));
            commandManager.registerCommand(new AdminChatCommand(this));
            commandManager.registerCommand(new KickAllCommand(this));

            commandManager.registerCommand(new PingCommand(this));
            commandManager.registerCommand(new HealCommand(this));
            commandManager.registerCommand(new FlyCommand());
            commandManager.registerCommand(new ClearCommand(this));
            commandManager.registerCommand(new VanishCommand(this));
            commandManager.registerCommand(new RulesCommand(this));
            commandManager.registerCommand(new HatCommand(this));
            commandManager.registerCommand(new SeenCommand());
            commandManager.registerCommand(new SudoCommand(this));
            commandManager.registerCommand(new SpeedCommand(this));
            commandManager.registerCommand(new XpCommand(this));
            commandManager.registerCommand(new CustomGradientCommand(this));
            commandManager.registerCommand(new ItemCommand(this));
            commandManager.registerCommand(new UuidCommand(this));
            commandManager.registerCommand(new PTimeCommand(this));
            commandManager.registerCommand(new RepairCommand(this));
            commandManager.registerCommand(new PotionCommand(this));
            commandManager.registerCommand(new DisposeCommand(this));
            commandManager.registerCommand(new RespawnCommand(this));
            commandManager.registerCommand(new PWeatherCommand(this));
            commandManager.registerCommand(new SpawnMobCommand(this));
            commandManager.registerCommand(new FirstJoinCommand());
            commandManager.registerCommand(new CommandSpyCommand(this));
            commandManager.registerCommand(new ConsoleSpyCommand(this));
            commandManager.registerCommand(new OpenInventoryCommand(this));
            commandManager.registerCommand(new OpenEnderChestCommand(this));
            commandManager.registerCommand(new EnchantmentBookCommand(this));
            commandManager.registerCommand(new GamemodeCreativeCommand(this));
            commandManager.registerCommand(new GamemodeSurvivalCommand(this));
            commandManager.registerCommand(new GamemodeSpectatorCommand(this));
            commandManager.registerCommand(new GamemodeAdventureCommand(this));
            commandManager.registerCommand(new GiveSittingStickCommand(this));
        } catch (Exception e) {
            getLogger().severe("Failed to register commands with ACF: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            commandManager.registerCommand(new TpHereCommand(this));
            commandManager.registerCommand(new TpHereAllCommand(this));
            commandManager.registerCommand(new TeleportToPlayerCommand(this));
            commandManager.registerCommand(new WorldCommand(this));
            commandManager.registerCommand(new TpPosCommand(this));
            commandManager.registerCommand(new WarpCommand(this));
        } catch (Exception e) {
            getLogger().severe("Failed to register teleport commands with ACF: " + e.getMessage());
            e.printStackTrace();
        }

        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandSpyListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffModeListener(this), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinSyncListener( this), this);

        consoleSpyListener = new ConsoleSpyListener(this);
        Logger.getLogger("").addHandler(consoleSpyListener);

        getLogger().info("Sage has been enabled!");
    }

    @Override
    public void onDisable() {
        saveMarkedChairs();

        if (chatLogManager != null) {
            chatLogManager.saveLogs();
        }

        if (spyManager != null) {
            for (java.util.UUID uuid : spyManager.getConsoleSpyPlayers()) {
                org.bukkit.entity.Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    spyManager.setConsoleSpy(player, false);
                }
            }
        }

        if (consoleSpyListener != null) {
            Logger.getLogger("").removeHandler(consoleSpyListener);
            consoleSpyListener.cleanup();
        }

        if (openInventoryCommand != null) {
            openInventoryCommand.cleanup();
        }
        if (staffModeManager != null) {
            staffModeManager.disableAllStaffMode();
        }

        Bukkit.getScheduler().cancelTasks(this);

        getLogger().info("Sage has been disabled!");
    }

    public static Sage getInstance() {
        return instance;
    }

    public PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public SpyManager getSpyManager() {
        return spyManager;
    }

    public StaffModeManager getStaffModeManager() {
        return staffModeManager;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public ChatLogManager getChatLogManager() {
        return chatLogManager;
    }

    public ItemDatabaseManager getItemDatabaseManager() {
        return itemDatabaseManager;
    }

    public Map<String, PunishmentData> getPunishmentReasons() {
        return punishmentReasons;
    }

    public Set<String> getMarkedChairs() {
        return markedChairs;
    }


    private void loadPunishmentReasons() {
        punishmentReasons = new HashMap<>();

        if (!getConfig().contains("punishment-reasons")) {
            getLogger().warning("No punishment reasons found in config!");
            return;
        }

        for (String reason : Objects.requireNonNull(getConfig().getConfigurationSection("punishment-reasons")).getKeys(false)) {
            String path = "punishment-reasons." + reason;
            PunishmentData data = new PunishmentData(reason);

            for (String stackKey : Objects.requireNonNull(getConfig().getConfigurationSection(path)).getKeys(false)) {
                int stack = Integer.parseInt(stackKey);
                String type = getConfig().getString(path + "." + stackKey + ".type");
                String duration = getConfig().getString(path + "." + stackKey + ".duration", null);

                data.addStackPunishment(stack, type, duration);
            }

            punishmentReasons.put(reason.toLowerCase(), data);
        }
    }

    public void reloadPunishments() {
        reloadConfig();
        loadPunishmentReasons();
    }

    private void loadMarkedChairs() {
        File chairsFile = new File(getDataFolder(), "marked_chairs.yml");
        if (!chairsFile.exists()) {
            return;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(chairsFile);
            List<String> chairs = config.getStringList("marked-chairs");
            markedChairs.addAll(chairs);
            getLogger().info("Loaded " + chairs.size() + " marked chairs!");
        } catch (Exception e) {
            getLogger().warning("Failed to load marked chairs: " + e.getMessage());
        }
    }

    private void saveMarkedChairs() {
        File chairsFile = new File(getDataFolder(), "marked_chairs.yml");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        try {
            FileConfiguration config = new YamlConfiguration();
            config.set("marked-chairs", new java.util.ArrayList<>(markedChairs));
            config.save(chairsFile);
            getLogger().info("Saved " + markedChairs.size() + " marked chairs!");
        } catch (IOException e) {
            getLogger().warning("Failed to save marked chairs: " + e.getMessage());
        }
    }
}