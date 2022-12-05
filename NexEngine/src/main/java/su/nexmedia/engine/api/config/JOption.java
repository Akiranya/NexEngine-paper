package su.nexmedia.engine.api.config;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.StringUtil;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class JOption<T> {

    public static final ValueLoader<Boolean>      LOADER_BOOLEAN     = JYML::getBoolean;
    public static final ValueLoader<Integer>      LOADER_INT         = JYML::getInt;
    public static final ValueLoader<Double>       LOADER_DOUBLE      = JYML::getDouble;
    public static final ValueLoader<Long>         LOADER_LONG        = JYML::getLong;
    public static final ValueLoader<String>       LOADER_STRING      = (cfg, path, def) -> StringUtil.color(cfg.getString(path, def));
    public static final ValueLoader<Set<String>>  LOADER_SET_STRING  = (cfg, path, def) -> StringUtil.color(cfg.getStringSet(path));
    public static final ValueLoader<List<String>> LOADER_LIST_STRING = (cfg, path, def) -> StringUtil.color(cfg.getStringList(path));
    public static final ValueLoader<ItemStack>    LOADER_ITEM        = JYML::getItem;

    protected final ValueLoader<T> valueLoader;
    protected final String         path;
    protected final String[]       description;
    protected final T              defaultValue;
    protected       T              value;
    protected JWriter writer;

    public JOption(@NotNull String path, @NotNull ValueLoader<T> valueLoader, @NotNull T defaultValue) {
        this(path, "", valueLoader, defaultValue);
    }

    public JOption(@NotNull String path, @NotNull String description, @NotNull ValueLoader<T> valueLoader, @NotNull Supplier<T> defaultValue) {
        this(path, description, valueLoader, defaultValue.get());
    }

    public JOption(@NotNull String path, @NotNull String description, @NotNull ValueLoader<T> valueLoader, @NotNull T defaultValue) {
        this.path = path;
        this.description = description.split("\n");
        this.valueLoader = valueLoader;
        this.defaultValue = defaultValue;
    }

    @NotNull
    public static JOption<Boolean> create(@NotNull String path, @NotNull String description, boolean defaultValue) {
        return new JOption<>(path, description, LOADER_BOOLEAN, defaultValue);
    }

    @NotNull
    public static JOption<Integer> create(@NotNull String path, @NotNull String description, int defaultValue) {
        return new JOption<>(path, description, LOADER_INT, defaultValue);
    }

    @NotNull
    public static JOption<Double> create(@NotNull String path, @NotNull String description, double defaultValue) {
        return new JOption<>(path, description, LOADER_DOUBLE, defaultValue);
    }

    @NotNull
    public static JOption<Long> create(@NotNull String path, @NotNull String description, long defaultValue) {
        return new JOption<>(path, description, LOADER_LONG, defaultValue);
    }

    @NotNull
    public static JOption<String> create(@NotNull String path, @NotNull String description, @NotNull String defaultValue) {
        return new JOption<>(path, description, LOADER_STRING, defaultValue);
    }

    @NotNull
    public static JOption<List<String>> create(@NotNull String path, @NotNull String description, @NotNull List<String> defaultValue) {
        return new JOption<>(path, description, LOADER_LIST_STRING, defaultValue);
    }

    @NotNull
    public static JOption<Set<String>> create(@NotNull String path, @NotNull String description, @NotNull Set<String> defaultValue) {
        return new JOption<>(path, description, LOADER_SET_STRING, defaultValue);
    }

    @NotNull
    public static JOption<ItemStack> create(@NotNull String path, @NotNull String description, @NotNull ItemStack defaultValue) {
        return new JOption<>(path, description, LOADER_ITEM, defaultValue);
    }

    @Deprecated
    public void load(@NotNull JYML cfg) {
        this.read(cfg);
    }

    public void read(@NotNull JYML cfg) {
        if (!cfg.contains(this.getPath())) {
            this.write(cfg);
        }
        cfg.setComments(this.getPath(), this.getDescription());
        this.value = this.valueLoader.loadFromConfig(cfg, this.getPath(), this.getDefaultValue());
    }

    public void write(@NotNull JYML cfg) {
        if (this.getWriter() != null) {
            this.getWriter().write(cfg, this.getPath());
        }
        else {
            cfg.set(this.getPath(), this.get());
        }
    }

    public void remove(@NotNull JYML cfg) {
        cfg.remove(this.getPath());
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public String[] getDescription() {
        return description;
    }

    @NotNull
    public ValueLoader<T> getValueLoader() {
        return valueLoader;
    }

    @NotNull
    public T getDefaultValue() {
        return defaultValue;
    }

    @NotNull
    public T get() {
        return this.value == null ? this.getDefaultValue() : this.value;
    }

    public void set(@NotNull T value) {
        this.value = value;
    }

    @Nullable
    public JWriter getWriter() {
        return writer;
    }

    @NotNull
    public JOption<T> setWriter(@Nullable JWriter writer) {
        this.writer = writer;
        return this;
    }

    @Deprecated
    public void set(@NotNull JYML cfg, @NotNull T value) {
        cfg.set(this.getPath(), value);
    }

    // TODO Rename to Reader, add Writer interface here
    public interface ValueLoader<T> {

        @NotNull T loadFromConfig(@NotNull JYML cfg, @NotNull String path, @NotNull T def);
    }

    public interface Writer {

        void write(@NotNull JYML cfg, @NotNull String path);
    }
}