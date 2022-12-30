package su.nexmedia.engine.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.config.EngineConfig;
import su.nexmedia.engine.hooks.Hooks;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ItemUtil {

    private static final NexEngine ENGINE;

    static {
        ENGINE = NexEngine.get();
    }

    public static int addToLore(@NotNull List<String> lore, int pos, @NotNull String value) {
        if (pos >= lore.size() || pos < 0) {
            lore.add(value);
        }
        else {
            lore.add(pos, value);
        }
        return pos < 0 ? pos : pos + 1;
    }

    @NotNull
    public static Component getItemName(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return (meta == null || !meta.hasDisplayName()) ? Component.translatable(item.getType()) : Objects.requireNonNull(meta.displayName());
    }

    @NotNull
    public static List<Component> getLore(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return (meta == null || !meta.hasLore()) ? new ArrayList<>() : Objects.requireNonNull(meta.lore());
    }

    public static void setSkullTexture(@NotNull ItemStack item, @NotNull String value) {
        if (item.getType() != Material.PLAYER_HEAD) return;
        if (!(item.getItemMeta() instanceof SkullMeta meta)) return;

        GameProfile profile = new GameProfile(EngineConfig.getIdForSkullTexture(value), null);
        profile.getProperties().put("textures", new Property("textures", value));

        Method method = Reflex.getMethod(meta.getClass(), "setProfile", GameProfile.class);
        if (method != null) {
            Reflex.invokeMethod(method, meta, profile);
        }
        else {
            Reflex.setFieldValue(meta, "profile", profile);
        }

        item.setItemMeta(meta);
    }

    @Nullable
    public static String getSkullTexture(@NotNull ItemStack item) {
        if (item.getType() != Material.PLAYER_HEAD) return null;

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return null;

        GameProfile profile = (GameProfile) Reflex.getFieldValue(meta, "profile");
        if (profile == null) return null;

        Collection<Property> properties = profile.getProperties().get("textures");
        Optional<Property> opt = properties.stream().filter(prop -> {
            return prop.getName().equalsIgnoreCase("textures") || prop.getSignature().equalsIgnoreCase("textures");
        }).findFirst();

        return opt.map(Property::getValue).orElse(null);
    }

    public static void setPlaceholderAPI(@NotNull ItemStack item, @NotNull Player player) {
        if (!Hooks.hasPlaceholderAPI()) return;

        item.editMeta(meta -> {
            // Replace displayName
            if (meta.hasDisplayName()) {
                Component original = Objects.requireNonNull(meta.displayName());
                Component replaced = ComponentUtil.setPlaceholderAPI(player, original);
                meta.displayName(replaced);
            }

            // Replace lore
            if (meta.hasLore()) {
                List<Component> original = Objects.requireNonNull(meta.lore());
                List<Component> replaced = original.stream().map(c -> ComponentUtil.setPlaceholderAPI(player, c)).collect(Collectors.toList());
                meta.lore(replaced);
            }
        });
    }

    public static void replace(@NotNull ItemStack item, @NotNull UnaryOperator<String> replacer) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Replace item name
        Component oldName = meta.displayName();
        if (oldName != null) {
            Component newName = ComponentUtil.replace(oldName, replacer);
            meta.displayName(newName);
        }

        // Replace item lore
        List<Component> oldLore = meta.lore();
        List<Component> newLore;
        if (oldLore != null) {
            newLore = ComponentUtil.replace(oldLore, replacer);
            meta.lore(ComponentUtil.stripEmpty(newLore));
        }

        item.setItemMeta(meta);
    }

    public static void replaceLore(@NotNull ItemStack item, @NotNull String placeholder, @NotNull List<String> replacer) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<Component> oldLore = meta.lore();
        if (oldLore == null) return;

        List<Component> newLore = ComponentUtil.replace(oldLore, placeholder, false, ComponentUtil.asComponent(replacer));
        meta.lore(ComponentUtil.stripEmpty(newLore));
        item.setItemMeta(meta);
    }

    public static boolean isWeapon(@NotNull ItemStack item) {
        return isSword(item) || isAxe(item) || isTrident(item);
    }

    public static boolean isTool(@NotNull ItemStack item) {
        return ENGINE.getNMS().isTool(item);
    }

    public static boolean isArmor(@NotNull ItemStack item) {
        return ENGINE.getNMS().isArmor(item);
    }

    public static boolean isBow(@NotNull ItemStack item) {
        return item.getType() == Material.BOW || item.getType() == Material.CROSSBOW;
    }

    public static boolean isSword(@NotNull ItemStack item) {
        return ENGINE.getNMS().isSword(item);
    }

    public static boolean isAxe(@NotNull ItemStack item) {
        return ENGINE.getNMS().isAxe(item);
    }

    public static boolean isTrident(@NotNull ItemStack item) {
        return item.getType() == Material.TRIDENT;
    }

    public static boolean isPickaxe(@NotNull ItemStack item) {
        return ENGINE.getNMS().isPickaxe(item);
    }

    public static boolean isShovel(@NotNull ItemStack item) {
        return ENGINE.getNMS().isShovel(item);
    }

    public static boolean isHoe(@NotNull ItemStack item) {
        return ENGINE.getNMS().isHoe(item);
    }

    public static boolean isElytra(@NotNull ItemStack item) {
        return item.getType() == Material.ELYTRA;
    }

    public static boolean isFishingRod(@NotNull ItemStack item) {
        return item.getType() == Material.FISHING_ROD;
    }

    public static boolean isHelmet(@NotNull ItemStack item) {
        return ENGINE.getNMS().isHelmet(item);
    }

    public static boolean isChestplate(@NotNull ItemStack item) {
        return ENGINE.getNMS().isChestplate(item);
    }

    public static boolean isLeggings(@NotNull ItemStack item) {
        return ENGINE.getNMS().isLeggings(item);
    }

    public static boolean isBoots(@NotNull ItemStack item) {
        return ENGINE.getNMS().isBoots(item);
    }

    @NotNull
    public static String toJson(@NotNull ItemStack item) {
        return ENGINE.getNMS().toJSON(item);
    }

    @NotNull
    public static String getNBTTag(@NotNull ItemStack item) {
        return ENGINE.getNMS().getNBTTag(item);
    }

    @Nullable
    public static String toBase64(@NotNull ItemStack item) {
        return ENGINE.getNMS().toBase64(item);
    }

    @NotNull
    public static List<String> toBase64(@NotNull ItemStack[] item) {
        return toBase64(Arrays.asList(item));
    }

    @NotNull
    public static List<String> toBase64(@NotNull List<ItemStack> items) {
        return new ArrayList<>(items.stream().map(ItemUtil::toBase64).filter(Objects::nonNull).toList());
    }

    @Nullable
    public static ItemStack fromBase64(@NotNull String data) {
        return ENGINE.getNMS().fromBase64(data);
    }

    @NotNull
    public static ItemStack[] fromBase64(@NotNull List<String> list) {
        List<ItemStack> items = list.stream().map(ItemUtil::fromBase64).filter(Objects::nonNull).toList();
        return items.toArray(new ItemStack[list.size()]);
    }
}
