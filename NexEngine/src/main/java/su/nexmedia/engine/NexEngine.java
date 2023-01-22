package su.nexmedia.engine;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.item.PluginItemRegistry;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.EngineConfig;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.item.BreweryItem;
import su.nexmedia.engine.hooks.item.InteractiveBooksItem;
import su.nexmedia.engine.hooks.item.ItemsAdderItem;
import su.nexmedia.engine.hooks.item.MMOItemsItem;
import su.nexmedia.engine.hooks.misc.VaultHook;
import su.nexmedia.engine.hooks.npc.CitizensHook;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.nms.NMS;
import su.nexmedia.engine.utils.Reflex;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NexEngine extends NexPlugin<NexEngine> {

    private static NexEngine instance;
    private Set<NexPlugin<?>> childrens;

    NMS nms;
    private EditorManager editorManager;
    private PluginItemRegistry pluginItemRegistry;

    public NexEngine() {
        instance = this;
    }

    public static @NotNull NexEngine get() {
        return instance;
    }

    @Override
    protected @NotNull NexEngine getSelf() {
        return this;
    }

    final boolean loadCore() {
        this.childrens = new HashSet<>();

        if (!this.setupNMS()) {
            this.error("Could not setup NMS version. Plugin will be disabled.");
            return false;
        }

        if (!this.getServer().getVersion().contains("Spigot")) {
            isPaper = true;
            this.info("Seems like we have Paper based fork here...");
        }

        this.editorManager = new EditorManager(this);
        this.editorManager.setup();

        return true;
    }

    private boolean setupNMS() {
        Version current = Version.CURRENT;

        String pack = NMS.class.getPackage().getName();
        Class<?> clazz = Reflex.getClass(pack, current.name());
        if (clazz == null) return false;

        try {
            this.nms = (NMS) clazz.getConstructor().newInstance();
            this.info("Loaded NMS version: " + current.name());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.nms != null;
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {
        if (this.editorManager != null) {
            this.editorManager.shutdown();
            this.editorManager = null;
        }

        if (this.pluginItemRegistry != null) {
            this.pluginItemRegistry.unregisterAll();
        }

        if (Hooks.hasCitizens()) CitizensHook.shutdown();
        if (Hooks.hasVault()) VaultHook.shutdown();
    }

    @Override
    public void registerHooks() {
        if (Hooks.hasVault()) {
            VaultHook.setup(this);
        }

        pluginItemRegistry = new PluginItemRegistry(this);
        pluginItemRegistry.registerForConfig("itemsadder", () -> new ItemsAdderItem(this));
        pluginItemRegistry.registerForConfig("mmoitems", () -> new MMOItemsItem(this));
        pluginItemRegistry.registerForConfig("brewery", () -> new BreweryItem(this));
        pluginItemRegistry.registerForConfig("interactivebooks", () -> new InteractiveBooksItem(this));
    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<NexEngine> mainCommand) {

    }

    @Override
    public void registerPermissions() {

    }

    @Override
    public void loadConfig() {
        EngineConfig.load(this);
    }

    @Override
    public void loadLang() {
        this.getLangManager().loadMissing(EngineLang.class);
        this.getLangManager().setupEditorEnum(MenuItemType.class);
        this.getLang().saveChanges();
    }

    void addChildren(@NotNull NexPlugin<?> child) {
        this.childrens.add(child);
    }

    public @NotNull Set<NexPlugin<?>> getChildrens() {
        return this.childrens;
    }

    public @NotNull PluginItemRegistry getPluginItemRegistry() {
        return Objects.requireNonNull(pluginItemRegistry);
    }

}
