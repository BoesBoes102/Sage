package com.boes.sage.managers;

import com.boes.sage.Sage;
import com.boes.sage.Utils.JsonStorageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class KitManager {
    private final JsonStorageManager storageManager;
    private final Map<UUID, Long> kitCooldowns;

    public KitManager(Sage plugin) {
        this.storageManager = new JsonStorageManager(new File(plugin.getDataFolder(), "kits.json"));
        this.kitCooldowns = new HashMap<>();
        loadCooldowns();
    }

    public void createKit(String name, String duration, ItemStack[] items) {
        createKit(name, duration, items, new ItemStack[4], null);
    }

    public void createKit(String name, String duration, ItemStack[] items, ItemStack[] armor, ItemStack offhand) {
        JsonObject json = storageManager.load();
        JsonObject kitObj = new JsonObject();
        kitObj.addProperty("duration", duration);
        
        List<ItemStack> itemList = new ArrayList<>(Arrays.asList(items));
        itemList.removeIf(Objects::isNull);
        kitObj.add("items", serializeItemStacks(itemList.toArray(new ItemStack[0])));

        List<ItemStack> armorList = new ArrayList<>(Arrays.asList(armor));
        armorList.removeIf(Objects::isNull);
        kitObj.add("armor", serializeItemStacks(armorList.toArray(new ItemStack[0])));

        if (offhand != null && !offhand.getType().isAir()) {
            kitObj.add("offhand", serializeItemStacks(new ItemStack[]{offhand}));
        }
        
        json.add(name.toLowerCase(), kitObj);
        storageManager.save(json);
    }

    public void deleteKit(String name) {
        JsonObject json = storageManager.load();
        json.remove(name.toLowerCase());
        storageManager.save(json);
    }

    public boolean kitExists(String name) {
        return storageManager.load().has(name.toLowerCase());
    }

    public Set<String> getKitNames() {
        JsonObject json = storageManager.load();
        Set<String> names = new HashSet<>();
        for (String key : json.keySet()) {
            if (!key.equals("cooldowns")) {
                names.add(key);
            }
        }
        return names;
    }

    public String getKitDuration(String name) {
        JsonObject json = storageManager.load();
        if (json.has(name.toLowerCase())) {
            return json.getAsJsonObject(name.toLowerCase()).get("duration").getAsString();
        }
        return "0";
    }

    public List<ItemStack> getKitItems(String name) {
        JsonObject json = storageManager.load();
        if (json.has(name.toLowerCase())) {
            JsonArray itemsArray = json.getAsJsonObject(name.toLowerCase()).getAsJsonArray("items");
            return deserializeItemStacks(itemsArray);
        }
        return new ArrayList<>();
    }

    public ItemStack[] getKitItemsArray(String name) {
        List<ItemStack> items = getKitItems(name);
        return items.toArray(new ItemStack[0]);
    }

    public List<ItemStack> getKitArmor(String name) {
        JsonObject json = storageManager.load();
        if (json.has(name.toLowerCase())) {
            JsonObject kitObj = json.getAsJsonObject(name.toLowerCase());
            if (kitObj.has("armor")) {
                JsonArray armorArray = kitObj.getAsJsonArray("armor");
                return deserializeItemStacks(armorArray);
            }
        }
        return new ArrayList<>();
    }

    public ItemStack[] getKitArmorArray(String name) {
        List<ItemStack> armor = getKitArmor(name);
        ItemStack[] result = new ItemStack[4];
        for (int i = 0; i < Math.min(armor.size(), 4); i++) {
            result[i] = armor.get(i);
        }
        return result;
    }

    public ItemStack getKitOffhand(String name) {
        JsonObject json = storageManager.load();
        if (json.has(name.toLowerCase())) {
            JsonObject kitObj = json.getAsJsonObject(name.toLowerCase());
            if (kitObj.has("offhand")) {
                JsonArray offhandArray = kitObj.getAsJsonArray("offhand");
                List<ItemStack> offhandList = deserializeItemStacks(offhandArray);
                if (!offhandList.isEmpty()) {
                    return offhandList.get(0);
                }
            }
        }
        return null;
    }

    public void saveKitItems(String name, ItemStack[] items) {
        JsonObject json = storageManager.load();
        JsonObject kitObj = json.getAsJsonObject(name.toLowerCase());
        if (kitObj == null) {
            kitObj = new JsonObject();
        }
        
        List<ItemStack> itemList = new ArrayList<>(Arrays.asList(items));
        itemList.removeIf(Objects::isNull);
        kitObj.add("items", serializeItemStacks(itemList.toArray(new ItemStack[0])));
        
        json.add(name.toLowerCase(), kitObj);
        storageManager.save(json);
    }
    
    private JsonArray serializeItemStacks(ItemStack[] items) {
        JsonArray array = new JsonArray();
        for (ItemStack item : items) {
            if (item != null && item.getType().isItem()) {
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
                    dataOutput.writeObject(item);
                    dataOutput.close();
                    array.add(Base64Coder.encodeLines(outputStream.toByteArray()));
                } catch (IOException ignored) {
                }
            }
        }
        return array;
    }
    
    private List<ItemStack> deserializeItemStacks(JsonArray array) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            try {
                byte[] decoded = Base64Coder.decodeLines(array.get(i).getAsString());
                ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded);
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                items.add((ItemStack) dataInput.readObject());
                dataInput.close();
            } catch (IOException | ClassNotFoundException ignored) {
            }
        }
        return items;
    }

    public long parseDuration(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return 0;
        }

        String regex = "(\\d+)\\s*([smhd])";
        long millis = 0;

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(durationStr.toLowerCase());

        while (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            char unit = matcher.group(2).charAt(0);

            switch (unit) {
                case 's':
                    millis += TimeUnit.SECONDS.toMillis(amount);
                    break;
                case 'm':
                    millis += TimeUnit.MINUTES.toMillis(amount);
                    break;
                case 'h':
                    millis += TimeUnit.HOURS.toMillis(amount);
                    break;
                case 'd':
                    millis += TimeUnit.DAYS.toMillis(amount);
                    break;
            }
        }

        return millis;
    }

    public boolean canClaimKit(Player player, String kitName) {
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        String cooldownKey = uuid + ":" + kitName;

        return !kitCooldowns.containsKey(uuid) || kitCooldowns.get(uuid) <= now;
    }

    public void claimKit(Player player, String kitName) {
        UUID uuid = player.getUniqueId();
        String duration = getKitDuration(kitName);
        long cooldownTime = parseDuration(duration);

        if (cooldownTime > 0) {
            kitCooldowns.put(uuid, System.currentTimeMillis() + cooldownTime);
            saveCooldowns();
        }

        ItemStack[] items = getKitItemsArray(kitName);
        for (ItemStack item : items) {
            if (item != null) {
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItem(player.getLocation(), item.clone());
                } else {
                    player.getInventory().addItem(item.clone());
                }
            }
        }

        ItemStack[] armor = getKitArmorArray(kitName);
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null && !armor[i].getType().isAir()) {
                ItemStack currentArmor = inventory.getArmorContents()[i];
                if (currentArmor == null || currentArmor.getType().isAir()) {
                    inventory.setArmorContents(armor);
                    break;
                } else {
                    if (inventory.firstEmpty() == -1) {
                        player.getWorld().dropItem(player.getLocation(), armor[i].clone());
                    } else {
                        inventory.addItem(armor[i].clone());
                    }
                }
            }
        }

        ItemStack offhand = getKitOffhand(kitName);
        if (offhand != null && !offhand.getType().isAir()) {
            ItemStack currentOffhand = inventory.getItemInOffHand();
            if (currentOffhand == null || currentOffhand.getType().isAir()) {
                inventory.setItemInOffHand(offhand.clone());
            } else {
                if (inventory.firstEmpty() == -1) {
                    player.getWorld().dropItem(player.getLocation(), offhand.clone());
                } else {
                    inventory.addItem(offhand.clone());
                }
            }
        }
    }

    public long getKitCooldownRemaining(Player player, String kitName) {
        UUID uuid = player.getUniqueId();
        if (!kitCooldowns.containsKey(uuid)) {
            return 0;
        }

        long remaining = kitCooldowns.get(uuid) - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    private void loadCooldowns() {
        JsonObject json = storageManager.load();
        if (json.has("cooldowns")) {
            JsonObject cooldownsObj = json.getAsJsonObject("cooldowns");
            for (String key : cooldownsObj.keySet()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    long cooldown = cooldownsObj.get(key).getAsLong();
                    if (cooldown > System.currentTimeMillis()) {
                        kitCooldowns.put(uuid, cooldown);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void saveCooldowns() {
        JsonObject json = storageManager.load();
        JsonObject cooldownsObj = new JsonObject();
        
        for (Map.Entry<UUID, Long> entry : kitCooldowns.entrySet()) {
            if (entry.getValue() > System.currentTimeMillis()) {
                cooldownsObj.addProperty(entry.getKey().toString(), entry.getValue());
            }
        }
        
        json.add("cooldowns", cooldownsObj);
        storageManager.save(json);
    }
}