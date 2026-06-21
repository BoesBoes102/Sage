package com.boes.sage.features.itemedit.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.boes.sage.Sage;
import com.boes.sage.features.itemedit.ItemEditService;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@CommandAlias("itemedit|ie")
@Description("Edit the item in your main hand")
@CommandPermission("sage.itemedit")
public class ItemEditCommand extends BaseCommand {

    private static final List<ItemFlag> ALL_ITEM_FLAGS = Arrays.asList(ItemFlag.values());

    private final Sage plugin;
    private final ItemEditService service;

    public ItemEditCommand(Sage plugin) {
        this.plugin = plugin;
        this.service = plugin.getItemEditService();
    }

    @Default
    @CatchUnknown
    public void showHelp(Player player) {
        player.sendMessage("§e/itemedit rename <text>");
        player.sendMessage("§e/itemedit lore <add|set|insert|remove|clear|copy|paste>");
        player.sendMessage("§e/itemedit enchant <add|remove|clear>");
        player.sendMessage("§e/itemedit amount <amount>");
        player.sendMessage("§e/itemedit type <material>");
        player.sendMessage("§e/itemedit damage <value>");
        player.sendMessage("§e/itemedit unbreakable <true|false>");
        player.sendMessage("§e/itemedit custommodeldata <value|clear>");
        player.sendMessage("§e/itemedit repaircost <value>");
        player.sendMessage("§e/itemedit flags <add|remove|clear>");
        player.sendMessage("§e/itemedit glow <true|false>");
        player.sendMessage("§e/itemedit color <color>");
        player.sendMessage("§e/itemedit trim <material> <pattern>|clear");
        player.sendMessage("§e/itemedit potion <color|add|remove|clear>");
        player.sendMessage("§e/itemedit firework <power|clear>");
        player.sendMessage("§e/itemedit book <author|title|addpage|setpage|removepage|clearpages>");
        player.sendMessage("§e/itemedit skullowner <player>");
        player.sendMessage("§e/itemedit hidetooltip <true|false>");
        player.sendMessage("§e/itemedit fireresistant <true|false>");
        player.sendMessage("§e/itemedit glider <true|false>");
        player.sendMessage("§e/itemedit rarity <common|uncommon|rare|epic|clear>");
        player.sendMessage("§e/itemedit itemmodel <namespace:key|clear>");
        player.sendMessage("§e/itemedit tooltipstyle <namespace:key|clear>");
        player.sendMessage("§e/itemedit maxstacksize <value|clear>");
        player.sendMessage("§e/itemedit maxdurability <value|clear>");
    }

