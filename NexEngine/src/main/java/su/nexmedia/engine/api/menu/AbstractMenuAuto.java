package su.nexmedia.engine.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.CollectionsUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractMenuAuto<P extends NexPlugin<P>, I> extends AbstractMenu<P> {

    @Deprecated
    protected int[] objectSlots;

    public AbstractMenuAuto(@NotNull P plugin, @NotNull JYML cfg, @NotNull String path) {
        super(plugin, cfg, path);
        this.objectSlots = cfg.getIntArray(path + "Object.Slots");
    }

    public AbstractMenuAuto(@NotNull P plugin, @NotNull String title, int size, int[] slots) {
        super(plugin, title, size);
        this.objectSlots = slots;
    }

    protected int[] getObjectSlots() {
        return objectSlots;
    }

    @NotNull
    protected abstract List<I> getObjects(@NotNull Player player);

    @NotNull
    protected abstract ItemStack getObjectStack(@NotNull Player player, @NotNull I object);

    @NotNull
    protected abstract IMenuClick getObjectClick(@NotNull Player player, @NotNull I object);

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        int len = this.getObjectSlots().length;
        List<I> list = new ArrayList<>(this.getObjects(player));
        List<List<I>> split = CollectionsUtil.split(list, len);

        int pages = split.size();
        int page = Math.min(pages, this.getPage(player));
        if (pages < 1) list = Collections.emptyList();
        else list = split.get(page - 1);

        int count = 0;
        for (I object : list) {
            ItemStack item = this.getObjectStack(player, object);
            IMenuItem menuItem = new MenuItem(item, this.getObjectSlots()[count++]);
            menuItem.setClick(this.getObjectClick(player, object));
            this.addItem(player, menuItem);
        }
        this.setPage(player, page, pages);
    }
}
