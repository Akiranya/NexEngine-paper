package su.nexmedia.engine.utils;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Colorizer {

    public static final char AMPERSAND_CHAR = LegacyComponentSerializer.AMPERSAND_CHAR;

    public static final Pattern PATTERN_HEX = Pattern.compile(
        "#([A-Fa-f0-9]{6})"
    );

    @NotNull
    public static String apply(@NotNull String str) {
        return legacy(str);
    }

    @NotNull
    public static List<String> apply(@NotNull List<String> list) {
        list.replaceAll(Colorizer::legacy);
        return list;
    }

    @NotNull
    public static Set<String> apply(@NotNull Set<String> set) {
        return set.stream().map(Colorizer::apply).collect(Collectors.toSet());
    }

    /**
     * Translates ampersand ({@code &}) color codes into section ({@code §}) color codes.
     * <p>
     * The translation supports three different RGB formats:
     * <ul>
     *     <li>Legacy Mojang color and formatting codes (such as §a or §l)</li>
     *     <li>Adventure-specific RGB format (such as §#a25981)</li>
     *     <li>BungeeCord RGB color code format (such as §x§a§2§5§9§8§1)</li>
     * </ul>
     *
     * @param str a legacy text where its color codes are in <b>ampersand</b> {@code &} format
     *
     * @return a legacy text where its color codes are in <b>section</b> {@code §} format
     */
    @NotNull
    public static String legacy(@NotNull String str) {
        return LegacyComponentSerializer.legacySection().serialize(LegacyComponentSerializer.legacy(AMPERSAND_CHAR).deserialize(str));
    }

    /**
     * Translates section ({@code §}) color codes into ampersand ({@code &}) color codes.
     * <p>
     * It's essentially a reverse of {@link #legacy(String)}.
     */
    @NotNull
    public static String plain(@NotNull String str) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(LegacyComponentSerializer.legacySection().deserialize(str));
    }

    @NotNull
    public static String strip(@NotNull String str) {
        String stripped = ChatColor.stripColor(str);
        return stripped == null ? "" : stripped;
    }

}
