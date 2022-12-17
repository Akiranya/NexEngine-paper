package su.nexmedia.engine.api.menu;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.type.ClickType;
import su.nexmedia.engine.actions.ActionManipulator;

import java.util.HashMap;
import java.util.Map;

public class MenuItem extends AbstractMenuItem {

    public MenuItem(@NotNull ItemStack item) {
        super(item);
    }

    public MenuItem(@NotNull ItemStack item, int... slots) {
        super(item, slots);
    }

    public MenuItem(@NotNull ItemStack item, @Nullable Enum<?> type, int... slots) {
        super(item, type, slots);
    }

    public MenuItem(@NotNull String id, @NotNull ItemStack item, int... slots) {
        super(id, item, null, slots);
    }

    public MenuItem(@NotNull String id, @NotNull ItemStack item, @Nullable Enum<?> type, int... slots) {
        super(id, item, type, slots);
    }

    public MenuItem(
        @NotNull String id, @Nullable Enum<?> type, int[] slots,
        @NotNull Map<String, MenuItemDisplay> displayMap,
        @NotNull Map<ClickType, ActionManipulator> customClicks) {
        super(id, type, slots, displayMap, customClicks);
    }

    @Deprecated
    public MenuItem(
        @NotNull String id, @Nullable Enum<?> type, int[] slots,
        @NotNull Map<String, MenuItemDisplay> displayMap,
        @NotNull Map<ClickType, ActionManipulator> customClicks,
        int animationTickInterval, String[] animationFrames, boolean animationIgnoreUnavailableFrames,
        boolean animationRandomOrder) {
        super(
            id, type, slots,
            displayMap, customClicks,

            animationTickInterval, animationFrames, animationIgnoreUnavailableFrames, animationRandomOrder
        );
    }

    public MenuItem(@NotNull IMenuItem menuItem) {
        super(menuItem.getId(), menuItem.getType(), menuItem.getSlots(), new HashMap<>(menuItem.getDisplayMap()), new HashMap<>(menuItem.getClickCustomActions()));
    }
}
