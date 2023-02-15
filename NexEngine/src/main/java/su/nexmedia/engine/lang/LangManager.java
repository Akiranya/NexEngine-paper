package su.nexmedia.engine.lang;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.editor.EditorButtonType;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.utils.Reflex;
import su.nexmedia.engine.utils.StringUtil;

import java.lang.reflect.Field;
import java.util.*;

public class LangManager<P extends NexPlugin<P>> extends AbstractManager<P> {

    protected JYML config;
    protected final Map<String, LangMessage> messages;
    protected final Map<String, String> placeholders;

    public static final String DIR_LANG = "/lang/";

    public LangManager(@NotNull P plugin) {
        super(plugin);
        this.messages = new HashMap<>();
        this.placeholders = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        this.plugin.getConfigManager().extractResources(DIR_LANG);
        this.config = JYML.loadOrExtract(plugin, DIR_LANG + "messages_" + plugin.getConfigManager().languageCode + ".yml");
        this.plugin.info("Using '" + plugin.getConfigManager().languageCode + "' language.");

        this.getConfig().getSection("Placeholders").forEach(placeholder -> {
            this.placeholders.put(placeholder, this.getConfig().getString("Placeholders." + placeholder, ""));
        });

        if (this.plugin.isEngine()) {
            this.setupEnum(EntityType.class);
            this.setupEnum(Material.class);
            this.setupEnum(GameMode.class);

            for (PotionEffectType type : PotionEffectType.values()) {
                this.getConfig().addMissing("PotionEffectType." + type.getName(), StringUtil.capitalizeUnderscored(type.getName()));
            }
            for (Enchantment enchantment : Enchantment.values()) {
                getEnchantment(enchantment);
            }
            for (World world : this.plugin.getServer().getWorlds()) {
                getWorld(world);
            }
            this.getConfig().saveChanges();
        } else {
            NexEngine.get().getLangManager().getMessages().forEach((key, message) -> {
                this.getMessages().put(key, new LangMessage(this.plugin, message.getRaw()));
            });
        }
    }

    @Override
    protected void onShutdown() {
        this.messages.clear();
        this.placeholders.clear();
    }

    @NotNull
    public JYML getConfig() {
        return config;
    }

    @NotNull
    public Map<String, LangMessage> getMessages() {
        return messages;
    }

    @NotNull
    public Map<String, String> getPlaceholders() {
        return placeholders;
    }

    @NotNull
    public LangMessage getMessage(@NotNull LangKey key) {
        LangMessage message = this.getMessages().get(key.getPath());
        if (message == null) {
            message = this.loadMessage(key);
        }
        return message;
    }

    @NotNull
    public Optional<String> getMessage(@NotNull String path) {
        return Optional.ofNullable(this.getConfig().getString(path));
    }

    @NotNull
    private LangMessage loadMessage(@NotNull LangKey key) {
        if (this.write(key)) {
            this.getConfig().saveChanges();
        }

        List<String> list = this.getConfig().getStringList(key.getPath());
        String text = !list.isEmpty() ? String.join("\\n", list) : this.getConfig().getString(key.getPath(), "<Missing Message [" + key.getPath() + "]>");
        LangMessage message = new LangMessage(plugin, text);
        this.getMessages().put(key.getPath(), message);

        return message;
    }

