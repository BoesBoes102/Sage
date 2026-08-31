package com.boes.sage;

import com.boes.sage.command.SageCommandManager;
import com.boes.sage.Utils.DatabaseManager;
import com.boes.sage.Utils.OfflinePlayerDataManager;
import com.boes.sage.Utils.PlayerRuntimeDataManager;
import com.boes.sage.commands.QOLCommands.*;
import com.boes.sage.commands.QOLCommands.DisposeCommand.DisposeCommand;
import com.boes.sage.commands.QOLCommands.WorkbenchCommands.*;
import com.boes.sage.commands.TeleportCommands.*;
import com.boes.sage.features.back.BackFeature;
import com.boes.sage.features.back.BackService;
import com.boes.sage.features.back.commands.BackCommand;
import com.boes.sage.features.god.GodFeature;
import com.boes.sage.features.god.GodService;
import com.boes.sage.features.god.commands.GodCommand;
import com.boes.sage.features.messaging.MessagingFeature;
import com.boes.sage.features.messaging.MessagingService;
import com.boes.sage.features.messaging.commands.MessageCommand;
import com.boes.sage.features.messaging.commands.ReplyCommand;
import com.boes.sage.features.punishment.data.PunishmentData;
import com.boes.sage.features.alts.AltAccountService;
import com.boes.sage.features.alts.commands.AltsCommand;
import com.boes.sage.features.alts.commands.AltsAllCommand;
import com.boes.sage.features.alts.listeners.AltsListener;
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
import com.boes.sage.features.openinv.OpenInvFeature;
import com.boes.sage.features.openinv.OpenInventoryService;
import com.boes.sage.features.openinv.OpenEnderChestService;
import com.boes.sage.features.openinv.commands.OpenInventoryCommand;
import com.boes.sage.features.openinv.commands.OpenEnderChestCommand;
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
import com.boes.sage.features.spy.commands.MessageSpyCommand;
import com.boes.sage.features.staffmode.commands.StaffModeCommand;
import com.boes.sage.features.usage.commands.UsageCommand;
import com.boes.sage.features.vanish.commands.VanishCommand;
import com.boes.sage.features.warp.commands.WarpCommand;
import com.boes.sage.commands.QOLCommands.DisposeCommand.DisposeListener;
import com.boes.sage.listeners.PlayerJoinListener;
import com.boes.sage.listeners.PlayerJoinSyncListener;
import com.boes.sage.commands.StaffCommands.KickAllCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.incendo.cloud.parser.ArgumentParseResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Sage extends JavaPlugin {

    private static Sage instance;
    private final FeatureRegistry featureRegistry = new FeatureRegistry();
    private DatabaseManager databaseManager;
    private PlayerRuntimeDataManager playerRuntimeDataManager;
    private SageCommandManager commandManager;
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
    private GodFeature godFeature;
    private MessagingFeature messagingFeature;
    private BackFeature backFeature;
    private OpenInvFeature openInvFeature;
    private AltAccountService altAccountService;
    private TempWorkbenchManager tempWorkbenchManager;
    private Map<String, PunishmentData> punishmentReasons;
    private FileConfiguration punishmentsConfig;
    private FileConfiguration rulesConfig;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        initializeSplitConfigs();
        loadAllAvailableWorlds();
        databaseManager = new DatabaseManager(this);
        playerRuntimeDataManager = new PlayerRuntimeDataManager(this);
        OfflinePlayerDataManager.init(this);
        altAccountService = new AltAccountService(this);

        try {
            commandManager = new SageCommandManager(this);

            commandManager.registerParser(PotionEffectType.class, (ctx, input) -> {
                String token = input.readString();
                PotionEffectType type = PotionEffectType.getByName(token.toUpperCase());
                if (type == null) {
                    return ArgumentParseResult.failure(new IllegalArgumentException("Invalid potion effect type: " + token));
                }
                return ArgumentParseResult.success(type);
            });

            commandManager.registerParser(Enchantment.class, (ctx, input) -> {
                String token = input.readString();
                Enchantment enchantment = Enchantment.getByName(token.toUpperCase());
                if (enchantment == null) {
                    return ArgumentParseResult.failure(new IllegalArgumentException("Invalid enchantment: " + token));
                }
                return ArgumentParseResult.success(enchantment);
            });

            commandManager.registerParser(Attribute.class, (ctx, input) -> {
                String token = input.readString();
                Attribute attribute = org.bukkit.Registry.ATTRIBUTE.stream()
                    .filter(value -> value.getKey().getKey().equalsIgnoreCase(token))
                    .findFirst()
                    .orElse(null);
                if (attribute == null) {
                    return ArgumentParseResult.failure(new IllegalArgumentException("Invalid attribute: " + token));
                }
                return ArgumentParseResult.success(attribute);
            });

            commandManager.registerSuggestions("potioneffecttypes", () ->
                Arrays.stream(PotionEffectType.values())
                    .filter(type -> type != null && type.getKey() != null)
                    .map(type -> type.getKey().getKey())
                    .collect(Collectors.toList())
            );

            commandManager.registerSuggestions("potiontypes", () ->
                List.of("splash", "lingering", "normal")
            );

            commandManager.registerSuggestions("amplifierPlaceholder", () -> List.of("<amplifier>"));

            commandManager.registerSuggestions("durationPlaceholder", () ->
                List.of("infinite", "1", "10", "30", "60", "120")
            );

            commandManager.registerSuggestions("materials", () ->
                Arrays.stream(Material.values())
                    .map(m -> m.name().toLowerCase())
                    .collect(Collectors.toList())
            );

            commandManager.registerSuggestions("enchantments", () ->
                Arrays.stream(Enchantment.values())
                    .map(e -> e.getKey().getKey())
                    .collect(Collectors.toList())
            );

            commandManager.registerSuggestions("attributes", () ->
                org.bukkit.Registry.ATTRIBUTE.stream()
                    .map(attribute -> attribute.getKey().getKey())
                    .collect(Collectors.toList())
            );

            commandManager.registerSuggestions("itemflags", () ->
                Arrays.stream(org.bukkit.inventory.ItemFlag.values())
                    .map(flag -> flag.name().toLowerCase())
                    .collect(Collectors.toList())
            );

            commandManager.registerSuggestions("itemRarities", () ->
                List.of("common", "uncommon", "rare", "epic")
            );

            commandManager.registerSuggestions("colors", () ->
                List.of("black", "blue", "aqua", "fuchsia", "gray", "green", "lime", "maroon", "navy",
                    "olive", "orange", "purple", "red", "silver", "teal", "white", "yellow")
            );

            commandManager.registerSuggestions("dyecolors", () ->
                Arrays.stream(org.bukkit.DyeColor.values())
                    .map(color -> color.name().toLowerCase())
                    .collect(Collectors.toList())
            );

            commandManager.registerSuggestions("bannerpatterns", () ->
                org.bukkit.Registry.BANNER_PATTERN.stream()
                    .map(pattern -> org.bukkit.Registry.BANNER_PATTERN.getKeyOrThrow(pattern).getKey())
                    .collect(Collectors.toList())
            );

            commandManager.registerSuggestions("trimmaterials", () ->
                org.bukkit.Registry.TRIM_MATERIAL.stream()
                    .map(material -> org.bukkit.Registry.TRIM_MATERIAL.getKeyOrThrow(material).getKey())
                    .collect(Collectors.toList())
            );

            commandManager.registerSuggestions("trimpatterns", () ->
                org.bukkit.Registry.TRIM_PATTERN.stream()
                    .map(pattern -> org.bukkit.Registry.TRIM_PATTERN.getKeyOrThrow(pattern).getKey())
                    .collect(Collectors.toList())
            );

            commandManager.registerSuggestions("equipmentslotgroups", () -> {
                List<String> groups = new ArrayList<>();
                for (java.lang.reflect.Field field : org.bukkit.inventory.EquipmentSlotGroup.class.getFields()) {
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        groups.add(field.getName().toLowerCase());
                    }
                }
                return groups;
            });

            commandManager.registerSuggestions("players", () ->
                Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .sorted()
                    .collect(Collectors.toList())
            );

            commandManager.registerSuggestions("ptimeOptions", () ->
                List.of("day", "night", "midnight", "dawn", "sunrise", "morning", "noon", "afternoon", "sunset", "dusk",
                    "0", "1000", "6000", "9000", "12000", "18000", "reset")
            );

            commandManager.registerSuggestions("pweatherOptions", () ->
                List.of("clear", "rain", "thunder", "reset")
            );

            commandManager.registerSuggestions("speedOptions", () ->
                List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "reset")
            );

            commandManager.registerSuggestions("speedModes", () ->
                List.of("both", "fly", "walk")
            );

            commandManager.registerSuggestions("repairModes", () ->
                List.of("hand", "all")
            );

            commandManager.registerSuggestions("sudoModes", () ->
                List.of("true", "false", "chat")
            );

            commandManager.registerSuggestions("sudoTargets", () -> {
                List<String> targets = new ArrayList<>();
                targets.add("@a");
                targets.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .sorted()
                    .collect(Collectors.toList()));
                return targets;
            });

            commandManager.registerSuggestions("chatLogTypes", () ->
                List.of("message", "command")
            );

            commandManager.registerSuggestions("chatLogPages", (ctx, input) ->
                java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    List<String> pages = new ArrayList<>();

                    Optional<String> type = ctx.optional("type");
                    Optional<String> playerName = ctx.optional("player");

                    if (type.isPresent() && playerName.isPresent()) {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName.get());
                        if (target.hasPlayedBefore()) {
                            int totalPages = getChatLogService().getTotalPages(target.getUniqueId(), type.get());
                            for (int i = 1; i <= totalPages; i++) {
                                pages.add(String.valueOf(i));
                            }
                        }
                    }

                    return pages.stream().map(org.incendo.cloud.suggestion.Suggestion::suggestion).collect(Collectors.toList());
                })
            );

            commandManager.registerSuggestions("bans", () ->
                Arrays.stream(Bukkit.getBanList(org.bukkit.BanList.Type.NAME).getBanEntries().toArray(new org.bukkit.BanEntry[0]))
                    .map(entry -> entry.getTarget().toString())
                    .collect(Collectors.toList())
            );
        } catch (Exception e) {
            getLogger().severe("Failed to initialize command manager: " + e.getMessage());
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
        godFeature = new GodFeature();
        messagingFeature = new MessagingFeature();
        backFeature = new BackFeature();
        openInvFeature = new OpenInvFeature();
        tempWorkbenchManager = new TempWorkbenchManager(this);
        Bukkit.getPluginManager().registerEvents(tempWorkbenchManager, this);

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
        featureRegistry.register("god", godFeature);
        featureRegistry.register("messaging", messagingFeature);
        featureRegistry.register("back", backFeature);
        featureRegistry.register("openInv", openInvFeature);
        featureRegistry.all().forEach((key, feature) -> feature.register(this));
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AltsListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinSyncListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DisposeListener(), this);
        loadPunishmentReasons();

        try {
            commandManager.registerSuggestions("itemdb", () ->
                getItemDatabaseService().getItemNames().stream().sorted().collect(Collectors.toList())
            );

            commandManager.registerSuggestions("punishReasons", () ->
                new ArrayList<>(punishmentReasons.keySet())
            );

            commandManager.registerSuggestions("kits", () ->
                getKitService().getKitNames().stream().sorted().collect(Collectors.toList())
            );

            commandManager.registerSuggestions("kitGiveTargets", () -> {
                List<String> targets = new ArrayList<>();
                targets.add("all");
                targets.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .sorted()
                    .collect(Collectors.toList()));
                return targets;
            });

            commandManager.registerSuggestions("warp", () -> getWarpService().getWarpNames(false));
            commandManager.registerSuggestions("warpAdmin", () -> getWarpService().getWarpNames(true));

            commandManager.registerSuggestions("worldNames", () -> {
                File[] folders = getServer().getWorldContainer().listFiles();
                List<String> worldNames = new ArrayList<>();

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

            commandManager.registerSuggestions("entitytypes", () ->
                Arrays.stream(org.bukkit.entity.EntityType.values())
                    .filter(et -> et.isSpawnable() && et.isAlive())
                    .map(et -> et.name().toLowerCase())
                    .collect(Collectors.toList())
            );

            commandManager.registerSuggestions("spawnAmounts", () ->
                List.of("1", "10", "25", "50", "100", "250", "500", "1000")
            );

            commandManager.registerSuggestions("durations", (ctx, input) -> {
                String token = input.lastRemainingToken();
                List<String> durations = new ArrayList<>();

                if (token.isEmpty()) {
                    durations.add("1s");
                    durations.add("1m");
                    durations.add("1h");
                    durations.add("1d");
                    durations.add("permanent");
                } else if (token.matches("\\d+")) {
                    durations.add(token + "s");
                    durations.add(token + "m");
                    durations.add(token + "h");
                    durations.add(token + "d");
                }

                return java.util.concurrent.CompletableFuture.completedFuture(
                    durations.stream().map(org.incendo.cloud.suggestion.Suggestion::suggestion).collect(Collectors.toList())
                );
            });

            commandManager.registerSuggestions("none", () -> new ArrayList<>());

            commandManager.registerSuggestions("levelPlaceholder", () -> List.of("<level>"));

            commandManager.registerSuggestions("amountPlaceholder", () -> List.of("<amount>"));

            commandManager.registerSuggestions("reasonPlaceholder", () -> List.of("<reason>"));

            commandManager.registerNamedParser("positiveAmount",
                    org.incendo.cloud.parser.standard.IntegerParser.integerParser(1));

            commandManager.registerNamedParser("nonNegativeAmount",
                    org.incendo.cloud.parser.standard.IntegerParser.integerParser(0));
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
            if (isCommandEnabled("kit")) {
                commandManager.registerCommand(new KitCommand(this));
                commandManager.registerCommand(new GiveKitCommand(this));
            }
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
            commandManager.registerCommand(new SeenCommand(this));
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
            commandManager.registerCommand(new FirstJoinCommand(this));
            commandManager.registerCommand(new CommandSpyCommand(this));
            commandManager.registerCommand(new OpenInventoryCommand(this));
            commandManager.registerCommand(new OpenEnderChestCommand(this));
            commandManager.registerCommand(new EnderChestCommand());
            commandManager.registerCommand(new EnchantmentBookCommand(this));
            commandManager.registerCommand(new GamemodeCreativeCommand(this));
            commandManager.registerCommand(new GamemodeSurvivalCommand(this));
            commandManager.registerCommand(new GamemodeSpectatorCommand(this));
            commandManager.registerCommand(new GamemodeAdventureCommand(this));
            commandManager.registerCommand(new UsageCommand(this));
            commandManager.registerCommand(new GodCommand(this));
            commandManager.registerCommand(new MessageCommand(this));
            commandManager.registerCommand(new ReplyCommand(this));
            commandManager.registerCommand(new MessageSpyCommand(this));

            commandManager.registerCommand(new CraftingTableCommand());
            commandManager.registerCommand(new StonecutterCommand());
            commandManager.registerCommand(new LoomCommand());
            commandManager.registerCommand(new CartographyTableCommand());
            commandManager.registerCommand(new SmithingTableCommand());
            commandManager.registerCommand(new AnvilCommand());
            commandManager.registerCommand(new GrindstoneCommand());
            commandManager.registerCommand(new FurnaceCommand(this));
            commandManager.registerCommand(new BlastFurnaceCommand(this));
            commandManager.registerCommand(new SmokerCommand(this));
            commandManager.registerCommand(new BrewingStandCommand(this));
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
            commandManager.registerCommand(new BackCommand(this));
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

        if (openInvFeature != null) {
            openInvFeature.shutdown(this);
        }

        if (tempWorkbenchManager != null) {
            tempWorkbenchManager.cleanup();
        }

        Bukkit.getScheduler().cancelTasks(this);

        if (databaseManager != null) {
            databaseManager.close();
        }

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

    public SageCommandManager getCommandManager() {
        return commandManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerRuntimeDataManager getPlayerRuntimeDataManager() {
        return playerRuntimeDataManager;
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

    public GodService getGodService() {
        return godFeature == null ? null : godFeature.service();
    }

    public MessagingService getMessagingService() {
        return messagingFeature == null ? null : messagingFeature.service();
    }

    public BackService getBackService() {
        return backFeature == null ? null : backFeature.service();
    }

    public TempWorkbenchManager getTempWorkbenchManager() {
        return tempWorkbenchManager;
    }

    public OpenInventoryService getOpenInventoryService() {
        return openInvFeature == null ? null : openInvFeature.inventoryService();
    }

    public OpenEnderChestService getOpenEnderChestService() {
        return openInvFeature == null ? null : openInvFeature.enderChestService();
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
