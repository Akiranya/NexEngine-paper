package su.nexmedia.engine;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.ActionsManager;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.item.PluginItemRegistry;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.EngineConfig;
import su.nexmedia.engine.craft.CraftManager;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.hooks.HookManager;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.*;
import su.nexmedia.engine.hooks.external.citizens.CitizensHook;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.manager.player.blocktracker.PlayerBlockTracker;
import su.nexmedia.engine.nms.NMS;
import su.nexmedia.engine.utils.Reflex;

import java.util.HashSet;
import java.util.Set;

public class NexEngine extends NexPlugin<NexEngine> implements Listener {

    private static NexEngine         instance;
    private Set<NexPlugin<?>> childrens;

    NMS nms;
    ActionsManager actionsManager;
    CraftManager   craftManager;
    private EditorManager editorManager;
    @Deprecated private HookManager   hookManager;

    public NexEngine() {
        instance = this;
    }

    @NotNull
    public static NexEngine get() {
        return instance;
    }

    @Override
    @NotNull
    protected NexEngine getSelf() {
        return this;
    }

    final boolean loadCore() {
        this.childrens = new HashSet<>();

        if (!this.setupNMS()) {
            this.error("Could not setup NMS version. Plugin will be disabled.");
            return false;
        }

        this.getPluginManager().registerEvents(this, this);

        this.hookManager = new HookManager(this);
        this.hookManager.setup();

        this.actionsManager = new ActionsManager(this);
        this.actionsManager.setup();

        this.craftManager = new CraftManager(this);
        this.craftManager.setup();

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
        }
        catch (Exception e) {
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
        if (this.actionsManager != null) {
            this.actionsManager.shutdown();
            this.actionsManager = null;
        }
        if (this.hookManager != null) {
            this.hookManager.shutdown();
        }
        if (this.craftManager != null) {
            this.craftManager.shutdown();
            this.craftManager = null;
        }

        PlayerBlockTracker.shutdown();
        if (Hooks.hasCitizens()) CitizensHook.shutdown();
        if (Hooks.hasVault()) VaultHook.shutdown();
    }

    @Override
    public void registerHooks() {
        //this.registerHook(Hooks.VAULT, VaultHook.class);
        if (Hooks.hasVault()) {
            VaultHook.setup();
        }

        if (Hooks.hasItemsAdder())
            PluginItemRegistry.registerForConfig("itemsadder", ItemsAdderHook::new);
        if (Hooks.hasMMOItems())
            PluginItemRegistry.registerForConfig("mmoitems", MMOItemsHook::new);
        if (Hooks.hasBrewery())
            PluginItemRegistry.registerForConfig("brewery", BreweryHook::new);
        if (Hooks.hasInteractiveBooks())
            PluginItemRegistry.registerForConfig("interactivebooks", InteractiveBooksHook::new);
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

    @NotNull
    @Deprecated
    public HookManager getHookManager() {
        return this.hookManager;
    }

    void addChildren(@NotNull NexPlugin<?> child) {
        this.childrens.add(child);
    }

    @NotNull
    public Set<NexPlugin<?>> getChildrens() {
        return this.childrens;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHookLate(PluginEnableEvent e) {
        String name = e.getPlugin().getName();
        /*if (name.equalsIgnoreCase(Hooks.MYTHIC_MOBS)) {
            //this.registerHook(Hooks.MYTHIC_MOBS, MythicMobsHook.class);
        }
        else if (name.equalsIgnoreCase(Hooks.WORLD_GUARD)) {
            //this.registerHook(Hooks.WORLD_GUARD, WorldGuardHook.class);
        }*/
        if (name.equalsIgnoreCase(Hooks.CITIZENS)) {
            CitizensHook.setup();
        }
    }
}
