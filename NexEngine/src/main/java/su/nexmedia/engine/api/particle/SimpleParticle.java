package su.nexmedia.engine.api.particle;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.StringUtil;

public class SimpleParticle {

    private final Particle particle;
    private final Object data;

    public SimpleParticle(@NotNull Particle particle, @Nullable Object data) {
        this.particle = particle;
        this.data = data;
    }

    @NotNull
    public static SimpleParticle of(@NotNull Particle particle) {
        return SimpleParticle.of(particle, null);
    }

    @NotNull
    public static SimpleParticle of(@NotNull Particle particle, @Nullable Object data) {
        return new SimpleParticle(particle, data);
    }

    @NotNull
    public static SimpleParticle read(@NotNull JYML cfg, @NotNull String path) {
        String name = cfg.getString(path + ".Name", "");
        Particle particle = StringUtil.getEnum(name, Particle.class).orElse(Particle.REDSTONE);

        Class<?> dataType = particle.getDataType();
        Object data = null;
        if (dataType == BlockData.class) {
            Material material = Material.getMaterial(cfg.getString(path + ".Material", ""));
            data = material != null ? material.createBlockData() : Material.STONE.createBlockData();
        } else if (dataType == Particle.DustOptions.class) {
            Color color = StringUtil.parseColor(cfg.getString(path + ".Color", ""));
            double size = cfg.getDouble(path + ".Size", 1D);
            data = new Particle.DustOptions(color, (float) size);
        } else if (dataType == Particle.DustTransition.class) {
            Color colorStart = StringUtil.parseColor(cfg.getString(path + ".Color_From", ""));
            Color colorEnd = StringUtil.parseColor(cfg.getString(path + ".Color_To", ""));
            double size = cfg.getDouble(path + ".Size", 1D);
            data = new Particle.DustTransition(colorStart, colorEnd, 1.0f);
        } else if (dataType == ItemStack.class) {
            ItemStack item = cfg.getItem(path + ".Item");
            data = item.getType().isAir() ? new ItemStack(Material.STONE) : item;
        } else if (dataType != Void.class) return SimpleParticle.of(Particle.REDSTONE);

        return SimpleParticle.of(particle, data);
    }

    public static void write(@NotNull SimpleParticle particle, @NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Name", particle.getParticle().name());

        Object data = particle.getData();
        if (data instanceof BlockData blockData) {
            cfg.set(path + ".Material", blockData.getMaterial().name());
        } else if (data instanceof Particle.DustTransition dustTransition) {
            Color colorStart = dustTransition.getColor();
            Color colorEnd = dustTransition.getToColor();
            cfg.set(path + ".Color_From", colorStart.getRed() + "," + colorStart.getGreen() + "," + colorStart.getBlue());
            cfg.set(path + ".Color_To", colorEnd.getRed() + "," + colorEnd.getGreen() + "," + colorEnd.getBlue());
            cfg.set(path + ".Size", dustTransition.getSize());
        } else if (data instanceof Particle.DustOptions dustOptions) {
            Color color = dustOptions.getColor();
            cfg.set(path + ".Color", color.getRed() + "," + color.getGreen() + "," + color.getBlue());
            cfg.set(path + ".Size", dustOptions.getSize());
        } else if (data instanceof ItemStack item) {
            cfg.setItem(path + ".Item", item);
        }
    }

    @NotNull
    public Particle getParticle() {
        return particle;
    }

    @Nullable
    public Object getData() {
        return data;
    }

    public void play(@NotNull Location location, double speed, int amount) {
        this.play(location, 0D, speed, amount);
    }

    public void play(@NotNull Location location, double offsetAll, double speed, int amount) {
        this.play(location, offsetAll, offsetAll, offsetAll, speed, amount);
    }

    public void play(@NotNull Location location, double xOffset, double yOffset, double zOffset, double speed, int amount) {
        this.play(null, location, xOffset, yOffset, zOffset, speed, amount);
    }

    public void play(@NotNull Player player, @NotNull Location location, double speed, int amount) {
        this.play(player, location, 0D, speed, amount);
    }

    public void play(@NotNull Player player, @NotNull Location location, double offsetAll, double speed, int amount) {
        this.play(player, location, offsetAll, offsetAll, offsetAll, speed, amount);
    }

    public void play(@Nullable Player player, @NotNull Location location, double xOffset, double yOffset, double zOffset, double speed, int amount) {
        if (player == null) {
            EffectUtil.playParticle(location, this.getParticle(), this.getData(), xOffset, yOffset, zOffset, speed, amount);
        } else {
            EffectUtil.playParticle(player, location, this.getParticle(), this.getData(), xOffset, yOffset, zOffset, speed, amount);
        }
    }
}