    /**
     * Loads and sets missing messages from the specified class. This method is used to generate the default language
     * file or add new messages to it.
     *
     * @param clazz A class to load LangKey messages from.
     */
    public void loadMissing(@NotNull Class<?> clazz) {
        for (Field field : Reflex.getFields(clazz)) {
            if (!LangKey.class.isAssignableFrom(field.getType())) {
                continue;
            }

            LangKey langKey;
            try {
                langKey = (LangKey) field.get(this);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

            // Do not load/set messages of super class(es) or if they are already present in the lang file.
            if (!field.getDeclaringClass().equals(clazz)) {
                continue;
            }

            // Clear old loaded messages.
            this.getMessages().remove(langKey.getPath());

            // For engine, we have to preload the message, so it can be added to child plugin's messages.
            if (this.plugin.isEngine()) {
                this.loadMessage(langKey);
            } else { // For child plugins, we can only write it to the config and not precache it.
                this.write(langKey);
            }
        }
        this.getConfig().saveChanges();
    }

    private boolean write(@NotNull LangKey key) {
        if (!this.getConfig().contains(key.getPath())) {
            String textDefault = key.getDefaultText();
            String[] textSplit = textDefault.split("\n");
            this.getConfig().set(key.getPath(), textSplit.length > 1 ? Arrays.asList(textSplit) : textDefault);
            return true;
        }
        return false;
    }

    public void setupEnum(@NotNull Class<? extends Enum<?>> clazz) {
        if (!clazz.isEnum()) return;
        for (Object eName : clazz.getEnumConstants()) {
            String name = eName.toString();
            if (clazz == Material.class && name.startsWith("LEGACY")) continue;

            String path = clazz.getSimpleName() + "." + name;
            String val = StringUtil.capitalizeUnderscored(name);
            this.getConfig().addMissing(path, val);
        }
    }

    @NotNull
    public String getEnum(@NotNull Enum<?> e) {
        String path = e.getDeclaringClass().getSimpleName() + "." + e.name();
        String locEnum = this.getMessage(path).orElse(null);
        if (locEnum == null && !this.plugin.isEngine()) {
            return NexPlugin.getEngine().getLangManager().getEnum(e);
        }
        return locEnum == null ? "null" : locEnum;
    }

    public void setupEditorEnum(@NotNull Class<? extends Enum<? extends EditorButtonType>> clazz) {
        if (!clazz.isEnum()) return;
        for (Object eName : clazz.getEnumConstants()) {
            if (!(eName instanceof EditorButtonType buttonType)) continue;
            if (buttonType.getMaterial().isAir()) continue;

            String nameRaw = buttonType.name();
            String path = "Editor." + clazz.getSimpleName() + "." + nameRaw + ".";

            this.getConfig().addMissing(path + "Name", buttonType.getName());
            this.getConfig().addMissing(path + "Lore", buttonType.getLore());

            buttonType.setName(this.getConfig().getString(path + "Name", nameRaw));
            buttonType.setLore(this.getConfig().getStringList(path + "Lore"));
        }
    }

    @NotNull
    public static String getPotionType(@NotNull PotionEffectType type) {
        return NexEngine.get().getLangManager().getMessage("PotionEffectType." + type.getName()).orElse(type.getName());
    }

    @NotNull
    public static String getEntityType(@NotNull EntityType type) {
        return NexEngine.get().getLangManager().getEnum(type);
    }

    @NotNull
    public static String getMaterial(@NotNull Material type) {
        return NexEngine.get().getLangManager().getEnum(type);
    }

    @NotNull
    public static String getWorld(@NotNull World world) {
        return getByObject(world.getName(), "World");
    }

    @NotNull
    public static String getEnchantment(@NotNull Enchantment enchantment) {
        return getByObject(enchantment.getKey().getKey(), "Enchantment");
    }

    @NotNull
    private static String getByObject(@NotNull String nameRaw, @NotNull String path) {
        LangManager<NexEngine> manager = NexEngine.get().getLangManager();

        manager.getConfig().addMissing(path + "." + nameRaw, StringUtil.capitalizeUnderscored(nameRaw));
        manager.getConfig().saveChanges();

        return manager.getMessage(path + "." + nameRaw).orElse(nameRaw);
    }

    @NotNull
    public static String getBoolean(boolean b) {
        return NexEngine.get().getLangManager().getMessage(b ? EngineLang.OTHER_YES : EngineLang.OTHER_NO).getLocalized();
    }

    @NotNull
    public static String getPlain(@NotNull LangKey key) {
        return NexEngine.get().getLangManager().getMessage(key).getLocalized();
    }
}
