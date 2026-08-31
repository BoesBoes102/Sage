package com.boes.sage.features.itemedit.commands;

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
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Permission("sage.itemedit")
public class ItemEditCommand {

    private static final List<ItemFlag> ALL_ITEM_FLAGS = Arrays.asList(ItemFlag.values());

    private final Sage plugin;
    private final ItemEditService service;

    public ItemEditCommand(Sage plugin) {
        this.plugin = plugin;
        this.service = plugin.getItemEditService();
    }

    @Command("itemedit")
    @Command("ie")
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

    @Command("itemedit rename [text]")
    @Command("ie rename [text]")
    public void rename(Player player, @Argument("text") String text) {
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

    @Command("itemedit amount <amount>")
    @Command("ie amount <amount>")
    public void amount(Player player, @Argument("amount") int amount) {
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

    @Command("itemedit type <material>")
    @Command("ie type <material>")
    public void type(Player player, @Argument(value = "material", suggestions = "materials") Material material) {
        ItemStack item = requireItem(player);
        if (item == null) {
            return;
        }
        item.setType(material);
        player.sendMessage("§aUpdated the item type to §e" + material.name().toLowerCase(Locale.ROOT) + "§a.");
    }

    @Command("itemedit damage <damage>")
    @Command("ie damage <damage>")
    public void damage(Player player, @Argument("damage") int damage) {
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

    @Command("itemedit repaircost <repaircost>")
    @Command("ie repaircost <repaircost>")
    public void repairCost(Player player, @Argument("repaircost") int repairCost) {
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

    @Command("itemedit unbreakable <value>")
    @Command("ie unbreakable <value>")
    public void unbreakable(Player player, @Argument("value") boolean value) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.setUnbreakable(value);
        applyMeta(player, meta, "Set unbreakable to §e" + value + "§a.");
    }

    @Command("itemedit custommodeldata <value>")
    @Command("ie custommodeldata <value>")
    public void customModelData(Player player, @Argument("value") int value) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.setCustomModelData(value);
        applyMeta(player, meta, "Updated custom model data.");
    }

    @Command("itemedit custommodeldata clear")
    @Command("ie custommodeldata clear")
    public void clearCustomModelData(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.setCustomModelData(null);
        applyMeta(player, meta, "Cleared custom model data.");
    }

    @Command("itemedit itemmodel <key>")
    @Command("ie itemmodel <key>")
    public void itemModel(Player player, @Argument("key") String keyText) {
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

    @Command("itemedit itemmodel clear")
    @Command("ie itemmodel clear")
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

    @Command("itemedit tooltipstyle <key>")
    @Command("ie tooltipstyle <key>")
    public void tooltipStyle(Player player, @Argument("key") String keyText) {
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

    @Command("itemedit tooltipstyle clear")
    @Command("ie tooltipstyle clear")
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

    @Command("itemedit rarity <rarity>")
    @Command("ie rarity <rarity>")
    public void rarity(Player player, @Argument(value = "rarity", suggestions = "itemRarities") String rarityName) {
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

    @Command("itemedit rarity clear")
    @Command("ie rarity clear")
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

    @Command("itemedit maxstacksize <value>")
    @Command("ie maxstacksize <value>")
    public void maxStackSize(Player player, @Argument("value") int value) {
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

    @Command("itemedit maxstacksize clear")
    @Command("ie maxstacksize clear")
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

    @Command("itemedit maxdurability <value>")
    @Command("ie maxdurability <value>")
    public void maxDurability(Player player, @Argument("value") int value) {
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

    @Command("itemedit maxdurability clear")
    @Command("ie maxdurability clear")
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

    @Command("itemedit hidetooltip <value>")
    @Command("ie hidetooltip <value>")
    public void hideTooltip(Player player, @Argument("value") boolean value) {
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

    @Command("itemedit fireresistant <value>")
    @Command("ie fireresistant <value>")
    public void fireResistant(Player player, @Argument("value") boolean value) {
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

    @Command("itemedit glider <value>")
    @Command("ie glider <value>")
    public void glider(Player player, @Argument("value") boolean value) {
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

    @Command("itemedit skullowner <owner>")
    @Command("ie skullowner <owner>")
    public void skullOwner(Player player, @Argument(value = "owner", suggestions = "players") String ownerName) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        if (!(meta instanceof SkullMeta)) {
            player.sendMessage("§cThat item is not a player head.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            org.bukkit.OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerName);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }
                ItemMeta currentMeta = requireMeta(player);
                if (!(currentMeta instanceof SkullMeta currentSkullMeta)) {
                    player.sendMessage("§cThat item is not a player head.");
                    return;
                }
                currentSkullMeta.setOwningPlayer(owner);
                applyMeta(player, currentSkullMeta, "Updated the skull owner.");
            });
        });
    }

    @Command("itemedit glow <value>")
    @Command("ie glow <value>")
    public void glow(Player player, @Argument("value") boolean value) {
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

    @Command("itemedit enchant add <enchantment> <level>")
    @Command("ie enchant add <enchantment> <level>")
    public void addEnchant(
            Player player,
            @Argument(value = "enchantment", suggestions = "enchantments") Enchantment enchantment,
            @Argument("level") int level
    ) {
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

    @Command("itemedit enchant remove <enchantment>")
    @Command("ie enchant remove <enchantment>")
    public void removeEnchant(Player player, @Argument(value = "enchantment", suggestions = "enchantments") Enchantment enchantment) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.removeEnchant(enchantment);
        applyMeta(player, meta, "Removed the enchantment.");
    }

    @Command("itemedit enchant clear")
    @Command("ie enchant clear")
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

    @Command("itemedit flags add <flag>")
    @Command("ie flags add <flag>")
    public void addFlag(Player player, @Argument(value = "flag", suggestions = "itemflags") String flagName) {
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

    @Command("itemedit flags remove <flag>")
    @Command("ie flags remove <flag>")
    public void removeFlag(Player player, @Argument(value = "flag", suggestions = "itemflags") String flagName) {
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

    @Command("itemedit flags clear")
    @Command("ie flags clear")
    public void clearFlags(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.removeItemFlags(ALL_ITEM_FLAGS.toArray(new ItemFlag[0]));
        applyMeta(player, meta, "Cleared all item flags.");
    }

    @Command("itemedit hideall")
    @Command("ie hideall")
    public void hideAll(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.addItemFlags(ALL_ITEM_FLAGS.toArray(new ItemFlag[0]));
        applyMeta(player, meta, "Applied all item flags.");
    }

    @Command("itemedit attribute add <attribute> <amount> <slot>")
    @Command("ie attribute add <attribute> <amount> <slot>")
    public void addAttribute(
            Player player,
            @Argument(value = "attribute", suggestions = "attributes") Attribute attribute,
            @Argument("amount") double amount,
            @Argument(value = "slot", suggestions = "equipmentslotgroups") String slotName
    ) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        EquipmentSlotGroup slotGroup = resolveEquipmentSlotGroup(slotName);
        if (slotGroup == null) {
            player.sendMessage("§cUnknown slot group.");
            return;
        }
        String keyName = "itemedit_" + attribute.getKey().getKey().toLowerCase(Locale.ROOT) + "_" + UUID.randomUUID().toString().replace("-", "");
        AttributeModifier modifier = new AttributeModifier(NamespacedKey.minecraft(keyName), amount, AttributeModifier.Operation.ADD_NUMBER, slotGroup);
        meta.addAttributeModifier(attribute, modifier);
        applyMeta(player, meta, "Added the attribute modifier.");
    }

    @Command("itemedit attribute clear")
    @Command("ie attribute clear")
    public void clearAttributes(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        for (Attribute attribute : org.bukkit.Registry.ATTRIBUTE) {
            meta.removeAttributeModifier(attribute);
        }
        applyMeta(player, meta, "Cleared attribute modifiers.");
    }

    @Command("itemedit lore add <text>")
    @Command("ie lore add <text>")
    public void loreAdd(Player player, @Argument("text") String text) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        List<String> lore = getLore(meta);
        lore.add(service.colorize(text));
        meta.setLore(lore);
        applyMeta(player, meta, "Added a lore line.");
    }

    @Command("itemedit lore set <line> <text>")
    @Command("ie lore set <line> <text>")
    public void loreSet(Player player, @Argument("line") int line, @Argument("text") String text) {
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

    @Command("itemedit lore insert <line> <text>")
    @Command("ie lore insert <line> <text>")
    public void loreInsert(Player player, @Argument("line") int line, @Argument("text") String text) {
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

    @Command("itemedit lore remove <line>")
    @Command("ie lore remove <line>")
    public void loreRemove(Player player, @Argument("line") int line) {
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

    @Command("itemedit lore clear")
    @Command("ie lore clear")
    public void loreClear(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        meta.setLore(null);
        applyMeta(player, meta, "Cleared the lore.");
    }

    @Command("itemedit lore copy")
    @Command("ie lore copy")
    public void loreCopy(Player player) {
        ItemMeta meta = requireMeta(player);
        if (meta == null) {
            return;
        }
        service.setLoreClipboard(player.getUniqueId(), getLore(meta));
        player.sendMessage("§aCopied the lore to your clipboard.");
    }

    @Command("itemedit lore paste")
    @Command("ie lore paste")
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

    @Command("itemedit book author <text>")
    @Command("ie book author <text>")
    public void bookAuthor(Player player, @Argument("text") String author) {
        BookMeta meta = requireBookMeta(player);
        if (meta == null) {
            return;
        }
        meta.setAuthor(service.colorize(author));
        applyMeta(player, meta, "Updated the book author.");
    }

    @Command("itemedit book title <text>")
    @Command("ie book title <text>")
    public void bookTitle(Player player, @Argument("text") String title) {
        BookMeta meta = requireBookMeta(player);
        if (meta == null) {
            return;
        }
        meta.setTitle(service.colorize(title));
        applyMeta(player, meta, "Updated the book title.");
    }

    @Command("itemedit book addpage <text>")
    @Command("ie book addpage <text>")
    public void bookAddPage(Player player, @Argument("text") String pageText) {
        BookMeta meta = requireBookMeta(player);
        if (meta == null) {
            return;
        }
        meta.addPage(service.colorize(pageText));
        applyMeta(player, meta, "Added a book page.");
    }

    @Command("itemedit book setpage <page> <text>")
    @Command("ie book setpage <page> <text>")
    public void bookSetPage(Player player, @Argument("page") int page, @Argument("text") String pageText) {
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

    @Command("itemedit book removepage <page>")
    @Command("ie book removepage <page>")
    public void bookRemovePage(Player player, @Argument("page") int page) {
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

    @Command("itemedit book clearpages")
    @Command("ie book clearpages")
    public void bookClearPages(Player player) {
        BookMeta meta = requireBookMeta(player);
        if (meta == null) {
            return;
        }
        meta.setPages(new ArrayList<>());
        applyMeta(player, meta, "Cleared all book pages.");
    }

    @Command("itemedit color <color>")
    @Command("ie color <color>")
    public void color(Player player, @Argument(value = "color", suggestions = "colors") String colorText) {
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

    @Command("itemedit banner add <color> <pattern>")
    @Command("ie banner add <color> <pattern>")
    public void bannerAdd(
            Player player,
            @Argument(value = "color", suggestions = "dyecolors") String dyeColorName,
            @Argument(value = "pattern", suggestions = "bannerpatterns") String patternName
    ) {
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
        } catch (IllegalArgumentException exception) {
            player.sendMessage("§cUnknown banner color or pattern.");
            return;
        }
        patternType = org.bukkit.Registry.BANNER_PATTERN.get(NamespacedKey.minecraft(patternName.toLowerCase(Locale.ROOT)));
        if (patternType == null) {
            player.sendMessage("§cUnknown banner color or pattern.");
            return;
        }
        bannerMeta.addPattern(new Pattern(dyeColor, patternType));
        applyMeta(player, meta, "Added the banner pattern.");
    }

    @Command("itemedit banner clear")
    @Command("ie banner clear")
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

    @Command("itemedit trim <material> <pattern>")
    @Command("ie trim <material> <pattern>")
    public void trim(
            Player player,
            @Argument(value = "material", suggestions = "trimmaterials") String materialName,
            @Argument(value = "pattern", suggestions = "trimpatterns") String patternName
    ) {
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

    @Command("itemedit trim clear")
    @Command("ie trim clear")
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

    @Command("itemedit potion color <color>")
    @Command("ie potion color <color>")
    public void potionColor(Player player, @Argument(value = "color", suggestions = "colors") String colorText) {
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

    @Command("itemedit potion add <type> <duration> <amplifier> [ambient] [particles] [icon]")
    @Command("ie potion add <type> <duration> <amplifier> [ambient] [particles] [icon]")
    public void potionAdd(
            Player player,
            @Argument(value = "type", suggestions = "potioneffecttypes") PotionEffectType type,
            @Argument("duration") int durationTicks,
            @Argument("amplifier") int amplifier,
            @Argument("ambient") Boolean ambient,
            @Argument("particles") Boolean particles,
            @Argument("icon") Boolean icon
    ) {
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

    @Command("itemedit potion remove <type>")
    @Command("ie potion remove <type>")
    public void potionRemove(Player player, @Argument(value = "type", suggestions = "potioneffecttypes") PotionEffectType type) {
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

    @Command("itemedit potion clear")
    @Command("ie potion clear")
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

    @Command("itemedit firework power <power>")
    @Command("ie firework power <power>")
    public void fireworkPower(Player player, @Argument("power") int power) {
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

    @Command("itemedit firework clear")
    @Command("ie firework clear")
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
