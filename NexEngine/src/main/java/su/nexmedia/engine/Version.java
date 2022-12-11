package su.nexmedia.engine;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public enum Version {

    // KEEP VERSIONS LIST FROM LOWER TO HIGHER
    V1_17_R1, V1_18_R2, V1_19_R1, V1_19_R2,
    ;

    public static final Version CURRENT;

    static {
        String[] split = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        String versionRaw = split[split.length - 1];

        try {
            CURRENT = Version.valueOf(versionRaw.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean isLower(@NotNull Version version) {
        return this.ordinal() < version.ordinal();
    }

    public boolean isHigher(@NotNull Version version) {
        return this.ordinal() > version.ordinal();
    }

    public static boolean isAbove(@NotNull Version version) {
        return CURRENT.isHigher(version);
    }

    public static boolean isBehind(@NotNull Version version) {
        return CURRENT.isLower(version);
    }

    public boolean isCurrent() {
        return this == Version.CURRENT;
    }
}
