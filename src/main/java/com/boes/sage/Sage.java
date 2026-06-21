package com.boes.sage;

import co.aikar.commands.PaperCommandManager;
import com.boes.sage.commands.QOLCommands.*;
import com.boes.sage.commands.TeleportCommands.*;
import com.boes.sage.features.punishment.data.PunishmentData;
import com.boes.sage.features.alts.AltAccountService;
import com.boes.sage.features.FeatureRegistry;
import com.boes.sage.features.chatlog.ChatLogFeature;
import com.boes.sage.features.freeze.FreezeFeature;
import com.boes.sage.features.freeze.FreezeService;
import com.boes.sage.features.freeze.commands.FreezeCommand;
import com.boes.sage.features.itemedit.ItemEditFeature;
import com.boes.sage.features.itemedit.ItemEditService;
import com.boes.sage.features.itemedit.commands.ItemEditCommand;
import com.boes.sage.features.itemdb.ItemDatabaseFeature;
import com.boes.sage.features.kit.KitFeature;
import com.boes.sage.features.notification.NotificationFeature;
import com.boes.sage.features.punishment.PunishmentFeature;
import com.boes.sage.features.refund.RefundFeature;
import com.boes.sage.features.spy.SpyFeature;
import com.boes.sage.features.staffmode.StaffModeFeature;
import com.boes.sage.features.usage.UsageFeature;
import com.boes.sage.features.vanish.VanishFeature;
import com.boes.sage.features.warp.WarpFeature;
import com.boes.sage.features.chatlog.ChatLogService;
import com.boes.sage.features.itemdb.ItemDatabaseService;
import com.boes.sage.features.kit.KitService;
import com.boes.sage.features.notification.NotificationService;
import com.boes.sage.features.punishment.PunishmentService;
import com.boes.sage.features.refund.RefundService;
import com.boes.sage.features.spy.SpyService;
import com.boes.sage.features.staffmode.StaffModeService;
import com.boes.sage.features.usage.UsageBossBarService;
import com.boes.sage.features.vanish.VanishService;
import com.boes.sage.features.warp.WarpService;
import com.boes.sage.features.chatlog.commands.ChatLogsCommand;
import com.boes.sage.features.itemdb.commands.ItemDBCommand;
import com.boes.sage.features.kit.commands.GiveKitCommand;
import com.boes.sage.features.kit.commands.KitCommand;
import com.boes.sage.features.notification.commands.AdminChatCommand;
import com.boes.sage.features.notification.commands.BroadcastCommand;
import com.boes.sage.features.notification.commands.MuteChatCommand;
import com.boes.sage.features.notification.commands.StaffBroadcastCommand;
import com.boes.sage.features.notification.commands.StaffChatCommand;
import com.boes.sage.features.punishment.commands.BanCommand;
import com.boes.sage.features.punishment.commands.BlacklistCommand;
import com.boes.sage.features.punishment.commands.HistoryCommand;
import com.boes.sage.features.punishment.commands.KickCommand;
import com.boes.sage.features.punishment.commands.MuteCommand;
import com.boes.sage.features.punishment.commands.PunishCommand;
import com.boes.sage.features.punishment.commands.UnbanCommand;
import com.boes.sage.features.punishment.commands.UnblacklistCommand;
import com.boes.sage.features.punishment.commands.UnmuteCommand;
import com.boes.sage.features.punishment.commands.UnwarnCommand;
import com.boes.sage.features.punishment.commands.WarnCommand;
import com.boes.sage.features.refund.commands.RefundCommand;
import com.boes.sage.features.spy.commands.CommandSpyCommand;
import com.boes.sage.features.spy.commands.ConsoleSpyCommand;
import com.boes.sage.features.staffmode.commands.StaffModeCommand;
import com.boes.sage.features.usage.commands.UsageCommand;
import com.boes.sage.features.vanish.commands.VanishCommand;
import com.boes.sage.features.warp.commands.WarpCommand;
import com.boes.sage.listeners.InventoryClickListener;
import com.boes.sage.listeners.PlayerJoinListener;
import com.boes.sage.listeners.PlayerJoinSyncListener;
import com.boes.sage.listeners.PlayerQuitListener;
import com.boes.sage.commands.StaffCommands.KickAllCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Sage extends JavaPlugin {

    private static Sage instance;
    private final FeatureRegistry featureRegistry = new FeatureRegistry();
    private PaperCommandManager commandManager;
    private PunishmentFeature punishmentFeature;
    private SpyFeature spyFeature;
    private StaffModeFeature staffModeFeature;
    private VanishFeature vanishFeature;
    private WarpFeature warpFeature;
    private KitFeature kitFeature;
    private NotificationFeature notificationFeature;
    private ChatLogFeature chatLogFeature;
    private FreezeFeature freezeFeature;
    private ItemEditFeature itemEditFeature;
    private ItemDatabaseFeature itemDatabaseFeature;
    private UsageFeature usageFeature;
    private RefundFeature refundFeature;
    private AltAccountService altAccountService;
    private Map<String, PunishmentData> punishmentReasons;
    private OpenInventoryCommand openInventoryCommand;
    private OpenEnderChestCommand openEnderChestCommand;
    private FileConfiguration punishmentsConfig;
    private FileConfiguration rulesConfig;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        initializeSplitConfigs();
        loadAllAvailableWorlds();
        altAccountService = new AltAccountService(this);

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
            commandManager.getCommandContexts().registerContext(Attribute.class, c -> {
                String input = c.popFirstArg();
                Attribute attribute = Arrays.stream(Attribute.values())
                    .filter(value -> value.name().equalsIgnoreCase(input))
                    .findFirst()
                    .orElse(null);
                if (attribute == null) {
                    throw new IllegalArgumentException("Invalid attribute: " + input);
                }
                return attribute;
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

            commandManager.getCommandCompletions().registerCompletion("attributes", c ->
                Arrays.stream(Attribute.values())
                    .map(attribute -> attribute.name().toLowerCase())
                    .collect(Collectors.toList())
            );
        } catch (Exception e) {
            getLogger().severe("Failed to initialize ACF command manager: " + e.getMessage());
            e.printStackTrace();
        }

        punishmentFeature = new PunishmentFeature();
        spyFeature = new SpyFeature();
        staffModeFeature = new StaffModeFeature();
        vanishFeature = new VanishFeature();
        warpFeature = new WarpFeature();
        kitFeature = new KitFeature();
        notificationFeature = new NotificationFeature();
        chatLogFeature = new ChatLogFeature();
        freezeFeature = new FreezeFeature();
        itemEditFeature = new ItemEditFeature();
        itemDatabaseFeature = new ItemDatabaseFeature();
        usageFeature = new UsageFeature();
        refundFeature = new RefundFeature();

        featureRegistry.register("punishment", punishmentFeature);
        featureRegistry.register("spy", spyFeature);
        featureRegistry.register("staffMode", staffModeFeature);
        featureRegistry.register("vanish", vanishFeature);
        featureRegistry.register("warp", warpFeature);
        featureRegistry.register("kit", kitFeature);
        featureRegistry.register("notification", notificationFeature);
        featureRegistry.register("chatLog", chatLogFeature);
        featureRegistry.register("freeze", freezeFeature);
        featureRegistry.register("itemEdit", itemEditFeature);
        featureRegistry.register("itemDatabase", itemDatabaseFeature);
        featureRegistry.register("usage", usageFeature);
        featureRegistry.register("refund", refundFeature);
        featureRegistry.all().forEach((key, feature) -> feature.register(this));
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinSyncListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), this);
        loadPunishmentReasons();

        try {
            commandManager.getCommandCompletions().registerCompletion("itemdb", c ->
                getItemDatabaseService().getItemNames().stream().sorted().collect(Collectors.toList())
            );

            commandManager.getCommandCompletions().registerCompletion("punishReasons", c ->
                new java.util.ArrayList<>(punishmentReasons.keySet())
            );

            commandManager.getCommandCompletions().registerCompletion("kits", c ->
                getKitService().getKitNames().stream().sorted().collect(Collectors.toList())
            );

            commandManager.getCommandCompletions().registerCompletion("kitGiveTargets", c -> {
                List<String> targets = new java.util.ArrayList<>();
                targets.add("all");
                targets.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(player -> player.getName())
                    .sorted()
                    .collect(Collectors.toList()));
                return targets;
            });

            commandManager.getCommandCompletions().registerCompletion("warp", c ->
                getWarpService().getWarpNames(false));

            commandManager.getCommandCompletions().registerCompletion("worldNames", c -> {
                File[] folders = getServer().getWorldContainer().listFiles();
                List<String> worldNames = new java.util.ArrayList<>();

                if (folders != null) {
                    for (File folder : folders) {
                        if (folder.isDirectory() && new File(folder, "level.dat").exists()) {
                            worldNames.add(folder.getName());
                        }
                    }
                }

                Bukkit.getWorlds().stream()
                    .map(world -> world.getName())
                    .filter(name -> worldNames.stream().noneMatch(existing -> existing.equalsIgnoreCase(name)))
                    .forEach(worldNames::add);

                worldNames.sort(String.CASE_INSENSITIVE_ORDER);
                return worldNames;
            });

            commandManager.getCommandCompletions().registerCompletion("entitytypes", c ->
                Arrays.stream(org.bukkit.entity.EntityType.values())
                    .filter(et -> et.isSpawnable() && et.isAlive())
                    .map(et -> et.name().toLowerCase())
                    .collect(Collectors.toList())
            );

            commandManager.getCommandCompletions().registerCompletion("durations", c -> {
                String input = c.getInput();
                List<String> durations = new java.util.ArrayList<>();
                if (input.matches("\\d+.*")) {
                    durations.add("d");
                    durations.add("h");
                    durations.add("m");
                    durations.add("s");
                }
                return durations;
            });
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
            commandManager.registerCommand(new FreezeCommand(this));
            commandManager.registerCommand(new BroadcastCommand(this));
            commandManager.registerCommand(new StaffBroadcastCommand(this));
            commandManager.registerCommand(new MuteChatCommand(this));
            commandManager.registerCommand(new ChatLogsCommand(this));
            commandManager.registerCommand(new KitCommand(this));
            commandManager.registerCommand(new GiveKitCommand(this));
            commandManager.registerCommand(new ItemDBCommand(this));
            commandManager.registerCommand(new ItemEditCommand(this));
            commandManager.registerCommand(new StaffChatCommand(this));
            commandManager.registerCommand(new AdminChatCommand(this));
            commandManager.registerCommand(new KickAllCommand(this));
            commandManager.registerCommand(new RefundCommand(this));

            commandManager.registerCommand(new PingCommand(this));
            commandManager.registerCommand(new AltsCommand(this));
            commandManager.registerCommand(new AltsAllCommand(this));
            commandManager.registerCommand(new HealCommand(this));
            commandManager.registerCommand(new FeedCommand(this));
            commandManager.registerCommand(new FlyCommand());
            if (isCommandEnabled("clear")) {
                commandManager.registerCommand(new ClearCommand(this));
            }
            commandManager.registerCommand(new VanishCommand(this));
            if (isCommandEnabled("rules")) {
                commandManager.registerCommand(new RulesCommand(this));
            }
            commandManager.registerCommand(new HatCommand(this));
            commandManager.registerCommand(new SeenCommand());
            commandManager.registerCommand(new SudoCommand(this));
            commandManager.registerCommand(new SpeedCommand(this));
            commandManager.registerCommand(new XpCommand(this));

            commandManager.registerCommand(new ItemCommand(this));
            commandManager.registerCommand(new UuidCommand(this));
            commandManager.registerCommand(new PTimeCommand(this));
            commandManager.registerCommand(new RepairCommand(this));
            commandManager.registerCommand(new PotionCommand(this));
            if (isCommandEnabled("dispose")) {
                commandManager.registerCommand(new DisposeCommand(this));
            }
            if (isCommandEnabled("respawn")) {
                commandManager.registerCommand(new RespawnCommand(this));
            }
            commandManager.registerCommand(new PWeatherCommand(this));
            commandManager.registerCommand(new SpawnMobCommand(this));
            commandManager.registerCommand(new FirstJoinCommand());
            commandManager.registerCommand(new CommandSpyCommand(this));
            commandManager.registerCommand(new ConsoleSpyCommand(this));
            openInventoryCommand = new OpenInventoryCommand(this);
            commandManager.registerCommand(openInventoryCommand);
            openEnderChestCommand = new OpenEnderChestCommand(this);
            commandManager.registerCommand(openEnderChestCommand);
            commandManager.registerCommand(new EnchantmentBookCommand(this));
            commandManager.registerCommand(new GamemodeCreativeCommand(this));
            commandManager.registerCommand(new GamemodeSurvivalCommand(this));
            commandManager.registerCommand(new GamemodeSpectatorCommand(this));
            commandManager.registerCommand(new GamemodeAdventureCommand(this));
            commandManager.registerCommand(new UsageCommand(this));
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

        getLogger().info("Sage has been enabled!");
    }

    @Override
    public void onDisable() {
        if (chatLogFeature != null) {
            chatLogFeature.shutdown(this);
        }
        if (freezeFeature != null) {
            freezeFeature.shutdown(this);
        }
        if (spyFeature != null) {
            spyFeature.shutdown(this);
        }
        if (refundFeature != null) {
            refundFeature.shutdown(this);
        }
        if (notificationFeature != null) {
            notificationFeature.shutdown(this);
        }
        if (staffModeFeature != null) {
            staffModeFeature.shutdown(this);
        }
        if (usageFeature != null) {
            usageFeature.shutdown(this);
        }

        if (openInventoryCommand != null) {
            openInventoryCommand.cleanup();
        }

        if (openEnderChestCommand != null) {
            openEnderChestCommand.cleanup();
        }

        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("Sage has been disabled!");
    }

    private void loadAllAvailableWorlds() {
        File worldContainer = getServer().getWorldContainer();
        File[] folders = worldContainer.listFiles();

        if (folders == null) {
            getLogger().warning("Could not list world directories!");
            return;
        }

        int loadedCount = 0;
        for (File folder : folders) {
            if (folder.isDirectory() && new File(folder, "level.dat").exists()) {
                try {
                    org.bukkit.WorldCreator creator = new org.bukkit.WorldCreator(folder.getName());
                    org.bukkit.World world = Bukkit.getWorld(folder.getName());

                    if (world == null) {
                        world = creator.createWorld();
                        loadedCount++;
                    }
                } catch (Exception e) {
                    getLogger().warning("Failed to load world '" + folder.getName() + "': " + e.getMessage());
                }
            }
        }

        if (loadedCount > 0) {
            getLogger().info("Loaded " + loadedCount + " available worlds!");
        }
    }

    public static Sage getInstance() {
        return instance;
    }

    public PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public PunishmentService getPunishmentService() {
        return punishmentFeature == null ? null : punishmentFeature.service();
    }

    public SpyService getSpyService() {
        return spyFeature == null ? null : spyFeature.service();
    }

    public StaffModeService getStaffModeService() {
        return staffModeFeature == null ? null : staffModeFeature.service();
    }

    public VanishService getVanishService() {
        return vanishFeature == null ? null : vanishFeature.service();
    }

    public WarpService getWarpService() {
        return warpFeature == null ? null : warpFeature.service();
    }

    public KitService getKitService() {
        return kitFeature == null ? null : kitFeature.service();
    }

    public NotificationService getNotificationService() {
        return notificationFeature == null ? null : notificationFeature.service();
    }

    public ChatLogService getChatLogService() {
        return chatLogFeature == null ? null : chatLogFeature.service();
    }

    public FreezeService getFreezeService() {
        return freezeFeature == null ? null : freezeFeature.service();
    }

    public ItemDatabaseService getItemDatabaseService() {
        return itemDatabaseFeature == null ? null : itemDatabaseFeature.service();
    }

    public ItemEditService getItemEditService() {
        return itemEditFeature == null ? null : itemEditFeature.service();
    }

    public UsageBossBarService getUsageBossBarService() {
        return usageFeature == null ? null : usageFeature.service();
    }

    public RefundService getRefundService() {
        return refundFeature == null ? null : refundFeature.service();
    }

    public OpenInventoryCommand getOpenInventoryCommand() {
        return openInventoryCommand;
    }

    public OpenEnderChestCommand getOpenEnderChestCommand() {
        return openEnderChestCommand;
    }

    public AltAccountService getAltAccountService() {
        return altAccountService;
    }

    public Map<String, PunishmentData> getPunishmentReasons() {
        return punishmentReasons;
    }

    public FileConfiguration getPunishmentsConfig() {
        return punishmentsConfig;
    }

    public FileConfiguration getRulesConfig() {
        return rulesConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    private boolean isCommandEnabled(String commandKey) {
        return getConfig().getBoolean("commands." + commandKey + ".enabled", true);
    }

    private void loadPunishmentReasons() {
        punishmentReasons = new HashMap<>();

        if (punishmentsConfig == null || !punishmentsConfig.contains("punishment-reasons")) {
            getLogger().warning("No punishment reasons found in punishments.yml!");
            return;
        }

        for (String reason : Objects.requireNonNull(punishmentsConfig.getConfigurationSection("punishment-reasons")).getKeys(false)) {
            String path = "punishment-reasons." + reason;
            PunishmentData data = new PunishmentData(reason);

            for (String stackKey : Objects.requireNonNull(punishmentsConfig.getConfigurationSection(path)).getKeys(false)) {
                int stack = Integer.parseInt(stackKey);
                String type = punishmentsConfig.getString(path + "." + stackKey + ".type");
                String duration = punishmentsConfig.getString(path + "." + stackKey + ".duration", null);

                data.addStackPunishment(stack, type, duration);
            }

            punishmentReasons.put(reason.toLowerCase(), data);
        }
    }

    public void reloadPunishments() {
        reloadSplitConfigs();
        loadPunishmentReasons();
    }

    private void initializeSplitConfigs() {
        ensureSplitConfig("punishments.yml", "punishment-reasons");
        ensureSplitConfig("rules.yml", "rules");
        ensureSplitConfig("messages.yml", "messages", "broadcast");
        reloadSplitConfigs();
    }

    private void ensureSplitConfig(String fileName, String... rootKeys) {
        File file = new File(getDataFolder(), fileName);
        if (file.exists()) {
            return;
        }

        YamlConfiguration migratedConfig = new YamlConfiguration();
        boolean migrated = false;
        for (String rootKey : rootKeys) {
            ConfigurationSection section = getConfig().getConfigurationSection(rootKey);
            if (section == null) {
                continue;
            }

            migratedConfig.set(rootKey, section.getValues(true));
            migrated = true;
        }

        if (migrated) {
            try {
                migratedConfig.save(file);
            } catch (IOException exception) {
                getLogger().warning("Failed to migrate " + fileName + ": " + exception.getMessage());
                saveResource(fileName, false);
            }
            return;
        }

        saveResource(fileName, false);
    }

    private void reloadSplitConfigs() {
        punishmentsConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "punishments.yml"));
        rulesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "rules.yml"));
        messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
    }
}