    @Subcommand("rename")
    @Syntax("[text]")
    public void rename(Player player, @Optional String text) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (text == null || text.isBlank()) {
            meta.setDisplayName(null);
            applyMeta(player, meta, "Cleared the display name.");
            return;
        }
        meta.setDisplayName(service.colorize(text));
        applyMeta(player, meta, "Updated the display name.");
    }

    @Subcommand("amount")
    @CommandCompletion("1|16|32|64")
    public void amount(Player player, int amount) {
        ItemStack item = requireItem(player);
        if (item == null) {
            return;
        }
        if (amount < 1) {
            player.sendMessage("§cAmount must be at least 1.");
            return;
        }
        int maxAmount = item.getType().getMaxStackSize();
        if (amount > maxAmount) {
            player.sendMessage("§cThat material only stacks to " + maxAmount + ".");
            return;
        }
        item.setAmount(amount);
        player.sendMessage("§aUpdated the stack amount to §e" + amount + "§a.");
    }

    @Subcommand("type")
    @CommandCompletion("@materials")
    public void type(Player player, Material material) {
        ItemStack item = requireItem(player);
        if (item == null) {
            return;
        }
        item.setType(material);
        player.sendMessage("§aUpdated the item type to §e" + material.name().toLowerCase(Locale.ROOT) + "§a.");
    }

    @Subcommand("damage")
    @CommandCompletion("0|1|10|100")
    public void damage(Player player, int damage) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!(meta instanceof Damageable damageable)) {
            player.sendMessage("§cThat item cannot store damage.");
            return;
        }
        if (damage < 0) {
            player.sendMessage("§cDamage must be at least 0.");
            return;
        }
        damageable.setDamage(damage);
        applyMeta(player, meta, "Updated the item damage.");
    }

    @Subcommand("repaircost")
    @CommandCompletion("0|1|5|10")
    public void repairCost(Player player, int repairCost) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (repairCost < 0) {
            player.sendMessage("§cRepair cost must be at least 0.");
            return;
        }
        if (!invokeSetter(meta, "setRepairCost", repairCost)) {
            player.sendMessage("§cRepair cost is not supported for that item on this server build.");
            return;
        }
        applyMeta(player, meta, "Updated the repair cost.");
    }

    @Subcommand("unbreakable")
    public void unbreakable(Player player, boolean value) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.setUnbreakable(value);
        applyMeta(player, meta, "Set unbreakable to §e" + value + "§a.");
    }

    @Subcommand("custommodeldata")
    @CommandCompletion("1|100|1000")
    public void customModelData(Player player, int value) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.setCustomModelData(value);
        applyMeta(player, meta, "Updated custom model data.");
    }

    @Subcommand("custommodeldata clear")
    public void clearCustomModelData(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.setCustomModelData(null);
        applyMeta(player, meta, "Cleared custom model data.");
    }

    @Subcommand("itemmodel")
    public void itemModel(Player player, @Single String keyText) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        NamespacedKey key = parseNamespacedKey(player, keyText);
        if (key == null) {
            return;
        }
        if (!invokeSetter(meta, "setItemModel", key)) {
            player.sendMessage("§cItem model is not supported on this server build.");
            return;
        }
        applyMeta(player, meta, "Updated the item model.");
    }

    @Subcommand("itemmodel clear")
    public void clearItemModel(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!invokeSetter(meta, "setItemModel", (Object) null)) {
            player.sendMessage("§cItem model is not supported on this server build.");
            return;
        }
        applyMeta(player, meta, "Cleared the item model.");
    }

    @Subcommand("tooltipstyle")
    public void tooltipStyle(Player player, @Single String keyText) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        NamespacedKey key = parseNamespacedKey(player, keyText);
        if (key == null) {
            return;
        }
        if (!invokeSetter(meta, "setTooltipStyle", key)) {
            player.sendMessage("§cTooltip style is not supported on this server build.");
            return;
        }
        applyMeta(player, meta, "Updated the tooltip style.");
    }

    @Subcommand("tooltipstyle clear")
    public void clearTooltipStyle(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!invokeSetter(meta, "setTooltipStyle", (Object) null)) {
            player.sendMessage("§cTooltip style is not supported on this server build.");
            return;
        }
        applyMeta(player, meta, "Cleared the tooltip style.");
    }

    @Subcommand("rarity")
    public void rarity(Player player, String rarityName) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        Object rarity = resolveEnumConstant("org.bukkit.inventory.ItemRarity", rarityName);
        if (rarity == null) {
            player.sendMessage("§cUnknown rarity. Try common, uncommon, rare, or epic.");
            return;
        }
        if (!invokeSetter(meta, "setRarity", rarity)) {
            player.sendMessage("§cRarity is not supported on this server build.");
            return;
        }
        applyMeta(player, meta, "Updated the item rarity.");
    }

    @Subcommand("rarity clear")
    public void clearRarity(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!invokeSetter(meta, "setRarity", (Object) null)) {
            player.sendMessage("§cRarity is not supported on this server build.");
            return;
        }
        applyMeta(player, meta, "Cleared the item rarity.");
    }

    @Subcommand("maxstacksize")
    public void maxStackSize(Player player, int value) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (value < 1) {
            player.sendMessage("§cMax stack size must be at least 1.");
            return;
        }
        if (!invokeSetter(meta, "setMaxStackSize", value)) {
            player.sendMessage("§cMax stack size is not supported on this server build.");
            return;
        }
        applyMeta(player, meta, "Updated max stack size.");
    }

    @Subcommand("maxstacksize clear")
    public void clearMaxStackSize(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!invokeSetter(meta, "setMaxStackSize", (Object) null)) {
            player.sendMessage("§cMax stack size is not supported on this server build.");
            return;
        }
        applyMeta(player, meta, "Cleared max stack size.");
    }

    @Subcommand("maxdurability")
    public void maxDurability(Player player, int value) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (value < 1) {
            player.sendMessage("§cMax durability must be at least 1.");
            return;
        }
        if (!invokeSetter(meta, "setMaxDamage", value) && !invokeSetter(meta, "setMaxDurability", value)) {
            player.sendMessage("§cMax durability is not supported on this server build.");
            return;
        }
        applyMeta(player, meta, "Updated max durability.");
    }

    @Subcommand("maxdurability clear")
    public void clearMaxDurability(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        boolean success = invokeSetter(meta, "setMaxDamage", (Object) null) || invokeSetter(meta, "setMaxDurability", (Object) null);
        if (!success) {
            player.sendMessage("§cMax durability is not supported on this server build.");
            return;
        }
        applyMeta(player, meta, "Cleared max durability.");
    }

    @Subcommand("hidetooltip")
    public void hideTooltip(Player player, boolean value) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!invokeSetter(meta, "setHideTooltip", value)) {
            player.sendMessage("§cHidden tooltip is not supported on this server build.");
            return;
        }
        applyMeta(player, meta, "Set hide tooltip to §e" + value + "§a.");
    }

    @Subcommand("fireresistant")
    public void fireResistant(Player player, boolean value) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!invokeSetter(meta, "setFireResistant", value)) {
            player.sendMessage("§cFire resistance is not supported on this server build.");
            return;
        }
        applyMeta(player, meta, "Set fire resistant to §e" + value + "§a.");
    }

    @Subcommand("glider")
    public void glider(Player player, boolean value) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!invokeSetter(meta, "setGlider", value)) {
            player.sendMessage("§cGlider is not supported on this server build.");
            return;
        }
        applyMeta(player, meta, "Set glider to §e" + value + "§a.");
    }

    @Subcommand("skullowner")
    public void skullOwner(Player player, String ownerName) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!(meta instanceof SkullMeta skullMeta)) {
            player.sendMessage("§cThat item is not a player head.");
            return;
        }
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(ownerName));
        applyMeta(player, meta, "Updated the skull owner.");
    }

    @Subcommand("glow")
    public void glow(Player player, boolean value) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (value) {
            Enchantment enchantment = Enchantment.UNBREAKING;
            meta.addEnchant(enchantment, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        applyMeta(player, meta, "Set glow to §e" + value + "§a.");
    }

    @Subcommand("enchant add")
    @CommandCompletion("@enchantments 1|2|3|4|5")
    public void addEnchant(Player player, Enchantment enchantment, int level) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (level < 1) {
            player.sendMessage("§cLevel must be at least 1.");
            return;
        }
        meta.addEnchant(enchantment, level, true);
        applyMeta(player, meta, "Added the enchantment.");
    }

    @Subcommand("enchant remove")
    @CommandCompletion("@enchantments")
    public void removeEnchant(Player player, Enchantment enchantment) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.removeEnchant(enchantment);
        applyMeta(player, meta, "Removed the enchantment.");
    }

    @Subcommand("enchant clear")
    public void clearEnchants(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        Collection<Enchantment> enchants = new ArrayList<>(meta.getEnchants().keySet());
        for (Enchantment enchantment : enchants) {
            meta.removeEnchant(enchantment);
        }
        applyMeta(player, meta, "Cleared all enchantments.");
    }

    @Subcommand("flags add")
    public void addFlag(Player player, String flagName) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        ItemFlag flag = parseItemFlag(flagName);
        if (flag == null) {
            player.sendMessage("§cUnknown item flag.");
            return;
        }
        meta.addItemFlags(flag);
        applyMeta(player, meta, "Added item flag §e" + flag.name() + "§a.");
    }

    @Subcommand("flags remove")
    public void removeFlag(Player player, String flagName) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        ItemFlag flag = parseItemFlag(flagName);
        if (flag == null) {
            player.sendMessage("§cUnknown item flag.");
            return;
        }
        meta.removeItemFlags(flag);
        applyMeta(player, meta, "Removed item flag §e" + flag.name() + "§a.");
    }

    @Subcommand("flags clear")
    public void clearFlags(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.removeItemFlags(ALL_ITEM_FLAGS.toArray(new ItemFlag[0]));
        applyMeta(player, meta, "Cleared all item flags.");
    }

    @Subcommand("hideall")
    public void hideAll(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.addItemFlags(ALL_ITEM_FLAGS.toArray(new ItemFlag[0]));
        applyMeta(player, meta, "Applied all item flags.");
    }

    @Subcommand("attribute add")
    @Syntax("<attribute> <amount> <slot>")
    @CommandCompletion("@attributes 1|2|5|10 hand|off_hand|armor|body|any")
    public void addAttribute(Player player, Attribute attribute, double amount, String slotName) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        EquipmentSlotGroup slotGroup = resolveEquipmentSlotGroup(slotName);
        if (slotGroup == null) {
            player.sendMessage("§cUnknown slot group.");
            return;
        }
        String keyName = "itemedit_" + attribute.name().toLowerCase(Locale.ROOT) + "_" + UUID.randomUUID().toString().replace("-", "");
        AttributeModifier modifier = new AttributeModifier(NamespacedKey.minecraft(keyName), amount, AttributeModifier.Operation.ADD_NUMBER, slotGroup);
        meta.addAttributeModifier(attribute, modifier);
        applyMeta(player, meta, "Added the attribute modifier.");
    }

    @Subcommand("attribute clear")
    public void clearAttributes(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        for (Attribute attribute : Attribute.values()) {
            meta.removeAttributeModifier(attribute);
        }
        applyMeta(player, meta, "Cleared attribute modifiers.");
    }

    @Subcommand("lore add")
    public void loreAdd(Player player, String text) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        List<String> lore = getLore(meta);
        lore.add(service.colorize(text));
        meta.setLore(lore);
        applyMeta(player, meta, "Added a lore line.");
    }

    @Subcommand("lore set")
    public void loreSet(Player player, int line, String text) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        List<String> lore = getLore(meta);
        int index = line - 1;
        if (index < 0 || index >= lore.size()) {
            player.sendMessage("§cThat lore line does not exist.");
            return;
        }
        lore.set(index, service.colorize(text));
        meta.setLore(lore);
        applyMeta(player, meta, "Updated lore line §e" + line + "§a.");
    }

    @Subcommand("lore insert")
    public void loreInsert(Player player, int line, String text) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        List<String> lore = getLore(meta);
        int index = Math.max(0, Math.min(line - 1, lore.size()));
        lore.add(index, service.colorize(text));
        meta.setLore(lore);
        applyMeta(player, meta, "Inserted lore line.");
    }

    @Subcommand("lore remove")
    public void loreRemove(Player player, int line) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        List<String> lore = getLore(meta);
        int index = line - 1;
        if (index < 0 || index >= lore.size()) {
            player.sendMessage("§cThat lore line does not exist.");
            return;
        }
        lore.remove(index);
        meta.setLore(lore.isEmpty() ? null : lore);
        applyMeta(player, meta, "Removed lore line §e" + line + "§a.");
    }

    @Subcommand("lore clear")
    public void loreClear(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.setLore(null);
        applyMeta(player, meta, "Cleared the lore.");
    }

    @Subcommand("lore copy")
    public void loreCopy(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        service.setLoreClipboard(player.getUniqueId(), getLore(meta));
        player.sendMessage("§aCopied the lore to your clipboard.");
    }

    @Subcommand("lore paste")
    public void lorePaste(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        List<String> copied = service.getLoreClipboard(player.getUniqueId());
        if (copied == null) {
            player.sendMessage("§cYou do not have copied lore.");
            return;
        }
        meta.setLore(copied.isEmpty() ? null : copied);
        applyMeta(player, meta, "Pasted the copied lore.");
    }

    @Subcommand("book author")
    public void bookAuthor(Player player, String author) {
        BookMeta meta = requireBookMeta(player);
        if (meta == null) {
            return;
        }
        meta.setAuthor(service.colorize(author));
        applyMeta(player, meta, "Updated the book author.");
    }

    @Subcommand("book title")
    public void bookTitle(Player player, String title) {
        BookMeta meta = requireBookMeta(player);
        if (meta == null) {
            return;
        }
        meta.setTitle(service.colorize(title));
        applyMeta(player, meta, "Updated the book title.");
    }

    @Subcommand("book addpage")
    public void bookAddPage(Player player, String pageText) {
        BookMeta meta = requireBookMeta(player);
        if (meta == null) {
            return;
        }
        meta.addPage(service.colorize(pageText));
        applyMeta(player, meta, "Added a book page.");
    }

    @Subcommand("book setpage")
    public void bookSetPage(Player player, int page, String pageText) {
        BookMeta meta = requireBookMeta(player);
        if (meta == null) {
            return;
        }
        if (page < 1 || page > meta.getPageCount()) {
            player.sendMessage("§cThat page does not exist.");
            return;
        }
        meta.setPage(page, service.colorize(pageText));
        applyMeta(player, meta, "Updated book page §e" + page + "§a.");
    }

    @Subcommand("book removepage")
    public void bookRemovePage(Player player, int page) {
        BookMeta meta = requireBookMeta(player);
        if (meta == null) {
            return;
        }
        if (page < 1 || page > meta.getPageCount()) {
            player.sendMessage("§cThat page does not exist.");
            return;
        }
        List<String> pages = new ArrayList<>(meta.getPages());
        pages.remove(page - 1);
        meta.setPages(pages);
        applyMeta(player, meta, "Removed book page §e" + page + "§a.");
    }

    @Subcommand("book clearpages")
    public void bookClearPages(Player player) {
        BookMeta meta = requireBookMeta(player);
        if (meta == null) {
            return;
        }
        meta.setPages(new ArrayList<>());
        applyMeta(player, meta, "Cleared all book pages.");
    }

    @Subcommand("color")
    public void color(Player player, String colorText) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        Color color = parseColor(player, colorText);
        if (color == null) {
            return;
        }
        if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
            leatherArmorMeta.setColor(color);
            applyMeta(player, meta, "Updated leather armor color.");
            return;
        }
        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.setColor(color);
            applyMeta(player, meta, "Updated potion color.");
            return;
        }
        if (meta instanceof FireworkMeta fireworkMeta) {
            fireworkMeta.addEffect(org.bukkit.FireworkEffect.builder().withColor(color).build());
            applyMeta(player, meta, "Added a firework color.");
            return;
        }
        player.sendMessage("§cThat item does not support colors.");
    }

    @Subcommand("banner add")
    public void bannerAdd(Player player, String dyeColorName, String patternName) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!(meta instanceof BannerMeta bannerMeta)) {
            player.sendMessage("§cThat item is not a banner.");
            return;
        }
        DyeColor dyeColor;
        PatternType patternType;
        try {
            dyeColor = DyeColor.valueOf(dyeColorName.toUpperCase(Locale.ROOT));
            patternType = PatternType.valueOf(patternName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            player.sendMessage("§cUnknown banner color or pattern.");
            return;
        }
        bannerMeta.addPattern(new Pattern(dyeColor, patternType));
        applyMeta(player, meta, "Added the banner pattern.");
    }

    @Subcommand("banner clear")
    public void bannerClear(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!(meta instanceof BannerMeta bannerMeta)) {
            player.sendMessage("§cThat item is not a banner.");
            return;
        }
        bannerMeta.setPatterns(new ArrayList<>());
        applyMeta(player, meta, "Cleared banner patterns.");
    }

    @Subcommand("trim")
    public void trim(Player player, String materialName, String patternName) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!invokeTrim(meta, materialName, patternName)) {
            player.sendMessage("§cThat item does not support armor trims or the trim values were invalid.");
            return;
        }
        applyMeta(player, meta, "Updated the armor trim.");
    }

    @Subcommand("trim clear")
    public void trimClear(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!invokeSetter(meta, "setTrim", (Object) null)) {
            player.sendMessage("§cThat item does not support armor trims.");
            return;
        }
        applyMeta(player, meta, "Cleared the armor trim.");
    }

    @Subcommand("potion color")
    public void potionColor(Player player, String colorText) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!(meta instanceof PotionMeta potionMeta)) {
            player.sendMessage("§cThat item is not a potion.");
            return;
        }
        Color color = parseColor(player, colorText);
        if (color == null) {
            return;
        }
        potionMeta.setColor(color);
        applyMeta(player, meta, "Updated the potion color.");
    }

    @Subcommand("potion add")
    @CommandCompletion("@potioneffecttypes 200 1 true|false true|false true|false")
    public void potionAdd(Player player, PotionEffectType type, int durationTicks, int amplifier,
                          @Optional Boolean ambient, @Optional Boolean particles, @Optional Boolean icon) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!(meta instanceof PotionMeta potionMeta)) {
            player.sendMessage("§cThat item is not a potion.");
            return;
        }
        PotionEffect effect = new PotionEffect(type, durationTicks, amplifier,
            ambient != null && ambient,
            particles == null || particles,
            icon == null || icon);
        potionMeta.addCustomEffect(effect, true);
        applyMeta(player, meta, "Added the potion effect.");
    }

    @Subcommand("potion remove")
    @CommandCompletion("@potioneffecttypes")
    public void potionRemove(Player player, PotionEffectType type) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!(meta instanceof PotionMeta potionMeta)) {
            player.sendMessage("§cThat item is not a potion.");
            return;
        }
        potionMeta.removeCustomEffect(type);
        applyMeta(player, meta, "Removed the potion effect.");
    }

    @Subcommand("potion clear")
    public void potionClear(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!(meta instanceof PotionMeta potionMeta)) {
            player.sendMessage("§cThat item is not a potion.");
            return;
        }
        for (PotionEffect effect : new ArrayList<>(potionMeta.getCustomEffects())) {
            potionMeta.removeCustomEffect(effect.getType());
        }
        potionMeta.setColor(null);
        applyMeta(player, meta, "Cleared potion edits.");
    }

    @Subcommand("firework power")
    public void fireworkPower(Player player, int power) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!(meta instanceof FireworkMeta fireworkMeta)) {
            player.sendMessage("§cThat item is not a firework.");
            return;
        }
        if (power < 0 || power > 127) {
            player.sendMessage("§cPower must be between 0 and 127.");
            return;
        }
        fireworkMeta.setPower(power);
        applyMeta(player, meta, "Updated the firework power.");
    }

    @Subcommand("firework clear")
    public void fireworkClear(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!(meta instanceof FireworkMeta fireworkMeta)) {
            player.sendMessage("§cThat item is not a firework.");
            return;
        }
        fireworkMeta.clearEffects();
        fireworkMeta.setPower(0);
        applyMeta(player, meta, "Cleared firework data.");
    }

    private boolean invokeTrim(ItemMeta meta, String materialName, String patternName) {
        try {
            TrimMaterial trimMaterial = Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(materialName.toLowerCase(Locale.ROOT)));
            TrimPattern trimPattern = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(patternName.toLowerCase(Locale.ROOT)));
            if (trimMaterial == null || trimPattern == null) {
                return false;
            }
            return invokeSetter(meta, "setTrim", new ArmorTrim(trimMaterial, trimPattern));
        } catch (Throwable throwable) {
            return false;
        }
    }

    private BookMeta requireBookMeta(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return null;
        }
        if (!(meta instanceof BookMeta bookMeta)) {
            player.sendMessage("§cThat item is not a writable or written book.");
            return null;
        }
        return bookMeta;
    }

    private ItemFlag parseItemFlag(String flagName) {
        try {
            return ItemFlag.valueOf(flagName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private List<String> getLore(ItemMeta meta) {
        return meta.hasLore() && meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
    }

    private NamespacedKey parseNamespacedKey(Player player, String text) {
        if (!text.contains(":")) {
            player.sendMessage("§cUse the format namespace:key.");
            return null;
        }
        String[] parts = text.split(":", 2);
        if (parts[0].isBlank() || parts[1].isBlank()) {
            player.sendMessage("§cUse the format namespace:key.");
            return null;
        }
        return new NamespacedKey(parts[0].toLowerCase(Locale.ROOT), parts[1].toLowerCase(Locale.ROOT));
    }

    private Color parseColor(Player player, String colorText) {
        String text = colorText.trim();
        if (text.startsWith("#")) {
            try {
                return Color.fromRGB(Integer.parseInt(text.substring(1), 16));
            } catch (IllegalArgumentException exception) {
                player.sendMessage("§cInvalid hex color.");
                return null;
            }
        }
        switch (text.toLowerCase(Locale.ROOT)) {
            case "black": return Color.BLACK;
            case "blue": return Color.BLUE;
            case "aqua": return Color.AQUA;
            case "fuchsia": return Color.FUCHSIA;
            case "gray": return Color.GRAY;
            case "green": return Color.GREEN;
            case "lime": return Color.LIME;
            case "maroon": return Color.MAROON;
            case "navy": return Color.NAVY;
            case "olive": return Color.OLIVE;
            case "orange": return Color.ORANGE;
            case "purple": return Color.PURPLE;
            case "red": return Color.RED;
            case "silver": return Color.SILVER;
            case "teal": return Color.TEAL;
            case "white": return Color.WHITE;
            case "yellow": return Color.YELLOW;
            default:
                player.sendMessage("§cUnknown color. Use a named color or #RRGGBB.");
                return null;
        }
    }

    private Object resolveEnumConstant(String className, String value) {
        try {
            Class<?> type = Class.forName(className);
            Object[] constants = type.getEnumConstants();
            if (constants == null) {
                return null;
            }
            for (Object constant : constants) {
                Enum<?> enumConstant = (Enum<?>) constant;
                if (enumConstant.name().equalsIgnoreCase(value)) {
                    return constant;
                }
            }
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    private EquipmentSlotGroup resolveEquipmentSlotGroup(String value) {
        for (java.lang.reflect.Field field : EquipmentSlotGroup.class.getFields()) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!field.getName().equalsIgnoreCase(value)) {
                continue;
            }
            try {
                Object resolved = field.get(null);
                if (resolved instanceof EquipmentSlotGroup group) {
                    return group;
                }
            } catch (IllegalAccessException ignored) {
                return null;
            }
        }
        return null;
    }

    private boolean invokeSetter(Object target, String methodName, Object value) {
        Method[] methods = target.getClass().getMethods();
        for (Method method : methods) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                continue;
            }
            Class<?> parameterType = method.getParameterTypes()[0];
            if (value == null) {
                if (parameterType.isPrimitive()) {
                    continue;
                }
                try {
                    method.invoke(target, new Object[] { null });
                    return true;
                } catch (ReflectiveOperationException ignored) {
                    return false;
                }
            }
            if (!isCompatible(parameterType, value.getClass())) {
                continue;
            }
            try {
                method.invoke(target, value);
                return true;
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }
        return false;
    }

    private boolean isCompatible(Class<?> parameterType, Class<?> valueType) {
        if (parameterType.isAssignableFrom(valueType)) {
            return true;
        }
        if (!parameterType.isPrimitive()) {
            return false;
        }
        return (parameterType == int.class && valueType == Integer.class)
            || (parameterType == boolean.class && valueType == Boolean.class)
            || (parameterType == double.class && valueType == Double.class)
            || (parameterType == float.class && valueType == Float.class)
            || (parameterType == long.class && valueType == Long.class);
    }

    private ItemStack requireItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cYou must hold an item in your main hand.");
            return null;
        }
        return item;
    }

    private ItemMeta requireMeta(Player player) {
        ItemStack item = requireItem(player);
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage("§cThat item does not have editable meta.");
            return null;
        }
        return meta;
    }

    private void applyMeta(Player player, ItemMeta meta, String message) {
        ItemStack item = player.getInventory().getItemInMainHand();
        item.setItemMeta(meta);
        player.sendMessage("§a" + Objects.requireNonNull(message));
    }
}
